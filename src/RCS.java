import javax.net.ssl.*;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.net.Socket;

public class RCS {
    private static final Logger SERVER_LOGGER = Logger.getLogger("serverLogger");  // Creates the log files

    private static String serverMode;
    private static int serverPort;
    private static int serverMaxClients;
    private static int serverCurrentClients;
    private static final String serverFilesDirectory = "server_files";

    public static void main(String[] args) throws IOException {
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
    private static void checkServerArgs(String[] arguments) {
        if (arguments.length != 3) {
            System.out.println("ERROR: Incorrect number of parameters.");
            System.out.println("Use: java RCS <mode> <port> <max_clients>");
            System.exit(1);
        }

        try {
            serverMode = arguments[0];
            serverPort = Integer.parseInt(arguments[1]);
            serverMaxClients = Integer.parseInt(arguments[2]);

            System.out.println("Introduced arguments: " + Arrays.toString(arguments));

        } catch (Exception e) {
            System.out.println("Error in the arguments assignation.");
            System.out.println("Revise the type of arguments and try again.");
            System.out.println("Exiting...");
            SERVER_LOGGER.info("Error in the arguments assignation. Exiting...");
            System.exit(1);
        }
    }

    /**
     * Starts the server.
     */
    private static void startServer() throws IOException {
        switch (serverMode) {
            case "normal":
                runNormalServer();
                break;

            case "ssl":
                runSSLServer();
                break;

            default:
                System.out.println("ERROR: There are only these two modes: normal or ssl (lowercase)");
                SERVER_LOGGER.info("Error related with the mode entered by argument. Exiting...");
                System.exit(1);
        }
    }

    /**
     * Runs a "normal" server
     *
     * @throws IOException If an error occurs in the creation of the server socket.
     */
    private static void runNormalServer() throws IOException {
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
            Socket clientSocket = serverSocket.accept(); // Blocks the petition until a client connects.
            serverCurrentClients++; // Increases the variable by one every time a client is connected.

            System.out.println("Client connected from " + clientSocket.getInetAddress().getHostAddress());
            SERVER_LOGGER.info("Client connected from " + clientSocket.getInetAddress().getHostAddress());

            // Create and start a new thread for the client.
            ServerThread serverThread = new ServerThread(clientSocket, SERVER_LOGGER, serverFilesDirectory);
            serverThread.start();
        }
    }

    /**
     * Runs an SSL Server.
     */
    private static void runSSLServer() {

        // Path and password of the server keyStore
        String keyStorePath = "certs/serverKey.jks";
        String keyStorePassword = "servpass";

        // Path and password of the server trustedStore
        String trustStorePath = "certs/ServerTrustedStore.jks";
        String trustStorePassword = "servpass";

        SSLServerSocket serverSocket = null;

        try {
            // Access to the keyStore
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(new FileInputStream(keyStorePath), keyStorePassword.toCharArray());

            // Access to the key manager
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            KeyManager[] keyManagers = kmf.getKeyManagers();

            // Access to the trustedStore and trustManagers
            KeyStore trustedStore = KeyStore.getInstance("JKS");
            trustedStore.load(new FileInputStream(trustStorePath), trustStorePassword.toCharArray());
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(trustedStore);
            TrustManager[] trustManagers = tmf.getTrustManagers();


            // Create the SSLContext
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(keyManagers, trustManagers, null);

            // Create the SSLServerSocketFactory
            SSLServerSocketFactory ssf = sc.getServerSocketFactory();
            serverSocket = (SSLServerSocket) ssf.createServerSocket(serverPort);

            System.out.println("SSL Server correctly created");
            SERVER_LOGGER.info("SSL Server correctly created");

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Loop to accept clients while the number of clients is under the maximum specified.
        while (serverCurrentClients <= serverMaxClients) {
            SSLSocket clientSocket = null;

            try {
                clientSocket = (SSLSocket) serverSocket.accept(); // Blocks the petition until a client connects.
                serverCurrentClients++;

                System.out.println("Client connected from " + clientSocket.getInetAddress().getHostAddress());
                SERVER_LOGGER.info("Client connected from " + clientSocket.getInetAddress().getHostAddress());

                // Create and start a new thread for the client.
                ServerThread serverThread = new ServerThread(clientSocket, SERVER_LOGGER, serverFilesDirectory);
                serverThread.start();

            } catch (Exception e) {
                System.out.println("Error in the connection with the client. Exiting...");
                SERVER_LOGGER.info("Error in the connection with the client. Exiting...");
                System.exit(1);
            }
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
            SERVER_LOGGER.setUseParentHandlers(false); // Avoid to show the logs in the console

            SERVER_LOGGER.info("Logger of the server created and initialized.");

        } catch (Exception e) {
            System.out.println("Error in the creation of the server's logger. Exiting...");
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

    /**
     * Checks if the "server_files" directory exists, if not,
     * then creates it and fills it with 3 files .txt with "Hello World!" inside.
     */
    private static void checkFilesDirectory() {
        File filesDirectory = new File(serverFilesDirectory);

        if (!filesDirectory.exists()) {
            filesDirectory.mkdir();

            try {
                File file1 = new File(serverFilesDirectory + "/file1.txt");
                File file2 = new File(serverFilesDirectory + "/file2.txt");
                File file3 = new File(serverFilesDirectory + "/file3.txt");

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

            } catch (IOException e) {
                System.out.println("Error creating the files inside the directory. Exiting...");
                SERVER_LOGGER.info("Error creating the files inside the directory. Exiting...");
                System.exit(1);
            }
        }
    }
}