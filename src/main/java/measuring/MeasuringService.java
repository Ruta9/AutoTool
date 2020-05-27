package measuring;

import datastructure.MeasureData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jasome.api.JasomeMetricsScannerImpl;
import org.jasome.api.MetricMeasure;
import org.jasome.api.MetricType;

import java.util.*;
import java.util.stream.Collectors;

public class MeasuringService implements IMeasuringService{

    private List<String> metricsToConsider;
    private List<String> packageMetrics;
    private List<String> typeMetrics;
    private List<String> methodMetrics;
    private List<String> projectMetrics;

    private static final Logger logger = LogManager.getLogger(MeasuringService.class);

    public MeasuringService(){
        metricsToConsider = getAllAvailableMetrics();
        updateMetrics();
    }

    private void updateMetrics(){
        packageMetrics = metricsToConsider.stream().filter(m -> m.split("[.]")[0].toLowerCase().equals("package")).map(m-> m.split("[.]")[1]).collect(Collectors.toList());
        typeMetrics = metricsToConsider.stream().filter(m -> m.split("[.]")[0].toLowerCase().equals("class")).map(m-> m.split("[.]")[1]).collect(Collectors.toList());
        methodMetrics = metricsToConsider.stream().filter(m -> m.split("[.]")[0].toLowerCase().equals("method")).map(m-> m.split("[.]")[1]).collect(Collectors.toList());
        projectMetrics = metricsToConsider.stream().filter(m -> m.split("[.]")[0].toLowerCase().equals("project")).map(m-> m.split("[.]")[1]).collect(Collectors.toList());

    }

    public static List<String> getAllAvailableMetrics() {
        List<String> availableMetrics = new ArrayList<>();
        for (MetricType metricType : MetricType.ALL) {
            availableMetrics.add(metricType.getCodeStructure().toLowerCase() + "." + metricType.getDisplayName());
        }
        return availableMetrics;
    }

    @Override
    public void setMetricsToConsider(List<String> metricsToConsider){
        this.metricsToConsider = metricsToConsider;
        updateMetrics();
        logger.info("Metrics set for mining:");
        logger.info("Project: " + projectMetrics);
        logger.info("Package: " + packageMetrics);
        logger.info("Class: " + typeMetrics);
        logger.info("Method: " + methodMetrics);
    }

    @Override
    public List<MetricMeasure> getMetrics(String directory){
        JasomeMetricsScannerImpl jasomeMetricsScanner = new JasomeMetricsScannerImpl();
        return filterMetrics(jasomeMetricsScanner.getMetricsFromDirOrFile(directory));
    }

    @Override
    public List<MeasureData> getMetricsChange(List<MetricMeasure> metricsBefore,List<MetricMeasure> metricsAfter){
        List<MeasureData> measureDataList = new ArrayList<>();
        for(MetricMeasure metricMeasure:metricsBefore){
            measureDataList.add(new MeasureData(metricMeasure.getMetric(), getMetricFullName(metricMeasure.getMetric(),metricMeasure.getCodeStructure()) , metricMeasure.getCodeStructure(), metricMeasure.getName(), metricMeasure.getValue(), "-"));
        }
        for(MetricMeasure metricMeasure: metricsAfter){
            measureDataList.stream().filter( m ->
                    metricMeasure.getCodeStructure().equals(m.getMeasuredCodeStructure()) &&
                    metricMeasure.getMetric().equals(m.getMetric()) &&
                    metricMeasure.getName().equals(m.getMeasuredCodeStructureName())
            ).findFirst().ifPresentOrElse(m ->
                        m.setValueAfter(metricMeasure.getValue()),
                    () -> measureDataList.add(new MeasureData(metricMeasure.getMetric(), getMetricFullName(metricMeasure.getMetric(),metricMeasure.getCodeStructure()) , metricMeasure.getCodeStructure(), metricMeasure.getName(), "-", metricMeasure.getValue())));
        }
        return measureDataList;
    }

    private List<MetricMeasure> filterMetrics(List<MetricMeasure> metricMeasureList){
        return metricMeasureList.stream().filter(m ->
                (m.getCodeStructure().toLowerCase().equals("package") && packageMetrics != null && packageMetrics.contains(m.getMetric())) ||
                        (m.getCodeStructure().toLowerCase().equals("class") && typeMetrics != null && typeMetrics.contains(m.getMetric())) ||
                        (m.getCodeStructure().toLowerCase().equals("method") && methodMetrics != null && methodMetrics.contains(m.getMetric())) ||
                        (m.getCodeStructure().toLowerCase().equals("project") && projectMetrics != null && projectMetrics.contains(m.getMetric()))
        ).collect(Collectors.toList());
    }

    private String getMetricFullName(String metric, String codeStructure){
        return Arrays.stream(MetricType.ALL).filter(m -> m.getDisplayName().equals(metric) && m.getCodeStructure().toLowerCase().equals(codeStructure.toLowerCase())).findFirst().get().getFullName();
    }
}
