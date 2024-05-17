# Relatório do Projeto

### Linguagens e Frameworks
- O projeto foi desenvolvido em Java, HTML/CSS (Thymeleaf) e JavaScript como linguagens de programação. O framework Spring Boot é usado para o desenvolvimento do backend, enquanto Thymeleaf a renderização do lado do servidor no frontend. 

### Funcionalidades:
- O projeto permite a indexação de URLs na database presente nos barrels, pesquisa de palavras tanto nos barrels como usando a API do HackerNews, e exibição dos resultados de pesquisa.
- Uma status page atualizada em tempo real também é exibida se pedido pelo botão "Status".

### Arquivos Principais da Meta 2:  

###

#### MessagingController.java
- Este é o controlador principal que lida com as solicitações HTTP. Ele tem métodos para inserir URLs, realizar buscas e exibir a página de status.
search.html: Este é o template Thymeleaf para a página de busca. Ele contém o formulário de busca, a lista de resultados e os botões de navegação.
#### HackerNewsController.java
- Este é o controlador que lida com as solicitações para a API do HackerNews. Tem métodos para buscar as stories mais recentes para mais tarde serem processadas.


### Problemas Identificados:
- O projeto tem um problema com a renderização do template Thymeleaf. O erro ocorre ao tentar analisar a expressão de mudança de páginas no template search.html. Isso é causado porque a variável page é null no momento em que a expressão é avaliada. Uma solução proposta foi garantir que a variável page seja corretamente definida no contexto antes que o template seja renderizado, mas mesmo asism não ficou a 100%
