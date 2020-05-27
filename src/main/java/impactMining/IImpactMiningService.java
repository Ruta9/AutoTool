package impactMining;

import java.util.List;

public interface IImpactMiningService {

    void mineAtCommit(String repository, String commitSha, List<String> refactoringTypes, List<String> metricTypes);
    void mineBetweenTags(String repository, String startTag, String endTag, List<String> refactoringTypes, List<String> metricTypes);
    void mineFromRelease(String repository, String releaseTag, List<String> refactoringTypes, List<String> metricTypes);
    void mineFromAllCommits(String repository, List<String> refactoringTypes, List<String> metricTypes);

}
