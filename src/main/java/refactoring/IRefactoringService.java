package refactoring;

import datastructure.ImpactData;
import org.eclipse.jgit.lib.Repository;

import java.util.List;

public interface IRefactoringService {

    void setSelectedRefactoringTypes(List<String> refactoringTypes);
    List<ImpactData> getRefactoringsFromCommits(Repository repository, String ... commitShas);
}
