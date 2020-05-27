package measuring;


import datastructure.MeasureData;
import org.apache.commons.lang3.tuple.Pair;
import org.jasome.api.MetricMeasure;

import java.util.List;

public interface IMeasuringService {

    void setMetricsToConsider(List<String> metricsToConsider);
    List<MetricMeasure> getMetrics(String directory);
    List<MeasureData> getMetricsChange(List<MetricMeasure> metricsBefore,List<MetricMeasure> metricsAfter);

}
