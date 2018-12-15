![](app/src/main/banner.png)

# Desafio 4 Devs
Aplicação Android desenvolvida como solução para o problema apresentado [aqui](https://github.com/ForLogic/desafio-4-devs). Neste app, é possível cadastrar clientes, realizar avaliações individuais sobre o serviço com o mesmo e visualizar o resultado mensal da pesquisa de satisfação.
## Cadastro de Clientes
O cadastro contém as informações esenciais do cliente, sendo elas
- Nome (Razão Social ou nome fantasia)
- Nome de contato
- Data em que se tornou cliente

A fim de auxiliar a implementação, foram adicionados dois atributos sendo eles:
- Data da última avaliação em que participou
- Flag (Promotor, Neutro ou Detrator)
## Cadastro de avaliações
Ao pressionar o botão "Avaliações" no menu principal serão selecionados 20% os clientes já cadastrados de modo aletório e automático, estes compondo a pequisa de satisação mensal. Você será enviado para a tela de clientes selecionados, no qual será possível escolher qual dos participantes avaliar.
Ao selecionar um participante, você será enviado para a tela de avaliação, onde é possível:
- Escolher uma nota de 0 a 10
- Descrever o motivo da nota

## Visualização de resultados
A visualização de resultados só é possível se todos os clientes anteriormente selecionados forem avaliados. Assim, ao pressionar o botão Resultados, será gerado o cálculo do NPS e serão apresentados dados como:
- Pontuação NPS
- Barra de meta
- Lista de participantes

## Últimas considerações
Como sempre, existem melhorias que podem ser aplicadas como por exemplo:
- Uma melhor usabilidade da característica Assíncrona do Firebase permitindo o cadastro em modo offline
- Uma melhor abordagem ao selecionar automaticamente e aleatóriamente os 20% dos clientes
- Ao selecionar um cliente, alterar o status da flag afim de que ele não apareça novamente na lista de "Clientes Selecionados"
- Melhorar a velocidade das requisições
- Melhorar a portabilidade e leitura do código
