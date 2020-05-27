package output;

import datastructure.ImpactData;
import datastructure.MeasureData;
import datastructure.RefactoringData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.auto_tool.AutoToolProperties;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class CSVOutput implements IOutput{

    private static final Logger logger = LogManager.getLogger(AutoToolProperties.class.getName());
    private FileWriter csvWriter;
    private String header;

    public CSVOutput(){
        header = ImpactData.getHeaders() + "," + RefactoringData.getHeaders() + ", " + MeasureData.getHeaders() + "\r\n";
        try {
            csvWriter = new FileWriter((new SimpleDateFormat("yyyy-MM-dd_hhmmss").format(new Date())) + "impactResults.csv");
            csvWriter.append(header);
            csvWriter.flush();
        } catch (IOException ex) {
            logger.error("Failed to create results file");
            logger.error(ex.getMessage());
        }
    }

    public void saveImpactData(List<ImpactData> impactDataList){
        logger.info("saving impact data to file...");
        try {
            for(ImpactData impactData:impactDataList) {
                for (MeasureData measureData: impactData.getMeasureData()){
                    csvWriter.write(impactData.toCSV() + "," + impactData.getRefactoringData().toCSV() + "," + measureData.toCSV() + "\r\n");
                }
                csvWriter.flush();
            }
        } catch (IOException ex) {
            logger.error("Failed to write the output");
            logger.error(ex.getMessage());
        }
        logger.info("impact data saved");
    }

    public void saveImpactData(ImpactData impactData){
        try {
            for (MeasureData measureData: impactData.getMeasureData()){
                csvWriter.write(impactData.toCSV() + "," + impactData.getRefactoringData().toCSV() + "," + measureData.toCSV() + "\r\n");
            }
            csvWriter.flush();
        } catch (IOException ex) {
            logger.error("Failed to write the output for: " + impactData.getCommit());
            logger.error(ex.getMessage());
        }
    }

}
