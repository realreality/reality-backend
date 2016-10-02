package cz.bedla.praguehacks2016.realreality.geotools;

import java.util.Iterator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.opengis.feature.simple.SimpleFeature;

import cz.bedla.praguehacks2016.realreality.utils.Utils;

public class GeoToolsFeatureCollectionSupport {
    private final SimpleFeatureCollection featureCollection;

    public GeoToolsFeatureCollectionSupport(SimpleFeatureCollection featureCollection) {
        this.featureCollection = Objects.requireNonNull(featureCollection);
    }

    public void iterateFeatures(ItemAction itemAction) {
        iterateFeaturesIdx((feature, idx) -> itemAction.processAction(feature));
    }

    public void iterateFeaturesIdx(ItemActionIdx itemAction) {
        try (final SimpleFeatureIterator iterator = featureCollection.features()) {
            int i = 0;
            while (iterator.hasNext()) {
                final SimpleFeature feature = iterator.next();
                itemAction.processAction(feature, i++);
            }
        } catch (Exception e) {
            throw Utils.sneakyThrow(e);
        }
    }

    public void stream(StreamProcessor streamProcessor) {
        try (final SimpleFeatureIterator iterator = featureCollection.features()) {
            final Spliterator<SimpleFeature> spliterator = Spliterators.spliteratorUnknownSize(new Iterator<SimpleFeature>() {
                @Override
                public boolean hasNext() {
                    return iterator.hasNext();
                }

                @Override
                public SimpleFeature next() {
                    return iterator.next();
                }
            }, Spliterator.DISTINCT | Spliterator.ORDERED);

            streamProcessor.processStream(StreamSupport.stream(spliterator, false));
        } catch (Exception e) {
            throw Utils.sneakyThrow(e);
        }
    }

    @FunctionalInterface
    public interface ItemAction {
        void processAction(SimpleFeature feature) throws Exception;
    }

    @FunctionalInterface
    public interface ItemActionIdx {
        void processAction(SimpleFeature feature, int idx) throws Exception;
    }

    @FunctionalInterface
    public interface StreamProcessor {
        void processStream(Stream<SimpleFeature> stream) throws Exception;
    }
}
