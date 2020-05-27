package versioncontrol;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.lib.*;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class GitHubLocalService implements IVersionControlService {

    private static final Logger logger = LogManager.getLogger(GitHubLocalService.class);
    private String projectPath = "temp/clonedProject";
    private Repository repository;
    private File clonedProject;
    private Git git;

    public GitHubLocalService(String cloneUrl){
        clone(cloneUrl);
    }

    private void clone(String cloneUrl) {
        clonedProject = new File(projectPath);
        logger.info("Cloning {} ...", cloneUrl);
        try {
            git = Git.cloneRepository()
                    .setDirectory(clonedProject)
                    .setURI(cloneUrl)
                    .call();
            repository = git.getRepository();
            logger.info("Done cloning {}, current branch is {}", cloneUrl, repository.getBranch());
        } catch (GitAPIException e) {
            logger.error("Error while trying to clone repository " + cloneUrl);
            logger.error(e.getMessage());
        } catch (IOException e) {
            logger.error("Error while trying to get current branch");
            logger.error(e.getMessage());
        } catch (JGitInternalException e){
            logger.error("Some repository is already cloned. Please delete it before proceeding.");
            logger.error(e.getMessage());
            throw new JGitInternalException(e.getMessage());
        }
    }

    @Override
    public Repository getRepository(){
        if (repository == null) {
            RepositoryBuilder builder = new RepositoryBuilder();
            try {
                repository = builder
                        .setGitDir(new File(projectPath, ".git"))
                        .readEnvironment()
                        .findGitDir()
                        .build();
            } catch (IOException e) {
                logger.error("Failed to load cloned repository");
                logger.error(e.getMessage());
            }
        }
        return repository;
    }

    @Override
    public List<String> getAllCommitsBetweenTags(String startTag, String endTag) {
        logger.info("Getting all commits between tags (" + startTag + ") - (" + endTag + ")");
        List<String> commitShas = null;
        try {
            commitShas = StreamSupport.stream(git.log().addRange(getTagCommitId(startTag), getTagCommitId(endTag)).call().spliterator(), false).filter(c -> c.getParentCount() > 0).map(AnyObjectId::getName).collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Failed to get all commits between tags (" + startTag + ") - (" + endTag + ")");
            logger.error(e.getMessage());
        }
        logger.info("Commits fetched: " + commitShas.size());
        return commitShas;
    }

    @Override
    public List<String> getAllCommits() {
        logger.info("Getting all repository commits...");
        List<String> commitShas = null;
        try {
            commitShas = StreamSupport.stream(git.log().call().spliterator(), false).filter(c -> c.getParentCount() > 0).map(AnyObjectId::getName).collect(Collectors.toList());
        } catch (GitAPIException e) {
            logger.error("Failed to get all repository commits");
            logger.error(e.getMessage());
        }
        logger.info("Commits fetched: " + commitShas.size());
        return commitShas;
    }

    @Override
    public List<String> getAllReleaseCommits(String tag) {
        logger.info("Getting all release (" + tag + ") commits...");
        List<String> commitShas = null;
        try {
            LogCommand log = git.log();
            log.add(getTagCommitId(tag));
            commitShas = StreamSupport.stream(log.call().spliterator(), false).filter(c -> c.getParentCount() > 0).map(AnyObjectId::getName).collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Failed to get all release (" + tag + ") commits");
            logger.error(e.getMessage());
        }
        logger.info("Commits fetched: " + commitShas.size());
        return commitShas;
    }


    @Override
    public String getParentCommitSHA(String commit) {
        try {
            return repository.parseCommit(repository.resolve(commit)).getParent(0).getId().getName();
        } catch (Exception e) {
            logger.error("Failed to get parent SHA1 for commit: " + commit);
            logger.error(e.getMessage());
        }
        return null;
    }

    @Override
    public void checkoutCommit(String commit){
        try {
            git.checkout().setName(commit).call();
        } catch (GitAPIException e) {
            logger.info("Failed to checkout commit: " + commit);
            logger.info("Metric results for this commit might be incorrect.");
        }
    }


    @Override
    public void close(){
        git.close();
        removeClonedProject();
    }

    @Override
    public String getProjectPath(){
        return projectPath;
    }

    private void removeClonedProject(){
        try {
            FileUtils.deleteDirectory(clonedProject);
        } catch (IOException e) {
            logger.info("Could not delete project directory.");
            logger.info(e.getMessage());
        }
    }

    private ObjectId getTagCommitId(String tag){
        ObjectId tagId = null;
        try {
            Ref ref = repository.findRef(tag);
            Ref peeledRef = repository.peel(ref);
            tagId = peeledRef.getPeeledObjectId() != null ? peeledRef.getPeeledObjectId() : ref.getObjectId();
        } catch (IOException e) {
            logger.error("Failed to get reference for tag: " + tag);
            logger.error(e.getMessage());
        }
        return tagId;
    }
}
