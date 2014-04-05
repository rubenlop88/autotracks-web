package py.com.fpuna.autotracks.resource;

import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import py.com.fpuna.autotracks.model.Localizacion;

import py.com.fpuna.autotracks.model.Ruta;
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
    public void guardarRuta(Ruta ruta) {
        for (Localizacion l : ruta.getLocalizaciones()) {
            l.setRuta(ruta);
        }
        rutasService.guardarRuta(ruta);
    }

}
