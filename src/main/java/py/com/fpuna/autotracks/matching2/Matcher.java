package py.com.fpuna.autotracks.matching2;

import java.util.ArrayList;
import java.util.List;
import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import py.com.fpuna.autotracks.matching2.model.Candidate;
import py.com.fpuna.autotracks.matching2.model.Point;
import py.com.fpuna.autotracks.model.Localizacion;

@Stateless
public class Matcher {

    @Inject
    private SpatialTemporalMatching stm;

    @PersistenceContext
    private EntityManager em;

    @Asynchronous
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void match(List<Localizacion> localizaciones) {
        List<Point> points = getPoints(localizaciones);
        List<Candidate> results = stm.match(points);
        for (int i = 0; i < results.size(); i++) { // En teoria hay un resultado para cada localizacion.
            Localizacion l = localizaciones.get(i);
            Candidate c = results.get(i);
            l.setLatitudMatch(c.getLatitude());
            l.setLongitudMatch(c.getLongitude());
            em.merge(l);
        }
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
