package py.com.fpuna.autotracks.matching;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
//import javax.naming.Context;
//import javax.naming.InitialContext;
//import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;

import py.com.fpuna.autotracks.model.Localizacion;

import com.javadocmd.simplelatlng.LatLng;
import com.javadocmd.simplelatlng.util.LengthUnit;
import com.javadocmd.simplelatlng.window.RectangularWindow;
import javax.inject.Inject;

/**
 *
 * @author Alfredo Campuzano
 */
@Stateless
public class MatcherThread {

    @Resource(mappedName = "java:jboss/datasources/asutracksDS")
    DataSource ds;

    @PersistenceContext
    EntityManager em;
    
    @Inject
    STMatching stm;
    
    double rangeQueryRadius = 150;
    double lonMin, lonMax, latMin, latMax;
    ArrayList<CandidateNode> closeNodesList;
    ArrayList<CandidateNode> relevantNodesList;
//    STMatching stm = new STMatching();

    /**
     * M�todo que permite ralizar map matching de una lista de puntos
     *
     * @param localizaciones
     */
    @Asynchronous
    public void matchPoints(List<Localizacion> localizaciones) {
        String sqlStartNodes;
        Connection conn = null;
        Statement stStartNodes = null;
        ResultSet rsStartNodes = null;
        try {
            conn = ds.getConnection();
            stStartNodes = conn.createStatement();
            for (Localizacion localizacion : localizaciones) {

                System.out.println("id: " + localizacion.getId() + ", latitud: " + localizacion.getLatitud() + " longitud: " + localizacion.getLongitud());

                //Creates a rectangular window used for the range query (for selecting the candidate line strings and nodes)
                RectangularWindow rectangularWindow = new RectangularWindow(new LatLng(localizacion.getLatitud(), localizacion.getLongitud()), rangeQueryRadius, rangeQueryRadius, LengthUnit.METER);
                latMin = rectangularWindow.getMinLatitude();
                latMax = rectangularWindow.getMaxLatitude();
                lonMin = rectangularWindow.getLeftLongitude();
                lonMax = rectangularWindow.getRightLongitude();

                sqlStartNodes = "SELECT ST_NPoints(st_segmentize(geom_way,5)), osm_name, st_x(ST_PointN(st_segmentize(geom_way,5),generate_series(1, ST_NPoints(st_segmentize(geom_way,5))))), "
                        + "st_y(ST_PointN(st_segmentize(geom_way,5),generate_series(1, ST_NPoints(st_segmentize(geom_way,5))))), source, target, x1, y1, x2, y2"
                        + " from asu_2po_4pgr where ST_DWithin(geom_way, ST_GeomFromText('SRID=4326;POINT(" + localizacion.getLongitud() + " " + localizacion.getLatitud() + ")'), 0.002);";

                rsStartNodes = stStartNodes.executeQuery(sqlStartNodes);

                STMatching(rsStartNodes, localizacion);

                rsStartNodes.close();

            }
        } catch (SQLException ex) {
            Logger.getLogger(MatcherThread.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (conn != null) {
                if (stStartNodes != null) {
                    try {
                        stStartNodes.close();
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
        }
    }

    private void STMatching(ResultSet rsStartNodes, Localizacion localizacion) {
        closeNodesList = new ArrayList<CandidateNode>();
        relevantNodesList = new ArrayList<CandidateNode>();

        try {
            while (rsStartNodes.next()) {

                String name = rsStartNodes.getString(2);
                String way = rsStartNodes.getString(5);

                //read out the StartNodes Latitudes and Longitudes from the ResultSet
                Double StartLongitude = Double.parseDouble(rsStartNodes.getString(3));
                Double StartLatitude = Double.parseDouble(rsStartNodes.getString(4));
                
                String source = rsStartNodes.getString(5);
                String target = rsStartNodes.getString(6);
                
                Localizacion lSource = new Localizacion();
                lSource.setLongitud(Double.parseDouble(rsStartNodes.getString(7)));
                lSource.setLatitud(Double.parseDouble(rsStartNodes.getString(8)));
                
                Localizacion lTarget = new Localizacion();
                lTarget.setLongitud(Double.parseDouble(rsStartNodes.getString(7)));
                lTarget.setLatitud(Double.parseDouble(rsStartNodes.getString(8)));

                int maxSpeed = 0;
                /*try{
                 if(!rsStartNodes.getString(7).isEmpty())
                 maxSpeed = rsStartNodes.getInt(7);
                 }
                 catch(NullPointerException e){
                 }*/

                CandidateNode newStartCandidate = new CandidateNode(StartLatitude, StartLongitude, localizacion, name, way, maxSpeed, localizacion.getFecha().getTime());

                if (StartLongitude > (lonMin) && StartLongitude < (lonMax)
                        && StartLatitude > (latMin) && StartLatitude < (latMax)) {
                    newStartCandidate.findClosestVertex(source, target, lSource, lTarget);
                    closeNodesList.add(newStartCandidate);
                }
            }
        } catch (NumberFormatException e1) {
            e1.printStackTrace();
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        //Log.d(DEBUG, "Road nodes: " + new Integer(roadNodesList.size()).toString());
        //Log.d(DEBUG, "Close nodes: " + new Integer(closeNodesList.size()).toString());
        for (CandidateNode e : closeNodesList) {
            boolean doNotAdd = false;
            for (int i = 0; i < closeNodesList.size(); i++) {
                if (e.getWayName().equals(closeNodesList.get(i).getWayName()) && e.getDistanceToGPSFix() > closeNodesList.get(i).getDistanceToGPSFix()) {
                    doNotAdd = true;
                    break;
                }
            }

            if (!doNotAdd) {
                System.out.println("añadiendo calle: " + e.getStreetName());
                relevantNodesList.add(e);
                //Log.d(DEBUG, "New close point: Distance: "+ e.distanceToPoint + " Latitude "+e.getLatitude() + " Longitude " + 
                //		e.getLongitude() + " Street " + e.getStreetName() + " Way hash: " + e.getWayName().hashCode()); 
            }
        }

        if (relevantNodesList.size() > 0) {
            stm.updateRelevantNodesList(relevantNodesList);
            stm.assignObservationProbability();
            stm.assignTransmissionProbability();

            System.out.println("Cantidad de nodos relevantes: " + relevantNodesList.size());

            for (CandidateNode cd : stm.previousNodesList.get(stm.previousNodesList.size() - 1)) {
                if (cd.bestMatch) {
                    try {

                        Localizacion loc = cd.getParentFix();
                        loc.setLatitudMatch(cd.getLatitude());
                        loc.setLongitudMatch(cd.getLongitude());
                        loc.setMatched(true);
                        em.merge(loc);

                    } catch (Exception ex) {
                        Logger.getLogger(MatcherThread.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    System.out.println("calle: " + cd.getStreetName() + ", puntaje: " + cd.getObservationProbability() + " latitud: " + cd.getLatitude() + " longitud: " + cd.getLongitude());
                }
            }
        }

        //OverlayMapViewer.setCandidatePoints(closeNodesList);
    }

}
