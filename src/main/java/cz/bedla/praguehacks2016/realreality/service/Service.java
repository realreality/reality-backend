package cz.bedla.praguehacks2016.realreality.service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;
import org.opengis.feature.simple.SimpleFeature;
import org.springframework.stereotype.Component;

import com.google.common.primitives.Doubles;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
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

    public Map<String, Object> prices(double lon, double lat) {
        final Point center = createPoint(lon, lat);

        final SimpleFeature priceFeature = db.getPrices().parallelStream()
                .filter((f) -> center.intersects(geometry(f)))
                .findFirst()
                .orElse(null);

        return new HashMap<String, Object>() {{
            put("value", priceFeature != null ? findPrice(priceFeature.getAttribute("CENA")) : null);
            put("id", priceFeature != null ? priceFeature.getAttribute("my_id") : null);
        }};
    }

    public Map<String, Object> flood(double lon, double lat) {
        final Point center = createPoint(lon, lat);

        final Map<Integer, List<SimpleFeature>> items = db.getFlood().parallelStream()
                .filter((f) -> center.intersects(geometry(f)))
                .collect(Collectors.groupingBy((f) -> numInt(f.getAttribute("Q"))));

        final Map<String, Object> result = new HashMap<>();

        final List<Map.Entry<Integer, List<SimpleFeature>>> bigWater = items.entrySet().stream()
                .filter((e) -> e.getKey() != 0)
                .collect(Collectors.toList());
        bigWater.forEach((e) -> {
            Validate.validState(e.getValue().size() == 1, "More features than expected: " + e.getValue().size());
            result.put("q" + e.getKey(), new HashMap<String, Object>() {{
                put("type", "hit");
                put("value", e.getKey());
                put("q", findQFloodCoefficient(e.getKey()));
                final SimpleFeature feature = e.getValue().get(0);
                put("dist", findDistance(center, feature.getDefaultGeometry()));
                put("id", numInt(feature.getAttribute("my_id")));
            }});
        });

        final List<SimpleFeature> smallWaterFeatures = items.get(0);
        if (smallWaterFeatures != null) {
            final SimpleFeature smallWater = smallWaterFeatures.stream()
                    .sorted(Comparator.comparingDouble((f) -> findDistance(center, f.getDefaultGeometry())))
                    .findFirst().orElse(null);
            if (smallWater != null) {
                result.put("small", new HashMap<String, Object>() {{
                    put("type", "hit");
                    put("value", null);
                    put("cat", findFloodAreaCategory(smallWater.getAttribute("KATEG")));
                    put("dist", 0.0);
                    put("id", numInt(smallWater.getAttribute("my_id")));
                }});
            }
        }

        if (bigWater.isEmpty() && smallWaterFeatures == null) {
            final List<SimpleFeature> bigWaterFeatures = db.getFlood().parallelStream()
                    .filter((f -> numInt(f.getAttribute("Q")) != 0))
                    .collect(Collectors.toList());
            bigWaterFeatures.forEach(f -> {
                final Integer qValue = numInt(f.getAttribute("Q"));
                result.put("q" + qValue, new HashMap<String, Object>() {{
                    put("type", "distance");
                    put("value", qValue);
                    put("q", findQFloodCoefficient(qValue));
                    put("dist", findDistance(center, f.getDefaultGeometry()));
                    put("id", numInt(f.getAttribute("my_id")));
                }});
            });


            final SimpleFeature smallWaterFeature = db.getFlood().parallelStream()
                    .filter((f -> numInt(f.getAttribute("Q")) == 0))
                    .sorted(Comparator.comparingDouble((f) -> findDistance(center, f.getDefaultGeometry())))
                    .findFirst()
                    .orElseThrow(IllegalStateException::new);

            result.put("small", new HashMap<String, Object>() {{
                put("type", "distance");
                put("value", null);
                put("cat", findFloodAreaCategory(smallWaterFeature.getAttribute("CATEG")));
                put("dist", findDistance(center, smallWaterFeature.getDefaultGeometry()));
                put("id", numInt(smallWaterFeature.getAttribute("my_id")));
            }});
        }

        return result;
    }

    private String findFloodAreaCategory(Object value) {
        final Integer numInt = numInt(value);
        if (numInt == null) {
            return null;
        } else {
            switch (numInt) {
                case 1:
                    return "ACTIVE_ZONE";
                case 2:
                    return "Q100";
                default:
                    return null;
            }
        }
    }

    private Integer findQFloodCoefficient(Integer key) {
        if (key == null) {
            return null;
        } else {
            switch (key) {
                case 1770:
                    return 5;
                case 2720:
                    return 20;
                case 3150:
                    return 50;
                case 4020:
                    return 100;
                default:
                    return null;
            }
        }
    }

    private double findDistance(Point center, Object geometry) {
        return Utils.createLineString(
                new CoordinateArraySequence(
                        new DistanceOp(center, ((Geometry) geometry)).nearestPoints()))
                .getLength();
    }

    private Double findPrice(Object price) {
        return "N".equals(price) ?
                null :
                Doubles.tryParse(String.valueOf(price));
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
