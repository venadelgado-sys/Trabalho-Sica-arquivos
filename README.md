# 📁 SiCA - Sistema de Compartilhamento de Arquivos via Sockets TCP (Java)

Este projeto consiste no desenvolvimento de uma aplicação de rede baseada na arquitetura **Cliente/Servidor** utilizando a API de **Sockets TCP** nativa do Java. O objetivo principal é simular o funcionamento de um **SiCA (Sistema de Compartilhamento de Arquivos)**, comumente utilizado em ambientes corporativos para transferência e gerenciamento de arquivos entre máquinas de uma organização.

## 🚀 Funcionalidades

A aplicação permite que o **Cliente**, após estabelecer uma conexão TCP confiável com o **Servidor**, realize três operações principais:
1. **Listar arquivos:** Solicita e exibe a lista de todos os arquivos atualmente disponíveis no diretório do servidor.
2. **Enviar um arquivo (Upload):** Transmite um arquivo local do cliente para o servidor através de fluxos de bytes.
3. **Baixar um arquivo (Download):** Solicita um arquivo específico do servidor e o salva localmente na máquina do cliente.

O **Servidor**, por sua vez, atua em modo de escuta (*listen*), aguardando conexões, processando as requisições recebidas e gerenciando o armazenamento local dos arquivos.

## 🛠️ Estrutura do Projeto e Tecnologias

* **Linguagem de Programação:** Java
* **Protocolo de Transporte:** **TCP (Transmission Control Protocol)** — garantindo uma entrega de dados confiável, livre de erros e na ordem correta, ideal para transferência de arquivos.
* **Classes Principais do Java Utilizadas:**
  * `ServerSocket`: Utilizada no servidor para escutar requisições em uma porta específica.
  * `Socket`: Utilizada para estabelecer a comunicação e canal de dados entre o cliente e o servidor.
  * `InputStream` / `OutputStream` (e derivados como `DataInputStream`/`DataOutputStream` ou `BufferedInputStream`): Para a leitura e escrita limpa de fluxos de bytes e envio de comandos de controle.

## 📋 Como Executar a Aplicação

Para compilar e testar o sistema localmente através do terminal, siga os passos abaixo:

1. **Compilar os arquivos Java:**
   Abra o terminal na pasta do projeto e compile ambas as classes:
   ```bash
   javac Servidor.java Cliente.java
   ```

2. **Iniciar o Servidor:**
   Execute o servidor primeiro para que ele abra a porta TCP e fique aguardando conexões:
   ```bash
   java Servidor
   ```

3. **Iniciar o Cliente:**
   Abra um segundo terminal na mesma máquina (ou em outra na mesma rede) e execute o cliente para se conectar ao servidor (geralmente apontando para `localhost` ou o IP do servidor):
   ```bash
   java Cliente
   ```

4. **Interação:**
   Utilize o menu interativo gerado no terminal do cliente para escolher entre as opções de **Listar, Enviar (Upload) ou Baixar (Download)** arquivos.

## 📝 Documentação do Código
O código-fonte encontra-se **altamente documentado através de comentários detalhados** junto aos principais métodos implementados, explicando:
* A inicialização do `ServerSocket` e o bloco de aceitação de conexões (`accept()`).
* O fluxo de buffers para leitura de arquivos em blocos de bytes, garantindo eficiência mesmo com arquivos maiores.
* O protocolo de aplicação desenhado para diferenciar quando o cliente está enviando um comando textual (como listar) ou transmitindo dados brutos (bytes de um arquivo).

## ✒️ Autores
* **Seu Nome Completo** - *Desenvolvedor do Projeto* - [Seu Perfil no GitHub](https://github.com)
