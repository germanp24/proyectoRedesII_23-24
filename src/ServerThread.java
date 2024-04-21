import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ServerThread extends Thread {

    private static final int TAM_BUFFER = 1024;

    Socket client;

    ServerThread(Socket client) {
        this.client = client;
    }

    @Override
    public void run() {
        super.run();

        int tamMensaje = 0;
        byte[] byteBuffer = new byte[TAM_BUFFER]; // Creates the buffer

        //"servir", que es lo que hace el servidor
        //servir la conexión: Leer petición y responder (Request-response)
        try {
            InputStream in = client.getInputStream();
            OutputStream out = client.getOutputStream();

            // Request
            while ((tamMensaje = in.read(byteBuffer)) != -1) {
                String strRecibido = new String(byteBuffer, 0, tamMensaje);

            }
            client.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}