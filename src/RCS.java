import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.Scanner;
import java.util.logging.Logger;

public class RCS {
    private static final Logger SERVER_LOGGER = Logger.getLogger("serverLogger");  // Creates the log files
    private static final Scanner TECLADO= new Scanner(System.in);   // Read from keyboard

    private static String serverMode;
    private static int serverPort;
    private static int serverMaxClients;

    public static void main(String[] args) throws IOException {
        checkServerArgs(args);
        startServer(args);


    }

    /**
     * Comprueba que el numero de argumentos introducido es correcto.
     *
     * @param argumentos
     */
    private static void checkServerArgs(String[] argumentos) {    // Comprueba que los argumentos sean correcto
        if (argumentos.length != 3) {
            System.out.println("ERROR: Numero Incorrecto de Parametros.");
            System.out.println("Usa: java RCS <modo> <puerto> <max_clientes>");
            System.exit(1);
        }

        try{
            serverMode = argumentos[0];
            serverPort = Integer.parseInt(argumentos[1]);
            serverMaxClients = Integer.parseInt(argumentos[2]);

            System.out.println("Argumentos introducidos: " + Arrays.toString(argumentos));

        } catch (Exception e) {
            System.out.println("Error en la asignacion de argumentos");
            System.out.println("Revisa el tipo de los argumentos y vuelve a intentarlo de nuevo.");
            System.exit(1);
        }
    }

    /**
     * Inicia el modo servidor.
     *
     * @param argumentos
     */
    private static void startServer(String[] argumentos) throws IOException {
        System.out.println("Iniciando Servidor...");

        // Obtengo nombre de host e IP privada, separo, y me quedo solo con la IP.
        String hostnameAndIp = String.valueOf(InetAddress.getLocalHost());
        String[] ipParts = hostnameAndIp.split("/");
        String privateIpServer = ipParts[1];

        System.out.println("IP Privada: " + privateIpServer);
        System.out.println("Puerto: " + 1024);

        try{
            ServerSocket serverSocket = new ServerSocket(1024);
            System.out.println("Socket creado correctamente");
        } catch (Exception e){
            System.out.println("Error en la creación del socket");

        }

    }
}