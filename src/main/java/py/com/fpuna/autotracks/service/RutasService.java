package py.com.fpuna.autotracks.service;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import py.com.fpuna.autotracks.matching.MatcherThread;
import py.com.fpuna.autotracks.matching2.SpatialTemporalMatching;
import py.com.fpuna.autotracks.matching2.model.Candidate;
import py.com.fpuna.autotracks.matching2.model.Coordinate;
import py.com.fpuna.autotracks.matching2.model.Point;
import py.com.fpuna.autotracks.model.Localizacion;
import py.com.fpuna.autotracks.model.Ruta;
import py.com.fpuna.autotracks.model.Trafico;

@Stateless
public class RutasService {

    @Inject
    MatcherThread matcher;

    @PersistenceContext
    EntityManager em;

    @Inject
    SpatialTemporalMatching stm;

    public List<Ruta> obtenerRutas() {
        return em.createQuery("SELECT r FROM Ruta r").getResultList();
    }

    public List<Localizacion> obtenerLocalizaciones(long id) {
        return em.createQuery("SELECT l FROM Localizacion l WHERE l.ruta.id = :id ORDER BY l.fecha")
                .setParameter("id", id).getResultList();
    }

    public void guardarRuta(Ruta ruta) {
        em.persist(ruta);
        matcher.matchPoints(ruta.getLocalizaciones());
    }

    public List<Trafico> obtenerTrafico() {
        return em.createQuery("SELECT new py.com.fpuna.autotracks.model.Trafico(r.name, r.x1, r.y1, r.x2, r.y2, COUNT(l.id), SUM(l.velocidad))"
                + "FROM Localizacion l, Asu2po4pgr r where l.wayId = r.id group by r.id", Trafico.class).getResultList();
    }
    
    /**
     * Permite obtener el estado del tráfico en un momento dado
     * @param fecha
     * @return 
     */
    public List<Trafico> obtenerTrafico(Timestamp fecha) {
        Calendar inicio = Calendar.getInstance();
        //se setea el inicio 30 min antes
        inicio.setTimeInMillis(fecha.getTime() - 30 * 60 * 1000);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String query = "SELECT new py.com.fpuna.autotracks.model.Trafico(r.name, r.x1, r.y1, r.x2, r.y2, COUNT(l.id), SUM(l.velocidad))"
                + "FROM Localizacion l, Asu2po4pgr r where l.wayId = r.id and l.fecha between '" + sdf.format(inicio.getTime()) + "' and '" 
                + sdf.format(fecha) + "' group by r.id";
        return em.createQuery(query, Trafico.class).getResultList();
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
