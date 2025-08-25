## Teste técnico - Riachuelo

# Rodando projeto local
* O caminho mais prático é via docker compose, rodando o seguinte comando
```
docker compose up
```
* Outro caminho seria criando um banco de dados PostgreSQL local, ajustando com os parametros no application-dev.properties

# Documentação
* Disponibilizada localmente no endpoint http://localhost:8080/swagger-ui/index.html
* Projeto foi deployado utilizando o PaaS chamado Render, podendo ser acessado pelo seguinte url https://riachuelo-challenge.onrender.com/swagger-ui/index.html
* Code coverave
  <img width="1470" height="307" alt="image" src="https://github.com/user-attachments/assets/39a0d72f-adc7-4fc9-9dd9-cce254f65ff3" />


# Comentários
* Utilizado lombok para criação de DTOs em conjunto com records para melhor visualização de criação de DTOs utilizando o design pattern e anotação Builder
* Foi utilizado o mapstruct para realizar o Mapper dos DTOs para classe e vice versa
* Realizei uso de IA, mais precisamente, o chatgpt, para conseguir acelerar o processo de testes, aumentando a cobertura, identificação e realização de demais casos de testes e cenários.
