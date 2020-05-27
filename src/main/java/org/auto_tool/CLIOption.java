package org.auto_tool;

import java.util.List;

class CLIOption {

    String command;
    String description;
    List<String> arguments;

    CLIOption(String command, String description, List<String> arguments) {
        this.command = command;
        this.description = description;
        this.arguments = arguments;
    }

    public String toString(){
        String cmd = command + " ";
        for(String s:arguments){
            cmd = cmd + "<"+s+"> ";
        }
        cmd = cmd + " (" + description + ") ";
        return cmd;
    }
}
