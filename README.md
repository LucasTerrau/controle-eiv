# Controle de Prazos de EIV

API REST para acompanhar os prazos de análise de processos de **Estudo de Impacto de Vizinhança (EIV)** em uma prefeitura.

## Por que este projeto existe

Em 2025 estagiei na Secretaria de Planejamento da Prefeitura de Passos (MG), onde acompanhei processos de EIV em planilhas. Automatizei parte desse controle no Google Sheets e fiquei pensando em como ele seria como sistema: com alertas automáticos, regras de prazo testadas e os dados em um banco.

Este projeto é essa ideia posta em prática, feita para estudar Java e Spring Boot. As regras de prazo são simplificadas e não seguem a legislação de nenhum município específico. Todos os dados de exemplo são fictícios.

## O que ele faz

- Cadastra processos de EIV com protocolo, empreendimento, requerente, endereço e engenheiro responsável.
- Calcula o **vencimento da análise** a partir da data de protocolo e do prazo (30 dias por padrão, configurável).
- Classifica cada processo como `NO_PRAZO`, `VENCE_EM_BREVE`, `VENCIDO`, `SUSPENSO` ou `CONCLUIDO`.
- **Suspende o prazo** enquanto o processo está `AGUARDANDO_DOCUMENTOS` e soma os dias parados ao vencimento quando a análise volta.
- Lista os **alertas**: processos em análise vencidos ou que vencem nos próximos 7 dias, do mais urgente para o menos urgente.
- Mostra um **resumo** com a contagem de processos por situação.
- Impede protocolo duplicado e a reabertura de processos já aprovados ou reprovados.

## Tecnologias

Java 21 · Spring Boot 4 (Web MVC, Data JPA, Validation) · PostgreSQL · H2 · Flyway · JUnit 5 + AssertJ + MockMvc · OpenAPI/Swagger · Docker

## Como rodar

**Opção 1: só com Java 21 (banco H2 em memória)**

```bash
./mvnw spring-boot:run
```

No Windows, use `mvnw.cmd spring-boot:run`. A API sobe em `http://localhost:8080` já com 5 processos de exemplo.

- Documentação interativa (Swagger): http://localhost:8080/swagger-ui.html
- Console do banco H2: http://localhost:8080/h2-console (JDBC URL `jdbc:h2:mem:eiv`, usuário `sa`)

**Opção 2: com Docker (PostgreSQL)**

```bash
docker compose up --build
```

**Testes**

```bash
./mvnw test
```

São 19 testes: regras de prazo na entidade, cálculo da situação com data fixa e a API de ponta a ponta.

## Endpoints

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/api/processos` | Cadastra um processo |
| `GET` | `/api/processos?status=&situacao=` | Lista os processos ordenados pelo vencimento, com filtros opcionais |
| `GET` | `/api/processos/{id}` | Busca um processo |
| `PUT` | `/api/processos/{id}` | Atualiza os dados de um processo |
| `PATCH` | `/api/processos/{id}/status` | Muda o status (`EM_ANALISE`, `AGUARDANDO_DOCUMENTOS`, `APROVADO`, `REPROVADO`) |
| `DELETE` | `/api/processos/{id}` | Exclui um processo |
| `GET` | `/api/processos/alertas` | Processos vencidos ou vencendo |
| `GET` | `/api/processos/resumo` | Contagem por situação do prazo |

Exemplo de cadastro:

```bash
curl -X POST http://localhost:8080/api/processos \
  -H "Content-Type: application/json" \
  -d '{
    "numeroProtocolo": "EIV-2026-0150",
    "empreendimento": "Academia Corpo em Forma",
    "requerente": "Corpo em Forma Ltda.",
    "endereco": "Rua Sete, 77",
    "dataProtocolo": "2026-10-01",
    "prazoAnaliseDias": 45
  }'
```

Erros seguem o padrão Problem Details (RFC 9457). Um cadastro com campos faltando, por exemplo, retorna `400` com a lista dos campos inválidos.

## Decisões de projeto

- **Prazo em dias corridos.** Prefeituras diferentes contam prazos de formas diferentes. Aqui o prazo é configurável (`eiv.prazo-padrao-dias`), mas a contagem é sempre em dias corridos. Dias úteis seriam uma boa evolução.
- **Regra de suspensão dentro da entidade.** A lógica de somar os dias parados fica em `ProcessoEiv.alterarStatus`, e não no service, para que nenhum caminho do código consiga mudar o status sem atualizar o vencimento.
- **Data de hoje injetada (`Clock`).** O cálculo de "vencido" depende do dia atual. Com um `Clock` injetado, os testes fixam a data e não quebram com o passar do tempo.
- **Esquema versionado com Flyway.** O banco é criado por migrations, e o Hibernate só valida se as entidades batem com as tabelas.

## Próximos passos

- Contagem em dias úteis, considerando feriados municipais
- Envio de e-mail ao engenheiro responsável quando um processo entra nos alertas
- Autenticação e perfis (servidor, engenheiro, gestor)
- Tela web para consulta dos processos

---

Lucas Terra Wachsmuth · [LinkedIn](https://www.linkedin.com/in/lucasterrau) · [GitHub](https://github.com/LucasTerrau)
