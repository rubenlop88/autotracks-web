package py.com.fpuna.autotracks.service;

import java.util.List;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import py.com.fpuna.autotracks.matching.MatcherThread;
import py.com.fpuna.autotracks.model.EstadoCalle;
import py.com.fpuna.autotracks.model.Localizacion;
import py.com.fpuna.autotracks.model.Ruta;

@Stateless
public class RutasService {

    @Inject
    MatcherThread matcher;

    @PersistenceContext
    EntityManager em;

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

}
