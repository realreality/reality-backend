package cz.bedla.praguehacks2016.realreality.utils;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.FileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;

import com.google.common.base.Charsets;

public final class GeoUtils {
    private GeoUtils() {
    }

    public static FileDataStore openDataStore(String dir, String shpFile) throws IOException {
        final DataStoreFactorySpi factory = new ShapefileDataStoreFactory();
        final Map<String, Serializable> create = new HashMap<>();
        create.put(ShapefileDataStoreFactory.URLP.key, Paths.get(dir, shpFile).toUri().toURL());
        create.put(ShapefileDataStoreFactory.CREATE_SPATIAL_INDEX.key, Boolean.TRUE);
        create.put(ShapefileDataStoreFactory.DBFCHARSET.key, Charsets.UTF_8.name());
        return (FileDataStore) factory.createNewDataStore(create);
    }
}
