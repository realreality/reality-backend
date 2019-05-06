package cz.bedla.praguehacks2016.realreality.utils;

import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MICROSECONDS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.linearref.LengthIndexedLine;

public class Utils {
    private static final CRSAuthorityFactory CRS_AUTHORITY_FACTORY = CRS.getAuthorityFactory(true);

    public static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

    public static Geometry transform(Geometry geometry, MathTransform transform) {
        try {
            return JTS.transform(geometry, transform);
        } catch (Exception e) {
            throw Utils.sneakyThrow(e);
        }
    }

    public static double[] transform(double x, double y, MathTransform mathTransform) {
        final Coordinate result;
        try {
            result = JTS.transform(new Coordinate(x, y), null, mathTransform);
        } catch (TransformException e) {
            throw sneakyThrow(e);
        }
        return new double[]{result.x, result.y};
    }

    private static CoordinateReferenceSystem createCoordinateReferenceSystem(String code) {
        try {
            return CRS_AUTHORITY_FACTORY.createCoordinateReferenceSystem(code);
        } catch (FactoryException e) {
            throw sneakyThrow(e);
        }
    }

    private static MathTransform findMathTransform(CoordinateReferenceSystem source, CoordinateReferenceSystem target) {
        try {
            return CRS.findMathTransform(source, target);
        } catch (FactoryException e) {
            throw sneakyThrow(e);
        }
    }

    public static double[] roundTo(double[] array, int places) {
        array = array.clone();
        for (int i = 0; i < array.length; i++) {
            array[i] = roundTo(array[i], places);
        }
        return array;
    }

    public static double roundTo(double value, int places) {
        final double c = (places < 0 ? 1.0 : Math.pow(10.0, places));
        return Math.round(value * c) / c;
    }

    public static String nanosTotoString(long nanos) {
        TimeUnit unit = chooseUnit(nanos);
        double value = (double) nanos / NANOSECONDS.convert(1, unit);

        // Too bad this functionality is not exposed as a regular method call
        return String.format(Locale.ROOT, "%.4g %s", value, abbreviate(unit));
    }

    public static TimeUnit chooseUnit(long nanos) {
        if (DAYS.convert(nanos, NANOSECONDS) > 0) {
            return DAYS;
        }
        if (HOURS.convert(nanos, NANOSECONDS) > 0) {
            return HOURS;
        }
        if (MINUTES.convert(nanos, NANOSECONDS) > 0) {
            return MINUTES;
        }
        if (SECONDS.convert(nanos, NANOSECONDS) > 0) {
            return SECONDS;
        }
        if (MILLISECONDS.convert(nanos, NANOSECONDS) > 0) {
            return MILLISECONDS;
        }
        if (MICROSECONDS.convert(nanos, NANOSECONDS) > 0) {
            return MICROSECONDS;
        }
        return NANOSECONDS;
    }

    public static String abbreviate(TimeUnit unit) {
        switch (unit) {
            case NANOSECONDS:
                return "ns";
            case MICROSECONDS:
                return "\u03bcs"; // μs
            case MILLISECONDS:
                return "ms";
            case SECONDS:
                return "s";
            case MINUTES:
                return "min";
            case HOURS:
                return "h";
            case DAYS:
                return "d";
            default:
                throw new AssertionError();
        }
    }

    public static LineString lineString(Geometry geometry) {
        if (geometry instanceof GeometryCollection) {
            if (geometry.getNumGeometries() == 1 && geometry.getGeometryN(0) instanceof LineString) {
                return (LineString) geometry.getGeometryN(0);
            } else {
                throw new IllegalStateException("Invalid geometry " + geometry);
            }
        } else if (geometry instanceof LineString) {
            return ((LineString) geometry);
        } else {
            throw new IllegalStateException("Invalid geometry " + geometry);
        }
    }

    public static List<Coordinate> findPointsOnLine(LineString lineString, int count) {
        final LengthIndexedLine indexedLine = new LengthIndexedLine(lineString);
        final double length = lineString.getLength();

        return IntStream.rangeClosed(0, count)
                .mapToDouble(idx -> idx / (double) count)
                .map(idx -> idx * length)
                .mapToObj(indexedLine::extractPoint)
                .collect(Collectors.toList());
    }

    public static List<Coordinate> findPointsOnLineMeters(LineString lineString, double meterStep) {
        final LengthIndexedLine indexedLine = new LengthIndexedLine(lineString);
        final double length = lineString.getLength();

        return metersStepsStream(length, meterStep)
                .mapToObj(indexedLine::extractPoint)
                .collect(Collectors.toList());
    }

    public static DoubleStream metersStepsStream(double length, double meterStep) {
        if (length <= 0) {
            throw new IllegalArgumentException("length should be positive");
        }

        if (meterStep <= 0) {
            throw new IllegalArgumentException("meterStep should be positive");
        }

        return DoubleStream.concat(
                DoubleStream
                        .iterate(0, v -> v + meterStep)
                        .limit(Math.round(length / meterStep)),
                DoubleStream.of(length)
        ).distinct();
    }

    public static Point createPoint(Coordinate coordinate) {
        return GEOMETRY_FACTORY.createPoint(coordinate);
    }

    public static LineString createLineString(CoordinateSequence coordinateSequence) {
        return GEOMETRY_FACTORY.createLineString(coordinateSequence);
    }

    public static boolean deleteRecursively(File root) {
        if (root != null && root.exists()) {
            if (root.isDirectory()) {
                File[] children = root.listFiles();
                if (children != null) {
                    for (File child : children) {
                        deleteRecursively(child);
                    }
                }
            }
            return root.delete();
        }
        return false;
    }

    public static void deleteFiles(String dir, String fileMask) {
        try {
            try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(Paths.get(dir), fileMask)) {
                for (Path entry : dirStream) {
                    entry.toFile().delete();
                }
            }
        } catch (IOException e) {
            throw sneakyThrow(e);
        }
    }

    /**
     * Throw even checked exceptions without being required
     * to declare them or catch them. Suggested idiom:
     * <p>
     * <code>throw sneakyThrow( some exception );</code>
     */
    public static RuntimeException sneakyThrow(Throwable t) {
        // http://www.mail-archive.com/javaposse@googlegroups.com/msg05984.html
        if (t == null)
            throw new NullPointerException();
        Utils.<RuntimeException>sneakyThrow0(t);
        return null;
    }

    @SuppressWarnings("unchecked")
    static private <T extends Throwable> void sneakyThrow0(Throwable t) throws T {
        throw (T) t;
    }
}
