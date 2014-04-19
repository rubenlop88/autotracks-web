package py.com.fpuna.autotracks.matching2.model;

import static py.com.fpuna.autotracks.matching.LocationUtils.*;

public class Coordinate {

    private double latitude;
    private double longitude;

    public Coordinate() {
    }

    public Coordinate(Coordinate other) {
        this(other.latitude, other.longitude);
    }

    public Coordinate(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double distanceTo(Coordinate other) {
        return distance(latitude, longitude, other.latitude, other.longitude);
    }

    public void moveTo(Coordinate other) {
        latitude = other.latitude;
        longitude = other.longitude;
    }

}
