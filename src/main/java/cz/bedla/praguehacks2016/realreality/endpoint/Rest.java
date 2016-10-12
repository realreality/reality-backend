package cz.bedla.praguehacks2016.realreality.endpoint;

import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.springframework.stereotype.Component;

import cz.bedla.praguehacks2016.realreality.service.Service;

@Component
@Path("/rest")
public class Rest {
    private final Service service;

    public Rest(Service service) {
        this.service = service;
    }

    @GET
    @Path("/zones-count")
    @Produces("application/json")
    public List<Map<String, Object>> zonesCount(@QueryParam("lon") double lon, @QueryParam("lat") double lat, @QueryParam("dist") double distance) {
        return service.findZonesInRadius(lon, lat, distance);
    }

    @GET
    @Path("/noise")
    @Produces("application/json")
    public Map<String, Object> noiseDay(@QueryParam("lon") double lon, @QueryParam("lat") double lat, @QueryParam("at-night") boolean atNight) {
        return atNight ? service.noiseNight(lon, lat) : service.noiseDay(lon, lat);
    }

    @GET
    @Path("/atmosphere")
    @Produces("application/json")
    public Map<String, Object> atmosphere(@QueryParam("lon") double lon, @QueryParam("lat") double lat) {
        return service.atmosphere(lon, lat);
    }

    @GET
    @Path("/prices")
    @Produces("application/json")
    public Map<String, Object> prices(@QueryParam("lon") double lon, @QueryParam("lat") double lat) {
        return service.prices(lon, lat);
    }

    @GET
    @Path("/flood")
    @Produces("application/json")
    public Map<String, Object> flood(@QueryParam("lon") double lon, @QueryParam("lat") double lat) {
        return service.flood(lon, lat);
    }
}
