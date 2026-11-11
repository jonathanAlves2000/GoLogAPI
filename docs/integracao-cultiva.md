# Integração Cultiva ↔ GoLog TMS

Guia técnico para o middleware da Cultiva (backend Laravel, `Cultiva\Integrations\GoLog\Clients\GoLogClient`)
operar logística pelo GoLog: provisionar motoristas e veículos, enviar pedidos, roteirizar e acompanhar a
execução por **webhooks**.

> Versão do contrato: novembro/2026. Todos os exemplos foram executados contra a API real; os payloads de
> pedidos seguem exatamente o formato que `DispatchDeliveryOrdersBatchAction` já envia.

---

## 1. Visão geral do fluxo

```
Cultiva (Laravel)                                   GoLog TMS
─────────────────                                   ─────────
1. POST /login ───────────────────────────────────▶ token JWT (2 h)
2. Provisiona motorista/veículo/escala (5 passos) ─▶ users, drivers, tractors, equipament-groups, workSchedule
3. Compra registrada → DeliveryOrder (pending)
4. POST /integration/invoices/json ───────────────▶ cria COLETA (produtor) + ENTREGA (varejista) vinculadas
                                   ◀─────────────── webhook SHIPMENT_CREATED
5. POST /api-route-optimization ──────────────────▶ Google Route Optimization → viagens e paradas
                                   ◀─────────────── webhook SHIPMENT_PLANNED (viagem, sequência, placa, motorista)
6. Motorista executa no app GoLog
                                   ◀─────────────── SHIPMENT_STARTED / SHIPMENT_OCCURRENCE / SHIPMENT_DELIVERED
```

O GoLog é **multi-tenant**: a Cultiva opera no próprio tenant e nunca enxerga dados de outras operadoras —
qualquer ID de outro tenant responde `404`.

---

## 2. Ambiente e credenciais

| Item | Valor |
|---|---|
| Base URL | `http://<host-golog>:8081` (local: `http://localhost:8081`) |
| Tenant Cultiva (`GOLOG_TENANT_ID`) | `5647b3d4-f42f-4905-a617-896afa8dccef` |
| Usuário do middleware (`GOLOG_EMAIL`) | `cultiva@cultiva.com` — perfil **ADMIN** (necessário para criar usuários motoristas) |
| Usuário de operação (painel web) | `operacao@cultiva.com` — perfil **OPERATOR** |
| Senhas | entregues à parte pela equipe GoLog (não versionar) |

O `.env` atual do backend da Cultiva continua válido:

```dotenv
GOLOG_BASE_URL=http://<host-golog>:8081
GOLOG_EMAIL="cultiva@cultiva.com"
GOLOG_PASSWORD="<senha entregue pela GoLog>"
GOLOG_TENANT_ID=5647b3d4-f42f-4905-a617-896afa8dccef
GOLOG_TIMEOUT=60
```

Os três motoristas do `GoLogDriverSeeder` (Henrique Costa, Camila Rocha, Thiago Almeida) existem no GoLog com
**os mesmos UUIDs** de usuário, motorista, trator, conjunto e escala referenciados no seeder — o
`php artisan migrate:fresh --seed` da Cultiva funciona sem ajustes. Eles também têm escalas em todos os dias
úteis até 31/12/2026.

---

## 3. Autenticação

```http
POST /login
Content-Type: application/json

{ "email": "cultiva@cultiva.com", "password": "..." }
```

```json
{
  "token": "eyJ...",
  "userId": "…", "userName": "Cultiva Integracao Middleware", "userRole": "ADMIN",
  "companyId": "5647b3d4-f42f-4905-a617-896afa8dccef",
  "companyName": "Cultiva Marketplace Agro Ltda", "companyType": "TENANT_HEADQUARTER", "isMaster": false
}
```

* Envie `Authorization: Bearer <token>` em todas as chamadas. O token expira em **120 minutos**; ao receber
  `401` com `"Sessão ausente ou expirada"`, faça login de novo e repita a chamada.
* `X-Tenant-Id` é opcional para a Cultiva (o tenant vem do token). Se enviado, deve ser o próprio tenant — valores
  diferentes são ignorados, nunca ampliam o acesso.
* O cache de 60 s do `GoLogClient::login()` é seguro; pode ser estendido até ~110 min.

---

## 4. Provisionamento de motoristas e frota

Sequência usada por `/v1/golog/*` na Cultiva (inalterada):

| Passo | Endpoint GoLog | Observações |
|---|---|---|
| 1 | `POST /user` (`userProfile: "DRIVER"`, `companyId`: tenant Cultiva) | CPF com 11 dígitos; e-mail e CPF únicos |
| 2 | `POST /driver` | `cnhExpiration` em `dd/MM/yyyy`, data futura; o usuário precisa ter perfil DRIVER |
| 3 | `POST /tractor` | Placa `AAA-1234` ou `AAA1A23`; RENAVAM com 11 dígitos |
| 4 | `POST /equipament-group` (`equipament1Id`) | Necessário só para cavalo mecânico (ver abaixo) |
| 5 | `POST /workSchedule` | **Agora devolve a escala criada com `id`** (antes vinha corpo vazio) |

### Novidades opcionais no cadastro de veículo (`POST /tractor`)

| Campo | Efeito |
|---|---|
| `vehicleType` | `MOTO`, `CARRO`, `UTILITARIO`, `VAN`, `VUC`, `TOCO`, `TRUCK`, `BITRUCK` ou `CAVALO_MECANICO` (padrão quando omitido). Veículos **rígidos** ganham automaticamente um conjunto de veículo único — **não chame o passo 4** para eles; obtenha o conjunto em `GET /equipament-group`. |
| `cargoVolume` | Volume útil (m³) da carroceria; limita o volume no otimizador. |
| `kmPerLiter` | Consumo médio. Com o combustível, permite calcular o CO₂ das viagens. Sem ele, a viagem fica fora dos indicadores de emissão (nada é presumido). |
| `typeFuel` | Além de `DIESEL`, `GASOLINA`, `ETANOL`: `FLEX`, `GNV`, `ELETRICO`. |

O payload atual da Cultiva (sem esses campos) continua aceito; o campo legado `"Type Fuel"` é ignorado com segurança.

### Escala (`POST /workSchedule`)

```json
{ "driverId": "…", "equipamentGroupId": "…", "scheduleDate": "2026-12-01",
  "startWorkday": "08:00:00", "endWorkday": "18:00:00", "status": "ATIVO" }
```

Regra: o mesmo motorista ou conjunto não pode ter turnos **sobrepostos** (dias diferentes são permitidos).
Conflito responde `409` com a mensagem do motivo.

---

## 5. Envio de pedidos — `POST /integration/invoices/json`

Também disponível em `/api/v1/integration/invoices/json`.

```json
{
  "invoices": [
    {
      "invoiceNumber": "DO-1234-COL", "series": "1", "accessKey": "NFE00001234COL20261201",
      "typeOperation": "COLETA", "weight": 120.0, "volume": 0.6, "value": 980.0,
      "scheduledDate": "2026-12-01T11:00:00Z",
      "notes": "[Frutas] Coleta Pedido #1234 - Morango",
      "senderName": "Sítio Bergmann", "senderDocument": "11222333000181",
      "customerName": "Sítio Bergmann", "customerDocument": "11222333000181",
      "customerEmail": "", "customerPhone": "(47)99999-0000",
      "street": "Rua XV de Novembro", "number": "500", "district": "Centro",
      "city": "Pomerode", "state": "SC", "cep": "89107-000",
      "latitude": "-26.835158", "longitude": "-49.164375",
      "typeTransportName": "CLIMATIZADO", "shipmentTypeName": "Frutas"
    },
    {
      "invoiceNumber": "DO-1234-ENT", "...": "mesmos campos, typeOperation ENTREGA e endereço do varejista"
    }
  ]
}
```

Resposta:

```json
{ "totalReceived": 2, "successCount": 2, "errorCount": 0,
  "createdShipmentIds": ["<id coleta>", "<id entrega>"], "errors": [], "processingTimeMs": 41 }
```

### Regras de processamento

| Tema | Comportamento |
|---|---|
| **Ordem dos IDs** | `createdShipmentIds` segue a ordem de `invoices` (o mapeamento `index*2` / `index*2+1` da Cultiva é válido). Se houver `errorCount > 0`, os itens com erro não geram ID — trate o lote inteiro como falho, como o middleware já faz. |
| **Vínculo coleta → entrega** | Pares `PREFIXO-COL` / `PREFIXO-ENT` no mesmo lote são vinculados automaticamente: o otimizador garante a coleta antes da entrega, no mesmo veículo. Para outro padrão de numeração, envie `originInvoiceNumber` na ENTREGA. |
| **Endereço** | Cada item usa o **próprio** endereço/coordenadas (coleta no produtor, entrega no varejista). Coordenadas iguais reaproveitam o endereço já cadastrado. |
| **Data/hora** | `scheduledDate` aceita ISO-8601 com fuso (`…Z`, `-03:00`) e converte para o horário de Brasília; sem fuso é tratado como horário local. É o **limite de chegada**; o atendimento pode começar antes conforme a tolerância do perfil de custo (perfil “Equilibrado Cultiva”: 180 min). |
| **Cliente** | Procurado pelo documento **dentro do tenant Cultiva**; se não existir, é criado. Mantenha o documento estável por produtor/varejista para não duplicar cadastros. |
| **Tipos** | `typeTransportName` e `shipmentTypeName` são resolvidos pelo nome (sem diferenciar maiúsculas) no catálogo do tenant; se omitidos, usa o primeiro tipo disponível. Tipos da Cultiva: transporte `CARGA SECA`, `CLIMATIZADO`, `REFRIGERADO`; carga `Frutas`, `Verduras`, `Legumes`, `Tubérculos e raízes`, `Grãos e cereais`. |
| **Lote vazio** | `400` — `"A lista de faturas não pode estar vazia."` |

> **Atenção — idempotência:** o GoLog ainda não deduplica por `invoiceNumber`/`accessKey`. Reenviar o mesmo
> lote cria remessas duplicadas. Em caso de timeout, consulte as remessas pendentes (seção 7) antes de reenviar.

---

## 6. Roteirização — `POST /api-route-optimization`

```json
{ "shipmentIds": ["…"], "workScheduleIds": ["…"], "routePriority": "TEMPO" }
```

Opcionais: `profileId` e sobreposições pontuais (`kmCostMultiplier`, `hourCostMultiplier`, `fixedCostPerVehicle`,
`penaltyCostUnserved`, `defaultServiceDurationSeconds`, `timeWindowLeadMinutes`). Sem `profileId`, usa o perfil
padrão do tenant.

Resposta (**novo formato**; o middleware hoje lê só o corpo como texto, então nada quebra):

```json
{
  "routesCount": 3, "plannedShipments": 6,
  "transportIds": ["…", "…", "…"],
  "skipped": [
    { "shipmentIds": ["<coleta>", "<entrega>"], "label": "<coleta>/<entrega>",
      "reasons": ["fora da janela de horário das escalas"] }
  ],
  "totalDistanceMeters": 248600, "totalCost": 1230.5
}
```

* Remessas em `skipped` **continuam `PENDENTE`** no GoLog. Recomendação: marcar como `AGUARDANDO_INICIO`
  apenas o que vier no webhook `SHIPMENT_PLANNED` (ou não estiver em `skipped`), em vez de todas as enviadas.
* Validação prévia (falha o pedido inteiro com `400`/`404` explicando o motivo): listas vazias, escala
  desativada ou de outro tenant, remessa inexistente, remessa já `INICIADO`/`FINALIZADO`, remessa sem data ou
  endereço, ou mistura de tenants.
* Replanejar remessas ainda não iniciadas move-as para a nova viagem (sem paradas duplicadas).

---

## 7. Acompanhamento

### 7.1 Webhooks (recomendado)

Configure uma vez (pelo próprio usuário do middleware):

```http
PATCH /company/5647b3d4-f42f-4905-a617-896afa8dccef/webhook
{ "webhookUrl": "https://api.cultiva.com/v1/golog/webhook", "webhookSecret": "<segredo forte>", "webhookActive": true }
```

Teste de conectividade: `POST /company/{id}/webhook/test` (envia `TEST_PING` e devolve status e latência).
A URL precisa ser acessível **a partir do servidor do GoLog**.

Cada evento é um `POST` JSON:

```json
{
  "event": "SHIPMENT_PLANNED",
  "timestamp": "2026-12-01T10:02:11Z",
  "companyId": "5647b3d4-f42f-4905-a617-896afa8dccef",
  "data": {
    "shipmentId": "…", "typeOperation": "ENTREGA", "status": "AGUARDANDO_INICIO",
    "scheduledDate": "2026-12-01T12:00:00", "weight": 120.0, "volume": 0.6,
    "customerId": "…", "customerName": "Empório Garcia", "originShipmentId": "<coleta>",
    "sequenceOrder": 2, "transportId": "…", "codeTransport": 41,
    "driverName": "Henrique Costa Motorista", "vehiclePlate": "CST4B19"
  }
}
```

| Evento | Quando | Campos extras em `data` |
|---|---|---|
| `TEST_PING` | Teste manual | — |
| `SHIPMENT_CREATED` | Remessa importada | — |
| `SHIPMENT_PLANNED` | Entrou numa viagem | `sequenceOrder`, `transportId`, `codeTransport`, `driverName`, `vehiclePlate` |
| `SHIPMENT_UNPLANNED` | Viagem cancelada; volta a `PENDENTE` | — |
| `SHIPMENT_STARTED` | Motorista registrou início | `occurrenceId`, `occurrenceType`, `description`, `transportId`, `occurredAt` |
| `SHIPMENT_DELIVERED` | Motorista registrou fim | idem |
| `SHIPMENT_OCCURRENCE` | Outra ocorrência (atraso, avaria…) | idem |

Headers: `X-GoLog-Event`, `X-GoLog-Delivery` (UUID único por envio), `X-GoLog-Timestamp` e
`X-GoLog-Signature: sha256=<hex>` — HMAC-SHA256 do **corpo bruto** com o `webhookSecret`.

Validação em PHP/Laravel:

```php
$raw = $request->getContent();
$expected = 'sha256=' . hash_hmac('sha256', $raw, config('services.golog.webhook_secret'));
abort_unless(hash_equals($expected, (string) $request->header('X-GoLog-Signature')), 401);
```

Entrega: assíncrona, timeout de 10 s, **sem nova tentativa** em caso de falha — responda `2xx` rápido e
processe em fila. Para reconciliação, use as consultas abaixo.

### 7.2 Consultas

| Necessidade | Endpoint |
|---|---|
| Status de uma remessa | `GET /shipment/{id}` (`status`: `PENDENTE`, `AGUARDANDO_INICIO`, `INICIADO`, `FINALIZADO`) |
| Remessas por status | `GET /shipment/list-by-status?status=PENDENTE` |
| Remessa + parada + viagem | `GET /shipment/list-personalized` |
| Viagens | `GET /transport` · `GET /transport/{id}` |
| Paradas (sequência, distância e tempo planejados) | `GET /route-stops` |

---

## 8. Execução pelos motoristas

Os motoristas usam o app GoLog (login com e-mail/senha do passo 1). Cada parada gera ocorrências
`Inicio` e `Fim` (`POST /occurrence`), que mudam o status da remessa e disparam os webhooks
`SHIPMENT_STARTED` / `SHIPMENT_DELIVERED`. O motorista só enxerga e registra ocorrências nas próprias viagens.

---

## 9. Erros

Todas as respostas de erro seguem o mesmo formato:

```json
{ "status": 400, "message": "Placa inválida • RENAVAM deve conter exatamente 11 dígitos numéricos",
  "errors": ["Placa inválida", "RENAVAM deve conter exatamente 11 dígitos numéricos"],
  "timestamp": "2026-11-11T03:40:27Z" }
```

| Status | Significado |
|---|---|
| `400` | Validação, JSON malformado, enum inválido (a mensagem lista os valores aceitos) |
| `401` | Token ausente/expirado ou credenciais inválidas |
| `403` | Perfil sem permissão (ex.: OPERATOR criando usuário) |
| `404` | Registro inexistente **ou de outro tenant** |
| `409` | Conflito: duplicidade (CPF, e-mail, CNH, placa), escala sobreposta, registro em uso |
| `422` | Tenant sem catálogo mínimo para importar |
| `502` | Falha na API de otimização do Google (mensagem original incluída) |

---

## 10. Mudanças em relação à integração atual

| Mudança | Impacto no middleware |
|---|---|
| Importação usa o endereço de cada item e vincula pares COL/ENT | Nenhum ajuste; roteiros passam a respeitar coleta → entrega |
| `scheduledDate` com `Z` convertido para Brasília (antes: 3 h adiantado) | Nenhum ajuste; horários passam a ficar corretos |
| Tipos/responsável resolvidos no tenant (antes: primeiro registro do banco inteiro) | Opcional: enviar `typeTransportName`/`shipmentTypeName` |
| `POST /workSchedule` devolve a escala com `id` | `findWorkSchedule` vira desnecessário (pode permanecer) |
| Resposta de `/api-route-optimization` é um resumo com `skipped` | Recomendado: atualizar status só do que foi planejado |
| Webhooks de ciclo de vida | Recomendado: endpoint de webhook + verificação de assinatura |
| Erros padronizados (`message` + `errors`) | `throwFailedRequest` pode logar `message` diretamente |

## 11. Checklist de entrada em produção

- [ ] Senha do `cultiva@cultiva.com` trocada e guardada no cofre de segredos
- [ ] `GOLOG_BASE_URL` apontando para o servidor de produção
- [ ] Endpoint de webhook publicado, `webhookSecret` configurado e `TEST_PING` respondendo `2xx`
- [ ] Verificação de assinatura HMAC ativa
- [ ] Documento estável por produtor/varejista no payload
- [ ] Consumo `kmPerLiter` informado nos veículos (para os indicadores de CO₂)
- [ ] Rotina de reconciliação diária por `GET /shipment/list-by-status`
