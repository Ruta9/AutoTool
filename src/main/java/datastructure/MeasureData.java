package datastructure;

public class MeasureData {

    private final String metric;
    private final String metricFullName;
    private final String measuredCodeStructure;
    private final String measuredCodeStructureName;
    private String valueBefore;
    private String valueAfter;

    public MeasureData(String metric, String metricFullName, String measuredCodeStructure, String measuredCodeStructureName, String valueBefore, String valueAfter) {
        this.metric = metric;
        this.metricFullName = metricFullName;
        this.measuredCodeStructure = measuredCodeStructure;
        this.measuredCodeStructureName = measuredCodeStructureName;
        this.valueBefore = valueBefore;
        this.valueAfter = valueAfter;
    }

    public String getMetric() {
        return metric;
    }

    public String getValueBefore() {
        return valueBefore;
    }

    public String getValueAfter() {
        return valueAfter;
    }

    public String getMetricFullName() {
        return metricFullName;
    }

    public String getMeasuredCodeStructure() {
        return measuredCodeStructure;
    }

    public String getMeasuredCodeStructureName() {
        return measuredCodeStructureName;
    }

    public void setValueBefore(String valueBefore) {
        this.valueBefore = valueBefore;
    }

    public void setValueAfter(String valueAfter) {
        this.valueAfter = valueAfter;
    }

    public String toString(){
        return metric + ": " + valueBefore + " / " + valueAfter;
    }

    public String toCSV(){
        return metric + "," + metricFullName + "," + measuredCodeStructure + "," + measuredCodeStructureName.replace(",","|") + "," + valueBefore.replace(",",".") + "," + valueAfter.replace(",",".");
    }

    public static String getHeaders() {
        return "Metric" + "," + "Metric full name" + "," + "Affected code structure" + "," + "Code structure name" + "," + "Value before" + "," + "Value after";
    }
}
