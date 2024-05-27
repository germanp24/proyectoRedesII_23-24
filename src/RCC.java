import java.io.*;
import java.net.Socket;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Scanner;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.net.ssl.*;

/**
 * RCC (Remote Cloud Client) is a client that connects to a server to send, receive, list and execute files.
 */
public class RCC {
    private static final Logger CLIENT_LOGGER = Logger.getLogger("clientLogger");
    private static final Scanner KEYBOARD = new Scanner(System.in);

    private static String clientMode;
    private static String serverIP;
    private static int serverPort;
    private static String clientFolder;
    private static Boolean infiniteLoopStatus = true;
    private static String trustedStorePath = "certs/cacerts";

    private static final int BUFFER_SIZE = 1024;

    /**
     * Main method of the client.
     * @param args Arguments introduced by the user.
     */
    public static void main(String[] args){
        checkClientArgs(args);
        startLogger();
        checkFilesDirectory();
        startClient();
    }

    /**
     * Checks the arguments introduced by the user.
     *
     * @param arguments Arguments introduced by the user.
     */
    private static void checkClientArgs(String[] arguments) {
        if (arguments.length != 4) {
            System.out.println("Incorrect number of parameters, use: java RCC <mode> <host> <port> <clients_folder>");
            CLIENT_LOGGER.info("Incorrect number of parameters, use: java RCC <mode> <host> <port> <clients_folder>");
            System.exit(1);
        }
        try {
            clientMode = arguments[0];
            serverIP = arguments[1];
            serverPort = Integer.parseInt(arguments[2]);
            clientFolder = arguments[3];
            System.out.println("Introduced arguments: " + Arrays.toString(arguments));

        } catch (Exception e) {
            System.out.println("Error in the arguments assignation, exiting...");
            CLIENT_LOGGER.info("Error in the arguments assignation, exiting...");
            System.exit(1);
        }
    }

    /**
     * Starts the client bifurcating between the "normal" and "ssl" modes.
     */
    private static void startClient(){
        switch (clientMode){
            case "normal":
                runNormalClient();
                break;

            case "ssl":
                runSSLClient();
                break;

            default:
                System.out.println("ERROR: There are only these two modes: 'normal' or 'ssl' (lowercase). Exiting...");
                CLIENT_LOGGER.info("Error related with the mode entered by argument. Exiting...");
                System.exit(1);
        }
    }

    /**
     * Runs a "normal" client.
     */
    private static void runNormalClient() {
        System.out.println("Starting Normal Client...");
        CLIENT_LOGGER.info("Starting Normal Client...");

        Socket clientsocket = null;
        CLIENT_LOGGER.info("Starting client...");

        try {
            clientsocket = new Socket(serverIP,serverPort);
            System.out.println("Successfully connected to the server.");
            CLIENT_LOGGER.info("Successfully connected to the server.");

        } catch (IOException e) {
            System.out.println("Error in the creations of the client's socket. Exiting...");
            CLIENT_LOGGER.info("Error in the creations of the client's socket.");
            System.exit(1);
        }

        runPetitions(clientsocket);

        // This block of code is executed when the client chooses "EXIT".
        try {
            clientsocket.close();
        } catch (IOException e) {
            System.out.println("Error in the closing of the connection with the server.");
            CLIENT_LOGGER.info("Error in the closing of the connection with the server.");
        }

        System.out.println("Closed connection with the server.");
        CLIENT_LOGGER.info("Closed connection with the server.");
    }

    /**
     * Runs the "SSL" client mode.
     */
    private static void runSSLClient(){
        System.out.println("Starting SSL Client...");
        CLIENT_LOGGER.info("Starting SSL Client...");

        SSLSocket clientSocket = null;

        try {
            // Access to the trusted store "cacerts" with password "changeit"
            KeyStore trustedStore = KeyStore.getInstance("JKS");
            trustedStore.load(new FileInputStream(trustedStorePath), "changeit".toCharArray());

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(trustedStore);
            TrustManager[] trustManagers = tmf.getTrustManagers();

            // Get and initialize an SSL context
            // Get an SSL socket factory and a client socket
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustManagers, null); // This line is key

            SSLSocketFactory ssf = sc.getSocketFactory();
            clientSocket = (SSLSocket) ssf.createSocket(serverIP, serverPort); // To which server I am going to connect!

            // Add an event to detect the handshake!
            clientSocket.addHandshakeCompletedListener(new HandshakeCompletedListener() {
                @Override
                public void handshakeCompleted(HandshakeCompletedEvent event) {
                    X509Certificate cert;
                    try {
                        cert = (X509Certificate) event.getPeerCertificates()[0];
                        String certName = cert.getSubjectX500Principal().getName().substring(3, cert.getSubjectX500Principal().getName().indexOf(","));
                        System.out.println("Connected to the server with certificate name: " + certName);
                        CLIENT_LOGGER.info("Connected to the server with certificate name: " + certName);
                    } catch (SSLPeerUnverifiedException e) {
                        e.printStackTrace();
                    }
                }
            });

            // Launch the SSL handshake -> negotiate the cryptography
            clientSocket.startHandshake(); // DOES NOT BLOCK

            runPetitions(clientSocket);

            // This block of code is executed when the client chooses "EXIT".
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.out.println("Error in the closing of the connection with the server.");
                CLIENT_LOGGER.info("Error in the closing of the connection with the server.");
            }
    
            System.out.println("Closed connection with the server.");
            CLIENT_LOGGER.info("Closed connection with the server.");

        } catch(Exception e) {
            
        }


    }

    /**
     * Runs the petitions of the client.
     *
     * @param clientSocket The client's socket.
     */
    private static void runPetitions(Socket clientSocket){

        InputStream in = null;
        OutputStream out = null;
        try {
            in = clientSocket.getInputStream();
            out = clientSocket.getOutputStream();
        } catch (IOException e) {
            System.out.println("Error in the creation of the input and output streams. Exiting...");
            CLIENT_LOGGER.info("Error in the creation of the input and output streams.");
        }

        while(infiniteLoopStatus) {
            showOptions();
            String petition = "";
            try {
                petition = KEYBOARD.nextLine();
                
            } catch (Exception e) {
                System.out.println("Error in the reading of the petition. Exiting...");
                CLIENT_LOGGER.info("Error in the reading of the petition.");
            }

            if (petition.matches("[0-9]+")) {
                System.out.println("Error: You have to type the name of the petition, not the index. Exiting...");
                CLIENT_LOGGER.info("Error: You have to type the name of the petition, not the index. Exiting...");
                break;
            }

            String[] petitionTokens = petition.split(" ");

            switch (petitionTokens[0]) {
                case "LIST":
                    ListPetition(petition, out, in, petitionTokens);
                    break;

                case "SEND":
                    SendPetition(petition, out, in, petitionTokens);
                    break;

                case "RECEIVE":
                    ReceivePetition(petition, out, in, petitionTokens);
                    break;

                case "EXEC":
                    ExecPetition(petition, out, in, petitionTokens);
                    break;

                case "EXIT":
                    ExitPetition(petition, out, clientSocket);
                    break;
                default:
                    System.out.println("Petition not recognized.");
                    CLIENT_LOGGER.info("Petition not recognized.");
                    break;
            }
        }
    }

    /**
     * Lists the files in the server directory requested by the client.
     * @param petition The petition received from the client.
     * @param out The output stream to send the petition.
     * @param in The input stream to receive the response.
     * @param petitionTokens The tokens of the petition.
     */
    private static void ListPetition(String petition, OutputStream out, InputStream in, String[] petitionTokens) {
        if (petitionTokens.length != 2) {
            System.out.println("ERROR: Incorrect number of parameters, use: LIST <remote_directory>");
            CLIENT_LOGGER.info("Incorrect number of parameters.");
            return;
        }

        try {
            out.write(petition.getBytes());
            System.out.println("Petition sent to the server.");
            CLIENT_LOGGER.info("Petition sent to the server.");

        } catch (IOException e) {
            System.out.println("Error sending of the petition.");
            CLIENT_LOGGER.info("Error sending of the petition.");
        }

        byte[] buffer = new byte[BUFFER_SIZE];
        int messageSize = 0;

        try {
            messageSize = in.read(buffer);
            CLIENT_LOGGER.info("Response received from the server.");
        } catch (IOException e) {
            System.out.println("Error in the reception of the response.");
            CLIENT_LOGGER.info("Error in the reception of the response.");
        }

        try {
            String response = new String(buffer, 0, messageSize);
            System.out.println(response);
        } catch (Exception e) {
            System.out.println("Error in the conversion of the response.");
            CLIENT_LOGGER.info("Error in the conversion of the response.");
        }
    }

    /**
     * Sends a petition to the server.
     *
     * @param petition The petition to be sent.
     * @param out The output stream to send the petition.
     * @param in The input stream to receive the response.
     * @param petitionTokens The tokens of the petition.
     */
    private static void SendPetition(String petition, OutputStream out, InputStream in, String[] petitionTokens) {
        // Check the arguments length
        if (petitionTokens.length != 3) {
            System.out.println("ERROR: Incorrect number of parameters.");
            System.out.println("Use: SEND <local_file> <remote_directory>");
            CLIENT_LOGGER.info("Incorrect number of parameters.");
            return;
        }

        // Check if the file exist or is a directory
        String localFilePath = clientFolder + "/" + petitionTokens[1];
        File localFile = new File(localFilePath);
        if (!localFile.exists() || localFile.isDirectory()) {
            System.out.println("ERROR: The file does not exist or is a directory.");
            CLIENT_LOGGER.info("The file does not exist or is a directory.");
            return;
        }

        // Check if the remote directory exists
        // Send the remote directory, it will check if it exists and respond with the status
        try {
            out.write(petition.getBytes());
        } catch (Exception e) {
            System.out.println("ERROR: An error occurred while sending the remote directory for checking.");
            CLIENT_LOGGER.info("An error occurred while sending the remote directory for checking.");
            e.printStackTrace();
        }

        // Receive the response from the server, print an error if the directory if the received response is not "OK"
        byte[] buffer = new byte[BUFFER_SIZE];
        int messageSize = 0;
        try {
            messageSize = in.read(buffer);
        } catch (IOException e) {
            System.out.println("ERROR: An error occurred while receiving the response from the server.");
            CLIENT_LOGGER.info("An error occurred while receiving the response from the server.");
            e.printStackTrace();
        }

        String response = new String(buffer, 0, messageSize);
        if (!response.equals("OK")) {
            System.out.println("ERROR: The remote directory does not exist.");
            CLIENT_LOGGER.info("The remote directory does not exist.");
            return;
        }

        // Send the file to the server
        try {
            FileInputStream fileInputStream = new FileInputStream(localFile);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(out);

            byte[] fileBuffer = new byte[4096];
            int bytesRead;
            
            while ((bytesRead = bufferedInputStream.read(fileBuffer)) != -1) {
                bufferedOutputStream.write(fileBuffer, 0, bytesRead);
                bufferedOutputStream.flush();
            }
            
            System.out.println("File sent successfully.");
            CLIENT_LOGGER.info("File sent successfully.");
        } catch (IOException e) {
            System.out.println("ERROR: An error occurred while sending the file to the server.");
            CLIENT_LOGGER.info("An error occurred while sending the file to the server.");
            e.printStackTrace();
        }
    }

    /**
     * Receives a file from the server.
     * @param petition The petition to be sent.
     * @param out The output stream to send the petition.
     * @param in The input stream to receive the response.
     * @param petitionTokens The tokens of the petition.
     */
    private static void ReceivePetition(String petition, OutputStream out, InputStream in, String[] petitionTokens) {
        // Save the filename in a variable, i have to cut the petitionTokens[1] and get only the last word
        String[] remoteFileTokens = petitionTokens[1].split("/");
        // Get the last word
        String remoteFileName = remoteFileTokens[remoteFileTokens.length - 1];

        // Check the arguments length
        if (petitionTokens.length != 3) {
            System.out.println("ERROR: Incorrect number of parameters.");
            System.out.println("Use: RECEIVE <remote_file> <local_directory>");
            CLIENT_LOGGER.info("Incorrect number of parameters.");
            return;
        }

        // Check if the remote file exists
        try {
            out.write(petition.getBytes());
        } catch (IOException e) {
            System.out.println("ERROR: An error occurred while sending the remote file to the server.");
            CLIENT_LOGGER.info("An error occurred while sending the remote file to the server.");
            e.printStackTrace();
            return;
        }

        // Receive the response from the server, print an error if the file does not exist
        byte[] buffer = new byte[BUFFER_SIZE];
        int messageSize;
        try {
            messageSize = in.read(buffer);
        } catch (IOException e) {
            System.out.println("ERROR: An error occurred while receiving the response from the server.");
            CLIENT_LOGGER.info("An error occurred while receiving the response from the server.");
            e.printStackTrace();
            return;
        }

        String response = new String(buffer, 0, messageSize);
        if (response.equals("ERROR")) {
            System.out.println("ERROR: The remote file does not exist.");
            CLIENT_LOGGER.info("The remote file does not exist.");
            return;
        }
        else {
            System.out.println("The remote file exists.");
            CLIENT_LOGGER.info("The remote file exists.");
        }
  
        // Recibe el archivo del servidor
        try (FileOutputStream fileOutputStream = new FileOutputStream(clientFolder+"/"+remoteFileName)) {
            byte[] fileBuffer = new byte[BUFFER_SIZE];
            int bytesRead;

            bytesRead = in.read(fileBuffer);
            fileOutputStream.write(fileBuffer, 0, bytesRead);
            fileOutputStream.flush();


            System.out.println("File received successfully.");
            CLIENT_LOGGER.info("File received successfully.");
        } catch (IOException e) {
            System.out.println("ERROR: An error occurred while receiving the file from the server.");
            CLIENT_LOGGER.info("An error occurred while receiving the file from the server.");
            e.printStackTrace();
        }
    }

    /**
     * Executes a petition in the server.
     * @param petition The petition to be sent.
     * @param out The output stream to send the petition.
     * @param in The input stream to receive the response.
     * @param petitionTokens The tokens of the petition.
     */
    private static void ExecPetition(String petition, OutputStream out, InputStream in, String[] petitionTokens) {
        try {
            // Send the petition to the server
            PrintWriter writer = new PrintWriter(out, true);
            writer.println(petition);

            // Receive the response from the server
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void ExitPetition(String petition, OutputStream out, Socket clientSocket) {
        try {
            out.write(petition.getBytes());
            infiniteLoopStatus = false;
        } catch (IOException e) {
            throw new RuntimeException(e);
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
    private static void checkFilesDirectory() {
        File logsFolder = new File(clientFolder);

        if (!logsFolder.exists()) {
            logsFolder.mkdir();
        }

        // Create a file and write something in it
        File file = new File(clientFolder + "/test.txt");
        try {
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write("This is a test file.");
            fileWriter.close();
        } catch (IOException e) {
            System.out.println("Error in the creation of the test file.");
            CLIENT_LOGGER.info("Error in the creation of the test file.");
        }
    }

    /**
     * Shows the options available for the client.
     */
    private static void showOptions() {
        System.out.println("The available petitions are: ");
        System.out.println("1. LIST <remote_directory>");
        System.out.println("2. SEND <local_file> <remote_directory>");
        System.out.println("3. RECEIVE <remote_file> <local_directory>");
        System.out.println("4. EXEC <command> <arguments>");
        System.out.println("5. EXIT");
        System.out.println("* Remember that '/' is the root of the remote storage.");
    }
}