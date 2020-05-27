package datastructure;

import java.util.List;

public class ImpactData {

    private String repository;
    private String release;
    private String commit;
    private RefactoringData refactoringData;
    private List<MeasureData> measureData = null;

    public ImpactData(String repository, String release, String commit, RefactoringData refactoringData, List<MeasureData> measureData) {
        this.repository = repository;
        this.release = release;
        this.commit = commit;
        this.refactoringData = refactoringData;
        this.measureData = measureData;
    }

    public ImpactData(){

    }

    public String toString(){
        return "repository: " + repository + " release: " + release +  " commit: " + commit;
    }

    public String toCSV(){
        return repository + "," + release +  "," + commit;
    }

    public static String getHeaders() {
        return "Repository" + "," + "Release Tag" + "," + "Commit SHA";
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    public void setRelease(String release) {
        this.release = release;
    }

    public void setCommit(String commit) {
        this.commit = commit;
    }

    public void setRefactoringData(RefactoringData refactoringData) {
        this.refactoringData = refactoringData;
    }

    public void setMeasureData(List<MeasureData> measureData) {
        this.measureData = measureData;
    }



    public String getRepository() {
        return repository;
    }

    public String getRelease() {
        return release;
    }

    public String getCommit() {
        return commit;
    }

    public RefactoringData getRefactoringData() {
        return refactoringData;
    }

    public List<MeasureData> getMeasureData() {
        return measureData;
    }
}
