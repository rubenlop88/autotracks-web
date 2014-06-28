package py.com.fpuna.autotracks.resource;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import py.com.fpuna.autotracks.model.Localizacion;
import py.com.fpuna.autotracks.model.Resultado;
import py.com.fpuna.autotracks.model.Ruta;
import py.com.fpuna.autotracks.model.Trafico;
import py.com.fpuna.autotracks.service.RutasService;

@Path("rutas")
@Produces("application/json")
@Consumes("application/json")
public class RutasResource {

    @Inject
    RutasService rutasService;

    @GET
    public List<Ruta> obtenerRutas() {
        List<Ruta> rutas = rutasService.obtenerRutas();
        for (Ruta ruta : rutas) {
            ruta.setLocalizaciones(null);
        }
        return rutas;
    }

    @GET
    @Path("/{id}/localizaciones")
    public List<Localizacion> obtenerLocalizaciones(@PathParam("id") long id) {
        List<Localizacion> localizaciones = rutasService.obtenerLocalizaciones(id);
        for (Localizacion localizacion : localizaciones) {
            localizacion.setRuta(null);
        }
        return localizaciones;
    }

    @POST
    public Resultado guardarRuta(Ruta ruta) {
        // Si la app Android envio un serverId, seteamos este id como el id de
        // la ruta para agregar a la ruta existentes las localizaciones enviadas
        if (ruta.getServerId() != null) {
            ruta.setId(ruta.getServerId());
        }
        // Seteamos la ruta en cada localizacion para que se guarde la relacion
        // en la base de datos
        for (Localizacion l : ruta.getLocalizaciones()) {
            l.setRuta(ruta);
            l.setMatched(false);
        }
        // Guardamos la ruta y retornamos en el resultado el id de la ruta
        // guardada para que la app Android pueda reenviarnos el serverId.
        ruta = rutasService.guardarRuta(ruta);
        return new Resultado(true, null, ruta.getId());
    }

    /**
     * Servicio para obtener el estado del tráfico en un momento dado
     * @param fecha
     * @return 
     */
    @GET
    @Path("/traficoFecha")
    public List<Trafico> obtenerTrafico(@QueryParam("fecha") String fecha) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date fec = new Date();
        try {
            fec = sdf.parse(fecha);
        } catch (ParseException ex) {
            Logger.getLogger(RutasResource.class.getName()).log(Level.SEVERE, "Error al transformar la fecha", ex);
        }
        return rutasService.obtenerTrafico(new Timestamp(fec.getTime()));
    }
    
    @GET
    @Path("/trafico")
    public List<Trafico> obtenerTrafico() {
        return rutasService.obtenerTraficoActual();
    }
    
    @GET
    @Path("/traficoGlobal")
    public List<Trafico> obtenerTraficoGlobal() {
        return rutasService.obtenerTrafico();
    }

}
