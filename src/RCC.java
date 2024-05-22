import java.io.*;
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
    private static String clientFolder;
    private static Boolean infiniteLoopStatus = true;

    private static final int BUFFER_SIZE = 1024;

    /**
     * Main method of the client.
     * @param args
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
     * Starts the client.
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
                System.out.println("ERROR: There are only these two modes: normal or ssl (lowercase). Exiting...");
                CLIENT_LOGGER.info("Error related with the mode entered by argument. Exiting...");
                System.exit(1);
        }
    }

    /**
     * Runs a "normal" client
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
        // TO-DO
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
            String petition = KEYBOARD.nextLine();

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
     *
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

        String response = new String(buffer, 0, messageSize);
        System.out.println(response);
    }

    // MUST BE REVIEWED!!!
    /** Sends a petition to the server.
     *
     * @param petition The petition to be sent.
     * @param out The output stream to send the petition.
     * @param in The input stream to receive the response.
     * @param petitionTokens The tokens of the petition.
     */
    private static void SendPetition(String petition, OutputStream out, InputStream in, String[] petitionTokens) {
//        //Creo el archivo que quiero enviar al servidor
//        String fileName = userInput.readLine();
//        File file = new File(fileName);
//        byte[] buffer = new byte[(int) file.length()];//guardar los datos del archivo y enviarlos al servidor
//        FileInputStream fileInputStream = new FileInputStream(file);//leer los datos
//        OutputStream outputStream = clientsocket.getOutputStream();//envair los datos
//        int bytesRead;
//
//        //se ejecuta mientras haya datos para leer del archivo
//        //lee los datos del archivo y devuelve los bytes leídos
//        //devuelve -1 cuano no hay datos para leer
//        while((bytesRead = fileInputStream.read(buffer)) != -1){
//            outputStream.write(buffer,0, bytesRead);//el 0 indica que empezamos la escritura desde el inicio del buffer
//        }
//
//        outputStream.close();
//        fileInputStream.close();
//        System.out.println("Archivo enviado: " + fileName);
    }

    /**
     * Receives a file from the server.
     *
     * @param petition The petition to be sent.
     * @param out The output stream to send the petition.
     * @param in The input stream to receive the response.
     * @param petitionTokens The tokens of the petition.
     */
    private static void ReceivePetition(String petition, OutputStream out, InputStream in, String[] petitionTokens) {
        // Check the arguments length
        if (petitionTokens.length != 3) {
            System.out.println("ERROR: Incorrect number of parameters.");
            System.out.println("Use: RECEIVE <remote_file> <local_directory>");
            CLIENT_LOGGER.info("Incorrect number of parameters.");
            return;
        }

        try {
            out.write(petition.getBytes());
            System.out.println("Petition sent to the server.");
            CLIENT_LOGGER.info("Petition sent to the server.");

            // Receive the file from the server
            String localDirectory = petitionTokens[2];
            File localFile = new File(localDirectory + "/" + petitionTokens[1]);

            FileOutputStream fileOutputStream = new FileOutputStream(localFile);
            byte[] buffer = new byte[BUFFER_SIZE];

            // Read the file size
            byte[] fileSizeBuffer = new byte[Long.BYTES];
            in.read(fileSizeBuffer);
            long fileSize = Long.parseLong(new String(fileSizeBuffer).trim());

            // Receive the file from the server
            long totalBytesRead = 0;
            while (totalBytesRead < fileSize) {
                int bytesRead = in.read(buffer);
                fileOutputStream.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;
            }

            fileOutputStream.close();
            System.out.println("File received successfully.");
            CLIENT_LOGGER.info("File received successfully.");
        } catch (IOException e) {
            System.out.println("ERROR: An error occurred while receiving the file.");
            CLIENT_LOGGER.info("An error occurred while receiving the file.");
            e.printStackTrace();
        }
    }

    /**
     * Executes a petition in the server.
     *
     * @param petition The petition to be sent.
     * @param out The output stream to send the petition.
     * @param in The input stream to receive the response.
     * @param petitionTokens The tokens of the petition.
     */
    private static void ExecPetition(String petition, OutputStream out, InputStream in, String[] petitionTokens) {
        // TO-DO
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
    }
}