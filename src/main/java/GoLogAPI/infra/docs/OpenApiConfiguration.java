package GoLogAPI.infra.docs;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfiguration {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("GoLog TMS — API de Roteirização, Otimização Logística & Multi-Tenant")
                        .version("2.0.0")
                        .description("""
                                ### Plataforma Integrada de Gestão de Transportes (TMS) & Otimização de Frotas
                                
                                A **GoLog API** é uma solução corporativa de alta performance para planejamento de viagens, 
                                roteirização inteligente com restrições operacionais complexas (VRP com Janelas de Tempo, 
                                Compatibilidade de Carga e Tipos de Veículos), rastreamento em tempo real e isolamento 
                                estrito **Multi-Tenant** (Matrizes, Filiais e Clientes/Fornecedores).
                                
                                ---
                                #### 🔑 Autenticação & Cabeçalhos
                                - **Bearer JWT**: Obtenha o token através do endpoint `POST /login` e utilize no botão **Authorize** acima.
                                - **Header Multi-Tenant (`X-Tenant-Id`)**: Opcional para contas `Master` ou administradores de `Matriz` 
                                  alternarem o contexto da empresa em tempo real sem precisar gerar um novo token.
                                
                                ---
                                #### 🚀 Guia Rápido de Integração para Tenants (ERP / WMS / TMS Externos)
                                1. **Autenticação**: Faça `POST /login` com suas credenciais de tenant.
                                2. **Cadastros de Base**: Garanta que seus Destinatários/Clientes e Endereços com latitude/longitude estejam cadastrados via `POST /address` e `POST /company`.
                                3. **Injeção de Demandas**: Envie suas coletas e entregas para o backlog de roteirização via `POST /shipment`.
                                4. **Parametrização**: Configure ou selecione suas regras operacionais de otimização em `GET /optimization-profile`.
                                5. **Otimização**: Dispare o motor heurístico de roteirização via `POST /transport/optimize-vrp` para receber as rotas balanceadas com menor custo e tempo!
                                """)
                        .contact(new Contact()
                                .name("GoLog Engenharia & Suporte")
                                .email("contato@golog.com.br")
                                .url("https://golog.com.br"))
                        .license(new License()
                                .name("Proprietário / GoLog Soluções Logísticas")
                                .url("https://golog.com.br/termos")))
                .servers(List.of(
                        new Server().url("http://localhost:8081").description("Ambiente Local / Docker"),
                        new Server().url("http://147.15.18.21:8081").description("Servidor de Homologação / Nuvem")
                ))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .name("bearerAuth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Insira seu token JWT gerado pelo endpoint POST /login no formato: Bearer {token}"))
                        .addSecuritySchemes("tenantHeader", new SecurityScheme()
                                .name("X-Tenant-Id")
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .description("UUID do Tenant ativo para simulação e isolamento contextual (disponível para Master e Matriz)")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth").addList("tenantHeader"))
                .tags(List.of(
                        new Tag().name("Autenticação").description("Login, renovação e validação de tokens JWT para operadores, administradores e integrações"),
                        new Tag().name("Gerenciamento de Tenants").description("Provisionamento e consulta de Tenants (Matrizes, Filiais e Master GoLog)"),
                        new Tag().name("Cargas e Remessas (Shipments)").description("Entregas, coletas, dimensões, pesos, janelas de horário e status do backlog logístico"),
                        new Tag().name("Transportes & Roteirização").description("Criação de viagens, cálculo de rotas otimizadas (VRP), distâncias e custos"),
                        new Tag().name("Regras de Otimização & Custos").description("Perfis de roteirização, limites de jornada, restrições e custos por km/hora"),
                        new Tag().name("Frotas & Equipamentos").description("Gestão de cavalos mecânicos (Tractor), carretas (Trailer) e conjuntos acoplados"),
                        new Tag().name("Motoristas & Escalas").description("Cadastro de motoristas, CNH, custos de mão de obra e jornadas de trabalho"),
                        new Tag().name("Empresas & Parceiros").description("Cadastro de destinatários, clientes e transportadoras parceiras"),
                        new Tag().name("Endereços & Coordenadas").description("Pontos geocodificados com latitude e longitude para cálculo de rota"),
                        new Tag().name("Ocorrências & Telemetria").description("Rastreamento de veículos, telemetria em tempo real e apontamento de sinistros/atrasos")
                ));
    }
}
