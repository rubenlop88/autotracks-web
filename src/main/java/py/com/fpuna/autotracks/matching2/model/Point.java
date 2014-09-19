package py.com.fpuna.autotracks.matching2.model;

public class Point extends Coordinate {

    private long time;
    private Float accuracy;

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public Float getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(Float accuracy) {
        this.accuracy = accuracy;
    }
    
}
