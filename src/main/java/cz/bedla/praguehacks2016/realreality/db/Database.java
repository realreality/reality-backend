package cz.bedla.praguehacks2016.realreality.db;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import com.google.common.base.Predicate;
import com.google.common.base.Stopwatch;

import org.geotools.data.FileDataStore;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.opengis.feature.simple.SimpleFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import cz.bedla.praguehacks2016.realreality.utils.GeoUtils;

@Component
public class Database {

    private static final Logger LOG = LoggerFactory.getLogger(Database.class);

    private List<SimpleFeature> zones;
    private List<SimpleFeature> noiseDay;
    private List<SimpleFeature> noiseNight;
    private List<SimpleFeature> atmosphere;
    private List<SimpleFeature> prices;
    private List<SimpleFeature> flood;

    @PostConstruct
    private void loadData() throws IOException {
        noiseNight = loadShpNoiseNight();
        zones = loadShpZones();
        noiseDay = loadShpNoiseDay();        
        atmosphere = loadShpAtmosphere();
        // prices = loadShpPrices();
        // flood = loadShpFlood();        
    }

    private List<SimpleFeature> loadShpZones() {
        return loadShp((f) -> true /* !"M".equals(f.getAttribute("ZONA_L")) */, "DOP_ZPS_Stani_l.shp",
                "--- ZONES start load", "--- ZONES loaded at ");
    }

    private List<SimpleFeature> loadShpNoiseDay() {
        return loadShp((f) -> true, "HM_Ekola_ADP_pasma_den_p.shp", "--- NOISE DAY start load",
                "--- NOISE DAY loaded at ");
    }

    private List<SimpleFeature> loadShpNoiseNight() {
        return loadShp((f) -> true, "HM_Ekola_ADP_pasma_noc_p.shp", "--- NOISE NIGHT start load",
                "--- NOISE NIGHT loaded at ");
    }

    private List<SimpleFeature> loadShpAtmosphere() {
        return loadShp((f) -> true, "OVZ_Klima_ZnecOvzdusi_p.shp", "--- ATMOSPHERE start load",
                "--- ATMOSPHERE loaded at ");
    }

    private List<SimpleFeature> loadShpPrices() {
        return loadShp((f) -> true, "SED_CenovaMapa_p.shp", "--- PRICES start load", "--- PRICES loaded at ");
    }

    private List<SimpleFeature> loadShpFlood() {
        return loadShp((f) -> true, "zaplavy.shp", "--- FLOOD start load", "--- FLOOD loaded at ");
    }

    public List<SimpleFeature> getZones() {
        return zones;
    }

    public List<SimpleFeature> getNoiseDay() {
        return noiseDay;
    }

    public List<SimpleFeature> getNoiseNight() {
        return noiseNight;
    }

    public List<SimpleFeature> getAtmosphere() {
        return atmosphere;
    }

    public List<SimpleFeature> getPrices() {
        return prices;
    }

    public List<SimpleFeature> getFlood() {
        return flood;
    }

    private List<SimpleFeature> loadShp(Predicate<SimpleFeature> featureFilter, String fileName, String logBegin, String logEnd) {
        LOG.info(logBegin);

        final Stopwatch stopwatch = Stopwatch.createStarted();

        List<SimpleFeature> list = new ArrayList<>();

        FileDataStore store = null;
        try {
            store = GeoUtils.openDataStore(new ClassPathResource("/esri_shapefiles/" + fileName));
            final SimpleFeatureSource featureSource = store.getFeatureSource();

            try(SimpleFeatureIterator iterator = featureSource.getFeatures().features()) {
                while (iterator.hasNext()) {                    
                    SimpleFeature feature = iterator.next();
                    if (featureFilter.apply(feature)) {
                        list.add(feature);
                    }
                }               
            }                       
                
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } finally {
            if (store != null) {
                store.dispose();
            }
        }

        LOG.info(logEnd + stopwatch.stop().toString());

        return list;
    }

}
