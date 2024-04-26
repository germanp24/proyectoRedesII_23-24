import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;


public class RCC {
    private static final Logger CLIENT_LOGGER = Logger.getLogger("clientLogger");
    private static final Scanner KEYBOARD = new Scanner(System.in);

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

    /**
     * Checks the arguments introduced by the user.
     *
     * @param arguments
     */
    private static void checkClientArgs(String[] arguments) {
        if (arguments.length != 4) {
            System.out.println("ERROR: Incorrect number of parameters.");
            System.out.println("Use: java RCC <mode> <host> <port> <clients_folder>");
            CLIENT_LOGGER.info("Incorrect number of parameters.");
            System.exit(1);

        }
        try {
            clientMode = arguments[0];
            serverIP = arguments[1];
            serverPort = Integer.parseInt(arguments[2]);
            clientsFolder = arguments[3];


            System.out.println("Introduced arguments: " + Arrays.toString(arguments));

        } catch (Exception e) {
            System.out.println("Error en la asignación de argumentos");
            System.out.println("Error in the arguments assignation.");
            System.out.println("Revise the arguments type and try again.");
            System.out.println("Exiting...");
            CLIENT_LOGGER.info("Error in the arguments assignation. Exiting...");
            System.exit(1);
        }
    }

    /**
     * Starts the client.
     *
     * @param argumentos
     * @throws IOException
     */
    private static void startClient(String[] argumentos) throws IOException {
        switch (clientMode){
            case "normal":
                runNormalClient();
                break;

            case "ssl":
                runSSLClient();
                break;

            default:
                System.out.println("ERROR: There are only these two modes: normal or ssl (lowercase)");
                CLIENT_LOGGER.info("Error related with the mode entered by argument. Exiting...");
                System.exit(1);
        }


        System.out.println("Iniciando cliente...");

    }

    /**
     * Runs a "normal" client
     *
     * @throws IOException
     */
    private static void runNormalClient() throws IOException {
        System.out.println("Starting Normal Client...");
        CLIENT_LOGGER.info("Starting Normal Client...");

        System.out.println("Server IP: " + serverIP);
        System.out.println("Server Port " + serverPort);
        Socket clientsocket = null;

        try {
            CLIENT_LOGGER.info("Starting client...");

            // Create a connection with the server
            clientsocket = new Socket(serverIP, serverPort);
            System.out.print("Connection with the server " + serverIP + " established.\n");
            CLIENT_LOGGER.info("Successfully connected to the server.");

            runPetitions(clientsocket);

            clientsocket.close();
            System.out.println("Conexión al servidor cerrada");
            CLIENT_LOGGER.info("Connection with the server closed.");

        } catch (IOException e) {
            System.out.println("Error al conectar con el servidor:");
            CLIENT_LOGGER.info("Error in the creations of the client's socket.");
            System.exit(1);
        }
    }

    /**
     * Runs a "ssl" client
     *
     * @throws IOException
     */
    private static void runSSLClient() throws IOException {
        System.out.println("Starting SSL Client...");
        CLIENT_LOGGER.info("Starting SSL Client...");

        System.out.println("Dirección IP del servidor: " + serverIP);
        System.out.println("Puerto del servidor: " + serverPort);

        // TO-DO
    }


    private static void runPetitions(Socket clientSocket) {
        System.out.println("Introduce the petition you want to send to the server: ");
        System.out.println("The available petitions are: ");
        System.out.println("1. LIST <remote_directory>");
        System.out.println("2. SEND <local_file> <remote_directory>");
        System.out.println("3. RECEIVE <remote_file> <local_directory>");
        System.out.println("4. EXEC <command> <arguments>");

        String petition = KEYBOARD.nextLine();

        // Divide the petition in tokens
        String[] petitionTokens = petition.split(" ");

        // Check the first token to know the petition
        switch (petitionTokens[0]) {
            case "LIST":
                System.out.println("Petition LIST");

                // TO-DO
                break;
            case "SEND":
                System.out.println("Petition SEND");

                // TO-DO
                break;
            case "RECEIVE":
                System.out.println("Petition RECEIVE");

                // TO-DO
                break;
            case "EXEC":
                System.out.println("Petition EXEC");

                // TO-DO
                break;
            default:
                System.out.println("Petition not recognized.");

                // TO-DO
                break;
        }
    }

    /**
     * Initializes the logger for the client.
     */
    private static void startLogger() {
        try {
            checkLogsFolder();

            FileHandler fileHandler_RCC = new FileHandler("logs/RCC.log", 0, 1);
            CLIENT_LOGGER.addHandler(fileHandler_RCC);
            SimpleFormatter formatter_errors = new SimpleFormatter();
            fileHandler_RCC.setFormatter(formatter_errors);
            CLIENT_LOGGER.setUseParentHandlers(false);

            CLIENT_LOGGER.info("Client's logger correctly created.");

        } catch (Exception e) {
            System.out.println("Error in the creation of CLIENT_LOGGER.");
            System.exit(1);
        }
    }

    /**
     * Checks if the "logs" folder exists, if not, then creates it.
     */
    private static void checkLogsFolder() {
        File logsFolder = new File("logs");

        if (!logsFolder.exists()) {
            logsFolder.mkdir();
        }
    }

    /**
     * Checks that the "clients" folder exists, if not, then creates it.
     */
    private static void checkClientsFolder() {
        File logsFolder = new File(clientsFolder);

        if (!logsFolder.exists()) {
            logsFolder.mkdir();
        }
    }
}