package org.auto_tool;

import java.io.File;

public class LoggingSetup {

    protected static void createLogsFolder(){
        File dir = new File("logs");
        if (!dir.exists()) {dir.mkdir();}
    }
}
