import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Scanner;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.net.Socket;


public class RCS {
    private static final Logger SERVER_LOGGER = Logger.getLogger("serverLogger");  // Creates the log files
    private static final Scanner TECLADO = new Scanner(System.in);   // Read from keyboard

    private static String serverMode;
    private static int serverPort;
    private static int serverMaxClients;
    private static int serverCurrentClients;

    public static void main(String[] args) throws IOException {
        checkServerArgs(args);
        startLogger();

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

        try {
            serverMode = argumentos[0];
            serverPort = Integer.parseInt(argumentos[1]);
            serverMaxClients = Integer.parseInt(argumentos[2]);

            System.out.println("Argumentos introducidos: " + Arrays.toString(argumentos));

        } catch (Exception e) {
            System.out.println("Error en la asignacion de argumentos");
            System.out.println("Revisa el tipo de los argumentos y vuelve a intentarlo de nuevo.");
            System.out.println("Saliendo...");
            SERVER_LOGGER.info("Error in the arguments assignation. Exiting...");
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
        ServerSocket serverSocket = null;

        try {
            serverSocket = new ServerSocket(serverPort);
            System.out.println("Socket servidor creado correctamente");
            SERVER_LOGGER.info("Server socket correctly created");

            obtainServerIpPort();

        } catch (Exception e) {
            System.out.println("Error en la creación del socket servidor. Saliendo...");
            SERVER_LOGGER.info("Error in the creation of the server socket. Exiting...");
            System.exit(1);
        }

        // Loop to accept clients while the number of clients is under the maximum specified.
        while (serverCurrentClients <= serverMaxClients) {
            Socket clientSocket = serverSocket.accept(); // Blocks the petition until a client connects.
            serverCurrentClients++; // Increases the variable by one every time a client is connected.

            System.out.println("Cliente conectado desde " + clientSocket.getInetAddress().getHostAddress());
            SERVER_LOGGER.info("Client connected from " + clientSocket.getInetAddress().getHostAddress());

            // Crear y ejecutar un nuevo hilo para manejar al cliente
            ServerThread serverThread = new ServerThread(clientSocket);
            serverThread.start();
        }
    }

    /**
     * Starts and configure the logger for the server.
     */
    private static void startLogger() {
        try {
            checkLogsFolder();

            FileHandler fileHandler_RCS = new FileHandler("logs/RCS.log", 0, 1);
            SERVER_LOGGER.addHandler(fileHandler_RCS);
            SimpleFormatter formatter_errors = new SimpleFormatter();
            fileHandler_RCS.setFormatter(formatter_errors);
            SERVER_LOGGER.setUseParentHandlers(false); // Evita que el logger escriba en consola

            SERVER_LOGGER.info("Logger of the server created and initialized.");

        } catch (Exception e) {
            System.out.println("Error en la creación de SERVER_LOGGER.");
            System.exit(1);
        }
    }

    /**
     * Checks that the "logs" folder exists, if not, then creates it.
     */
    private static void checkLogsFolder() {  // check if logs folder exists, if not, create it
        File logsFolder = new File("logs");

        if (!logsFolder.exists()) {
            logsFolder.mkdir();
        }
    }

    public static void obtainServerIpPort() throws UnknownHostException {
        try {
            // Obtengo nombre de host e IP privada, separo, y me quedo solo con la IP.
            String hostnameAndIp = String.valueOf(InetAddress.getLocalHost());
            String[] ipParts = hostnameAndIp.split("/");
            String privateIpServer = ipParts[1];

            System.out.println("IP Privada Server: " + privateIpServer);
            System.out.println("Puerto Server: " + serverPort);

        } catch (Exception e) {
            System.out.println("Error al obtener la IP privada de este equipo. Saliendo...");
            SERVER_LOGGER.info("Error obtaining the private IP of the server. Exiting...");
        }
    }
}