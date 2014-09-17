package py.com.fpuna.autotracks.service;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import py.com.fpuna.autotracks.matching2.Matcher;
import py.com.fpuna.autotracks.model.Localizacion;
import py.com.fpuna.autotracks.model.Ruta;
import py.com.fpuna.autotracks.model.Trafico;

@Stateless
public class RutasService {
    
    private static final Integer DOS_HORAS = 2 * 60 * 60 * 1000;
    private static final Integer UNA_HORA = 1 * 60 * 60 * 1000;
    private static final Integer MEDIA_HORA = 30 * 60 * 1000;

    @Inject
    Matcher matcher;

    @PersistenceContext
    EntityManager em;

    public List<Ruta> obtenerRutas() {
        return em.createQuery("SELECT r FROM Ruta r order by r.fecha").getResultList();
    }
    
    public List<Ruta> obtenerRutas(Timestamp inicio, Timestamp fin) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return em.createQuery("SELECT r FROM Ruta r where r.fecha between '" + sdf.format(inicio) + "' AND '"
                + sdf.format(fin) + "' order by r.fecha").getResultList();
    }

    public List<Localizacion> obtenerLocalizaciones(long id) {
        return em.createQuery("SELECT l FROM Localizacion l WHERE l.ruta.id = :id ORDER BY l.fecha")
                .setParameter("id", id).getResultList();
    }

    public Ruta guardarRuta(Ruta ruta) {
        ruta = em.merge(ruta);
        matcher.match(ruta.getLocalizaciones());
        return ruta;
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
        inicio.setTimeInMillis(fecha.getTime() - MEDIA_HORA);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String query = "SELECT new py.com.fpuna.autotracks.model.Trafico(r.name, r.x1, r.y1, r.x2, r.y2, COUNT(l.id), SUM(l.velocidad))"
                + "FROM Localizacion l, Asu2po4pgr r where l.wayId = r.id and l.fecha between '" + sdf.format(inicio.getTime()) + "' and '" 
                + sdf.format(fecha) + "' group by r.id";
        return em.createQuery(query, Trafico.class).getResultList();
    }
    /**
     * Permite obtener el estado del tráfico en un momento dado, durante un periodo dado
     * @param fecha fecha del tráfico
     * @param tiempo periodo de tiempo en milis
     * @return 
     */
    public List<Trafico> obtenerTrafico(Timestamp fecha, Integer tiempo) {
        Calendar inicio = Calendar.getInstance();
        //se setea el inicio 30 min antes
        inicio.setTimeInMillis(fecha.getTime() - tiempo);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String query = "SELECT new py.com.fpuna.autotracks.model.Trafico(r.name, r.x1, r.y1, r.x2, r.y2, COUNT(l.id), SUM(l.velocidad))"
                + "FROM Localizacion l, Asu2po4pgr r where l.wayId = r.id and l.fecha between '" + sdf.format(inicio.getTime()) + "' and '" 
                + sdf.format(fecha) + "' group by r.id";
        return em.createQuery(query, Trafico.class).getResultList();
    }
    
    /**
     * Permite obtener el estado actual del tráfico en un momento dado
     * @return 
     */
    public List<Trafico> obtenerTraficoActual() {
        Calendar fin = Calendar.getInstance();
        Calendar inicio = Calendar.getInstance();
        //se setea el inicio 30 min antes
        inicio.setTimeInMillis(fin.getTimeInMillis() - DOS_HORAS);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String query = "SELECT new py.com.fpuna.autotracks.model.Trafico(r.name, r.x1, r.y1, r.x2, r.y2, COUNT(l.id), SUM(l.velocidad))"
                + "FROM Localizacion l, Asu2po4pgr r where l.wayId = r.id and l.fecha between '" + sdf.format(inicio.getTime()) + "' and '" 
                + sdf.format(fin.getTime()) + "' group by r.id";
        return em.createQuery(query, Trafico.class).getResultList();
    }

}
