package py.com.fpuna.autotracks.matching2.model;

public class Result {

    private int id;
    private int node;
    private Edge edge;

    public Result() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getNode() {
        return node;
    }

    public void setNode(int node) {
        this.node = node;
    }

    public Edge getEdge() {
        return edge;
    }

    public void setEdge(Edge edge) {
        this.edge = edge;
    }

    public Vertex getSource() {
        if (node == edge.getSource().getId()) {
            return edge.getSource();
        } else {
            return edge.getTarget();
        }
    }

    public Vertex getTarget() {
        if (node == edge.getSource().getId()) {
            return edge.getTarget();
        } else {
            return edge.getSource();
        }
    }

    public double getSpeed() {
        return edge.getSpeed();
    }

    public double getLength() {
        return edge.getLength();
    }

    public void calculateDistance() {
        edge.calculateDistance();
    }

}
