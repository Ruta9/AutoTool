package versioncontrol;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jgit.lib.Repository;

import java.util.List;

public interface IVersionControlService {

    List<String> getAllCommitsBetweenTags(String startTag, String endTag);
    List<String> getAllCommits();
    List<String> getAllReleaseCommits(String tag);
    String getParentCommitSHA(String commit);
    void checkoutCommit(String commit);
    void close();
    Repository getRepository();
    String getProjectPath();
}
