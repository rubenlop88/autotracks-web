package py.com.fpuna.autotracks.matching;

/**
 *
 * @author Alfredo Campuzano
 */
import java.util.ArrayList;

import py.com.fpuna.autotracks.model.Localizacion;

public class CandidateNode {

    String streetName, wayID;
    double nodeLatitude, nodeLongitude;
    Localizacion respondingGPSFix, nodeLocation;
    int maxSpeed;
    boolean startCandidate = false, endCandidate = false, connected = false, bestMatch = false;
    float distanceToRespondingGPSFix;
    double observationProbability;
    long timestamp;
    //contains all candidate nodes responding to the previous GPS Fix, however, the actual transmission probability is computed only for the 
    //most probable previous match 
    ArrayList<CandidateNode> pastNodesList = new ArrayList<CandidateNode>();
    ArrayList<Double> transmissionProbabilities = new ArrayList<Double>();
    //The spatial analysis function results (in regard to one or more previously obtained candidate nodes) are saved here
    ArrayList<Double> spatialAnalysisFunctionResults = new ArrayList<Double>();
    ArrayList<Double> temporalAnalysisFunctionResults = new ArrayList<Double>();

    public CandidateNode(double latitude, double longitude, Localizacion parentGPSFix, String name, String way, int maxSpeed, long parentGPSFixTimestamp) {

        Localizacion locationOfThePoint = new Localizacion();
        locationOfThePoint.setLatitud(latitude);
        locationOfThePoint.setLongitud(longitude);

        this.nodeLocation = locationOfThePoint;
        this.nodeLatitude = latitude;
        this.nodeLongitude = longitude;
        this.respondingGPSFix = parentGPSFix;
        this.streetName = name;
        this.wayID = way;
        this.distanceToRespondingGPSFix = LocationUtils.distance(respondingGPSFix, locationOfThePoint);
        this.maxSpeed = maxSpeed;
        this.timestamp = parentGPSFixTimestamp;
    }

    public boolean equals(CandidateNode NodeToCompare) {
        if (this.nodeLatitude == NodeToCompare.getLatitude()) {
            if (this.nodeLongitude == NodeToCompare.getLongitude()) {
                if (this.respondingGPSFix.equals(NodeToCompare.respondingGPSFix)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Método para encontrar el vértice más cercano al punto
     * @param idVertex1 id del vértice 1
     * @param idVertex2 id del vértice 2
     * @param vertex1 localización del vértice 1
     * @param vertex2 localización del vértice 2
     */
    public void findClosestVertex(String idVertex1, String idVertex2, Localizacion vertex1, Localizacion vertex2) {
        if (LocationUtils.distance(nodeLocation, vertex1) < LocationUtils.distance(nodeLocation, vertex2)) {
            this.wayID = idVertex1;
        } else {
            this.wayID = idVertex2;
        }
    }

    public double getLatitude() {
        return this.nodeLatitude;
    }

    public double getLongitude() {
        return this.nodeLongitude;
    }

    public Localizacion getParentFix() {
        return this.respondingGPSFix;
    }

    public String getStreetName() {
        return streetName;
    }

    public String getWayName() {
        return this.wayID;
    }

    public double getDistanceToGPSFix() {
        return this.distanceToRespondingGPSFix;
    }

    public void setStartOrEndNode(String input) {
        if (input.equals("start")) {
            startCandidate = true;
        } else {
            endCandidate = true;
        }
    }

    /*public GeoPoint toGeopoint(){
     return new GeoPoint(this.nodeLatitude, this.nodeLongitude);
     }*/
    public void setObservationProbability(double probability) {
        this.observationProbability = probability;
    }

    public double getObservationProbability() {
        return this.observationProbability;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public int getMaxSpeed() {
        return this.maxSpeed;
    }

    public void setTransmissionProbability(CandidateNode pastCandidate, Double transmissionProbability) {
        this.pastNodesList.add(pastCandidate);
        this.transmissionProbabilities.add(transmissionProbability);
        this.spatialAnalysisFunctionResults.add(transmissionProbability * observationProbability);
    }

    public void setTemporalAnalysisResults(Double temporalAnalysisResult) {
        this.temporalAnalysisFunctionResults.add(temporalAnalysisResult);
    }

    public void setLocation(Localizacion myLocation) {
        this.nodeLocation = myLocation;
    }

    public Localizacion getLocation() {
        return this.nodeLocation;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CandidateNode) {
            CandidateNode ejemplo = (CandidateNode) obj;
            if (ejemplo.nodeLatitude == this.nodeLatitude && ejemplo.nodeLongitude == this.nodeLongitude && ejemplo.timestamp == this.timestamp) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }

    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + (int) (Double.doubleToLongBits(this.nodeLatitude) ^ (Double.doubleToLongBits(this.nodeLatitude) >>> 32));
        hash = 29 * hash + (int) (Double.doubleToLongBits(this.nodeLongitude) ^ (Double.doubleToLongBits(this.nodeLongitude) >>> 32));
        hash = 29 * hash + (int) (this.timestamp ^ (this.timestamp >>> 32));
        return hash;
    }
}
