package py.com.fpuna.autotracks.matching2.model;

public class Edge {
    
    private int id;
    private double speed; // in km/h
    private double length; // in km
    private Vertex source;
    private Vertex target;
    
    public Edge() {
    }

    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public double getSpeed() {
        return speed;
    }
    
    public void setSpeed(double speed) {
        this.speed = speed;
    }
    
    public double getLength() {
        return length;
    }
    
    public void setLength(double length) {
        this.length = length;
    }
    
    public Vertex getSource() {
        return source;
    }
    
    public void setSource(Vertex source) {
        this.source = source;
    }
    
    public Vertex getTarget() {
        return target;
    }
    
    public void setTarget(Vertex target) {
        this.target = target;
    }

    public void calculateDistance() {
        length = source.distanceTo(target);
    }

}
