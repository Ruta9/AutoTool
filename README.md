# AutoTool
### Lietuviškai ([English version](https://github.com/Ruta9/AutoTool#english)):
Įrankis skirtas automatiškai rasti kodo pertvarkymus GitHub saugykloje patalpinto projekto istorijoje ir paskaičiuoti metrikas prieš ir po kodo pertvarkymo pritaikymo.

### Naudojamos bibliotekos:
1. [RefactoringMiner](https://github.com/tsantalis/RefactoringMiner)
2. [JaSoMe](https://github.com/rodhilton/jasome)

### Randami kodo pertvarkymai:
Įrankis gali rasti visus RefactoringMiner 2.0 atpažįstamus kodo pertvarkymo tipus, tačiau pilnai ištestuoti yra šie kodo pertvarkymo tipai:
- Extract Method
- Inline Method
- Pull Up Field
- Pull Up Method
- Push Down Field
- Push Down Method

### Skaičiuojamos metrikos:
Įrankis gali apskaičiuoti visas JaSoMe siūlomas metrikas, tačiau pilnai ištestuotos yra šios klasės lygyje skaičiuojamos metrikos:
- AHF
- MHF
- AIF
- MIF
- WMC
- TLOC
- RFC
- LCOM*

### Kaip paleisti?
#### 1. Jar
Įrankis naudoja Maven projekto valdymo įrankį. Norint surinkti ir gauti .jar failą, reikia paleisti komandą ```mvn clean compile assembly:single```. Tuomet kataloge ```target/lib``` turėtų atsirasti AutoTool.jar formato failas, paleidžiamas komanda ```java -jar AutoTool.jar```.

Per pirmąjį paleidimą įrankis sugeneruoja ```refactorings.properties``` ir ```measuring.properties``` failus. Jei šių failų nėra arba jie nėra pakeičiami naudotojo, ieškoma visų kodo pertvarkymo tipų ir skaičiuojamos visos galimos metrikos.
Norint tam tikrus kodo pertvarkymo tipus ar metrikas ignoruoti tereikia juos failuose ištrinti.

Paleidžiant įrankį reikia nustatyti vieną iš 4 analizės metodų. Galimos tokios komandos:
```
-all <repository>                     (bus analizuojami visi saugykloje esantys kodo pakeitimai)
-c <repository> <commitSHA>           (bus analizuojamas tik nurodytas kodo pakeitimas)
-r <repository> <releaseTag>          (bus analizuojami visi nurodytam projekto leidimui priklausantys kodo pakeitimai)
-bt <repository> <startTag> <endTag>  (bus analizuojami visi tarp leidimų atlikti kodo pakeitimai)

Argumentų pavyzdžiai:
<repository>                     [https://github.com/Swati4star/Images-to-PDF]
<releaseTag>/<startTag>/<endTag> [8.8.1]
<commitSHA>                      [d4bce13a443cf12da40a77c16c1e591f4f985b47]
```
#### 2. Maven priklausomybė
Pirmiausia reikia gauti AutoTool.jar failą (žr. [kaip paleisti - jar](https://github.com/Ruta9/AutoTool#1-jar)). Į projekto pom.xml failą reikia pridėti tokią priklausomybę:
```
        <dependency>
            <groupId>org.auto_tool</groupId>
            <artifactId>AutoTool</artifactId>
            <version>1.0</version>
        </dependency>
```
Ir galiausiai sugeneruotą AutoTool.jar failą reikia įdiegti į Maven lokalią saugyklą ([kaip tai padaryti](https://devcenter.heroku.com/articles/local-maven-dependencies))

Pavyzdys, kaip AutoTool įrankį naudoti savo projekte:
```
        String repositoryURL = "https://github.com/danilofes/refactoring-toy-example";
        List<String> refactoringTypes = RefactoringService.getAllAvailableRefactoringTypes(); // <- Visi įmanomi
        //List<String> metrics = MeasuringService.getAllAvailableMetrics(); <- taip pat visi įmanomi
        List<String> metrics = Arrays.asList("class.TLOC","class.Na","class.Ma"); // <- tiek norimus kodo pertvarkymo tipus, tiek metrikas galima ir nurodyti
        
        ImpactMiningService impactMiningService = new ImpactMiningService();
        impactMiningService.mineFromAllCommits(repositoryURL,refactoringTypes,metrics);
    
```
#### Išvestis
Surinkti duomenys bus išvesti į .csv formato failą, sugeneruotą įrankio paleidimo kataloge.




### English:
A tool which automatically mines refactorings and metrics' values before and after each found refactoring from commits history in GitHub

Libraries used:
1. [RefactoringMiner](https://github.com/tsantalis/RefactoringMiner)
2. [JaSoMe](https://github.com/rodhilton/jasome)

### Available refactorings:
AutoTool can mine all refactorings that are available in RefactoringMiner 2.0. However, only these are fully tested with AutoTool:
- Extract Method
- Inline Method
- Pull Up Field
- Pull Up Method
- Push Down Field
- Push Down Method

### Available metrics:
AutoTool can calculate all metrics from JaSoMe. However, only these class-level metrics are fully tested with AutoTool:
- AHF
- MHF
- AIF
- MIF
- WMC
- TLOC
- RFC
- LCOM*

### How to run?
#### 1. As a jar
AutoTool uses Maven. To build a jar, run ```mvn clean compile assembly:single```. Inside ```target/lib``` directory AutoTool.jar should have been generated. It can be run by executing ```java -jar AutoToo.jar```.

On the first run, AutoTool generates ```refactorings.properties``` and ```measuring.properties```. If these files are not found by the tool, or once generated they haven't been changed by the user, all of the available refactorings will be mined and all of the available metrics will be calculated.
In order to ignore some refactorings or metrics, simply remove them from the .properties files.

Available arguments for running AutoTool:
```
-all <repository>                     (mine all refactorings from provided repository)
-c <repository> <commitSHA>           (mine refactorings from provided commit)
-r <repository> <releaseTag>          (mine refactorings from provided release tag)
-bt <repository> <startTag> <endTag>  (mine refactorings between provided tags)

Arguments examples:
<repository>                     [https://github.com/Swati4star/Images-to-PDF]
<releaseTag>/<startTag>/<endTag> [8.8.1]
<commitSHA>                      [d4bce13a443cf12da40a77c16c1e591f4f985b47]
```
#### 2. As Maven dependency
First you need to generate the jar ([Instructions](https://github.com/Ruta9/AutoTool#1-jar)). Then add below dependency to pom.xml:
```
        <dependency>
            <groupId>org.auto_tool</groupId>
            <artifactId>AutoTool</artifactId>
            <version>1.0</version>
        </dependency>
```
Then install the generated AutoTool.jar to local Maven repository ([An article on how to do it](https://devcenter.heroku.com/articles/local-maven-dependencies))

Example on how to use AutoTool in your project:
```
        String repositoryURL = "https://github.com/danilofes/refactoring-toy-example";
        List<String> refactoringTypes = RefactoringService.getAllAvailableRefactoringTypes(); // <- All available refactorings
        //List<String> metrics = MeasuringService.getAllAvailableMetrics(); <- All available metrics
        List<String> metrics = Arrays.asList("class.TLOC","class.Na","class.Ma"); // <- you can also specify wanted refactoring types and metrics
        
        ImpactMiningService impactMiningService = new ImpactMiningService();
        impactMiningService.mineFromAllCommits(repositoryURL,refactoringTypes,metrics);
    
```
#### Output
AutoTool generates .csv file in the directory it is launched in.
