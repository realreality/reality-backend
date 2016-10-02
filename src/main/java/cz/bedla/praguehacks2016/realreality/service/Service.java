package cz.bedla.praguehacks2016.realreality.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.opengis.feature.simple.SimpleFeature;
import org.springframework.stereotype.Component;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.operation.distance.DistanceOp;

import cz.bedla.praguehacks2016.realreality.db.Database;
import cz.bedla.praguehacks2016.realreality.utils.Utils;

@Component
public class Service {

    private final Database db;

    public Service(Database db) {
        this.db = db;
    }

    public List<Map<String, Object>> findZonesInRadius(double lon, double lat, double distance) {
        final Point center = createPoint(lon, lat);

        return db.getZones().parallelStream()
                .filter(f -> isFeatureInRadius(f, center, distance))
                .map(f -> createZone(f, center))
                .collect(Collectors.toList());
    }

    private Map<String, Object> createZone(SimpleFeature feature, Point center) {
        return new HashMap<String, Object>() {{
            put("id", feature.getAttribute("my_id"));
            put("type", feature.getAttribute("ZONA_L"));
            put("dist", new DistanceOp(geometry(feature), center).distance());
        }};
    }

    private boolean isFeatureInRadius(SimpleFeature feature, Point center, double distance) {
        final Geometry geometry = geometry(feature);
        return new DistanceOp(geometry, center).distance() < distance;
    }

    public Map<String, Object> noiseDay(double lon, double lat) {
        return noise(lon, lat, db.getNoiseDay());
    }

    public Map<String, Object> noiseNight(double lon, double lat) {
        return noise(lon, lat, db.getNoiseNight());
    }

    public Map<String, Object> noise(double lon, double lat, List<SimpleFeature> list) {
        final Point center = createPoint(lon, lat);

        final SimpleFeature noiseFeature = list.parallelStream()
                .filter((f) -> center.intersects(geometry(f)))
                .findFirst()
                .orElse(null);

        return new HashMap<String, Object>() {{
            put("db-low", noiseFeature != null ? numDouble(noiseFeature.getAttribute("DB_LO")) : null);
            put("db-high", noiseFeature != null ? numDouble(noiseFeature.getAttribute("DB_HI")) : null);
            put("id", noiseFeature != null ? noiseFeature.getAttribute("my_id") : null);
        }};
    }

    public Map<String, Object> atmosphere(double lon, double lat) {
        final Point center = createPoint(lon, lat);

        final SimpleFeature noiseFeature = db.getAtmosphere().parallelStream()
                .filter((f) -> center.intersects(geometry(f)))
                .findFirst()
                .orElse(null);

        return new HashMap<String, Object>() {{
            put("value", noiseFeature != null ? numInt(noiseFeature.getAttribute("GRIDVALUE")) : null);
            put("id", noiseFeature != null ? noiseFeature.getAttribute("my_id") : null);
        }};
    }

    private Double numDouble(Object obj) {
        if (obj instanceof Number) {
            return ((Number) obj).doubleValue();
        } else {
            return null;
        }
    }

    private Integer numInt(Object obj) {
        if (obj instanceof Number) {
            return ((Number) obj).intValue();
        } else {
            return null;
        }
    }

    private boolean intersection(SimpleFeature feature, Point center) {
        return geometry(feature).intersects(center);
    }

    private Geometry geometry(SimpleFeature feature) {
        final Object obj = feature.getDefaultGeometry();
        if (obj instanceof Geometry) {
            return (Geometry) obj;
        } else {
            throw new IllegalStateException("Geometry not found: " + obj);
        }
    }

    private Point createPoint(double lon, double lat) {
        final double[] xy = Utils.transformWgs84ToMercator(lon, lat);
        return Utils.GEOMETRY_FACTORY.createPoint(new Coordinate(xy[0], xy[1]));
    }
}
