import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
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
    private static String clientHost;
    private static int clientPort;
    private static String carpeta_Cliente;

    public static void main(String[] args) throws IOException{

        System.out.println("Introduce el modo (SSL o común");
        clientMode = TECLADO.nextLine();

        System.out.println("Introduce el host:");
        clientHost = TECLADO.nextLine();

        System.out.println("Introduce el puerto:");
        TECLADO.nextInt();


        System.out.println("Introduce la carpeta del cliente:");
        TECLADO.nextLine();
        carpeta_Cliente = TECLADO.nextLine();

        //TECLADO.close();
        checkClientArgs(args);
        startLogger();

        startClient(args);

    }

    private static void checkClientArgs(String[] argumentos) {
        if (argumentos.length !=4){
            System.out.println("ERROR: Número de parámetros incorrecto");
            System.out.println("Usa: java RCC <modo> <host> <puerto> <carpeta_cliente>");
            System.exit(1);

        }try{
            clientMode = argumentos[0];
            clientHost = argumentos[1];
            clientPort = Integer.parseInt(argumentos[2]);
            carpeta_Cliente = argumentos[3];


            System.out.println("Argumentos introducidos: " + Arrays.toString(argumentos));

        }catch(Exception e){
            System.out.println("Error en la asignación de argumentos");
            System.out.println("Revisa el tipo de los argumentos y vuelve a intentarlo de nuevo.");
            System.exit(1);
        }
    }

    /**
     *
     * @param argumentos
     * @throws IOException
     */
    private static void startClient(String[] argumentos) throws IOException{
        System.out.println("Iniciando cliente...");

            //Obtener la dirección IP del cliente
            String clienteIP = InetAddress.getLocalHost().getHostAddress();
            System.out.println("Dirección IP del cliente: " + clienteIP);
           Socket clientsocket = null;

            try{
            //Creamos una conexión al servidor
            clientsocket= new Socket(clientHost, clientPort);
            System.out.println("Conexión al servidor establecida");
            CLIENTE_LOGGER.info("Socket cliente creado correctamente");
            clientsocket.close();
            System.out.println("Conexión al servidor cerrada");

        }catch(IOException e) {
            System.out.println("Error al conectar con el servidor:");
            CLIENTE_LOGGER.info("Error en la creación del socket cliente ");
            System.exit(1);
        }

    }

    private static void startLogger(){
        try{
            checkLogsFolder();

            FileHandler fileHandler_RCC = new FileHandler("logs/RCC.log", 0, 1);
            CLIENTE_LOGGER.addHandler(fileHandler_RCC);
            SimpleFormatter formatter_errors = new SimpleFormatter();
            fileHandler_RCC.setFormatter(formatter_errors);
            CLIENTE_LOGGER.setUseParentHandlers(false);

            CLIENTE_LOGGER.info("Logger del servidor creado e inicializado");

        }catch (Exception e){
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
