import java.io.*;
import java.net.Socket;
import java.util.logging.Logger;

public class ServerThread extends Thread {

    private static final int BUFFER_SIZE = 1024;
    private static Socket threadSocket;
    private static Logger ServerLogger;
    private static String serverFilesDirectory;
    private static int ServerCurrentClients;
    private static Boolean infiniteLoopStatus = true;


    /**
     *
     * Constructor of the ServerThread class.
     *
     */
    ServerThread(Socket threadSocket, Logger ServerLogger, String serverFilesDirectory, int ServerCurrentClients) {
        ServerThread.threadSocket = threadSocket;
        ServerThread.ServerLogger = ServerLogger;
        ServerThread.serverFilesDirectory = serverFilesDirectory;
        ServerThread.ServerCurrentClients = ServerCurrentClients;
    }

    @Override
    public void run() {
        super.run();

        while (infiniteLoopStatus) {
            processPetition();
        }
    }

    /**
     * Processes the petition received from the client.
     */
    private void processPetition(){
        // Create the input and output streams to communicate with the client.
        InputStream in = null;
        OutputStream out = null;
        try {
            in = threadSocket.getInputStream();
            out = threadSocket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Read the petition from the client.
        byte[] buffer = new byte[BUFFER_SIZE];
        int messageSize = 0;
        try {
            messageSize = in.read(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String petition = new String(buffer, 0, messageSize);
        String[] petitionTokens = petition.split(" ");

        switch (petitionTokens[0]) {
            case "LIST":
                System.out.println("Client" + threadSocket.getInetAddress() + " requested a LIST petition");
                ServerLogger.info("Client" + threadSocket.getInetAddress() + " requested a LIST petition");

                ListPetition(petition, out, in, petitionTokens);
                break;

            case "SEND":
                System.out.println("Client" + threadSocket.getInetAddress() + " requested a SEND petition");
                ServerLogger.info("Client" + threadSocket.getInetAddress() + " requested a SEND petition");

                SendPetition(petition, out, in, petitionTokens);
                break;

            case "RECEIVE":
                System.out.println("Client" + threadSocket.getInetAddress() + " requested a RECEIVE petition");
                ServerLogger.info("Client" + threadSocket.getInetAddress() + " requested a RECEIVE petition");

                ReceivePetition(petition, out, in, petitionTokens);
                break;
            case "EXEC":
                System.out.println("Client" + threadSocket.getInetAddress() + " requested a EXEC petition");
                ServerLogger.info("Client" + threadSocket.getInetAddress() + " requested a EXEC petition");

                ExecPetition(petition, out, in, petitionTokens);
                break;

            case "EXIT":
                System.out.println("Client" + threadSocket.getInetAddress() + " requested to close the connection");
                ServerLogger.info("Client" + threadSocket.getInetAddress() + " requested to close the connection");

                ExitPetition();
                break;

            default:
                System.out.println("Invalid petition received from the client");
                ServerLogger.info("Invalid petition received from the client");
                break;
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
    String directoryRequestedToList = petitionTokens[1];

    File completeServerDirectoryRequested = new File(serverFilesDirectory + directoryRequestedToList);

    // If the directory does not exist, send an error message to the client. Otherwise, list the files and directories.
    if (!completeServerDirectoryRequested.exists()) {
        try {
            out.write("Directory not found\n".getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    } else {
        File[] files = completeServerDirectoryRequested.listFiles();
        if (files != null) {
            if (files.length == 0) {
                // The directory is empty
                try {
                    out.write("The directory is empty\n".getBytes());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                StringBuilder filesList = new StringBuilder();
                for (File file : files) {
                    if (file.isDirectory()) {
                        filesList.append("[DIR]  ").append(file.getName()).append("\n");
                    } else {
                        filesList.append("[FILE] ").append(file.getName()).append("\n");
                    }
                }
                try {
                    System.out.println("Sending list of files and directories to the client");
                    ServerLogger.info("Sending list of files and directories to the client");
                    out.write(filesList.toString().getBytes());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } else {
            try {
                out.write("Failed to list directory contents\n".getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
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
    private static void ReceivePetition(String petition, OutputStream out, InputStream in, String[] petitionTokens){
        try {
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
        } catch (IOException e) {
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

    /**
     * Closes the connection with the client.
     *
     */
    private static void ExitPetition() {
        try {
            threadSocket.close();
            System.out.println("Connection with the client" + threadSocket.getInetAddress() + " closed.");
            ServerLogger.info("Connection with the client closed.");

            ServerCurrentClients--;
            infiniteLoopStatus = false;

            System.out.println("Current clients connected: " + ServerCurrentClients);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}