package org.auto_tool;

import impactMining.IImpactMiningService;
import impactMining.ImpactMiningService;
import measuring.MeasuringService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jasome.api.JasomeMetricsScannerImpl;
import refactoring.RefactoringService;
import versioncontrol.GitHubLocalService;

import java.util.*;
import java.util.concurrent.Semaphore;


public class AutoTool
{
    private static final Logger logger = LogManager.getLogger(AutoTool.class);
    private static List<CLIOption> options;

    public static void main( String[] args )
    {

        options = generateOptions();
        LoggingSetup.createLogsFolder();

        AutoToolProperties props = new AutoToolProperties(RefactoringService.getAllAvailableRefactoringTypes(), MeasuringService.getAllAvailableMetrics());
        List<String> refactoringTypes = props.getRefactorings();
        List<String> metrics = props.getMetrics();

        if (refactoringTypes.size() == 0){
            logger.info("No refactoring types found in properties. All available types will be mined.");
            refactoringTypes = RefactoringService.getAllAvailableRefactoringTypes();
        }
        if (metrics.size() == 0){
            logger.info("No measuring found in properties. All available metrics will be mined.");
            metrics = MeasuringService.getAllAvailableMetrics();
        }


        if(args.length > 0 && args[0].equals("-all") && (args.length-1) == options.stream().filter(o -> o.command.equals(args[0])).findFirst().get().arguments.size()){
            getImpactInAll(args[1],refactoringTypes, metrics);
        }
        else if(args.length > 0 && args[0].equals("-c") && (args.length-1) == options.stream().filter(o -> o.command.equals(args[0])).findFirst().get().arguments.size()){
            getImpactAtCommit(args[1],args[2],refactoringTypes, metrics);
        }
        else if(args.length > 0 && args[0].equals("-r") && (args.length-1) == options.stream().filter(o -> o.command.equals(args[0])).findFirst().get().arguments.size()){
            getImpactInRelease(args[1],args[2],refactoringTypes, metrics);
        }
        else if(args.length > 0 && args[0].equals("-bt") && (args.length-1) == options.stream().filter(o -> o.command.equals(args[0])).findFirst().get().arguments.size()){
            getImpactBetweenTags(args[1],args[2],args[3],refactoringTypes, metrics);
        }
        else{
            System.out.println("Wrong command or number of arguments");
            printHelp();
        }
    }

    private static void getImpactAtCommit(String repository, String commit, List<String> refactoringTypes, List<String> metricTypes){
        IImpactMiningService impactMiningService = new ImpactMiningService();
        impactMiningService.mineAtCommit(repository,commit,refactoringTypes,metricTypes);
    }

    private static void getImpactBetweenTags(String repository, String startTag, String endTag, List<String> refactoringTypes, List<String> metricTypes){
        IImpactMiningService impactMiningService = new ImpactMiningService();
        impactMiningService.mineBetweenTags(repository,startTag,endTag,refactoringTypes,metricTypes);
    }

    private static void getImpactInRelease(String repository, String releaseTag, List<String> refactoringTypes, List<String> metricTypes){
        IImpactMiningService impactMiningService = new ImpactMiningService();
        impactMiningService.mineFromRelease(repository,releaseTag,refactoringTypes,metricTypes);
    }

    private static void getImpactInAll(String repository, List<String> refactoringTypes, List<String> metricTypes){
        IImpactMiningService impactMiningService = new ImpactMiningService();
        impactMiningService.mineFromAllCommits(repository,refactoringTypes,metricTypes);
    }

    private static List<CLIOption> generateOptions(){
        List<CLIOption> cliOptionList = new ArrayList<>();
        cliOptionList.add(new CLIOption("-all","mine all refactorings from provided repository", Arrays.asList("repository")));
        cliOptionList.add(new CLIOption("-c","mine refactorings from provided commit", Arrays.asList("repository","commitSHA")));
        cliOptionList.add(new CLIOption("-r","mine refactorings from provided release tag", Arrays.asList("repository","releaseTag")));
        cliOptionList.add(new CLIOption("-bt","mine refactorings between provided tags", Arrays.asList("repository","startTag","endTag")));
        return cliOptionList;
    }

    private static void printHelp(){
        System.out.println("Available commands: \n");
        for(CLIOption option:options){
            System.out.println(option.toString());
        }
        System.out.println("\nArguments examples:");
        System.out.println("<repository>                     [https://github.com/Swati4star/Images-to-PDF]");
        System.out.println("<releaseTag>/<startTag>/<endTag> [8.8.1]");
        System.out.println("<commitSHA>                      [d4bce13a443cf12da40a77c16c1e591f4f985b47]");
    }
}
