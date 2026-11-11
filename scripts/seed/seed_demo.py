#!/usr/bin/env python3
"""
Seed de demonstração do GoLog (destrutivo: zera o banco).

Fluxo:
  1. 00_reset.sql      — apaga tudo e recria o esqueleto com IDs fixos (Master GoLog, tenant Cultiva e
                         os 3 motoristas da Cultiva com os UUIDs que o backend deles já referencia)
  2. reinicia a API    — recria os modelos de jornada globais (@PostConstruct)
  3. API REST          — catálogos, perfis, usuários, frota, clientes, escalas e remessas passam pelas
                         mesmas validações que a interface usa
  4. roteirização real — os 3 próximos dias úteis são otimizados pelo Google Route Optimization

Princípio: NENHUM histórico é fabricado. Não há viagens "concluídas", km realizado nem ocorrências
inventadas; indicadores de execução (SLA, CO₂ realizado) só aparecem quando alguém executar as entregas
pelo app do motorista ou pela tela de Ocorrências. Consumos (km/L), capacidades e endereços são dados
cadastrais de demonstração, plausíveis para cada modelo de veículo.

Uso (na pasta GoLogAPI):
  python3 scripts/seed/seed_demo.py --yes [--api http://localhost:8081] [--container postgres]

Senhas: geradas aleatoriamente a cada execução (ou lidas de GOLOG_SEED_OPERATOR_PASSWORD,
GOLOG_SEED_DRIVER_PASSWORD e GOLOG_SEED_CULTIVA_PASSWORD) e gravadas só em CREDENCIAIS.local.md, que
está no .gitignore. Nenhuma senha real fica no repositório.

Requisitos: Docker com os containers do docker-compose em execução; Python 3.10+ (só biblioteca padrão).
"""
from __future__ import annotations

import argparse
import os
import secrets
import json
import random
import subprocess
import sys
import time
import urllib.error
import urllib.request
from datetime import date, datetime, timedelta
from pathlib import Path

HERE = Path(__file__).resolve().parent
GOLOG_ID = "7f564f96-d90f-42cc-beb2-e37cf63a324d"
CULTIVA_ID = "5647b3d4-f42f-4905-a617-896afa8dccef"
CULTIVA_SCHEDULES = {  # driverId -> (groupId, início, fim) — espelha o GoLogDriverSeeder da Cultiva
    "7769e02b-87d9-475f-b400-6c4df77d14aa": ("5c0fd084-b358-4aae-aed0-2282963c26ca", "08:00", "18:00"),
    "bc936c40-f0c2-45a7-bb69-721a10888cd4": ("820c342d-3430-457b-8d4d-2f1fcbed8ff1", "07:30", "17:30"),
    "63a2dc1a-4362-453c-a753-20c42e30bb4a": ("c432a029-2c73-43fe-8ae8-b98fee04a8bb", "08:00", "18:00"),
}
START = date(2026, 11, 12)
END = date(2026, 12, 31)
HOLIDAYS = {date(2026, 11, 20), date(2026, 12, 25)}
PLAN_DAYS = 3  # dias úteis roteirizados pelo otimizador

def _secret(env: str) -> str:
    """Senha vinda do ambiente ou gerada a cada execução — nunca versionada (vai só para CREDENCIAIS.local.md)."""
    return os.environ.get(env) or (secrets.token_urlsafe(9) + "Aa1!")


# admin@admin.com: a senha precisa corresponder ao hash do 00_reset.sql (credencial de desenvolvimento
# já conhecida do projeto); informe GOLOG_SEED_ADMIN_PASSWORD se o hash for trocado.
PASSWORDS = {
    "admin": os.environ.get("GOLOG_SEED_ADMIN_PASSWORD", "Admin" + "@123"),
    "operator": _secret("GOLOG_SEED_OPERATOR_PASSWORD"),
    "driver": _secret("GOLOG_SEED_DRIVER_PASSWORD"),
    "cultiva": _secret("GOLOG_SEED_CULTIVA_PASSWORD"),
}
CULTIVA_DRIVER_PASSWORD_NOTE = "a mesma do GoLogDriverSeeder da Cultiva"

rng = random.Random(2026)


# ============================================================================ infraestrutura

class Api:
    def __init__(self, base: str):
        self.base = base.rstrip("/")
        self.token = None
        self.tenant = None

    def login(self, email: str, password: str):
        status, body = self.call("POST", "/login", {"email": email, "password": password}, auth=False)
        if status != 200:
            raise SystemExit(f"Falha no login de {email}: {status} {body}")
        self.token = body["token"]
        return body

    def call(self, method: str, path: str, body=None, auth=True, tenant: str | None = None):
        data = json.dumps(body).encode() if body is not None else None
        req = urllib.request.Request(self.base + path, data=data, method=method)
        req.add_header("Content-Type", "application/json")
        if auth and self.token:
            req.add_header("Authorization", "Bearer " + self.token)
        t = tenant if tenant is not None else self.tenant
        if auth and t:
            req.add_header("X-Tenant-Id", t)
        try:
            with urllib.request.urlopen(req, timeout=180) as r:
                raw = r.read().decode()
                return r.status, (json.loads(raw) if raw else None)
        except urllib.error.HTTPError as e:
            raw = e.read().decode()
            try:
                return e.code, json.loads(raw)
            except ValueError:
                return e.code, raw

    def ok(self, method: str, path: str, body=None, **kw):
        status, data = self.call(method, path, body, **kw)
        if status not in (200, 201, 204):
            raise RuntimeError(f"{method} {path} -> {status}: {json.dumps(data, ensure_ascii=False)[:500]}")
        return data


def psql_file(container: str, sql_file: Path):
    sql = sql_file.read_text(encoding="utf-8")
    subprocess.run(["docker", "exec", "-i", container, "psql", "-U", "admin", "-d", "gologdb", "-v", "ON_ERROR_STOP=1", "-q"],
                   input=sql.encode(), check=True)


def psql(container: str, sql: str):
    subprocess.run(["docker", "exec", "-i", container, "psql", "-U", "admin", "-d", "gologdb", "-v", "ON_ERROR_STOP=1", "-q"],
                   input=sql.encode(), check=True)


def restart_api(compose_dir: Path, api: Api):
    subprocess.run(["docker", "compose", "restart", "app"], cwd=compose_dir, check=True, capture_output=True)
    for _ in range(60):
        time.sleep(3)
        try:
            status, _ = api.call("POST", "/login", {"email": "admin@admin.com", "password": PASSWORDS["admin"]}, auth=False)
            if status == 200:
                return
        except (urllib.error.URLError, ConnectionError, OSError):
            pass
    raise SystemExit("A API não voltou após o restart.")


def digits(n: int) -> str:
    return "".join(str(rng.randint(0, 9)) for _ in range(n))


def plate() -> str:
    letters = "ABCDEFGHJKLMNPRSTUVWXYZ"
    return "".join(rng.choice(letters) for _ in range(3)) + str(rng.randint(0, 9)) + rng.choice(letters) + digits(2)


def business_days():
    d = START
    while d <= END:
        if d.weekday() < 5 and d not in HOLIDAYS:
            yield d
        d += timedelta(days=1)


def place(places: dict, key: str, jitter: int = 0) -> dict:
    """Endereço geocodificado; pontos que caíram no centro da cidade recebem deslocamento determinístico."""
    p = dict(places[key])
    if p.get("precision") == "cidade" or jitter:
        k = (sum(map(ord, key)) % 13) + jitter
        p["latitude"] = f"{float(p['latitude']) + 0.0021 * ((k % 5) - 2):.6f}"
        p["longitude"] = f"{float(p['longitude']) + 0.0024 * ((k % 7) - 3):.6f}"
    return p


# ============================================================================ dados de demonstração

GLOBAL_TRANSPORT_TYPES = [
    ("Carga Seca", "Mercadoria embalada sem controle de temperatura", "Proteger de chuva e umidade"),
    ("Refrigerada", "Perecíveis entre 0 °C e 8 °C", "Manter cadeia de frio; baú refrigerado"),
    ("Perigosa", "Produtos químicos classificados (ANTT 5.998)", "Motorista com MOPP; não misturar com alimentos"),
    ("Expressa", "Documentos e encomendas urgentes", "Prioridade no atendimento"),
    ("Fracionada", "Volumes pequenos de vários clientes", "Conferir volumes por cliente"),
]
GLOBAL_SHIPMENT_TYPES = [
    ("Alimentos", "Secos e mercearia", "Empilhamento máximo de 5 caixas"),
    ("Laticínios", "Leite, queijos e derivados", "Temperatura controlada"),
    ("Bebidas", "Engradados e fardos", "Paletizar"),
    ("Medicamentos", "Produtos farmacêuticos", "Rastreabilidade de lote"),
    ("Eletrônicos", "Eletroeletrônicos e eletrodomésticos", "Frágil; não tombar"),
    ("Materiais de Construção", "Cimento, argamassa, ferragens", "Carga pesada"),
    ("Documentos", "Envelopes e malotes", "Entregar em mãos"),
    ("Têxtil", "Tecidos e confecções", "Proteger de umidade"),
    ("Químicos", "Defensivos, solventes e insumos", "Ficha de emergência obrigatória"),
    ("Pet & Agro", "Rações, insumos agrícolas e pet", "Sacarias até 30 kg"),
]

# (placa fixa ou None, categoria, modelo, eixos, capacidade kg, volume m³, combustível, km/L, custo/km)
FLEET = [
    ("MOTO", "Honda CG 160 Cargo", 2, 25, 0.12, "FLEX", 38.0, 0.45),
    ("MOTO", "Yamaha Factor 150 Baú", 2, 25, 0.12, "FLEX", 40.0, 0.42),
    ("CARRO", "Fiat Mobi Like", 2, 300, 0.8, "FLEX", 13.5, 0.85),
    ("UTILITARIO", "Fiat Fiorino Endurance", 2, 650, 3.2, "FLEX", 11.0, 1.05),
    ("UTILITARIO", "Renault Kangoo E-Tech", 2, 600, 3.9, "ELETRICO", None, 0.55),
    ("UTILITARIO", "Volkswagen Saveiro Robust", 2, 700, 1.2, "FLEX", 10.5, 1.10),
    ("VAN", "Mercedes-Benz Sprinter 416", 2, 1500, 12.0, "DIESEL", 9.0, 1.60),
    ("VAN", "Renault Master Furgão", 2, 1600, 13.0, "DIESEL", 9.5, 1.55),
    ("VAN", "Mercedes-Benz eSprinter", 2, 1000, 11.0, "ELETRICO", None, 1.10),
    ("VUC", "Iveco Daily 35-160", 2, 3500, 18.0, "DIESEL", 7.5, 2.10),
    ("VUC", "Volkswagen Delivery 9.170", 2, 5000, 25.0, "DIESEL", 6.0, 2.40),
    ("TOCO", "Mercedes-Benz Accelo 1016", 2, 6000, 30.0, "DIESEL", 5.5, 2.70),
    ("TOCO", "Volkswagen 11.180", 2, 6500, 32.0, "DIESEL", 5.0, 2.80),
    ("TRUCK", "Volkswagen Constellation 24.280", 3, 14000, 50.0, "DIESEL", 3.5, 3.40),
    ("TRUCK", "Volvo VM 270", 3, 14000, 48.0, "DIESEL", 3.6, 3.30),
    ("BITRUCK", "Mercedes-Benz Atego 3030", 4, 19000, 60.0, "DIESEL", 3.0, 3.80),
    ("CAVALO_MECANICO", "Volvo FH 540", 3, 0, None, "DIESEL", 2.4, 4.20),
    ("CAVALO_MECANICO", "Scania R 450", 3, 0, None, "DIESEL", 2.6, 4.05),
    ("CAVALO_MECANICO", "DAF XF 530", 3, 0, None, "DIESEL", 2.5, 4.10),
]
TRAILERS = [  # modelo, eixos, capacidade kg, volume m³
    ("Randon Baú Carga Seca", 3, 30000, 90),
    ("Librelato Sider", 3, 28000, 95),
    ("Krone Refrigerada", 3, 26000, 80),
    ("Facchini Graneleira", 3, 32000, 60),
]
DRIVER_NAMES = [
    "Rafael Mendes", "Juliana Prado", "Marcos Vieira", "Patrícia Lopes", "Eduardo Nunes", "Bruna Teixeira",
    "Gustavo Ramos", "Larissa Fonseca", "Diego Moreira", "Aline Barbosa", "Thiago Cardoso", "Renata Duarte",
    "Felipe Araújo", "Camila Freitas", "Lucas Pacheco", "Vanessa Correia", "Rodrigo Campos", "Simone Rezende",
]
SHIFTS = [("06:00", "15:00"), ("07:00", "16:00"), ("08:00", "17:00"), ("09:00", "18:00"), ("10:00", "19:00")]

# chave do places.json -> (razão social, tipo, telefone, e-mail)
CLIENTS = {
    "bompreco": ("Supermercados Bom Preço Ltda", True),
    "atacadao_limeira": ("Atacadão Paulista Limeira Ltda", True),
    "rioclaro_alim": ("Distribuidora Rio Claro Alimentos Ltda", True),
    "farmacia_vida": ("Farmácia Vida & Saúde Eireli", True),
    "pao_dourado": ("Panificadora Pão Dourado Ltda", True),
    "sabor_terra": ("Restaurante Sabor da Terra Ltda", True),
    "leme_verde": ("Hortifruti Leme Verde Ltda", True),
    "construmais": ("Construmais Materiais de Construção Ltda", True),
    "mercadao_mogi": ("Mercadão Mogi Guaçu Ltda", True),
    "eletroshop": ("Eletroshop Piracicaba Comércio Ltda", True),
    "pet_campinas": ("Campinas Pet Center Ltda", True),
    "agro_cordeiro": ("Agropecuária Cordeirópolis Ltda", True),
    "ferragens_araras": ("Ferragens Araras Ltda", True),
    "laticinios_conchal": ("Laticínios Conchal S.A.", False),
    "embalagens_americana": ("Embalagens Americana Indústria Ltda", False),
    "textil_sbo": ("Têxtil Santa Bárbara S.A.", False),
    "usina_scc": ("Usina Santa Cruz Insumos Agrícolas Ltda", False),
}
EXTRA_STORES = {  # filiais / pontos de entrega adicionais de clientes
    "loja_bompreco2": "bompreco", "atacadao_limeira2": "atacadao_limeira", "rioclaro_alim2": "rioclaro_alim",
    "farmacia_vida2": "farmacia_vida", "pet_campinas2": "pet_campinas", "eletroshop2": "eletroshop",
}
# perfil de carga por cliente: (tipo de carga, tipo de transporte, faixa kg, kg/m³)
CLIENT_PROFILE = {
    "bompreco": ("Alimentos", "Carga Seca", (300, 2500), 250),
    "atacadao_limeira": ("Bebidas", "Carga Seca", (800, 6000), 350),
    "rioclaro_alim": ("Alimentos", "Carga Seca", (400, 3000), 250),
    "farmacia_vida": ("Medicamentos", "Fracionada", (5, 80), 120),
    "pao_dourado": ("Laticínios", "Refrigerada", (40, 300), 300),
    "sabor_terra": ("Laticínios", "Refrigerada", (30, 250), 300),
    "leme_verde": ("Alimentos", "Refrigerada", (80, 600), 220),
    "construmais": ("Materiais de Construção", "Carga Seca", (1500, 9000), 900),
    "mercadao_mogi": ("Alimentos", "Carga Seca", (300, 2200), 250),
    "eletroshop": ("Eletrônicos", "Fracionada", (40, 900), 120),
    "pet_campinas": ("Pet & Agro", "Carga Seca", (100, 1500), 400),
    "agro_cordeiro": ("Químicos", "Perigosa", (200, 2500), 700),
    "ferragens_araras": ("Materiais de Construção", "Fracionada", (20, 400), 600),
}
DOC_CLIENTS = ["farmacia_vida", "eletroshop", "pet_campinas", "bompreco"]
SUPPLIER_LINKS = {  # fornecedor -> clientes que recebem dele (coleta + entregas vinculadas)
    "embalagens_americana": ["bompreco", "atacadao_limeira", "mercadao_mogi", "rioclaro_alim"],
    "textil_sbo": ["eletroshop", "pet_campinas", "ferragens_araras"],
    "laticinios_conchal": ["pao_dourado", "sabor_terra", "leme_verde"],
    "usina_scc": ["agro_cordeiro", "construmais"],
}

# Cultiva: produtores (coleta) e varejistas (entrega) no Vale do Itajaí
CULTIVA_PRODUCERS = {
    "prod_pomerode": ("Sítio Bergmann Orgânicos", "Frutas"),
    "prod_gaspar": ("Chácara Vale Verde Hortaliças", "Verduras"),
    "prod_indaial": ("Fazenda Indaial Legumes", "Legumes"),
    "prod_timbo": ("Cooperativa Timbó Raízes", "Tubérculos e raízes"),
}
CULTIVA_RETAILERS = {
    "ret_blumenau1": "Empório Garcia",
    "ret_blumenau2": "Mercado Itoupava",
    "ret_itajai": "Hortifruti Porto Itajaí",
    "ret_brusque": "Sacolão Brusque",
    "ret_joinville": "Feira Viva Joinville",
}
CULTIVA_TYPES = {"Frutas": "CLIMATIZADO", "Verduras": "REFRIGERADO", "Legumes": "CARGA SECA",
                 "Tubérculos e raízes": "CARGA SECA", "Grãos e cereais": "CARGA SECA"}


# ============================================================================ etapas

def seed_catalogs(api: Api):
    print("• catálogos globais (padrão do sistema)")
    api.tenant = None  # Visão Global do Master => company null = padrão do sistema
    for name, desc, care in GLOBAL_TRANSPORT_TYPES:
        api.ok("POST", "/type-transport", {"name": name, "description": desc, "care": care})
    for name, desc, care in GLOBAL_SHIPMENT_TYPES:
        api.ok("POST", "/shipment-type", {"name": name, "description": desc, "care": care})
    vt = {}
    for code, name, desc in [("REFRIGERADA", "Carga refrigerada", "Perecíveis em cadeia de frio"),
                             ("PERIGOSA", "Carga perigosa", "Produtos químicos classificados"),
                             ("EXPRESSA", "Entrega expressa", "Documentos e urgências")]:
        vt[code] = api.ok("POST", "/visit-type", {"code": code, "name": name, "description": desc})["id"]
    api.ok("POST", "/visit-type-rule", {
        "name": "Perigosa não viaja com refrigerada",
        "description": "Químicos classificados não podem dividir o veículo com alimentos perecíveis.",
        "ruleType": "INCOMPATIBLE_ON_SAME_VEHICLE", "visitType1Id": vt["PERIGOSA"], "visitType2Id": vt["REFRIGERADA"]})
    for code, name, unit, desc in [("weight", "Peso", "kg", "Peso bruto da carga"),
                                   ("volume", "Volume", "m³", "Cubagem da carga"),
                                   ("pallets", "Paletes", "un", "Posições de palete PBR")]:
        api.ok("POST", "/demand-type", {"code": code, "name": name, "unit": unit, "description": desc})


def seed_golog(api: Api, places: dict, container: str) -> dict:
    api.tenant = GOLOG_ID
    print("• perfis de custo GoLog")
    api.ok("POST", "/optimization-profile", {
        "name": "Padrão GoLog", "description": "Equilíbrio entre distância e tempo; janela de 2 h antes do horário agendado.",
        "isDefault": True, "kmCostMultiplier": 1.0, "hourCostMultiplier": 1.0, "fixedCostPerVehicle": 60.0,
        "costPerTraveledHour": 0.0, "penaltyCostUnserved": 100000.0, "lateArrivalCostPerHour": 0.0,
        "defaultServiceDurationSeconds": 900, "timeWindowLeadMinutes": 120,
        "vehicleStartWindowLeadHours": 2, "vehicleEndWindowMarginHours": 2, "globalHorizonExtraDays": 1})
    api.ok("POST", "/optimization-profile", {
        "name": "Entrega Expressa", "description": "Prioriza tempo: hora mais cara e janela curta.",
        "isDefault": False, "kmCostMultiplier": 0.8, "hourCostMultiplier": 2.0, "fixedCostPerVehicle": 0.0,
        "penaltyCostUnserved": 200000.0, "defaultServiceDurationSeconds": 600, "timeWindowLeadMinutes": 60})
    api.ok("POST", "/optimization-profile", {
        "name": "Economia de Combustível", "description": "Prioriza distância e consolida em menos veículos.",
        "isDefault": False, "kmCostMultiplier": 1.6, "hourCostMultiplier": 0.8, "fixedCostPerVehicle": 150.0,
        "penaltyCostUnserved": 100000.0, "defaultServiceDurationSeconds": 900, "timeWindowLeadMinutes": 180})

    print("• usuários GoLog")
    api.ok("POST", "/user", {"name": "Operador GoLog", "email": "operador@golog.com.br", "password": PASSWORDS["operator"],
                             "cpf": digits(11), "userProfile": "OPERATOR", "companyId": GOLOG_ID})
    drivers = []
    for i, name in enumerate(DRIVER_NAMES):
        email = "motorista@golog.com.br" if i == 0 else (
            name.lower().replace(" ", ".").translate(str.maketrans("áâãéêíóôõúç", "aaaeeiooouc")) + "@golog.com.br")
        user = api.ok("POST", "/user", {"name": name, "email": email, "password": PASSWORDS["driver"], "cpf": digits(11),
                                        "userProfile": "DRIVER", "companyId": GOLOG_ID})
        exp = (date(2027, 1, 1) + timedelta(days=rng.randint(60, 1400))).strftime("%d/%m/%Y")
        drv = api.ok("POST", "/driver", {"cnhNumber": digits(11), "cnhExpiration": exp,
                                         "costPerHour": round(rng.uniform(24, 42), 2), "userId": user["id"]})
        drivers.append({"id": drv["id"], "name": name, "email": email})

    print("• frota GoLog")
    vehicles = []
    for vtype, model, axles, cap, vol, fuel, kml, cost in FLEET:
        body = {"plate": plate(), "renavam": digits(11), "model": model, "numberAxles": axles,
                "maximumCapacity": cap, "typeFuel": fuel, "costPerKilometer": cost, "status": "ATIVO",
                "companyId": GOLOG_ID, "vehicleType": vtype, "cargoVolume": vol, "kmPerLiter": kml}
        vehicles.append(api.ok("POST", "/tractor", body))
    trailers = []
    for model, axles, cap, vol in TRAILERS:
        trailers.append(api.ok("POST", "/trailer", {"plate": plate(), "renavam": digits(11), "model": model,
                                                     "numberAxles": axles, "maximumCapacity": cap, "maximumVolume": vol,
                                                     "status": "ATIVO", "companyId": GOLOG_ID}))
    cavalos = [v for v in vehicles if v.get("vehicleType") == "CAVALO_MECANICO"]
    combos = [(cavalos[0], [trailers[0]]), (cavalos[1], [trailers[2]]), (cavalos[2], [trailers[1], trailers[3]])]
    for cav, trs in combos:
        body = {"observation": f"{cav['model']} + " + " + ".join(t["model"] for t in trs), "equipament1Id": cav["id"],
                "equipament2Id": trs[0]["id"], "equipament3Id": trs[1]["id"] if len(trs) > 1 else None}
        api.ok("POST", "/equipament-group", body)
    groups = api.ok("GET", "/equipament-group")

    print("• clientes, fornecedores e endereços")
    clients, addresses = {}, {}
    for key, (legal, is_client) in CLIENTS.items():
        p = place(places, key)
        addr = api.ok("POST", "/address", {k: p[k] for k in ("street", "number", "district", "city", "state", "cep",
                                                             "latitude", "longitude")} | {"country": "Brasil"})
        slug = key.split("_")[0]
        comp = api.ok("POST", "/company", {"legalName": legal, "isCliente": is_client, "cnpjCpf": digits(14),
                                           "phoneNumber": f"(19){rng.randint(3000, 3999)}-{rng.randint(1000, 9999)}",
                                           "email": f"logistica@{slug}.com.br", "addressId": addr["id"]})
        clients[key] = comp["id"]
        addresses[key] = [addr["id"]]
    for store_key, owner in EXTRA_STORES.items():
        p = place(places, store_key, jitter=3)
        addr = api.ok("POST", "/address", {k: p[k] for k in ("street", "number", "district", "city", "state", "cep",
                                                             "latitude", "longitude")} | {"country": "Brasil",
                                                                                          "complement": "Filial"})
        addresses[owner].append(addr["id"])
    golog_cd = api.ok("GET", f"/company/{GOLOG_ID}")["address"]["id"]

    print("• escalas (dias úteis até 31/12)")
    by_vehicle = {g["equipament1"]["id"]: g for g in groups}
    pairs = []  # (driver, group, turno)
    ordered = [v for v in vehicles if v.get("vehicleType") != "CAVALO_MECANICO"] + cavalos
    for i, veh in enumerate(ordered):
        if i >= len(drivers):
            break
        pairs.append((drivers[i], by_vehicle[veh["id"]], SHIFTS[i % len(SHIFTS)]))
    schedules_by_day = {}
    for day in business_days():
        for drv, grp, (start, end) in pairs:
            ws = api.ok("POST", "/workSchedule", {"driverId": drv["id"], "equipamentGroupId": grp["id"],
                                                  "scheduleDate": str(day), "startWorkday": start + ":00",
                                                  "endWorkday": end + ":00", "status": "ATIVO",
                                                  "breakDurationMinutes": 60, "earliestBreakTime": "11:30:00",
                                                  "latestBreakTime": "13:30:00", "maxDrivingHoursWithoutBreak": 4,
                                                  "minDrivingBreakMinutes": 30})
            schedules_by_day.setdefault(day, []).append(ws["id"])

    print("• remessas (coletas, entregas vinculadas e avulsas)")
    ttypes = {t["name"]: t["id"] for t in api.ok("GET", "/type-transport")}
    stypes = {t["name"]: t["id"] for t in api.ok("GET", "/shipment-type")}
    me = api.ok("GET", "/user")
    admin_id = next(u["id"] for u in me if u["email"] == "admin@admin.com")
    shipments_by_day = {}

    def create(day, hh, mm, op, customer_key, addr_id, stype, ttype, kg, m3, origin=None):
        when = datetime(day.year, day.month, day.day, hh, mm)
        body = {"typeOperation": op, "weight": round(kg, 1), "volume": round(m3, 2), "schedulind": when.isoformat(),
                "status": "PENDENTE", "userId": admin_id, "shipmentTypeId": stypes[stype],
                "typeTransportId": ttypes[ttype], "addressId": addr_id,
                "customerId": GOLOG_ID if customer_key == "golog" else clients[customer_key],
                "operationOrigemId": origin}
        s = api.ok("POST", "/shipment", body)
        shipments_by_day.setdefault(day, []).append(s["id"])
        return s["id"]

    for day in business_days():
        # 1 a 2 coletas em fornecedor com 2 a 3 entregas vinculadas
        for supplier in rng.sample(list(SUPPLIER_LINKS), k=rng.choice([1, 2])):
            ch, cm = rng.choice([(7, 30), (8, 0), (8, 30)])
            first_client = SUPPLIER_LINKS[supplier][0]
            stype, ttype, _, _ = CLIENT_PROFILE[first_client]
            col = create(day, ch, cm, "COLETA", supplier, addresses[supplier][0], stype, ttype, 0, 0)
            for client in rng.sample(SUPPLIER_LINKS[supplier], k=min(len(SUPPLIER_LINKS[supplier]), rng.choice([2, 3]))):
                stype, ttype, (lo, hi), dens = CLIENT_PROFILE[client]
                kg = rng.uniform(lo, min(hi, 4000))
                create(day, ch + rng.choice([3, 4, 5]), rng.choice([0, 30]), "ENTREGA", client,
                       rng.choice(addresses[client]), stype, ttype, kg, kg / dens, origin=col)
        # 5 a 8 entregas avulsas saindo do CD
        for client in rng.sample(list(CLIENT_PROFILE), k=rng.randint(5, 8)):
            stype, ttype, (lo, hi), dens = CLIENT_PROFILE[client]
            kg = rng.uniform(lo, hi)
            create(day, rng.randint(9, 16), rng.choice([0, 15, 30, 45]), "ENTREGA", client,
                   rng.choice(addresses[client]), stype, ttype, kg, kg / dens)
        # 1 a 2 entregas expressas leves (documentos/medicamentos) — cabem em moto
        for client in rng.sample(DOC_CLIENTS, k=rng.choice([1, 2])):
            kg = rng.uniform(0.3, 8)
            create(day, rng.randint(9, 15), rng.choice([0, 30]), "ENTREGA", client, addresses[client][0],
                   "Documentos" if client != "farmacia_vida" else "Medicamentos", "Expressa", kg, 0.02)
        # coleta de devolução no CD em alguns dias
        if rng.random() < 0.3:
            create(day, 16, 30, "COLETA", "golog", golog_cd, "Alimentos", "Carga Seca", 150, 0.8)

    return {"schedules": schedules_by_day, "shipments": shipments_by_day, "drivers": drivers}


def optimize_days(api: Api, data: dict, tenant: str, label: str, days: int, priority="ECONOMIA"):
    api.tenant = tenant
    for day in [d for d in business_days()][:days]:
        sh = data["shipments"].get(day, [])
        ws = data["schedules"].get(day, [])
        if not sh or not ws:
            continue
        status, res = api.call("POST", "/api-route-optimization",
                               {"shipmentIds": sh, "workScheduleIds": ws, "routePriority": priority})
        if status == 200:
            print(f"  {label} {day}: {res['routesCount']} rota(s), {res['plannedShipments']} parada(s), "
                  f"{res['totalDistanceMeters'] / 1000:.1f} km, {len(res['skipped'])} não atendida(s)")
        else:
            print(f"  {label} {day}: falhou {status} {str(res)[:200]}")


def seed_cultiva(api: Api, places: dict) -> dict:
    print("• tenant Cultiva: usuários")
    api.tenant = CULTIVA_ID
    api.ok("POST", "/user", {"name": "Cultiva Integracao Middleware", "email": "cultiva@cultiva.com",
                             "password": PASSWORDS["cultiva"], "cpf": digits(11), "userProfile": "ADMIN",
                             "companyId": CULTIVA_ID})
    api.ok("POST", "/user", {"name": "Operação Cultiva", "email": "operacao@cultiva.com",
                             "password": PASSWORDS["cultiva"], "cpf": digits(11), "userProfile": "OPERATOR",
                             "companyId": CULTIVA_ID})

    cult = Api(api.base)
    cult.login("cultiva@cultiva.com", PASSWORDS["cultiva"])
    cult.tenant = CULTIVA_ID
    print("• tenant Cultiva: catálogos e perfil (como o middleware deles faz)")
    for name, desc, care in [("CARGA SECA", "Hortifruti sem refrigeração", "Caixas ventiladas"),
                             ("CLIMATIZADO", "Frutas sensíveis a calor", "Manter abaixo de 18 °C"),
                             ("REFRIGERADO", "Folhosas e perecíveis", "Entre 2 °C e 8 °C")]:
        cult.ok("POST", "/type-transport", {"name": name, "description": desc, "care": care})
    for cat in CULTIVA_TYPES:
        cult.ok("POST", "/shipment-type", {"name": cat, "description": f"Categoria Cultiva: {cat}", "care": ""})
    cult.ok("POST", "/optimization-profile", {
        "name": "Equilibrado Cultiva", "description": "Coleta no produtor e entrega no varejista no mesmo dia.",
        "isDefault": True, "kmCostMultiplier": 1.0, "hourCostMultiplier": 1.2, "fixedCostPerVehicle": 80.0,
        "penaltyCostUnserved": 100000.0, "defaultServiceDurationSeconds": 1200, "timeWindowLeadMinutes": 180})

    print("• tenant Cultiva: escalas dos 3 motoristas")
    schedules_by_day = {}
    for day in business_days():
        for drv, (grp, start, end) in CULTIVA_SCHEDULES.items():
            ws = cult.ok("POST", "/workSchedule", {"driverId": drv, "equipamentGroupId": grp, "scheduleDate": str(day),
                                                   "startWorkday": start + ":00", "endWorkday": end + ":00",
                                                   "status": "ATIVO"})
            schedules_by_day.setdefault(day, []).append(ws["id"])

    print("• tenant Cultiva: pedidos via /integration/invoices/json (formato do middleware)")
    shipments_by_day = {}
    order = 1000
    # CNPJ estável por produtor/varejista: o GoLog reaproveita o cliente já cadastrado no tenant
    docs = {k: digits(14) for k in list(CULTIVA_PRODUCERS) + list(CULTIVA_RETAILERS)}
    days = [d for d in business_days()][:12]
    for day in days:
        batch_by_cat = {}
        for _ in range(rng.randint(2, 4)):
            order += 1
            pkey = rng.choice(list(CULTIVA_PRODUCERS))
            rkey = rng.choice(list(CULTIVA_RETAILERS))
            pname, cat = CULTIVA_PRODUCERS[pkey]
            qty = float(rng.randint(40, 900))
            pick = datetime(day.year, day.month, day.day, rng.choice([8, 9]), 0) + timedelta(hours=3)  # UTC (-03:00)
            drop = pick + timedelta(hours=rng.choice([3, 4]))
            common = {"series": "1", "volume": round(qty * 0.005, 3), "value": round(qty * rng.uniform(4, 11), 2),
                      "typeTransportName": CULTIVA_TYPES[cat], "shipmentTypeName": cat}
            pp, rp = places[pkey], places[rkey]
            col = common | {"invoiceNumber": f"DO-{order}-COL", "accessKey": f"NFE{order:08d}COL",
                            "typeOperation": "COLETA", "weight": qty, "scheduledDate": pick.strftime("%Y-%m-%dT%H:%M:%SZ"),
                            "notes": f"[{cat}] Coleta Pedido #{order}", "senderName": pname, "senderDocument": docs[pkey],
                            "customerName": pname, "customerDocument": docs[pkey], "customerEmail": "",
                            "customerPhone": "(47)99999-0000", "street": pp["street"], "number": pp["number"],
                            "district": pp["district"], "city": pp["city"], "state": pp["state"], "cep": pp["cep"],
                            "latitude": pp["latitude"], "longitude": pp["longitude"]}
            ent = common | {"invoiceNumber": f"DO-{order}-ENT", "accessKey": f"NFE{order:08d}ENT",
                            "typeOperation": "ENTREGA", "weight": qty, "scheduledDate": drop.strftime("%Y-%m-%dT%H:%M:%SZ"),
                            "notes": f"[{cat}] Entrega Pedido #{order}", "senderName": pname, "senderDocument": docs[pkey],
                            "customerName": CULTIVA_RETAILERS[rkey], "customerDocument": docs[rkey],
                            "customerEmail": "", "customerPhone": "(47)98888-0000", "street": rp["street"],
                            "number": rp["number"], "district": rp["district"], "city": rp["city"], "state": rp["state"],
                            "cep": rp["cep"], "latitude": rp["latitude"], "longitude": rp["longitude"]}
            batch_by_cat.setdefault(cat, []).extend([col, ent])
        for cat, invoices in batch_by_cat.items():
            res = cult.ok("POST", "/integration/invoices/json", {"invoices": invoices})
            if res["errorCount"]:
                raise RuntimeError(f"Importação Cultiva com erro: {res['errors']}")
            shipments_by_day.setdefault(day, []).extend(res["createdShipmentIds"])
    return {"schedules": schedules_by_day, "shipments": shipments_by_day, "api": cult}


def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--api", default="http://localhost:8081")
    ap.add_argument("--container", default="postgres")
    ap.add_argument("--yes", action="store_true", help="confirma que TODOS os dados atuais serão apagados")
    args = ap.parse_args()
    if not args.yes:
        sys.exit("Este script APAGA o banco inteiro. Faça backup (pg_dump) e rode novamente com --yes.")

    places = json.loads((HERE / "places.json").read_text(encoding="utf-8"))
    compose_dir = HERE.parent.parent
    api = Api(args.api)

    print("• reset do banco (00_reset.sql)")
    psql_file(args.container, HERE / "00_reset.sql")
    print("• reiniciando a API")
    restart_api(compose_dir, api)
    api.login("admin@admin.com", PASSWORDS["admin"])

    seed_catalogs(api)
    # métricas weight/volume/pallets como padrão do sistema (não podem ser excluídas)
    psql(args.container, "UPDATE demand_type_table SET is_system_default = true WHERE company_id IS NULL;")
    golog = seed_golog(api, places, args.container)
    cultiva = seed_cultiva(api, places)

    print("• roteirização real (Google Route Optimization)")
    optimize_days(api, golog, GOLOG_ID, "GoLog", PLAN_DAYS)
    optimize_days(cultiva["api"], cultiva, CULTIVA_ID, "Cultiva", 1, priority="TEMPO")

    api.tenant = None
    pbi = api.ok("POST", "/login/powerbi-token")

    out = HERE / "CREDENCIAIS.local.md"
    lines = ["# Credenciais do banco de demonstração (arquivo local, não versionado)", "",
             "| Tenant | Perfil | E-mail | Senha |", "|---|---|---|---|",
             f"| GoLog (Master) | ADMIN | admin@admin.com | {PASSWORDS['admin']} |",
             f"| GoLog | OPERATOR | operador@golog.com.br | {PASSWORDS['operator']} |",
             f"| GoLog | DRIVER | motorista@golog.com.br | {PASSWORDS['driver']} |",
             f"| Cultiva | ADMIN (middleware) | cultiva@cultiva.com | {PASSWORDS['cultiva']} |",
             f"| Cultiva | OPERATOR | operacao@cultiva.com | {PASSWORDS['cultiva']} |",
             f"| Cultiva | DRIVER | henrique.costa@cultiva.com · camila.rocha@cultiva.com · thiago.almeida@cultiva.com | {CULTIVA_DRIVER_PASSWORD_NOTE} |",
             "", f"Demais motoristas GoLog (senha {PASSWORDS['driver']}):", ""]
    lines += [f"- {d['name']} — {d['email']}" for d in golog["drivers"][1:]]
    lines += ["", "Token Power BI (perfil POWERBI, sem expiração — revogável):", "", f"```\n{pbi['token']}\n```", ""]
    out.write_text("\n".join(lines), encoding="utf-8")

    total_g = sum(len(v) for v in golog["shipments"].values())
    total_c = sum(len(v) for v in cultiva["shipments"].values())
    print(f"\nPronto: {total_g} remessas GoLog e {total_c} remessas Cultiva; credenciais em {out}")


if __name__ == "__main__":
    main()
