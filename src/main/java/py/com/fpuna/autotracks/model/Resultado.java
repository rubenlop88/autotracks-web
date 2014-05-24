package py.com.fpuna.autotracks.model;

public class Resultado {

    private boolean exitoso;
    private String mensaje;
    private Long id; // TODO usar un tipo de dato generico T

    public Resultado(boolean exitoso, String mensaje, Long id) {
        this.exitoso = exitoso;
        this.mensaje = mensaje;
        this.id = id;
    }

    public boolean isExitoso() {
        return exitoso;
    }

    public void setExitoso(boolean exitoso) {
        this.exitoso = exitoso;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}
