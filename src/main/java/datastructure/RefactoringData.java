package datastructure;

import org.apache.commons.lang3.tuple.Pair;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RefactoringData {

    private final String name;
    private final String information;
    private final Set<String> affectedPackages; //Path
    private final List<Pair<String,String>> affectedClasses; //Names and Paths

    public RefactoringData(String name, String information, Set<String> affectedPackages, List<Pair<String, String>> affectedClasses) {
        this.name = name;
        this.information = information;
        this.affectedPackages = affectedPackages;
        this.affectedClasses = List.copyOf(affectedClasses);
    }

    public String toString(){
        String str = name + " ||" + information + "|| \n";
        str = str + "    Affected packages:\n";
        for(String pkg:affectedPackages){
            str = str + "            " + pkg + "\n";
        }
        str = str + "    Affected Classes:\n";
        for(Pair<String,String> classes:affectedClasses){
            str = str + "            " + classes.getRight() + "\n";
        }
        return str;
    }

    public String toCSV(){
        return name + "," + information.replace(",","|");
    }

    public static String getHeaders() {
        return "Refactoring name" + "," + "Refactoring information";
    }

    public String getName() {
        return name;
    }

    public String getInformation() {
        return information;
    }

    public Set<String> getAffectedPackages() {
        return affectedPackages;
    }

    public List<Pair<String, String>> getAffectedClasses() {
        return affectedClasses;
    }

    public Set<String> getAffectedPackagesRoots(){
        Set<String> roots = new HashSet<>();
        for (String pkg:affectedPackages){
            roots.add(pkg.substring(0,pkg.indexOf("/")));
        }
        return roots;
    }
}
