# Simulador de Monte Carlo para Aproximação de Pi

Projeto educacional que estima o valor de π pelo método de Monte Carlo, com implementações equivalentes em Python, Java e C#. A proposta é exercitar probabilidade, programação orientada a objetos, contratos explícitos e portabilidade de uma mesma solução entre linguagens.

Uma descrição mais conceitual das decisões do projeto está disponível em [visaodeprojet.md](visaodeprojet.md).

## Como funciona

Cada implementação gera pontos independentes e uniformemente distribuídos no quadrado `[-1, 1] × [-1, 1]`. Um ponto pertence ao círculo unitário quando:

$$x^2 + y^2 \leq 1$$

Como a razão entre a área do círculo e a área do quadrado é `π/4`, a proporção observada de pontos dentro do círculo permite estimar π. Se `k` dos `n` pontos estiverem dentro do círculo, então:

$$\hat{p} = \frac{k}{n}$$

$$\hat{\pi} = 4\hat{p} = 4\frac{k}{n}$$

Quanto maior a amostra, menor tende a ser a incerteza estatística. Como a convergência ocorre na ordem de $1/\sqrt{n}$, reduzir o erro padrão pela metade exige, aproximadamente, quadruplicar o número de pontos.

## Recursos

- Implementações do mesmo algoritmo em Python, Java e C#.
- Tamanho da amostra configurável pela linha de comando.
- Semente aleatória opcional para tornar uma execução reproduzível.
- Contagem dos pontos dentro do círculo e estimativa de π.
- Variância estimada, erro padrão e intervalo de confiança de 95%.
- Medição do tempo gasto no núcleo da simulação.
- Validação das entradas fornecidas pelo usuário.
- Testes automatizados sem bibliotecas de terceiros.
- Verificação automática das três implementações com GitHub Actions.

## Estrutura do projeto

```text
.
├── .github/workflows/ci.yml        # Integração contínua no GitHub Actions
├── .gitattributes                  # Normalização de arquivos de texto no Git
├── main.py                         # Implementação e CLI em Python
├── Main.java                      # Implementação e CLI em Java
├── Monte/
│   ├── Monte.csproj               # Projeto da aplicação C#
│   └── Program.cs                 # Implementação e CLI em C#
├── tests/
│   ├── python/
│   │   └── test_main.py           # Testes da implementação Python
│   └── java/
│       └── MonteCarloTests.java   # Testes da implementação Java
├── Monte.Tests/
│   ├── Monte.Tests.csproj         # Projeto executável de testes C#
│   └── Program.cs                 # Testes da implementação C#
├── visaodeprojet.md               # Visão conceitual do projeto
├── .gitignore
└── README.md
```

Os testes Java e C# usam executores simples baseados nas bibliotecas padrão de cada plataforma; não é necessário instalar um framework de testes externo.

## Pré-requisitos

Para executar todas as versões, instale:

- Python 3.10 ou superior;
- JDK 17 ou superior, com `javac` e `java` disponíveis no `PATH`;
- .NET SDK 10.

Confira o ambiente com:

```text
python --version
javac -version
java -version
dotnet --version
```

Em alguns sistemas, o executável do Python se chama `python3`; no Windows, também pode ser usado o launcher `py -3`.

## Execução

Execute os comandos a partir da raiz do projeto. Nos exemplos abaixo, a simulação usa `1.000.000` de pontos e a semente `42`.

### Python

Sintaxe:

```text
python main.py [sample_size] [--seed SEED]
```

PowerShell:

```powershell
python .\main.py 1000000 --seed 42
```

Se necessário, substitua `python` por `py -3`. Em Linux, macOS e outros shells:

```bash
python3 ./main.py 1000000 --seed 42
```

### Java

Sintaxe após a compilação:

```text
java Main [sample-size] [seed]
```

PowerShell:

```powershell
javac -encoding UTF-8 .\Main.java
java Main 1000000 42
```

Em Linux, macOS e outros shells:

```bash
javac -encoding UTF-8 ./Main.java
java Main 1000000 42
```

### C#

Sintaxe dos argumentos da aplicação:

```text
[sample-size] [seed]
```

PowerShell:

```powershell
dotnet run --project .\Monte\Monte.csproj -- 1000000 42
```

Em Linux, macOS e outros shells:

```bash
dotnet run --project ./Monte/Monte.csproj -- 1000000 42
```

O separador `--` informa ao comando `dotnet run` que os valores seguintes devem ser encaminhados à aplicação.

### Argumentos e valores padrão

| Argumento | Descrição | Valor padrão |
| --- | --- | --- |
| `sample_size` / `sample-size` | Quantidade inteira e positiva de pontos gerados. | `10.000.000` |
| `seed` | Inteiro usado para inicializar o gerador pseudoaleatório. | Não definida |

Na versão Python, a semente é informada pela opção `--seed`. Nas versões Java e C#, ela é o segundo argumento posicional; portanto, para informar uma semente nessas versões, também é necessário informar o tamanho da amostra.

Sem uma semente explícita, execuções sucessivas podem produzir estimativas diferentes, como esperado em uma simulação aleatória.

## Executando os testes

Todos os comandos devem ser executados a partir da raiz do projeto.

### Python

PowerShell:

```powershell
python -m unittest discover -s .\tests\python -p "test_*.py" -v
```

Em Linux, macOS e outros shells:

```bash
python3 -m unittest discover -s ./tests/python -p 'test_*.py' -v
```

### Java

PowerShell:

```powershell
javac -encoding UTF-8 .\Main.java .\tests\java\MonteCarloTests.java
java -cp ".;.\tests\java" MonteCarloTests
```

Em Linux, macOS e outros shells:

```bash
javac -encoding UTF-8 ./Main.java ./tests/java/MonteCarloTests.java
java -cp '.:./tests/java' MonteCarloTests
```

### C#

PowerShell:

```powershell
dotnet run --project .\Monte.Tests
```

Em Linux, macOS e outros shells:

```bash
dotnet run --project ./Monte.Tests
```

### Integração contínua

O workflow [`.github/workflows/ci.yml`](.github/workflows/ci.yml) executa os três conjuntos de testes automaticamente em cada `push` e `pull request`. Depois que o projeto for publicado, os resultados aparecerão na aba **Actions** do repositório no GitHub.

## Interpretando os resultados

Além da estimativa de π, a simulação apresenta medidas da incerteza amostral.

### Variância estimada

A variável que indica se um ponto caiu dentro do círculo segue uma distribuição de Bernoulli. Substituindo a probabilidade desconhecida por $\hat{p}$, a variância estimada do estimador de π é:

$$\widehat{\operatorname{Var}}(\hat{\pi}) = \frac{16\hat{p}(1-\hat{p})}{n}$$

Esse valor descreve a dispersão esperada do estimador entre repetições do experimento. Ele não é o erro absoluto entre a estimativa obtida e o valor verdadeiro de π.

### Erro padrão

O erro padrão está na mesma unidade da estimativa de π e corresponde à raiz quadrada da variância estimada:

$$\operatorname{SE}(\hat{\pi}) = \sqrt{\widehat{\operatorname{Var}}(\hat{\pi})}$$

### Intervalo de confiança de 95%

Usando a aproximação normal, o intervalo é calculado por:

$$\hat{\pi} \pm 1{,}96 \times \operatorname{SE}(\hat{\pi})$$

O intervalo é uma aproximação estatística. Em uma sequência de experimentos conduzidos da mesma forma, cerca de 95% dos intervalos assim construídos devem conter o valor verdadeiro. Ele não garante que toda execução individual produza uma boa aproximação.

## Reprodutibilidade

Ao repetir uma execução com a mesma linguagem, versão de runtime, semente e tamanho de amostra, a implementação pode repetir sua sequência pseudoaleatória e, consequentemente, seu resultado. Isso é útil para testes, demonstrações e investigação de comportamentos inesperados.

Python, Java e C# utilizam geradores pseudoaleatórios e algoritmos de inicialização diferentes. Por isso, usar a mesma semente e o mesmo tamanho de amostra nas três versões não implica gerar os mesmos pontos nem obter exatamente a mesma estimativa. A comparação adequada é entre as propriedades estatísticas e a lógica das implementações, não entre cada valor sorteado.

## Observação sobre desempenho

O tempo exibido serve como referência local para uma execução, não como um benchmark rigoroso entre linguagens. A comparação é influenciada por hardware, sistema operacional, versão do runtime, compilação JIT, aquecimento da máquina virtual, modo de compilação e outras cargas em execução.

Para uma avaliação mais justa, use o mesmo computador e o mesmo tamanho de amostra, execute cada versão várias vezes, descarte aquecimentos quando aplicável e compare medidas agregadas. Em C#, prefira uma compilação `Release` ao realizar medições (`dotnet run -c Release --project ./Monte/Monte.csproj -- ...`).


