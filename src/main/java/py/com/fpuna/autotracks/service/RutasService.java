package py.com.fpuna.autotracks.service;

import java.util.ArrayList;
import java.util.List;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import py.com.fpuna.autotracks.matching.MatcherThread;
import py.com.fpuna.autotracks.matching2.CandidateSelection;
import py.com.fpuna.autotracks.matching2.ShortestPathCalculation;
import py.com.fpuna.autotracks.matching2.SpatialTemporalMatching;
import py.com.fpuna.autotracks.matching2.model.Candidate;
import py.com.fpuna.autotracks.matching2.model.Coordinate;
import py.com.fpuna.autotracks.matching2.model.Point;
import py.com.fpuna.autotracks.model.EstadoCalle;
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

    public List<EstadoCalle> obtenerEstadosCalles() {
        return em.createQuery("SELECT new py.com.fpuna.autotracks.model.EstadoCalle(r, COUNT(l)) "
                + "FROM Asu2po4pgr r, Localizacion l WHERE l.wayId = r.id GROUP BY r.id", EstadoCalle.class)
                .getResultList();
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
