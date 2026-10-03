import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.Scanner;

public class ClienteSica {

    // Endereço do servidor.
    // 127.0.0.1 significa que o servidor está neste mesmo computador.
    private static final String HOST = "127.0.0.1";

    // Deve ser a mesma porta utilizada pelo servidor.
    private static final int PORTA = 5000;

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        System.out.println("=================================");
        System.out.println("        CLIENTE SiCA");
        System.out.println("=================================");

        try (
            Socket socket = new Socket(HOST, PORTA);

            DataInputStream entrada =
                    new DataInputStream(
                            new BufferedInputStream(
                                    socket.getInputStream()));

            DataOutputStream saida =
                    new DataOutputStream(
                            new BufferedOutputStream(
                                    socket.getOutputStream()))
        ) {

            System.out.println("Conectado ao servidor!");
            System.out.println();

            boolean executando = true;

            while (executando) {

                System.out.println();
                System.out.println("========== MENU SiCA ==========");
                System.out.println("1 - Listar arquivos do servidor");
                System.out.println("2 - Enviar arquivo");
                System.out.println("3 - Baixar arquivo");
                System.out.println("0 - Sair");
                System.out.println("===============================");
                System.out.print("Escolha: ");

                String opcao = scanner.nextLine();

                switch (opcao) {

                    case "1":
                        listarArquivos(saida, entrada);
                        break;

                    case "2":
                        enviarArquivo(scanner, saida, entrada);
                        break;

                    case "3":
                        baixarArquivo(scanner, saida, entrada);
                        break;

                    case "0":
                        saida.writeUTF("SAIR");
                        saida.flush();

                        executando = false;
                        System.out.println("Cliente encerrado.");
                        break;

                    default:
                        System.out.println("Opção inválida.");
                }
            }

        } catch (ConnectException e) {

            System.out.println(
                    "Não foi possível conectar ao servidor."
            );

            System.out.println(
                    "Verifique se o ServidorSica está executando."
            );

        } catch (IOException e) {

            System.out.println(
                    "Erro de comunicação: "
                    + e.getMessage()
            );
        }

        scanner.close();
    }

    /*
     * Solicita ao servidor a lista dos arquivos disponíveis.
     */
    private static void listarArquivos(
            DataOutputStream saida,
            DataInputStream entrada) throws IOException {

        saida.writeUTF("LISTAR");
        saida.flush();

        int quantidade = entrada.readInt();

        System.out.println();
        System.out.println("====== ARQUIVOS NO SERVIDOR ======");

        if (quantidade == 0) {
            System.out.println("Nenhum arquivo disponível.");
        }

        for (int i = 0; i < quantidade; i++) {
            System.out.println(
                    (i + 1) + " - " + entrada.readUTF()
            );
        }

        System.out.println("==================================");
    }

    /*
     * Envia um arquivo escolhido pelo usuário para o servidor.
     */
    private static void enviarArquivo(
            Scanner scanner,
            DataOutputStream saida,
            DataInputStream entrada) throws IOException {

        System.out.print(
                "Digite o caminho do arquivo que deseja enviar: "
        );

        String caminho = scanner.nextLine();

        Path arquivo = Paths.get(caminho);

        if (!Files.exists(arquivo)
                || !Files.isRegularFile(arquivo)) {

            System.out.println("Arquivo não encontrado.");
            return;
        }

        String nomeArquivo =
                arquivo.getFileName().toString();

        long tamanho = Files.size(arquivo);

        saida.writeUTF("ENVIAR");
        saida.writeUTF(nomeArquivo);
        saida.writeLong(tamanho);

        try (InputStream arquivoEntrada =
                     new BufferedInputStream(
                             Files.newInputStream(arquivo))) {

            byte[] buffer = new byte[8192];
            int quantidade;

            while ((quantidade =
                    arquivoEntrada.read(buffer)) != -1) {

                saida.write(buffer, 0, quantidade);
            }
        }

        saida.flush();

        String resposta = entrada.readUTF();

        if (resposta.equals("OK")) {

            System.out.println(
                    entrada.readUTF()
            );

        } else {

            System.out.println(
                    "Erro: " + entrada.readUTF()
            );
        }
    }

    /*
     * Solicita um arquivo ao servidor e salva uma cópia
     * no computador do cliente.
     */
    private static void baixarArquivo(
            Scanner scanner,
            DataOutputStream saida,
            DataInputStream entrada) throws IOException {

        System.out.print(
                "Digite o nome do arquivo para baixar: "
        );

        String nomeArquivo = scanner.nextLine();

        saida.writeUTF("BAIXAR");
        saida.writeUTF(nomeArquivo);
        saida.flush();

        String resposta = entrada.readUTF();

        if (resposta.equals("ERRO")) {

            System.out.println(
                    "Erro: " + entrada.readUTF()
            );

            return;
        }

        long tamanho = entrada.readLong();

        // O arquivo baixado será salvo na pasta atual.
        Path destino = Paths.get(
                "download_" + Paths.get(nomeArquivo)
                        .getFileName()
        );

        try (OutputStream arquivoSaida =
                     new BufferedOutputStream(
                             Files.newOutputStream(destino))) {

            byte[] buffer = new byte[8192];
            long restante = tamanho;

            while (restante > 0) {

                int quantidade = entrada.read(
                        buffer,
                        0,
                        (int) Math.min(buffer.length, restante)
                );

                if (quantidade == -1) {
                    throw new EOFException(
                            "Conexão encerrada durante o download."
                    );
                }

                arquivoSaida.write(buffer, 0, quantidade);
                restante -= quantidade;
            }
        }

        System.out.println(
                "Arquivo baixado com sucesso!"
        );

        System.out.println(
                "Salvo como: " + destino.toAbsolutePath()
        );
    }
}