package py.com.fpuna.autotracks.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;
import py.com.fpuna.autotracks.matching.MatcherThread;
import py.com.fpuna.autotracks.matching2.SpatialTemporalMatching;
import py.com.fpuna.autotracks.matching2.model.Candidate;
import py.com.fpuna.autotracks.matching2.model.Coordinate;
import py.com.fpuna.autotracks.matching2.model.Point;
import py.com.fpuna.autotracks.model.Localizacion;
import py.com.fpuna.autotracks.model.Ruta;

@Stateless
public class RutasService {

    @Inject
    MatcherThread matcher;

    @PersistenceContext
    EntityManager em;

    @Inject
    SpatialTemporalMatching stm;

    @Resource(mappedName = "java:jboss/datasources/asutracksDS")
    DataSource ds;

    public List<Ruta> obtenerRutas() {
        return em.createQuery("SELECT r FROM Ruta r").getResultList();
    }

    public List<Localizacion> obtenerLocalizaciones(long id) {
        return em.createQuery("SELECT l FROM Localizacion l WHERE l.ruta.id = :id")
                .setParameter("id", id).getResultList();
    }

    public void guardarRuta(Ruta ruta) {
        em.persist(ruta);
        matcher.matchPoints(ruta.getLocalizaciones());
    }
        
    public String obtenerTrafico() {
        String retorno = null;
        Statement statement = null;
        ResultSet result = null;
        try {
            Connection con = ds.getConnection();
            String query = "SELECT r.osm_name, r.x1, r.y1, r.x2, r.y2,COUNT(l.id) As tot, sum(l.velocidad)"
                    + "FROM localizacion l, asu_2po_4pgr r where l.way_id = r.id group by r.id;";
            
            statement = con.createStatement();
            
            result = statement.executeQuery(query);
            
            JsonArray jArray = new JsonArray();
            JsonObject json;
            
            while (result.next()) {
                json = new JsonObject();
                json.addProperty("nombre", result.getString(1));
                json.addProperty("x1", result.getDouble(2));
                json.addProperty("y1", result.getDouble(3));
                json.addProperty("x2", result.getDouble(4));
                json.addProperty("y2", result.getDouble(5));
                json.addProperty("cantidad", result.getLong(6));
                json.addProperty("kmh", result.getDouble(7) * 3.6 / result.getLong(6));
                jArray.add(json);
            }
            
            retorno = jArray.toString();
            
        } catch (SQLException ex) {
            Logger.getLogger(RutasService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception e) {
            Logger.getLogger(RutasService.class.getName()).log(Level.SEVERE, null, e);
        }
        return retorno;
    }

    public List<Coordinate> obtenerPath(long id) {
        List<Localizacion> localizacions = obtenerLocalizaciones(id);
        List<Point> points = getPoints(localizacions);
        List<Candidate> results = stm.match(points);
        List<Coordinate> coordinates = new ArrayList<>();
        for (Candidate r : results) {
            coordinates.add(r);
        }
        return coordinates;
    }

    private List<Point> getPoints(List<Localizacion> localizacions) {
        List<Point> points = new ArrayList<>();
        for (Localizacion l : localizacions) {
            Point p = new Point();
            p.setLatitude(l.getLatitud());
            p.setLongitude(l.getLongitud());
            p.setTime(l.getFecha().getTime());
            points.add(p);
        }
        return points;
    }

}
