public class RCS {

    private String[] argumentos;
    private String modo;
    private int puerto;
    private int max_clientes;

    /**
     * Constructor de la clase RCS
     *
     * @param modo
     * @param puerto
     * @param max_clientes
     */
    public RCS (String modo, int puerto, int max_clientes){
        this.modo=modo;
        this.puerto=puerto;
        this.max_clientes=max_clientes;
    }


    public String[] getArgumentos() {
        return argumentos;
    }

    public void setArgumentos(String[] argumentos) {
        this.argumentos = argumentos;
    }

    public String getModo() {
        return modo;
    }

    public void setModo(String modo) {
        this.modo = modo;
    }

    public int getPuerto() {
        return puerto;
    }

    public void setPuerto(int puerto) {
        this.puerto = puerto;
    }

    public int getMax_clientes() {
        return max_clientes;
    }

    public void setMax_clientes(int max_clientes) {
        this.max_clientes = max_clientes;
    }
}
