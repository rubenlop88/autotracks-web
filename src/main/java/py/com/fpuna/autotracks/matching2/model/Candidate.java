package py.com.fpuna.autotracks.matching2.model;

public class Candidate extends Coordinate {

    private Edge edge;
    private Vertex nearest;
    private Candidate pre;
    private double f;

    public Edge getEdge() {
        return edge;
    }

    public void setEdge(Edge edge) {
        this.edge = edge;
    }

    public Candidate getPre() {
        return pre;
    }

    public void setPre(Candidate pre) {
        this.pre = pre;
    }

    public double getF() {
        return f;
    }

    public void setF(double f) {
        this.f = f;
    }

    public Vertex getNearestVertex() {
        if (nearest == null) {
            if (distanceTo(edge.getSource()) < distanceTo(edge.getTarget())) {
                nearest = edge.getSource();
            } else {
                nearest = edge.getTarget();
            }
        }
        return nearest;
    }

}
