# GoLogAPI 

O **GoLogAPI** é o ecossistema de backend robusto desenvolvido para o gerenciamento, otimização logística e monitoramento de frotas em tempo real. Projetado com foco em alta performance e escalabilidade, o sistema integra serviços de telemetria, roteirização inteligente e persistência isolada de dados.

Este repositório faz parte do projeto central de otimização logística multiplataforma, atuando como o core engine que serve o aplicativo móvel (**GoLogApp**), sitema WEb (**GoLogWEB) e processa dados de dispositivos IoT.

---

## 🛠️ Tecnologias e Ferramentas

* **Ambiente de Desenvolvimento:** Ubuntu (Linux Nativo)
* **Linguagem:** Java 21 (JDK 21)
* **Framework Principal:** Spring Boot 3.5.10
* **Persistência de Dados:** Spring Data JPA / Hibernate
* **Banco de Dados:** PostgreSQL
* **Segurança:** Spring Security & JWT (JSON Web Tokens)
* **Gerenciamento de Dependências:** Maven
* **Containerização:** Docker & Docker Compose
* **Integrações de Terceiros:** Google Maps Route Optimization API


## 🏗️ Arquitetura e Padrões de Projeto

O backend foi construído seguindo os princípios do **SOLID** e patterns de mercado para garantir manutenibilidade e baixo acoplamento:

* **Camada de Controladores (REST API):** Endpoints limpos e padronizados que gerenciam as requisições HTTP, utilizando DTOs para isolar completamente as entidades do banco de dados e ResponseEntity para personalicação das repostas em JSON.
* **Camada de Serviço (Business Logic):** Centralização das regras de negócio, garantindo que a lógica seja independente da forma de transporte dos dados.
* **Camada de Repository:** Interfaces que estendem o *Spring Data JPA*, responsáveis por intermediar de forma abstrata e eficiente a comunicação e as consultas ao PostgreSQL.
* **Camada de Validações:** Componentes dedicados a aplicar regras estritas de integridade de dados através do *Jakarta Bean Validation* e padrões customizados (`@Pattern`).
* **Mappers:** Camada de conversão responsável por mapear de forma segura e performática os dados entre Entidades (Models) e DTOs.
* **Global Exception Handler:** Tratamento centralizado de exceções da API utilizando `@ControllerAdvice`, garantindo respostas de erro padronizadas e limpas para o cliente (mobile/Web).
* **DTOs (Data Transfer Objects):** Objetos de transferência de dados (implementados preferencialmente como *Java Records*) para validar e expor apenas os dados necessários em cada requisição.
* **SpringDoc OpenAPI (Swagger):** Documentação interativa e automatizada dos endpoints da API, facilitando a integração com o aplicativo móvel e testes de rotas.
* **Spring Security com JWT:** Controle de autenticação e autorização stateless baseado em tokens *JSON Web Tokens*, garantindo a segurança dos endpoints da API.
* **Data Isolation (Docker):** Utilização estrita de containers Docker e Docker Compose para padronizar o ambiente de desenvolvimento e isolar a instância do banco de dados PostgreSQL.

## 🚦 Principais Funcionalidades

* **Roteirização Avançada:** Integração com a API do Google Maps para otimização de frotas e cálculo de caminhos eficientes.
* **Processamento de Telemetria:** Prontidão para receber e processar dados de rastreamento de módulos IoT/hardware.
* **Autenticação Segura:** Controle de acesso baseado em perfis com Spring Security e JWT.
* **Validações de Dados:** Validações de dados com Patterns para integritade dos dados.
**Validação Estrita de Dados:** Uso de *Jakarta Bean Validation* e expressões regulares (`@Pattern`) diretamente nos DTOs.
## 📦 Como Executar o Projeto

### Pré-requisitos
* Docker e Docker Compose instalados.
* JDK 21 configurado localmente (opcional, caso queira rodar fora do container).

### Passos para Execução

1. **Clonar o repositório:**
   ```bash
   git clone https://github.com/jonathanAlves2000/GoLogAPI
   cd GoLogAPI

2. **Swagger UI**
   http://147.15.18.21:8081/swagger-ui/index.html#/   
