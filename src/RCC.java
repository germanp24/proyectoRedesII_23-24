import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;


public class RCC {
    private static final Logger CLIENTE_LOGGER = Logger.getLogger("clientLogger");
    private static final Scanner TECLADO = new Scanner(System.in);

    private static String clientMode;
    private static String serverIP;
    private static int serverPort;
    private static String clientsFolder;

    public static void main(String[] args) throws IOException {
        checkClientArgs(args);
        startLogger();
        checkClientsFolder();

        startClient(args);

    }

    private static void checkClientArgs(String[] argumentos) {
        if (argumentos.length != 4) {
            System.out.println("ERROR: Número de parámetros incorrecto");
            System.out.println("Usa: java RCC <modo> <host> <puerto> <carpeta_cliente>");
            System.exit(1);

        }
        try {
            clientMode = argumentos[0];
            serverIP = argumentos[1];
            serverPort = Integer.parseInt(argumentos[2]);
            clientsFolder = argumentos[3];


            System.out.println("Argumentos introducidos: " + Arrays.toString(argumentos));

        } catch (Exception e) {
            System.out.println("Error en la asignación de argumentos");
            System.out.println("Revisa el tipo de los argumentos y vuelve a intentarlo de nuevo.");
            System.exit(1);
        }
    }

    /**
     * Inicia el proceso cliente.
     *
     * @param argumentos
     * @throws IOException
     */
    private static void startClient(String[] argumentos) throws IOException {
        System.out.println("Iniciando cliente...");
        System.out.println("Dirección IP del servidor: " + serverIP);
        System.out.println("Puerto del servidor: " + serverPort);
        Socket clientsocket = null;

        try {
            //Creamos una conexión al servidor
            clientsocket = new Socket(serverIP, serverPort);
            System.out.print("Conexión con servidor " + argumentos[1] + " establecida.\n");
            CLIENTE_LOGGER.info("Conexión con el servidor exitosa");

            // Aqui se ejecutaran cosas

            clientsocket.close();
            System.out.println("Conexión al servidor cerrada");
            CLIENTE_LOGGER.info("Connection with the server closed.");

        } catch (IOException e) {
            System.out.println("Error al conectar con el servidor:");
            CLIENTE_LOGGER.info("Error in the creations of the client's socket.");
            System.exit(1);
        }
    }


    /**
     * Inicia el logger del cliente.
     */
    private static void startLogger() {
        try {
            checkLogsFolder();

            FileHandler fileHandler_RCC = new FileHandler("logs/RCC.log", 0, 1);
            CLIENTE_LOGGER.addHandler(fileHandler_RCC);
            SimpleFormatter formatter_errors = new SimpleFormatter();
            fileHandler_RCC.setFormatter(formatter_errors);
            CLIENTE_LOGGER.setUseParentHandlers(false);

            CLIENTE_LOGGER.info("Logger del cliente creado e inicializado");

        } catch (Exception e) {
            System.out.println("Error en la creacion de CLIENT_LOGGER.");
            System.exit(1);
        }
    }

    /**
     * Comprueba que la carpeta existe, sino la crea.
     */
    private static void checkLogsFolder() {  // check if logs folder exists, if not, create it
        File logsFolder = new File("logs");

        if (!logsFolder.exists()) {
            logsFolder.mkdir();
        }
    }

    private static void checkClientsFolder() {
        File logsFolder = new File(clientsFolder);

        if (!logsFolder.exists()) {
            logsFolder.mkdir();
        }
    }
}