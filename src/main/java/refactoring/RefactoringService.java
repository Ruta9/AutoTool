package refactoring;

import datastructure.ImpactData;
import datastructure.RefactoringData;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jgit.lib.Repository;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringHandler;
import org.refactoringminer.api.RefactoringType;
import org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl;

import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

public class RefactoringService implements IRefactoringService{

    private static final Logger logger = LogManager.getLogger(RefactoringService.class);
    private GitHistoryRefactoringMinerImpl refactoringMiner;

    public RefactoringService(){
        refactoringMiner = new GitHistoryRefactoringMinerImpl();
    }

    public static List<String> getAllAvailableRefactoringTypes() {
        List<String> availableRefactoringTypes = new LinkedList<>();
        for (RefactoringType refactoringType: RefactoringType.ALL) {
            availableRefactoringTypes.add(refactoringType.getDisplayName().replace(' ', '_'));
        }
        return availableRefactoringTypes;
    }

    @Override
    public void setSelectedRefactoringTypes(List<String> refactoringTypes){
        List<RefactoringType> selectedRefactoringTypes = new ArrayList<>();
        for(String refactoringType:refactoringTypes){
            selectedRefactoringTypes.add(RefactoringType.fromName(refactoringType.replace("_"," ")));
        }
        refactoringMiner.setRefactoringTypesToConsider(selectedRefactoringTypes.toArray(new RefactoringType[selectedRefactoringTypes.size()]));
        logger.info("Refactorings set for mining:");
        refactoringTypes.forEach(logger::info);
    }

    @Override
    public List<ImpactData> getRefactoringsFromCommits(Repository repository, String ... commitShas){
        logger.info("Refactorings detection started");
        List<ImpactData> impactDataList = new ArrayList<>();
        for(String sha:commitShas) {
                refactoringMiner.detectAtCommit(repository, sha, new RefactoringHandler() {
                    @Override
                    public void handle(String commitId, List<Refactoring> refactorings) {
                        refactorings = removeRefactoringsIfAffectTheSameClasses(refactorings);
                        for (Refactoring ref : refactorings) {
                            List<RefactoringData> refactoringDataList = createRefactoringData(ref);
                            for(RefactoringData d: refactoringDataList){
                                ImpactData impactData = new ImpactData();
                                impactData.setRefactoringData(d);
                                impactData.setCommit(sha);
                                impactDataList.add(impactData);
                            }
                        }
                    }
                },30);
        }
        logger.info("Refactorings detection finished");
        logger.info("Total " + impactDataList.size() + " refactorings detected");
        return impactDataList;
    }

    private List<Refactoring> removeRefactoringsIfAffectTheSameClasses(List<Refactoring> refactoringList){
        List<Refactoring> filteredRefactorings = new ArrayList<>();
        List<String> affectedClasses = getAllInvolvedClasses(refactoringList);
        Map<String, Integer> frequencyMap= new HashMap<>();
        for (String c: affectedClasses){
            frequencyMap.put(c,Collections.frequency(affectedClasses,c));
        }
        affectedClasses = frequencyMap.entrySet().stream().filter(entry -> entry.getValue() > 1).map(Map.Entry::getKey).collect(Collectors.toList());
        for (Refactoring r: refactoringList){
            if (!refactoringContainsDuplicateAffectedClass(r, affectedClasses)) filteredRefactorings.add(r);
        }
        return filteredRefactorings;
    }

    private List<String> getAllInvolvedClasses(List<Refactoring> refactoringList){
        List<String> involvedClasses = new ArrayList<>();
        for (Refactoring r: refactoringList){
            for (Pair<String,String> c:r.getInvolvedClassesBeforeRefactoring()){
                involvedClasses.add(c.getLeft());
            }
            for (Pair<String,String> c:r.getInvolvedClassesAfterRefactoring()){
                involvedClasses.add(c.getLeft());
            }
        }
        return involvedClasses;
    }

    private boolean refactoringContainsDuplicateAffectedClass(Refactoring r, List<String> affectedClasses){
        for (Pair<String,String> c:r.getInvolvedClassesBeforeRefactoring()){
            if (affectedClasses.contains(c.getLeft())) return true;
        }
        for (Pair<String,String> c:r.getInvolvedClassesAfterRefactoring()){
            if (affectedClasses.contains(c.getLeft())) return true;
        }
        return false;
    }

    private List<RefactoringData> createRefactoringData(Refactoring ref){
        List<RefactoringData> refactoringDataList = new ArrayList<>();
        List<String> affectedClassesBefore = new ArrayList<>();
        List<String> affectedClassesAfter = new ArrayList<>();
        Set<String> affectedPackagesBefore = getRefactoringAffectedPackages(ref,true);
        Set<String> affectedPackagesAfter = getRefactoringAffectedPackages(ref,false);

        List<Pair<String,String>> affectedClasses = new ArrayList<>();
        ref.getInvolvedClassesBeforeRefactoring().forEach(pair -> {
            String classPath = pair.getLeft();
            String className = classPath.substring(classPath.lastIndexOf("/"));
            affectedClassesBefore.add(className);
            affectedClasses.add(new ImmutablePair<>(className,classPath));
            }
        );
        refactoringDataList.add(new RefactoringData(ref.getName(),ref.toString(),affectedPackagesBefore,affectedClasses));
        affectedClasses.clear();
        ref.getInvolvedClassesAfterRefactoring().forEach(pair -> {
                    String classPath = pair.getLeft();
                    String className = classPath.substring(classPath.lastIndexOf("/"));
                    affectedClassesAfter.add(className);
                    affectedClasses.add(new ImmutablePair<>(className,classPath));
                }
        );
        affectedClassesBefore.removeAll(affectedClassesAfter);
        if (affectedClassesBefore.size() == 0) return refactoringDataList;
        refactoringDataList.add(new RefactoringData(ref.getName()+"_AFTER",ref.toString(),affectedPackagesAfter,affectedClasses));
        return refactoringDataList;

    }

    private Set<String> getRefactoringAffectedPackages(Refactoring r, boolean before){
        Set<String> affectedPackages = new HashSet<>();
        if (before){
            for (Pair<String,String> c: r.getInvolvedClassesBeforeRefactoring()){
                int slashIndex = c.getLeft().lastIndexOf("/");
                affectedPackages.add(slashIndex == -1 ? c.getLeft() : c.getLeft().substring(0,slashIndex));
            }
        }
        else {
            for (Pair<String,String> c: r.getInvolvedClassesAfterRefactoring()){
                int slashIndex = c.getLeft().lastIndexOf("/");
                affectedPackages.add(slashIndex == -1 ? c.getLeft() : c.getLeft().substring(0,slashIndex));
            }
        }
        return affectedPackages;
    }

}
