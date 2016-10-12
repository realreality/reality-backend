package cz.bedla.praguehacks2016.realreality.db;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.geotools.data.FileDataStore;
import org.geotools.data.simple.SimpleFeatureSource;
import org.opengis.feature.simple.SimpleFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.common.base.Predicate;
import com.google.common.base.Stopwatch;

import cz.bedla.praguehacks2016.realreality.geotools.GeoToolsFeatureCollectionSupport;
import cz.bedla.praguehacks2016.realreality.utils.GeoUtils;

@Component
public class Database {
    private static final Logger LOG = LoggerFactory.getLogger(Database.class);

    @Value("${dir.shp}")
    private String dirShp;

    private List<SimpleFeature> zones;
    private List<SimpleFeature> noiseDay;
    private List<SimpleFeature> noiseNight;
    private List<SimpleFeature> atmosphere;
    private List<SimpleFeature> prices;
    private List<SimpleFeature> flood;

    @PostConstruct
    private void loadData() throws IOException {
        zones = new LinkedList<>();
        noiseDay = new LinkedList<>();
        noiseNight = new LinkedList<>();
        atmosphere = new LinkedList<>();
        prices = new LinkedList<>();
        flood = new LinkedList<>();

        loadShpZones();
        loadShpNoiseDay();
        loadShpNoiseNight();
        loadShpAtmosphere();
        loadShpPrices();
        loadShpFlood();
    }

    private void loadShpZones() throws IOException {
        loadShp(zones, (f) -> true /*!"M".equals(f.getAttribute("ZONA_L"))*/,
                dirShp, "DOP_ZPS_Stani_l-mercator.shp",
                "--- ZONES start load", "--- ZONES loaded at ");
    }

    private void loadShpNoiseDay() throws IOException {
        loadShp(noiseDay, (f) -> true,
                dirShp, "HM_Ekola_ADP_pasma_den_p-mercator.shp",
                "--- NOISE DAY start load", "--- NOISE DAY loaded at ");
    }

    private void loadShpNoiseNight() throws IOException {
        loadShp(noiseNight, (f) -> true,
                dirShp, "HM_Ekola_ADP_pasma_noc_p-mercator.shp",
                "--- NOISE NIGHT start load", "--- NOISE NIGHT loaded at ");
    }

    private void loadShpAtmosphere() throws IOException {
        loadShp(atmosphere, (f) -> true,
                dirShp, "OVZ_Klima_ZnecOvzdusi_p-mercator.shp",
                "--- ATMOSPHERE start load", "--- ATMOSPHERE loaded at ");
    }

    private void loadShpPrices() throws IOException {
        loadShp(prices, (f) -> true,
                dirShp, "SED_CenovaMapa_p-mercator.shp",
                "--- PRICES start load", "--- PRICES loaded at ");
    }

    private void loadShpFlood() throws IOException {
        loadShp(flood, (f) -> true,
                dirShp, "zaplavy-mercator.shp",
                "--- FLOOD start load", "--- FLOOD loaded at ");
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

    private static void loadShp(List<SimpleFeature> list,
                                Predicate<SimpleFeature> featureFilter,
                                String dirShp, String fileName,
                                String logBegin, String logEnd) throws IOException {
        LOG.info(logBegin);

        final Stopwatch stopwatch = Stopwatch.createStarted();

        final FileDataStore store = GeoUtils.openDataStore(dirShp, fileName);
        final SimpleFeatureSource featureSource = store.getFeatureSource();

        new GeoToolsFeatureCollectionSupport(featureSource.getFeatures())
                .iterateFeatures(feature -> {
                    if (featureFilter.apply(feature)) {
                        list.add(feature);
                    }
                });

        LOG.info(logEnd + stopwatch.stop().toString());
    }

}
