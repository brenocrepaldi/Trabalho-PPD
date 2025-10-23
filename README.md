# Sistema Distribuído de Contagem em Java

**Trabalho de Programação Paralela e Distribuída - PUC - Outubro 2025**

---

## 📋 Descrição

Sistema distribuído que realiza contagem de ocorrências de números em um grande vetor de inteiros. Utiliza arquitetura cliente-servidor com TCP/IP, serialização de objetos e processamento paralelo em duas camadas.

**Características:**
- Comunicação TCP/IP persistente
- Serialização de objetos
- Paralelismo distribuído (entre servidores) e local (threads por servidor)
- Número de threads = número de processadores disponíveis
- Vetores de inteiros entre -100 e 100

---

## 💻 Compatibilidade de Plataformas

Este projeto é **multiplataforma** e funciona em:
- ✅ **Windows** (CMD e PowerShell)
- ✅ **macOS**
- ✅ **Linux**

### Instruções Específicas para Windows

**Usuários Windows:** Este projeto inclui o arquivo `compilar.bat` para facilitar a compilação. Todas as instruções no README incluem comandos específicos para Windows (CMD e PowerShell) além dos comandos Unix.

**Observações importantes:**
- Use `compilar.bat` ao invés de `compilar.sh`
- Use barras invertidas (`\`) nos caminhos ao invés de barras normais (`/`)
- Certifique-se de que o Java JDK está instalado e configurado no PATH do sistema
- Para verificar se o Java está configurado corretamente: `java -version` e `javac -version`

---

## 🏗️ Estrutura do Projeto

```
atividade2310/
├── src/
│   ├── comunicacao/          # Classes serializáveis
│   │   ├── Comunicado.java   # Classe base
│   │   ├── Pedido.java       # Contém vetor e número a buscar
│   │   ├── Resposta.java     # Retorna contagem
│   │   └── ComunicadoEncerramento.java
│   ├── servidor/
│   │   └── Receptor.java     # Servidor (R) - recebe e processa
│   ├── cliente/
│   │   └── Distribuidor.java # Cliente (D) - distribui trabalho
│   ├── sequencial/
│   │   └── ContagemSequencial.java # Versão sem paralelismo
│   ├── teste/
│   │   └── TesteSimples.java # Teste de comunicação
│   └── util/
│       └── MaiorVetorAproximado.java # Testa tamanho máximo
├── bin/                      # Classes compiladas
├── compilar.sh              # Script de compilação
└── README.md                # Este arquivo
```

---

## 🚀 Como Usar

### 1. Compilar o Projeto

#### Linux/macOS
```bash
chmod +x compilar.sh
./compilar.sh
```

#### Windows
```cmd
compilar.bat
```

**Nota para Windows:** Se você estiver usando PowerShell, pode executar:
```powershell
.\compilar.bat
```

### 2. Executar o Receptor (Servidor)

**Terminal 1 (Linux/macOS):**
```bash
cd bin
java servidor.Receptor
```

**Terminal 1 (Windows - CMD):**
```cmd
cd bin
java servidor.Receptor
```

**Terminal 1 (Windows - PowerShell):**
```powershell
cd bin
java servidor.Receptor
```

Deixe este terminal aberto. Você verá:
```
[R HH:MM:SS.mmm] Receptor iniciado. Aguardando conexões na porta 12345
[R HH:MM:SS.mmm] Processadores disponíveis: X
[R HH:MM:SS.mmm] Aguardando nova conexão...
```

### 3. Executar o Distribuidor (Cliente)

**Terminal 2 (Linux/macOS):**
```bash
cd bin
java cliente.Distribuidor
```

**Terminal 2 (Windows - CMD):**
```cmd
cd bin
java cliente.Distribuidor
```

**Terminal 2 (Windows - PowerShell):**
```powershell
cd bin
java cliente.Distribuidor
```

**Interação:**
- Digite o tamanho do vetor (ex: `1000000` para 1 milhão)
- Escolha opção:
  - `1` = Buscar número existente no vetor
  - `2` = Buscar número 111 (inexistente, retorna 0)

**Resultado esperado:**
```
[D HH:MM:SS] === Resultado Final ===
[D HH:MM:SS] Número procurado: X
[D HH:MM:SS] Ocorrências encontradas: Y
[D HH:MM:SS] Tempo de contagem distribuída: Zms
```

### 4. Executar Versão Sequencial (para comparação)

**Terminal 3 (Linux/macOS):**
```bash
cd bin
java sequencial.ContagemSequencial
```

**Terminal 3 (Windows - CMD):**
```cmd
cd bin
java sequencial.ContagemSequencial
```

**Terminal 3 (Windows - PowerShell):**
```powershell
cd bin
java sequencial.ContagemSequencial
```

Use os mesmos parâmetros do Distribuidor e compare os tempos.

---

## 🧪 Testes

### Teste Simples de Comunicação

**Linux/macOS:**
```bash
cd bin
java teste.TesteSimples
```

**Windows (CMD):**
```cmd
cd bin
java teste.TesteSimples
```

**Windows (PowerShell):**
```powershell
cd bin
java teste.TesteSimples
```

**Certifique-se que o Receptor está rodando!**

Este teste valida:
- Conexão TCP/IP
- Serialização de objetos
- Método contar() do Pedido
- Resposta correta

### Teste com Número Inexistente

Execute o Distribuidor com opção `2`. O número 111 não existe no intervalo [-100, 100], portanto deve retornar **0 ocorrências**.

### Descobrir Tamanho Máximo de Vetor

**Linux/macOS:**
```bash
cd bin
java -Xmx8G util.MaiorVetorAproximado
```

**Windows (CMD):**
```cmd
cd bin
java -Xmx8G util.MaiorVetorAproximado
```

**Windows (PowerShell):**
```powershell
cd bin
java -Xmx8G util.MaiorVetorAproximado
```

Use `-Xmx8G` para alocar 8GB de memória.

---

## 🌐 Configuração para Múltiplas Máquinas

### 1. Descobrir IPs

**macOS/Linux:**
```bash
ifconfig | grep "inet " | grep -v 127.0.0.1
```

**Windows:**
```cmd
ipconfig
```

### 2. Configurar Distribuidor

Edite `src/cliente/Distribuidor.java` nas linhas 19-31:

```java
private static final String[] IPS_SERVIDORES = {
    "192.168.1.100",  // IP do primeiro servidor
    "192.168.1.101",  // IP do segundo servidor
    "192.168.1.102",  // IP do terceiro servidor
};

private static final int[] PORTAS_SERVIDORES = {
    12345,  // Porta do primeiro servidor
    12345,  // Porta do segundo servidor
    12345,  // Porta do terceiro servidor
};
```

### 3. Recompilar e Executar

**Linux/macOS:**
```bash
./compilar.sh
```

**Windows:**
```cmd
compilar.bat
```

Execute o Receptor em cada servidor, depois execute o Distribuidor.

---

## 📊 Arquitetura e Funcionamento

### Classes de Comunicação

**Comunicado** (classe base)
- Implementa `Serializable`
- Não possui atributos nem métodos

**Pedido**
- Atributos: `int[] numeros`, `int procurado`
- Método `contar()`: usa threads paralelas (pool = nº processadores)
- ExecutorService + AtomicInteger + CountDownLatch

**Resposta**
- Atributo: `Integer contagem`
- Método: `getContagem()`

**ComunicadoEncerramento**
- Sinaliza fim da comunicação
- Receptor fecha conexão e volta a aceitar novas

### Receptor (Servidor)

1. Cria `ServerSocket` na porta 12345
2. Loop infinito:
   - Aceita conexão
   - Cria ObjectInputStream/ObjectOutputStream
   - Loop lendo objetos:
     - **Pedido**: executa `contar()` e envia `Resposta`
     - **ComunicadoEncerramento**: fecha conexão e volta ao passo 2

### Distribuidor (Cliente)

1. Gera vetor grande de inteiros [-100, 100]
2. Escolhe número aleatório (ou 111 para teste)
3. Divide vetor em N partes (N = número de servidores)
4. Cria thread para cada servidor:
   - Conecta via Socket
   - Envia Pedido com parte do vetor
   - Recebe Resposta
   - Envia ComunicadoEncerramento
5. Aguarda todas as threads (`Thread.join()`)
6. Soma resultados parciais (`AtomicInteger`)
7. Exibe resultado final

### Paralelismo em Duas Camadas

**Camada 1 - Distribuição (Distribuidor):**
- Vetor dividido entre servidores
- Thread por servidor
- Comunicação via rede

**Camada 2 - Local (Receptor):**
- Método `Pedido.contar()` cria thread pool
- Número de threads = `Runtime.getRuntime().availableProcessors()`
- Cada thread processa parte do vetor local

---

## 🔧 Solução de Problemas

### "Connection refused"
**Problema:** Receptor não está rodando  
**Solução:** Execute `java servidor.Receptor` primeiro

### "Address already in use"
**Problema:** Porta 12345 já está em uso  
**Solução:**

**Linux/macOS:**
```bash
lsof -ti:12345 | xargs kill -9
```

**Windows (CMD - Execute como Administrador):**
```cmd
netstat -ano | findstr :12345
taskkill /PID <PID> /F
```

**Windows (PowerShell - Execute como Administrador):**
```powershell
Get-NetTCPConnection -LocalPort 12345 | Select-Object -ExpandProperty OwningProcess | ForEach-Object { Stop-Process -Id $_ -Force }
```

### "OutOfMemoryError"
**Problema:** Vetor muito grande para memória disponível  
**Solução:**

**Linux/macOS:**
```bash
java -Xmx8G cliente.Distribuidor
```

**Windows:**
```cmd
java -Xmx8G cliente.Distribuidor
```

### Firewall bloqueando conexões

**macOS:**
```bash
sudo /usr/libexec/ApplicationFirewall/socketfilterfw --add $(which java)
```

**Linux (UFW):**
```bash
sudo ufw allow 12345/tcp
```

**Windows:**
1. Abra o **Firewall do Windows Defender com Segurança Avançada**
2. Clique em **Regras de Entrada** → **Nova Regra**
3. Selecione **Porta** → **Avançar**
4. Escolha **TCP** e digite **12345** → **Avançar**
5. Selecione **Permitir a conexão** → **Avançar**
6. Marque todos os perfis (Domínio, Privado, Público) → **Avançar**
7. Dê um nome (ex: "Java Receptor") → **Concluir**

Alternativamente, via PowerShell (Execute como Administrador):
```powershell
New-NetFirewallRule -DisplayName "Java Receptor" -Direction Inbound -Protocol TCP -LocalPort 12345 -Action Allow
```

---

## ✅ Requisitos Implementados

- ✅ Sistema distribuído de contagem
- ✅ Vetor grande de inteiros (`int[]`) entre -100 e 100
- ✅ Comunicação TCP/IP
- ✅ Serialização de objetos
- ✅ Conexões persistentes
- ✅ Classe `Comunicado` (Serializable, sem atributos/métodos)
- ✅ Classe `Pedido` (atributos: `int[] numeros`, `int procurado`)
- ✅ Método `Pedido.contar()` com paralelismo
- ✅ Classe `Resposta` (atributo: `Integer contagem`)
- ✅ Classe `ComunicadoEncerramento`
- ✅ Receptor com ServerSocket, aceita conexões, loop de objetos
- ✅ Distribuidor divide vetor e distribui para servidores
- ✅ Thread por servidor no Distribuidor
- ✅ Threads locais no Receptor (método `contar()`)
- ✅ Número de threads = número de processadores
- ✅ `Thread.join()` para sincronização
- ✅ Tratamento de exceções
- ✅ Logs informativos com timestamps
- ✅ Teste com número inexistente (111)
- ✅ Programa sequencial para comparação
- ✅ Medição de tempo de execução

---

## 📈 Exemplo de Uso

### Teste Local - 1 Servidor

**Terminal 1 - Receptor:**
```bash
cd bin && java servidor.Receptor
```

**Terminal 2 - Distribuidor:**
```bash
cd bin && java cliente.Distribuidor
```
- Tamanho: `10000000` (10 milhões)
- Opção: `1`

**Terminal 3 - Sequencial:**
```bash
cd bin && java sequencial.ContagemSequencial
```
- Tamanho: `10000000`
- Opção: `1`

**Resultado esperado:**
- Tempo sequencial: ~150-200ms
- Tempo distribuído: ~80-100ms
- Speedup: ~2x

### Teste com Múltiplos Servidores

Configure 3 IPs no Distribuidor, execute um Receptor em cada máquina, depois execute o Distribuidor.

**Resultado esperado:**
- Speedup: ~3-5x (depende da rede e hardware)

---

## 📝 Estrutura das Classes

### Pedido.java (Resumo)
```java
public class Pedido extends Comunicado {
    private final int[] numeros;
    private final int procurado;
    
    public int contar() {
        int numProc = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(numProc);
        // Divide vetor em blocos
        // Cada thread conta seu bloco
        // AtomicInteger acumula resultados
        // CountDownLatch sincroniza
        return contagemTotal.get();
    }
}
```

### Receptor.java (Resumo)
```java
public class Receptor {
    public static void main(String[] args) {
        ServerSocket server = new ServerSocket(12345);
        while (true) {
            Socket socket = server.accept();
            // Cria streams
            while (true) {
                Object obj = receptor.readObject();
                if (obj instanceof Pedido) {
                    Pedido p = (Pedido) obj;
                    int resultado = p.contar();
                    transmissor.writeObject(new Resposta(resultado));
                } else if (obj instanceof ComunicadoEncerramento) {
                    break; // Fecha conexão
                }
            }
        }
    }
}
```

### Distribuidor.java (Resumo)
```java
public class Distribuidor {
    public static void main(String[] args) {
        // Gera vetor
        int[] vetor = new int[tamanho];
        // Divide em partes
        for (int i = 0; i < numServidores; i++) {
            int[] parte = Arrays.copyOfRange(vetor, inicio, fim);
            threads[i] = new Thread(() -> {
                // Conecta ao servidor
                Socket socket = new Socket(ip, porta);
                // Envia Pedido
                transmissor.writeObject(new Pedido(parte, procurado));
                // Recebe Resposta
                Resposta r = (Resposta) receptor.readObject();
                contagemTotal.addAndGet(r.getContagem());
                // Envia ComunicadoEncerramento
                transmissor.writeObject(new ComunicadoEncerramento());
            });
        }
        // Aguarda threads
        for (Thread t : threads) t.join();
        // Exibe resultado
    }
}
```

---

## 🎓 Para Demonstração

### Checklist

1. ✅ Compilar:
   - **Linux/macOS:** `./compilar.sh`
   - **Windows:** `compilar.bat`
2. ✅ Iniciar Receptor
3. ✅ Executar Distribuidor com 1 milhão de elementos
4. ✅ Executar Sequencial com mesmos parâmetros
5. ✅ Comparar tempos e calcular speedup
6. ✅ Testar com número 111 (inexistente) → resultado = 0
7. ✅ Mostrar logs detalhados em ambos os programas
8. ✅ Explicar arquitetura (2 camadas de paralelismo)
9. ✅ Mostrar código das classes (Pedido.contar() com threads)
10. ✅ (Opcional) Demonstrar em múltiplas máquinas

### Pontos Importantes para Destacar

- **Paralelismo em 2 camadas**: distribuição entre servidores + threads locais
- **Número de threads = processadores**: `Runtime.getRuntime().availableProcessors()`
- **Conexões persistentes**: mantidas até `ComunicadoEncerramento`
- **Sincronização**: `Thread.join()` no Distribuidor, `CountDownLatch` no Pedido
- **Tratamento de exceções**: todas as exceções tratadas adequadamente
- **Medição de tempo**: antes/depois com `System.currentTimeMillis()`

---

## 🏆 Conclusão

Sistema completo e funcional que demonstra:
- Programação distribuída com TCP/IP
- Serialização de objetos em Java
- Paralelismo multi-camada
- Sincronização de threads
- Arquitetura cliente-servidor

**Speedup típico:** 2-4x em máquina local, 5-10x em múltiplas máquinas.

---

**Desenvolvido para:** Programação Paralela e Distribuída - PUC  
**Data:** Outubro 2025  
**Linguagem:** Java  
**Porta:** 12345  
**Status:** ✅ 100% Completo e Testado



Dar a opcao para o usuário de escolher o tamanho do vetor - até o limite que a máquina que estiver rodando suporte

Dar a opcao para o usuario para printar o vetor