package py.com.fpuna.autotracks.matching;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.sql.DataSource;

import py.com.fpuna.autotracks.model.Localizacion;

/**
 *
 * @author Alfredo Campuzano
 */
public class STMatching {

    @Resource(mappedName = "java:jboss/datasources/asutracksDS")
    DataSource ds;
    ArrayList<CandidateNode> relevantNodesList;
    ArrayList<ArrayList<CandidateNode>> previousNodesList = new ArrayList<ArrayList<CandidateNode>>();
    double lastBearing = 0, newBearing = 0, outlierBearing = 0, bearingDifference = 0;
    boolean outlierDetected = false;
    Boolean initPoint = true;
    //Mean and standard deviation in meters
    double mu = 5;
    static double sigma = 10;
    static String DEBUG = "Bearing";

    //Likelihood that the raw fix should be mapped to the candidate in question, without considering the neighboring points
    public void assignObservationProbability() {
        for (CandidateNode e : relevantNodesList) {
            double probability = (1 / (Math.sqrt(6.28) * sigma)) * Math.exp(-((Math.pow((e.getDistanceToGPSFix() - mu), 2)) / (2 * Math.pow(sigma, 2))));
            e.setObservationProbability(probability);
        }

        CandidateNode observationBestMatch = null, observationSecondBestMatch;

        observationBestMatch = relevantNodesList.get(0);
        observationSecondBestMatch = relevantNodesList.get(0);

        for (CandidateNode e : relevantNodesList) {
            if (e.getObservationProbability() > observationBestMatch.getObservationProbability()) {
                observationSecondBestMatch = observationBestMatch;
                observationBestMatch = e;
            } else if (!e.equals(observationBestMatch) && (observationBestMatch.equals(observationSecondBestMatch) || e.getObservationProbability() > observationSecondBestMatch.getObservationProbability())) {
                observationSecondBestMatch = e;
            }
        }

        pointWeighting(observationBestMatch, observationSecondBestMatch);

        previousNodesList.add(relevantNodesList);
    }

    public void assignTransmissionProbability() {
        if (previousNodesList.size() > 1) {
            //For all the candidate nodes from the previous fix compute the likelihood that the 
            //“true�? path from GPS_FIX_i-1 to GPS_FIX_i follows the shortest path from candidate node e to candidate node f
            double highestSpatialResult = 0;
            CandidateNode highestSpatialNode = null;
            int highestSpatialIndex = 0;
            String sqlCost;
            Connection conn = null;
            Statement statement = null;
            ResultSet resultSet = null;

            try {
                conn = ds.getConnection();
                statement = conn.createStatement();
            } catch (SQLException e) {
                Logger.getLogger(STMatching.class.getName()).log(Level.SEVERE, "Error al obtener conexión a db", e);
            }

            for (CandidateNode f : previousNodesList.get(previousNodesList.size() - 1)) {
                for (CandidateNode e : previousNodesList.get(previousNodesList.size() - 2)) {
                    //Compute the transmission probability only for the edges connecting the previous correct match and 
                    //the new candidates for current observation
                    if (e.bestMatch == true) {
                        //Distance between the GPS fixes
                        double distanceBetweenRawPoints = LocationUtils.distance(e.getParentFix(), f.getParentFix());

                        Localizacion locationOfThePreviousPoint = new Localizacion();
                        locationOfThePreviousPoint.setLatitud(e.getLatitude());
                        locationOfThePreviousPoint.setLongitud(e.getLongitude());

                        Localizacion locationOfTheNextPoint = new Localizacion();
                        locationOfTheNextPoint.setLatitud(f.getLatitude());
                        locationOfTheNextPoint.setLongitud(f.getLongitude());

                        //Distance and approximate speed between the two candidate nodes
                        double distanceBetweenTheCandidateNodes = LocationUtils.distance(locationOfThePreviousPoint, locationOfTheNextPoint);

                        if (statement != null) {
                            sqlCost = "SELECT sum(cost) FROM pgr_dijkstra('SELECT id AS id,source::integer,target::integer,"
                                    + "km::double precision AS cost FROM asu_2po_4pgr'," + e.wayID + ", " + f.wayID + ", false, false);";

//                            System.out.println(sqlCost);
                            try {
                                resultSet = statement.executeQuery(sqlCost);
                                resultSet.next();
                                //se multiplica por mil para pasar a metros
                                distanceBetweenTheCandidateNodes = resultSet.getDouble(1) * 1000;
                            } catch (SQLException ex) {
                                Logger.getLogger(STMatching.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                        double meanSpeed = distanceBetweenTheCandidateNodes / (f.getTimestamp() - e.getTimestamp());

                        //Computing transmission probability
                        double transmissionProbability = (distanceBetweenRawPoints / distanceBetweenTheCandidateNodes);
                        if (f.equals(e) || LocationUtils.distance(f.nodeLocation, e.nodeLocation) < 10) {
                            System.out.println("IGUALES!!!");
                            f.setTransmissionProbability(e, 0.0);
                        } else {
                            f.setTransmissionProbability(e, transmissionProbability);
                        }

                        //Always equals 1 because we're moving on one segment max, between the measurements - TO IMPLEMENT moving on multiple segments (sums, etc.)
                        double temporalAnalysisFunctionResult = (f.getMaxSpeed() * meanSpeed) / (f.getMaxSpeed() * meanSpeed);
                        f.setTemporalAnalysisResults(temporalAnalysisFunctionResult);
                        break;
                    }
                }

                //Determine the candidate node with the overall highest spatial/temporal function score
                for (Double e : f.spatialAnalysisFunctionResults) {
                    if (e > highestSpatialResult) {
                        highestSpatialResult = e;
                        highestSpatialNode = f;
                        highestSpatialIndex = f.spatialAnalysisFunctionResults.indexOf(e);
                    }
                }
            }

            if (highestSpatialNode == null) {
                System.out.println("no se encontró el  highestSpatialNode");
                highestSpatialNode = previousNodesList.get(previousNodesList.size() - 1).get(0);
            }
            //Eventually, consider this node the best possible match for the current observation, and use to connect 
            //to it during the succeding observation matching process, also draw it on the map.
            highestSpatialNode.bestMatch = true;
//            previousNodesList.get(previousNodesList.size() - 1).add(highestSpatialNode);
            //OverlayMapViewer.setCandidatePoint(highestSpatialNode);

            //Build the GPS trajectory
            //OverlayMapViewer.buildWaySegment(highestSpatialNode.pastNodesList.get(highestSpatialIndex).getParentFix(), highestSpatialNode.getParentFix(), Color.RED);
            //Build the first edge after the second matching process, compute the initial bearing
            if (initPoint) {
                //OverlayMapViewer.buildRoadSegment(highestSpatialNode.pastNodesList.get(highestSpatialIndex), highestSpatialNode, Color.BLUE);
                lastBearing = LocationUtils.bearing(highestSpatialNode.pastNodesList.get(highestSpatialIndex).getLocation(), highestSpatialNode.getLocation());
                newBearing = lastBearing;
            }

            //Compute the succeeding bearing...
            if (!initPoint) {
                if (!((highestSpatialNode.pastNodesList.get(highestSpatialIndex).getLatitude() == highestSpatialNode.getLatitude())
                        && (highestSpatialNode.pastNodesList.get(highestSpatialIndex).getLatitude() == highestSpatialNode.getLatitude()))) {
                    newBearing = LocationUtils.bearing(highestSpatialNode.pastNodesList.get(highestSpatialIndex).getLocation(), highestSpatialNode.getLocation());
                } else {
                    newBearing = lastBearing;
                }
            }

            //, absolute value of the difference between the bearings...
            bearingDifference = Math.abs(Math.abs(newBearing) - Math.abs(lastBearing));

            initPoint = false;

            //... and check for the outliers.
            if (!initPoint) {
                if (bearingDifference < 35 && outlierDetected == false) {
                    lastBearing = newBearing;
                    //OverlayMapViewer.buildRoadSegment(highestSpatialNode.pastNodesList.get(highestSpatialIndex), highestSpatialNode, Color.BLUE);
                } else if (outlierDetected == true) {
                    //OverlayMapViewer.buildRoadSegment(highestSpatialNode.pastNodesList.get(highestSpatialIndex), highestSpatialNode, Color.BLUE);
                    //lastBearing = newBearing;

                    bearingDifference = Math.abs(Math.abs(newBearing) - Math.abs(outlierBearing));
                    if (bearingDifference < 35) {
                        //asumption that the candidate node was an outlier proven FALSE
                        lastBearing = newBearing;
                        //OverlayMapViewer.buildRoadSegment(highestSpatialNode.pastNodesList.get(highestSpatialIndex), highestSpatialNode, Color.BLUE);
                        outlierDetected = false;
                    } else {
                        //asumption that the candidate node was an outlier proven TRUE - continue using the bearing from the correct
                        //matches, 2 ticks before
                        outlierDetected = false;
                        //OverlayMapViewer.buildRoadSegment(highestSpatialNode.pastNodesList.get(highestSpatialIndex), highestSpatialNode, Color.BLUE);
                        //initPoint = true
                    }
                } else if (bearingDifference > 35 && highestSpatialNode.pastNodesList.get(highestSpatialIndex).getWayName() != highestSpatialNode.getWayName()) {
                    //OverlayMapViewer.buildRoadSegment(highestSpatialNode.pastNodesList.get(highestSpatialIndex), highestSpatialNode, Color.RED);
                    //lastBearing = newBearing;
                    outlierDetected = true;
                    outlierBearing = LocationUtils.bearing(highestSpatialNode.pastNodesList.get(highestSpatialIndex).getLocation(), highestSpatialNode.getLocation());
                    //OverlayMapViewer.buildRoadSegment(highestSpatialNode.pastNodesList.get(highestSpatialIndex), highestSpatialNode, Color.BLUE);
                }
            }
            
            //se cierra la conexión con la db
            if (conn != null) {
                if (statement != null) {
                    try {
                        statement.close();
                    } catch (SQLException ex) {
                        Logger.getLogger(MatcherThread.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                try {
                    conn.close();
                } catch (SQLException ex) {
                    Logger.getLogger(MatcherThread.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } /*else if (previousNodesList.size() > 0) {
         double highestSpatialResult = 0;
         CandidateNode highestSpatialNode = null;
         int highestSpatialIndex = 0;
         }*/

    }

    public void updateRelevantNodesList(ArrayList<CandidateNode> list) {
        relevantNodesList = list;
    }

    public void assignObservationProbability(CandidateNode node) {
        double probability = (1 / (Math.sqrt(6.28) * sigma)) * Math.exp(-((Math.pow((node.getDistanceToGPSFix() - mu), 2)) / (2 * Math.pow(sigma, 2))));
        node.setObservationProbability(probability);
    }

    public void pointWeighting(CandidateNode observationBestMatch, CandidateNode observationSecondBestMatch) {
        if (observationBestMatch.getWayName().equals(observationSecondBestMatch.getWayName())) {
            Localizacion locationBestMatch = observationBestMatch.getLocation();
            Localizacion locationSecondMatch = observationSecondBestMatch.getLocation();

            double distanceBetween = LocationUtils.distance(locationSecondMatch, locationBestMatch);

            double ratio = (observationSecondBestMatch.getObservationProbability() / observationBestMatch.getObservationProbability()) / 2.0;
            double theTrueDistance = distanceBetween * ratio;

            double dist = (theTrueDistance / 1000.0) / 6371.0;
            double lat1 = Math.toRadians(observationBestMatch.getLatitude());
            double lon1 = Math.toRadians(observationBestMatch.getLongitude());
            double bearing = Math.toRadians(LocationUtils.bearing(locationBestMatch, locationSecondMatch));

            double lat2 = Math.asin(Math.sin(lat1) * Math.cos(dist) + Math.cos(lat1) * Math.sin(dist) * Math.cos(bearing));
            double a = Math.atan2(Math.sin(bearing) * Math.sin(dist) * Math.cos(lat1), Math.cos(dist) - Math.sin(lat1) * Math.sin(lat2));
            System.out.println("a = " + a);
            double lon2 = lon1 + a;

            lon2 = (lon2 + 3 * Math.PI) % (2 * Math.PI) - Math.PI;

            double matchedLatitude = Math.toDegrees(lat2);
            double matchedLongitude = Math.toDegrees(lon2);

            CandidateNode geoMatchedCandidate = new CandidateNode(matchedLatitude, matchedLongitude,
                    observationBestMatch.getParentFix(), observationBestMatch.getStreetName(),
                    observationBestMatch.getWayName(), observationBestMatch.getMaxSpeed(), observationBestMatch.getTimestamp());

            assignObservationProbability(geoMatchedCandidate);

            if (previousNodesList.isEmpty()) {
                geoMatchedCandidate.bestMatch = true;
//                OverlayMapViewer.setCandidatePoint(geoMatchedCandidate);
            }

            relevantNodesList.add(geoMatchedCandidate);
        } else if (previousNodesList.isEmpty()) {
            observationBestMatch.bestMatch = true;
//            OverlayMapViewer.setCandidatePoint(observationBestMatch);
        }
    }
}
