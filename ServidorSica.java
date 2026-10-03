import java.io.*;
import java.net.*;
import java.nio.file.*;

public class ServidorSica {

    // Porta utilizada pelo servidor para receber conexões TCP.
    private static final int PORTA = 5000;

    // Pasta onde os arquivos compartilhados ficarão armazenados.
    private static final Path PASTA_SERVIDOR =
            Paths.get("arquivos_servidor").toAbsolutePath().normalize();

    public static void main(String[] args) {

        // Cria a pasta de arquivos caso ela ainda não exista.
        try {
            Files.createDirectories(PASTA_SERVIDOR);
        } catch (IOException e) {
            System.out.println("Erro ao criar a pasta do servidor.");
            e.printStackTrace();
            return;
        }

        System.out.println("=================================");
        System.out.println("       SERVIDOR SiCA");
        System.out.println("=================================");
        System.out.println("Pasta: " + PASTA_SERVIDOR);
        System.out.println("Porta: " + PORTA);
        System.out.println("Aguardando clientes...");

        // ServerSocket fica aguardando conexões TCP.
        try (ServerSocket servidor = new ServerSocket(PORTA)) {

            while (true) {

                // Aceita uma nova conexão.
                Socket cliente = servidor.accept();

                System.out.println(
                        "Cliente conectado: "
                        + cliente.getInetAddress()
                );

                // Cada cliente é atendido em uma thread.
                new Thread(() -> atenderCliente(cliente)).start();
            }

        } catch (IOException e) {
            System.out.println("Erro no servidor: " + e.getMessage());
        }
    }

    /*
     * Este método recebe e processa as requisições enviadas
     * pelo cliente.
     */
    private static void atenderCliente(Socket socket) {

        try (
            Socket cliente = socket;
            DataInputStream entrada =
                    new DataInputStream(
                            new BufferedInputStream(
                                    cliente.getInputStream()));

            DataOutputStream saida =
                    new DataOutputStream(
                            new BufferedOutputStream(
                                    cliente.getOutputStream()))
        ) {

            while (true) {

                // Recebe o comando enviado pelo cliente.
                String comando = entrada.readUTF();

                switch (comando) {

                    case "LISTAR":
                        listarArquivos(saida);
                        break;

                    case "ENVIAR":
                        receberArquivo(entrada, saida);
                        break;

                    case "BAIXAR":
                        enviarArquivo(entrada, saida);
                        break;

                    case "SAIR":
                        System.out.println("Cliente desconectado.");
                        return;

                    default:
                        saida.writeUTF("ERRO");
                        saida.writeUTF("Comando desconhecido.");
                        saida.flush();
                }
            }

        } catch (IOException e) {
            System.out.println("Conexão encerrada.");
        }
    }

    /*
     * Envia para o cliente a lista de arquivos disponíveis
     * na pasta do servidor.
     */
    private static void listarArquivos(DataOutputStream saida)
            throws IOException {

        try (DirectoryStream<Path> arquivos =
                     Files.newDirectoryStream(PASTA_SERVIDOR)) {

            int quantidade = 0;

            // Primeiro contamos os arquivos.
            for (Path arquivo : arquivos) {
                if (Files.isRegularFile(arquivo)) {
                    quantidade++;
                }
            }

            saida.writeInt(quantidade);

            // Envia novamente os nomes.
            try (DirectoryStream<Path> arquivos2 =
                         Files.newDirectoryStream(PASTA_SERVIDOR)) {

                for (Path arquivo : arquivos2) {
                    if (Files.isRegularFile(arquivo)) {
                        saida.writeUTF(arquivo.getFileName().toString());
                    }
                }
            }

            saida.flush();
        }
    }

    /*
     * Recebe um arquivo enviado pelo cliente e salva
     * dentro da pasta arquivos_servidor.
     */
    private static void receberArquivo(
            DataInputStream entrada,
            DataOutputStream saida) throws IOException {

        String nomeArquivo = entrada.readUTF();

        // O tamanho é enviado antes dos dados do arquivo.
        long tamanho = entrada.readLong();

        if (tamanho < 0) {
            saida.writeUTF("ERRO");
            saida.writeUTF("Tamanho de arquivo inválido.");
            saida.flush();
            return;
        }

        // Evita que o cliente consiga criar caminhos fora da pasta.
        nomeArquivo = Paths.get(nomeArquivo)
                .getFileName()
                .toString();

        Path destino = PASTA_SERVIDOR.resolve(nomeArquivo)
                .normalize();

        long restante = tamanho;

        try (OutputStream arquivo =
                     new BufferedOutputStream(
                             Files.newOutputStream(destino))) {

            byte[] buffer = new byte[8192];

            while (restante > 0) {

                int quantidade = entrada.read(
                        buffer,
                        0,
                        (int) Math.min(buffer.length, restante)
                );

                if (quantidade == -1) {
                    throw new EOFException(
                            "Conexão encerrada durante o envio.");
                }

                arquivo.write(buffer, 0, quantidade);
                restante -= quantidade;
            }
        }

        saida.writeUTF("OK");
        saida.writeUTF("Arquivo recebido com sucesso.");
        saida.flush();

        System.out.println(
                "Arquivo recebido: " + nomeArquivo
                + " (" + tamanho + " bytes)"
        );
    }

    /*
     * Localiza o arquivo solicitado pelo cliente e envia
     * seu conteúdo pela conexão TCP.
     */
    private static void enviarArquivo(
            DataInputStream entrada,
            DataOutputStream saida) throws IOException {

        String nomeArquivo = entrada.readUTF();

        nomeArquivo = Paths.get(nomeArquivo)
                .getFileName()
                .toString();

        Path arquivo = PASTA_SERVIDOR.resolve(nomeArquivo)
                .normalize();

        if (!Files.exists(arquivo)
                || !Files.isRegularFile(arquivo)) {

            saida.writeUTF("ERRO");
            saida.writeUTF("Arquivo não encontrado.");
            saida.flush();
            return;
        }

        long tamanho = Files.size(arquivo);

        saida.writeUTF("OK");
        saida.writeLong(tamanho);

        try (InputStream entradaArquivo =
                     new BufferedInputStream(
                             Files.newInputStream(arquivo))) {

            byte[] buffer = new byte[8192];
            int quantidade;

            while ((quantidade = entradaArquivo.read(buffer)) != -1) {
                saida.write(buffer, 0, quantidade);
            }
        }

        saida.flush();

        System.out.println(
                "Arquivo enviado para o cliente: "
                + nomeArquivo
        );
    }
}