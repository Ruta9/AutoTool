package impactMining;


import datastructure.ImpactData;
import datastructure.MeasureData;
import measuring.IMeasuringService;
import measuring.MeasuringService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jasome.api.MetricMeasure;
import output.CSVOutput;
import output.IOutput;
import refactoring.IRefactoringService;
import refactoring.RefactoringService;
import versioncontrol.GitHubLocalService;
import versioncontrol.IVersionControlService;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class ImpactMiningService implements IImpactMiningService{

    private static final Logger logger = LogManager.getLogger(ImpactMiningService.class);
    private IRefactoringService refactoringService;
    private IVersionControlService versionControlService;
    private IMeasuringService measuringService;
    private IOutput output;


    public ImpactMiningService(){
        refactoringService = new RefactoringService();
        measuringService = new MeasuringService();
        output = new CSVOutput();
    }

    @Override
    public void mineAtCommit(String repository, String commitSha, List<String> refactoringTypes,List<String> metricTypes) {
        versionControlService = new GitHubLocalService(repository);
        logger.info("Mining from repository: " + repository + " commit: " + commitSha);
        refactoringService.setSelectedRefactoringTypes(refactoringTypes);
        measuringService.setMetricsToConsider(metricTypes);
        Iterator<ImpactData> impactDataIterator =  refactoringService.getRefactoringsFromCommits(versionControlService.getRepository(),commitSha).iterator();
        while(impactDataIterator.hasNext()){
            ImpactData impactData = impactDataIterator.next();
            impactData.setRepository(repository);
            getMetricsForRefactoring(impactData, 60, () -> {
                if (impactData.getMeasureData() != null){
                    output.saveImpactData(impactData);
                }
                impactDataIterator.remove();
            });
        }
        closeConnections();
    }

    @Override
    public void mineBetweenTags(String repository, String startTag, String endTag, List<String> refactoringTypes,List<String> metricTypes) {
        versionControlService = new GitHubLocalService(repository);
        logger.info("Mining from repository: " + repository + " between tags: (" + startTag + ") (" + endTag + ")");
        refactoringService.setSelectedRefactoringTypes(refactoringTypes);
        measuringService.setMetricsToConsider(metricTypes);
        List<String> commitShas = versionControlService.getAllCommitsBetweenTags(startTag,endTag);
        Iterator<ImpactData> impactDataIterator = refactoringService.getRefactoringsFromCommits(versionControlService.getRepository(),commitShas.toArray(new String[commitShas.size()])).iterator();
        commitShas.clear();
        while(impactDataIterator.hasNext()){
            ImpactData impactData = impactDataIterator.next();
            impactData.setRepository(repository);
            impactData.setRelease(startTag + "-" + endTag);
            getMetricsForRefactoring(impactData, 60, () -> {
                if (impactData.getMeasureData() != null){
                    output.saveImpactData(impactData);
                }
                impactDataIterator.remove();
            });
        }
        closeConnections();
    }

    @Override
    public void mineFromRelease(String repository, String releaseTag, List<String> refactoringTypes, List<String> metricTypes) {
        versionControlService = new GitHubLocalService(repository);
        logger.info("Mining from repository: " + repository + " release: " + releaseTag);
        refactoringService.setSelectedRefactoringTypes(refactoringTypes);
        measuringService.setMetricsToConsider(metricTypes);
        List<String> commitShas = versionControlService.getAllReleaseCommits(releaseTag);
        Iterator<ImpactData> impactDataIterator;
        if (commitShas == null || commitShas.size() == 0){
            logger.info("No commits found. Mining stopped.");
        }
        else {
            impactDataIterator = refactoringService.getRefactoringsFromCommits(versionControlService.getRepository(), commitShas.toArray(new String[commitShas.size()])).iterator();
            commitShas.clear();
            while(impactDataIterator.hasNext()){
                ImpactData impactData = impactDataIterator.next();
                impactData.setRepository(repository);
                impactData.setRelease(releaseTag);
                getMetricsForRefactoring(impactData, 60, () -> {
                    if (impactData.getMeasureData() != null){
                        output.saveImpactData(impactData);
                    }
                    impactDataIterator.remove();
                });
            }
        }
        closeConnections();
    }

    @Override
    public void mineFromAllCommits(String repository, List<String> refactoringTypes,List<String> metricTypes) {
        versionControlService = new GitHubLocalService(repository);
        logger.info("Mining from repository: " + repository + " - All commits");
        refactoringService.setSelectedRefactoringTypes(refactoringTypes);
        measuringService.setMetricsToConsider(metricTypes);
        List<String> commitShas = versionControlService.getAllCommits();
        Iterator<ImpactData> impactDataIterator = refactoringService.getRefactoringsFromCommits(versionControlService.getRepository(),commitShas.toArray(new String[commitShas.size()])).iterator();
        commitShas.clear();
        while(impactDataIterator.hasNext()){
            ImpactData impactData = impactDataIterator.next();
            impactData.setRepository(repository);
            getMetricsForRefactoring(impactData, 60, () -> {
                if (impactData.getMeasureData() != null){
                    output.saveImpactData(impactData);
                }
                impactDataIterator.remove();
            });
        }
        closeConnections();
    }

    private void getMetricsForRefactoring(ImpactData impactData, int timeout, MiningHandler miningHandler){
        ExecutorService service = Executors.newSingleThreadExecutor();
        Future<?> f = null;
        try {
            Runnable r = () -> getMetricsForAffectedPackages(impactData);
            f = service.submit(r);
            service.submit(miningHandler::handler);
            f.get(timeout, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            logger.info("Impact data for " + impactData.getCommit() + ": " + impactData.getRefactoringData().getName() + " Ignored due to timeout while calculating the metrics.");
            impactData.setMeasureData(null);
        } catch (Exception e) {
            logger.error("Error while getting metrics for refactoring in commit " + impactData.getCommit());
            logger.error(e.getMessage());
            impactData.setMeasureData(null);
        } finally {
            f.cancel(true);
            service.shutdown();
        }
    }

    private void getMetricsForAffectedPackages(ImpactData impactData){
        logger.info("Calculating metrics for: " + impactData.getRefactoringData().getName() + " in commit: " + impactData.getCommit());
        List<MeasureData> measureDataList = new ArrayList<>();
        List<MetricMeasure> metricMeasureListBefore = new ArrayList<>();
        List<MetricMeasure> metricMeasureListAfter = new ArrayList<>();
        Set<String> affectedPkgs = new HashSet<>();
        for (String affectedPackage : impactData.getRefactoringData().getAffectedPackages()) {
            affectedPkgs.add(versionControlService.getProjectPath() + "/" + affectedPackage);
        }
        versionControlService.checkoutCommit(versionControlService.getParentCommitSHA(impactData.getCommit()));
        if (Thread.interrupted()) {
            impactData.setMeasureData(null);
        }
        for(String affectedPackage: affectedPkgs){
            metricMeasureListBefore.addAll(measuringService.getMetrics(affectedPackage));
        }
        versionControlService.checkoutCommit(impactData.getCommit());
        if (Thread.interrupted()) {
            impactData.setMeasureData(null);
        }
        for(String affectedPackage: affectedPkgs){
            metricMeasureListAfter.addAll(measuringService.getMetrics(affectedPackage));
        }
        measureDataList.addAll(measuringService.getMetricsChange(metricMeasureListBefore, metricMeasureListAfter));

        if (Thread.interrupted()) {
            impactData.setMeasureData(null);
        }
        //filter out classes that are not affected - no need to get their metrics.
        measureDataList = measureDataList.stream().filter(d ->
                (!d.getMeasuredCodeStructure().toLowerCase().equals("class"))
                        || (d.getMeasuredCodeStructure().toLowerCase().equals("class")
                        && impactData.getRefactoringData().getAffectedClasses().stream().map(p -> p.getLeft().replace(".java","").substring(1)).collect(Collectors.toList()).contains(d.getMeasuredCodeStructureName())))
                .collect(Collectors.toList());
        impactData.setMeasureData(measureDataList);

    }


    private void closeConnections(){
        versionControlService.close();
    }


}
