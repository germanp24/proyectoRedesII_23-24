import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.Scanner;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class RCS {
    private static final Logger SERVER_LOGGER = Logger.getLogger("serverLogger");  // Creates the log files
    private static final Scanner TECLADO= new Scanner(System.in);   // Read from keyboard

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

        try{
            serverMode = argumentos[0];
            serverPort = Integer.parseInt(argumentos[1]);
            serverMaxClients = Integer.parseInt(argumentos[2]);

            System.out.println("Argumentos introducidos: " + Arrays.toString(argumentos));

        } catch (Exception e) {
            System.out.println("Error en la asignacion de argumentos");
            System.out.println("Revisa el tipo de los argumentos y vuelve a intentarlo de nuevo.");
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

        // Obtengo nombre de host e IP privada, separo, y me quedo solo con la IP.
        String hostnameAndIp = String.valueOf(InetAddress.getLocalHost());
        String[] ipParts = hostnameAndIp.split("/");
        String privateIpServer = ipParts[1];

        System.out.println("IP Privada Server: " + privateIpServer);
        System.out.println("Puerto Server: " + serverPort);

        ServerSocket serverSocket = null;

        try{
            serverSocket = new ServerSocket(serverPort);
            System.out.println("Socket creado correctamente");
            SERVER_LOGGER.info("Socket servidor creado correctamente.");
        } catch (Exception e){
            System.out.println("Error en la creación del socket servidor");
            SERVER_LOGGER.info("Error en la creación del socket servidor");
            System.exit(1);
        }



        serverSocket.accept();  //Bloquea la ejecución hasta que recibe una petición.

    }

    private static void startLogger() {
        try {
            checkLogsFolder();

            FileHandler fileHandler_RCS = new FileHandler("logs/RCS.log", 0, 1);
            SERVER_LOGGER.addHandler(fileHandler_RCS);
            SimpleFormatter formatter_errors = new SimpleFormatter();
            fileHandler_RCS.setFormatter(formatter_errors);
            SERVER_LOGGER.setUseParentHandlers(false); // Evita que el logger escriba en consola

            SERVER_LOGGER.info("Logger del servidor creado e inicializado.");

        } catch (Exception e) {
            System.out.println("Error en la creación de SERVER_LOGGER.");
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
}