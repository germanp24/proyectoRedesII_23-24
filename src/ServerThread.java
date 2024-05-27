import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.logging.Logger;

/**
 *
 * ServerThread is a class that extends the Thread class and is used to manage the petitions received from the clients in different threads.
 *
 */
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

    /**
     * Starts the thread.
     */
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
            System.out.println("Problem creating the input and output streams.");
        }

        // Read the petition from the client.
        byte[] buffer = new byte[BUFFER_SIZE];
        int messageSize = 0;
        try {
            messageSize = in.read(buffer);
        } catch (IOException e) {
            System.out.println("Problem reading the petition from the client.");
            ServerLogger.info("Problem reading the petition from the client.");
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
                    System.out.println("Failed to list directory contents");
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
        String remoteDirectoryAskedToCheck = petitionTokens[2];

        // Check if the directory exists, and if is a directory
        File remoteDirectory = new File(serverFilesDirectory + remoteDirectoryAskedToCheck);
        if (!remoteDirectory.exists() || !remoteDirectory.isDirectory()) {
            try {
                out.write("ERROR".getBytes());
                ServerLogger.info("The directory requested doesn't exist in the server.");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return;
        } else {
            try {
                out.write("OK".getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // Receive the file from the client
        String fileName = petitionTokens[1];
        String filePath = serverFilesDirectory + remoteDirectoryAskedToCheck + fileName;
        
        try {
            BufferedInputStream bufferedInputStream = new BufferedInputStream(in);
            FileOutputStream fileOutputStream = new FileOutputStream(filePath);
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
    
            byte[] fileBuffer = new byte[4096];
            int bytesRead;

            bytesRead = bufferedInputStream.read(fileBuffer);
            bufferedOutputStream.write(fileBuffer, 0, bytesRead);
            bufferedOutputStream.flush();


        } catch( Exception e)  {
            System.out.println("ERROR: An error occurred while receiving the file from the client.");
            ServerLogger.info("An error occurred while receiving the file from the client.");
            
        }

        System.out.println("File received successfully.");
        ServerLogger.info("File received successfully.");
    }

    /**
     * Receives a file from the client and saves it in the server.
     *
     * @param petition The petition received from the client.
     * @param out      The output stream to send data to the client.
     * @param in       The input stream to receive data from the client.
     * @param petitionTokens The tokens of the petition received from the client.
     */
    private static void ReceivePetition(String petition, OutputStream out, InputStream in, String[] petitionTokens) {
        String fileName = petitionTokens[1];
        String filePath = serverFilesDirectory + fileName;

        // Check if the file exists in the server
        File file = new File(filePath);
        if (!file.exists()) {
            try {
                out.write("ERROR".getBytes());
                ServerLogger.info("The file requested doesn't exist in the server.");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return;
        } else {
            try {
                out.write("OK".getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // Send the file to the client
        try (FileInputStream fileInputStream = new FileInputStream(filePath)) {
            byte[] fileBuffer = new byte[BUFFER_SIZE];
            int bytesRead;
            
            while ((bytesRead = fileInputStream.read(fileBuffer)) != -1) {
                out.write(fileBuffer, 0, bytesRead);
            }
            
            System.out.println("File sent successfully.");
            ServerLogger.info("File sent successfully.");
        } catch (IOException e) {
            System.out.println("ERROR: An error occurred while sending the file to the client.");
            ServerLogger.info("An error occurred while sending the file to the client.");
            e.printStackTrace();
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
        try {
            // Build the command to execute
            StringBuilder command = new StringBuilder();
            for (int i = 1; i < petitionTokens.length; i++) {
                command.append(petitionTokens[i]).append(" ");
            }

            // Execute the command
            Process process = Runtime.getRuntime().exec(command.toString().trim());
            BufferedReader processOutputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader processErrorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            // Wait for the process to finish
            int exitCode = process.waitFor();

            PrintWriter writer = new PrintWriter(out, true);

            // Send the output of the process to the client
            String line;
            while ((line = processOutputReader.readLine()) != null) {
                writer.println(line);
            }

            // Si hubo errores, enviar los errores al cliente
            if (exitCode != 0) {
                while ((line = processErrorReader.readLine()) != null) {
                    writer.println("ERROR: " + line);
                }
            }

        } catch (IOException | InterruptedException e) {
            try (PrintWriter writer = new PrintWriter(out, true)) {
                writer.println("ERROR: " + e.getMessage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
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