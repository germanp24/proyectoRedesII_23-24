import javax.net.ssl.*;
import java.io.*;
import java.net.*;
import java.security.*;
import java.util.Arrays;
import java.util.logging.*;

/**
 * RCS (Remote Commander Server) is a server that allows clients to execute commands on the server's machine.
 */
public class RCS {
    private static final Logger SERVER_LOGGER = Logger.getLogger("serverLogger");

    private static String serverMode;
    private static int serverPort;
    private static int serverMaxClients;
    private static int serverCurrentClients;
    private static final String serverFilesDirectory = "server_files";
    private static final String serverKeyStorePath = "./certs/serverKey.jks";

    /**
     * Main method of the server.
     *
     * @param args Arguments introduced by the user.
     */
    public static void main(String[] args){
        checkServerArgs(args);
        startLogger();
        checkFilesDirectory();
        startServer();
    }

    /**
     * Check the arguments introduced by the user.
     *
     * @param arguments Arguments introduced by the user.
     */
    public static void checkServerArgs(String[] arguments) {
        if (arguments.length != 3) {
            System.out.println("Incorrect number of parameters, use: java RCS <mode> <port> <max_clients>");
            System.exit(1);
        }

        // If argument 3 is less than 1, then exit.
        if (Integer.parseInt(arguments[2]) < 1) {
            System.out.println("The number of clients must be greater than 0. Exiting...");
            SERVER_LOGGER.info("The number of clients must be greater than 0. Exiting...");
            System.exit(1);
        }

        try {
            serverMode = arguments[0];
            serverPort = Integer.parseInt(arguments[1]);
            serverMaxClients = Integer.parseInt(arguments[2]);
            System.out.println("Arguments: " + Arrays.toString(arguments));

        } catch (Exception e) {
            System.out.println("Error in the arguments assignation. Exiting...");
            SERVER_LOGGER.info("Error in the arguments assignation. Exiting...");
            System.exit(1);
        }
    }

    /**
     * Starts the server.
     */
    public static void startServer(){
        switch (serverMode) {
            case "normal":
                runNormalServer();
                break;

            case "ssl":
                runSSLServer();
                break;

            default:
                System.out.println("Error selecting modes, there are are only two: 'normal' or 'ssl' (lowercase)");
                SERVER_LOGGER.info("Error related with the mode entered by argument. Exiting...");
                System.exit(1);
        }
    }

    /**
     * Runs a "normal" server.
     */
    public static void runNormalServer(){
        System.out.println("Starting Normal Server...");
        ServerSocket serverSocket = null;

        try {
            serverSocket = new ServerSocket(serverPort);
            System.out.println("Server socket correctly created");
            SERVER_LOGGER.info("Server socket correctly created");

        } catch (Exception e) {
            System.out.println("Error in the creation of the server socket. Exiting...");
            SERVER_LOGGER.info("Error in the creation of the server socket. Exiting...");
            System.exit(1);
        }

        // Loop to accept clients while the number of clients is under the maximum specified.
        while (serverCurrentClients <= serverMaxClients) {
            Socket clientSocket = null; // Blocks the petition until a client connects.

            try {
                clientSocket = serverSocket.accept();

            } catch (IOException e) {
                System.out.println("Error accepting the client connection.");
                SERVER_LOGGER.info("Error accepting the client connection.");
            }

            System.out.println("Client connected from " + clientSocket.getInetAddress().getHostAddress());
            SERVER_LOGGER.info("Client connected from " + clientSocket.getInetAddress().getHostAddress());

            serverCurrentClients++; // Increases the variable by one every time a client is connected.
            System.out.println("Current clients connected: " + serverCurrentClients);

            // Create and start a new thread for the client.
            ServerThread serverThread = new ServerThread(clientSocket, SERVER_LOGGER, serverFilesDirectory, serverCurrentClients);
            serverThread.start();
        }
    }

    /**
     * Runs an SSL Server.
     */
    public static void runSSLServer() {
        System.out.println("Starting SSL Server...");
        SERVER_LOGGER.info("Starting SSL Server...");

        SSLServerSocket serverSocket = null;
        
        try {
            // Access to the keystore
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(new FileInputStream(serverKeyStorePath), "servpass".toCharArray());

            // Access to the keys of the keystore
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, "servpass".toCharArray());
            KeyManager[] keyManagers = kmf.getKeyManagers();

            // Get a SSLServerSocketFactory and create the server socket
            try {
                SSLContext sc = SSLContext.getInstance("SSL");
                sc.init(keyManagers, null, null);
                SSLServerSocketFactory ssf = sc.getServerSocketFactory();
                serverSocket = (SSLServerSocket) ssf.createServerSocket(serverPort);
                System.out.println("SSL Server running on port " + serverPort);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch(Exception e) {
            System.out.println("Error in the creation of the SSL Server Socket. Exiting...");
            SERVER_LOGGER.info("Error in the creation of the SSL Server Socket. Exiting...");
            System.exit(1);
        }

        // Loop to accept clients while the number of clients is under the maximum specified.
        while (serverCurrentClients <= serverMaxClients) {
            SSLSocket clientSocket = null;

            try {
                clientSocket = (SSLSocket) serverSocket.accept();

            } catch (IOException e) {
                System.out.println("Error accepting the client connection.");
                SERVER_LOGGER.info("Error accepting the client connection.");
            }

            System.out.println("Client connected from " + clientSocket.getInetAddress().getHostAddress());
            SERVER_LOGGER.info("Client connected from " + clientSocket.getInetAddress().getHostAddress());

            serverCurrentClients++; // Increases the variable by one every time a client is connected.
            System.out.println("Current clients connected: " + serverCurrentClients);

            // Create and start a new thread for the client.
            ServerThread serverThread = new ServerThread(clientSocket, SERVER_LOGGER, serverFilesDirectory, serverCurrentClients);
            serverThread.start();
        }
        
    }

    /**
     * Starts and configure the logger for the server.
     */
    public static void startLogger() {
        try {
            checkLogsFolder();

            FileHandler fileHandler_RCS = new FileHandler("logs/RCS.log", 0, 1);
            SERVER_LOGGER.addHandler(fileHandler_RCS);
            SimpleFormatter formatter_errors = new SimpleFormatter();
            fileHandler_RCS.setFormatter(formatter_errors);
            SERVER_LOGGER.setUseParentHandlers(false); // Avoid to show the logs in the console (?)

            System.out.println("Logger of the server created and initialized.");
            SERVER_LOGGER.info("Logger of the server created and initialized.");

        } catch (Exception e) {
            System.out.println("Error in the creation of the server's logger. Exiting...");
            System.exit(1);
        }
    }

    /**
     * Checks that the "logs" folder exists, if not, then creates it.
     */
    public static void checkLogsFolder() {
        File logsFolder = new File("logs");

        if (!logsFolder.exists()) {
            logsFolder.mkdir();
        }
    }

    /**
     * Checks if the "server_files" directory exists, if not,
     * then creates it and fills it with 3 files .txt with "Hello World!" inside.
     */
    public static void checkFilesDirectory() {

        File serverFilesDirectory = new File(RCS.serverFilesDirectory);

        // If the directory does not exist, then creates it and fills it with 3 files .txt with "Hello World!" inside.
        if (!serverFilesDirectory.exists()) {
            serverFilesDirectory.mkdir();

            try {
                // Create some files
                File file1 = new File(RCS.serverFilesDirectory + "/file1.txt");
                File file2 = new File(RCS.serverFilesDirectory + "/file2.txt");
                File file3 = new File(RCS.serverFilesDirectory + "/file3.txt");

                file1.createNewFile();
                file2.createNewFile();
                file3.createNewFile();

                FileWriter fileWriter1 = new FileWriter(file1);
                FileWriter fileWriter2 = new FileWriter(file2);
                FileWriter fileWriter3 = new FileWriter(file3);

                fileWriter1.write("Hello World 1!");
                fileWriter2.write("Hello World 2!");
                fileWriter3.write("Hello World 3!");

                fileWriter1.close();
                fileWriter2.close();
                fileWriter3.close();

                //Create some directories
                File dir1 = new File(RCS.serverFilesDirectory + "/dir1");
                File dir2 = new File(RCS.serverFilesDirectory + "/dir2");

                dir1.mkdir();
                dir2.mkdir();

                // Create one file on dir1
                File file4 = new File(RCS.serverFilesDirectory + "/dir1/file4.txt");
                file4.createNewFile();
                FileWriter fileWriter4 = new FileWriter(file4);
                fileWriter4.write("Hello World 4!");
                fileWriter4.close();

            } catch (IOException e) {
                System.out.println("Error creating the files inside the directory. Exiting...");
                SERVER_LOGGER.info("Error creating the files inside the directory. Exiting...");
                System.exit(1);
            }
        }
    }
} 