package py.com.fpuna.autotracks.model;

public class Trafico {

    private String nombre;
    private Double x1;
    private Double y1;
    private Double x2;
    private Double y2;
    private Long cantidad;
    private Double kmh;

    public Trafico(String nombre, Double x1, Double y1, Double x2, Double y2, Long cantidad, Double kmh) {
        this.nombre = nombre;
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.cantidad = cantidad;
        this.kmh = kmh;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Double getX1() {
        return x1;
    }

    public void setX1(Double x1) {
        this.x1 = x1;
    }

    public Double getY1() {
        return y1;
    }

    public void setY1(Double y1) {
        this.y1 = y1;
    }

    public Double getX2() {
        return x2;
    }

    public void setX2(Double x2) {
        this.x2 = x2;
    }

    public Double getY2() {
        return y2;
    }

    public void setY2(Double y2) {
        this.y2 = y2;
    }

    public Long getCantidad() {
        return cantidad;
    }

    public void setCantidad(Long cantidad) {
        this.cantidad = cantidad;
    }

    public Double getKmh() {
        return kmh * 3.6 / cantidad; // promedio en kmh
    }

    public void setKmh(Double kmh) {
        this.kmh = kmh;
    }

}
