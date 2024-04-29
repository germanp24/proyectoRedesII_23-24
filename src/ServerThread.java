import java.io.*;
import java.net.Socket;
import java.util.logging.Logger;

public class ServerThread extends Thread {

    private static final int BUFFER_SIZE = 1024;
    private static Socket threadSocket;
    private static Logger ServerLogger;
    private static String serverFilesDirectory;


    ServerThread(Socket threadSocket, Logger ServerLogger, String serverFilesDirectory) {
        ServerThread.threadSocket = threadSocket;
        ServerThread.ServerLogger = ServerLogger;
        ServerThread.serverFilesDirectory = serverFilesDirectory;
    }

    @Override
    public void run() {
        super.run();

        try {
            InputStream in = threadSocket.getInputStream();
            OutputStream out = threadSocket.getOutputStream();

            byte[] buffer = new byte[BUFFER_SIZE];
            int messageSize = in.read(buffer);
            String petition = new String(buffer, 0, messageSize);
            String[] petitionTokens = petition.split(" ");
            String petitionType = petitionTokens[0];

            switch (petitionType) {
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

                default:
                    // If the petition is not recognized, the server will send a message to the client
                    out.write("Petition not recognized".getBytes());
                    out.write("NULL".getBytes());
                    ServerLogger.info("Petition not recognized. Sending message to the client.");
                    break;
            }

        } catch (Exception e) {
            System.out.println("Error in the petition received from the client");
            ServerLogger.info("Error in the petition received from the client");
        }
    }

    /**
     * Lists the files in the server directory requested by the client.
     *
     * @param petition The petition received from the client.
     * @param out      The output stream to send data to the client.
     * @param in       The input stream to receive data from the client.
     * @param petitionTokens The tokens of the petition received from the client.
     */
    private static void ListPetition(String petition, OutputStream out, InputStream in, String[] petitionTokens) {
        System.out.println("Client" + threadSocket.getInetAddress() + " requested a LIST petition");
        ServerLogger.info("Client" + threadSocket.getInetAddress() + " requested a LIST petition");
        String remoteDirectoryRequested = petitionTokens[1];

        File serverDirectoryRequested = new File(serverFilesDirectory + remoteDirectoryRequested);
        if (!serverDirectoryRequested.exists()) {
            try {
                out.write("Directory not found".getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            File[] files = serverDirectoryRequested.listFiles();
            StringBuilder filesList = new StringBuilder();
            for (File file : files) {
                filesList.append(file.getName()).append("\n");
            }
            try {
                System.out.println("Sending list of files to the client");
                ServerLogger.info("Sending list of files to the client");

                out.write("Contents of the directory: ".getBytes());
                out.write(filesList.toString().getBytes());
                out.write("NULL".getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    /**
     * Sends a file to the client.
     *
     * @param petition The petition received from the client.
     * @param out      The output stream to send data to the client.
     * @param in       The input stream to receive data from the client.
     * @param petitionTokens The tokens of the petition received from the client.
     */
    private static void SendPetition(String petition, OutputStream out, InputStream in, String[] petitionTokens) {
        // TO-DO
    }

    /**
     * Receives a file from the client and saves it in the server.
     *
     * @param petition The petition received from the client.
     * @param out      The output stream to send data to the client.
     * @param in       The input stream to receive data from the client.
     * @param petitionTokens The tokens of the petition received from the client.
     */
    private static void ReceivePetition(String petition, OutputStream out, InputStream in, String[] petitionTokens) throws IOException {
        System.out.println("Client" + threadSocket.getInetAddress() + " requested a RECEIVE petition");
        ServerLogger.info("Client" + threadSocket.getInetAddress() + " requested a RECEIVE petition");

        String filePathRequested = petitionTokens[1];
        String clientDirectory = petitionTokens[2];

        String fullFilePath = serverFilesDirectory + filePathRequested;

        // Check if the file exists
        File file = new File(fullFilePath);
        if (!file.exists() || !file.isFile()) {
            out.write("ERROR: The file requested doesn't exist in the server.".getBytes());
            ServerLogger.info("The file requested doesn't exist in the server.");

            return;
        }

        long fileSize = file.length();
        out.write(Long.toString(fileSize).getBytes());
        out.write("\n".getBytes()); // Separator between the file size and the file data

        // Send the file to the client
        try (FileInputStream fileInputStream = new FileInputStream(file);
             BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream)) {

            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;

            while ((bytesRead = bufferedInputStream.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }

            System.out.println("File sent to the client.");
        } catch (IOException e) {
            try {
                out.write("Error trying to send the file to the client".getBytes());
                ServerLogger.info("Error trying to send the file to the client.");
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            throw new RuntimeException(e);
        }
    }

    /**
     * Executes a command in the server.
     *
     * @param petition The petition received from the client.
     * @param out      The output stream to send data to the client.
     * @param in       The input stream to receive data from the client.
     * @param petitionTokens The tokens of the petition received from the client.
     */
    private static void ExecPetition(String petition, OutputStream out, InputStream in, String[] petitionTokens) {
        // TO-DO
    }
}