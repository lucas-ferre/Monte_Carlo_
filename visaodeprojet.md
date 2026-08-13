# Simulador de Monte Carlo para Aproximação de Pi

## Visão geral

Este projeto explora o método de Monte Carlo para aproximar o valor de π. Um grande conjunto de pontos aleatórios é distribuído em um plano bidimensional, e a proporção de pontos que cai dentro de um círculo inscrito em um quadrado fornece a estimativa.

Além da aproximação de π, o resultado apresenta a quantidade de pontos observados, a variância estimada do estimador, o erro padrão, um intervalo de confiança aproximado de 95% e o tempo da simulação. Essas medidas descrevem uma única execução; o projeto não calcula uma média de grupos de simulações.

O objetivo educacional é revisar conceitos de Python e transportar a mesma solução para Java e C#, preservando a organização e o comportamento essenciais em três ecossistemas diferentes.

## Fundamento matemático

O método consiste em amostrar pontos independentes e identicamente distribuídos dentro de um quadrado de lado $2r$ e verificar quantos pertencem ao círculo inscrito de raio $r$. A probabilidade $p$ de um ponto cair dentro do círculo é a razão entre as áreas:

$$p = \frac{\pi r^2}{(2r)^2} = \frac{\pi}{4}$$

Se $k$ de $n$ pontos estiverem dentro do círculo, a proporção observada e o estimador de π são:

$$\hat{p} = \frac{k}{n}$$

$$\hat{\pi} = 4\hat{p}$$

Como cada ponto representa um ensaio de Bernoulli, a variância do estimador pode ser aproximada substituindo a probabilidade desconhecida por $\hat{p}$:

$$\widehat{\mathrm{Var}}(\hat{\pi}) = \frac{16\hat{p}(1-\hat{p})}{n}$$

A raiz quadrada dessa variância estimada é o erro padrão. Com uma aproximação normal, o intervalo de confiança de 95% é calculado por $\hat{\pi} \pm 1{,}96\,\mathrm{SE}(\hat{\pi})$. A variância não deve ser interpretada como o erro absoluto da estimativa.

## Organização conceitual

Para facilitar a tradução entre Python, Java e C#, o código adota contratos explícitos, dados de resultado imutáveis e responsabilidades bem delimitadas. A solução está organizada em três camadas lógicas: modelagem de dados, contratos e implementação.

### Modelagem de dados

O resultado da simulação é representado por um objeto que, depois de construído, não deve sofrer alterações.

- Em Python, `@dataclass(frozen=True)` impede a reatribuição normal dos campos. Como o resultado contém apenas valores escalares imutáveis, ele funciona como um registro de resultado imutável. O parâmetro `frozen=True`, isoladamente, não constitui uma garantia geral de segurança entre threads para qualquer classe.
- Em Java, campos `final` preservam os valores atribuídos durante a construção do resultado.
- Em C#, um `readonly struct` com propriedades somente de leitura representa o mesmo propósito.

### Definição de contratos

Java e C# oferecem interfaces para definir o comportamento esperado do simulador. Em Python, o módulo `abc` (*Abstract Base Classes*) permite declarar um contrato equivalente por meio de uma classe abstrata e de métodos abstratos.

Esse contrato separa o que uma simulação deve oferecer de como o algoritmo é implementado. Assim, novas estratégias podem respeitar a mesma entrada e produzir o mesmo tipo de resultado.

### Motor de implementação

Uma classe concreta implementa o contrato do simulador, valida o tamanho da amostra, gera os pontos, contabiliza os acertos e calcula as medidas estatísticas. No Python, *type hints* tornam parâmetros e retornos explícitos; em Java e C#, essa informação faz parte do sistema de tipos das próprias linguagens.

A estrutura é deliberadamente semelhante entre as três versões, mas respeita as convenções básicas de cada ecossistema. A correspondência é conceitual, e não uma tradução sintática exata linha por linha.

## Reprodutibilidade e limites

Uma semente opcional permite reproduzir resultados quando também são mantidos a linguagem, a versão do runtime e o tamanho da amostra, o que facilita testes e experimentos. Contudo, os geradores pseudoaleatórios de Python, Java e C# não são equivalentes. A mesma semente não garante a mesma sequência de pontos entre as linguagens.

O projeto tem finalidade didática. O tempo medido é útil para observar uma execução local, mas não deve ser tratado, sem uma metodologia controlada, como um benchmark definitivo entre runtimes. Instruções de execução, testes e interpretação dos resultados estão no [README.md](README.md).
