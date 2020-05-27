package org.auto_tool;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.*;

public class AutoToolProperties {

    private static final Logger logger = LogManager.getLogger(AutoToolProperties.class.getName());
    private List<String> refactoringTypes;
    private List<String> metricTypes;

    public AutoToolProperties(List<String> refactoringTypes, List<String> metricTypes){
        this.refactoringTypes = refactoringTypes;
        this.metricTypes = metricTypes;
        checkProperties();
    }

    private void checkProperties(){
        checkRefactoringsProperties();
        checkMetricsProperties();
    }

    private void checkRefactoringsProperties(){
        try (InputStream input = new FileInputStream("refactorings.properties")) {
            new Properties().load(input);
        }
        catch (FileNotFoundException ex) {
            logger.info("refactorings.properties not found. Generating .properties file. All of the refactoring types will be included in the mining");
            generateRefactoringsProperties();
        }
        catch (IOException ex) {
            logger.info("Failed to read refactorings.properties");
            logger.error(ex.getStackTrace().toString());
            throw new AutoToolsPropertiesException("Failed to read refactorings.properties");
        }
    }

    private void checkMetricsProperties(){
        try (InputStream input = new FileInputStream("measuring.properties")) {
            new Properties().load(input);
        }
        catch (FileNotFoundException ex) {
            logger.info("measuring.properties not found. Generating .properties file. All of the measuring will be included in the mining");
            generateMetricsProperties();
        }
        catch (IOException ex) {
            logger.info("Failed to read measuring.properties");
            logger.error(ex.getStackTrace().toString());
            throw new AutoToolsPropertiesException("Failed to read measuring.properties");
        }
    }


    private void generateRefactoringsProperties(){
        logger.info("Creating new refactorings.properties");

        try (OutputStream output = new FileOutputStream("refactorings.properties")) {
            Properties props = new Properties();

            for(String refactoringType:refactoringTypes){
                props.setProperty("rminer." + refactoringType,"1");
            }

            props.store(output, null);
            logger.info("refactorings.properties created");

        } catch (IOException ex) {
            logger.info("Error creating refactorings.properties");
            logger.error(ex.getStackTrace().toString());
            throw new AutoToolsPropertiesException("Failed to create refactorings.properties");
        }
    }

    private void generateMetricsProperties(){
        logger.info("Creating new measuring.properties");

        try (OutputStream output = new FileOutputStream("measuring.properties")) {
            Properties props = new Properties();

            for(String metric:metricTypes){
                props.setProperty(metric,"1");
            }

            props.store(output, null);
            logger.info("measuring.properties created");

        } catch (IOException ex) {
            logger.info("Error creating measuring.properties");
            logger.error(ex.getStackTrace().toString());
            throw new AutoToolsPropertiesException("Failed to create measuring.properties");
        }
    }


    public List<String> getRefactorings(){
        List<String> selectedRefactorings = new ArrayList<>();
        Properties props = new Properties();
        try (InputStream input = new FileInputStream("refactorings.properties")) {
            props.load(input);
            for(String refactoringType:refactoringTypes){
                String propertyValue = props.getProperty("rminer." + refactoringType);
                if (propertyValue != null && propertyValue.equals("1")) selectedRefactorings.add(refactoringType);
            }
        }
        catch (IOException ex) {
            logger.info("Failed to read refactorings.properties");
            logger.error(ex.getStackTrace().toString());
            throw new AutoToolsPropertiesException("Failed to read refactorings.properties");
        }
        return selectedRefactorings;
    }

    public List<String> getMetrics(){
        List<String> selectedMetrics = new ArrayList<>();
        Properties props = new Properties();
        try (InputStream input = new FileInputStream("measuring.properties")) {
            props.load(input);
            for(String metric:metricTypes){
                String propertyValue = props.getProperty(metric);
                if (propertyValue != null && propertyValue.equals("1")) {
                    selectedMetrics.add(metric);
                }
            }
        }
        catch (IOException ex) {
            logger.info("Failed to read measuring.properties");
            logger.error(ex.getStackTrace().toString());
            throw new AutoToolsPropertiesException("Failed to read measuring.properties");
        }
        return selectedMetrics;
    }

}
