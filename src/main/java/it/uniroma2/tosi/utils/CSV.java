package it.uniroma2.tosi.utils;

import it.uniroma2.tosi.acume.AcumeInfo;
import it.uniroma2.tosi.entities.AcumeEntry;
import it.uniroma2.tosi.entities.ClassifierEvaluation;
import it.uniroma2.tosi.entities.JavaFile;
import it.uniroma2.tosi.entities.Release;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.System.exit;
import static java.util.Collections.max;

public class CSV {
    private static final Logger logger = Logger.getLogger(CSV.class.getName());

    private CSV() {
        throw new IllegalStateException("Utility class");
    }

    public static void releaseCSVCreator(String projName, List<LocalDateTime> releases,
                                         Map<LocalDateTime, String> releaseID,
                                         Map<LocalDateTime, String> releaseNames){

        String outName = projName + "ReleaseInfo.csv";
        //Name of CSV for output
        try (FileWriter fileWriter = new FileWriter(outName)){
            fileWriter.append("Index,Version ID,Version Name,Date");
            fileWriter.append("\n");
            int index=0;
            for (LocalDateTime localDateTime : releases) {
                index += 1;
                fileWriter.append(Integer.toString(index));
                fileWriter.append(",");
                fileWriter.append(releaseID.get(localDateTime));
                fileWriter.append(",");
                fileWriter.append(releaseNames.get(localDateTime));
                fileWriter.append(",");
                fileWriter.append(localDateTime.toString());
                fileWriter.append("\n");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE,"Error in CSV Release List writer");
            e.printStackTrace();
        }
    }

    public static void buginessCSVCreator(int numRun,String projName, List<Release> releases){
        File subfolder=new File("output");
        if(!subfolder.mkdirs() && !subfolder.exists()){
            logger.log(Level.SEVERE, "Errore nella creazione della cartella di output");
        }

        String outName = subfolder+File.separator+projName +"_"+numRun+"_BuginessInfo.csv";
        //Name of CSV for output
        try (FileWriter fileWriter = new FileWriter(outName)){
            fileWriter.append("Release,Filename,size,LOC_added,MAX_LOC_added,AVG_LOC_Added,CHURN,MAX_Churn," +
                    "AVG_Churn,NR,NAUTH,CHGSETSIZE,MAX_ChgSet,AVG_ChgSet,BUGGYNESS");
            fileWriter.append("\n");
            for (Release release: releases) {
                for (JavaFile javaFile : release.getJavaFiles()) {
                    printJavaFileInfo(fileWriter, release, javaFile);
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE,"Error in CSV Release List writer");
            e.printStackTrace();
        }
    }

    private static void printJavaFileInfo(FileWriter fileWriter, Release release, JavaFile javaFile) throws IOException {
        fileWriter.append(release.getReleaseName());
        fileWriter.append(",");
        fileWriter.append(javaFile.getName());
        fileWriter.append(",");
        fileWriter.append(String.valueOf(javaFile.getLoc()));
        fileWriter.append(",");
        fileWriter.append(String.valueOf(javaFile.getLocAdded()));
        fileWriter.append(",");
        if(javaFile.getLocAddedList().isEmpty()){
            fileWriter.append("0,0");
        }else{
            fileWriter.append(String.valueOf(max(javaFile.getLocAddedList())));
            fileWriter.append(",");
            fileWriter.append(String.valueOf((int) javaFile.getLocAddedList().stream()
                    .mapToInt(Integer::intValue)
                    .average().orElse(0.0)));
        }
        fileWriter.append(",");
        fileWriter.append(String.valueOf(javaFile.getChurn()));
        fileWriter.append(",");
        if(javaFile.getChurnList().isEmpty()){
            fileWriter.append("0,0");
        }else{
            fileWriter.append(String.valueOf(max(javaFile.getChurnList())));
            fileWriter.append(",");
            fileWriter.append(String.valueOf((int) javaFile.getChurnList().stream()
                    .mapToInt(Integer::intValue)
                    .average().orElse(0.0)));
        }
        fileWriter.append(",");
        fileWriter.append(String.valueOf(javaFile.getNumR()));
        fileWriter.append(",");
        fileWriter.append(String.valueOf(javaFile.getAuthors().size()));
        fileWriter.append(",");
        if(javaFile.getChgSet().isEmpty()){
            fileWriter.append("0,0,0");
        }else{
            fileWriter.append(String.valueOf(javaFile.getChgSet().stream().mapToInt(Integer::intValue).sum()));
            fileWriter.append(",");
            fileWriter.append(String.valueOf(max(javaFile.getChgSet())));
            fileWriter.append(",");
            fileWriter.append(String.valueOf((int) javaFile.getChgSet().stream()
                    .mapToInt(Integer::intValue)
                    .average().orElse(0.0)));
        }
        fileWriter.append(",");
        fileWriter.append(javaFile.getBuggy());

        fileWriter.append("\n");
    }

    public static void evaluationCsv(String projName, List<ClassifierEvaluation> classifierEvaluations) {
        File subfolder=new File("output");
        if(!subfolder.mkdirs() && !subfolder.exists()){
            logger.log(Level.SEVERE, "Errore nella creazione della cartella di output");
        }

        String outName = subfolder+File.separator+projName.toLowerCase() +"Evaluation.csv";
        try (FileWriter fileWriter = new FileWriter(outName)) {
            fileWriter.append("DATASET,TRAIN_RELEASES,%TRAIN_INSTANCES,CLASSIFIER,FEATURE_SELECTION,BALANCING," +
                    "COST_SENSITIVE,PRECISION,RECALL,AUC,KAPPA,TP,FP,TN,FN,NPOFB20\n");
            for (ClassifierEvaluation eval : classifierEvaluations) {
                fileWriter.append(projName);
                fileWriter.append(",");
                fileWriter.append(String.valueOf(eval.getWalkForwardIterationIndex()));
                fileWriter.append(",");
                fileWriter.append(String.valueOf(eval.getTrainingPercent()));
                fileWriter.append(",");
                fileWriter.append(eval.getClassifier());
                fileWriter.append(",");
                fileWriter.append(String.valueOf(eval.isFeatureSelection()));
                fileWriter.append(",");
                fileWriter.append(String.valueOf(eval.isSampling()));
                fileWriter.append(",");
                fileWriter.append(String.valueOf(eval.isCostSensitive()));
                fileWriter.append(",");
                fileWriter.append(String.valueOf(eval.getPrecision()));
                fileWriter.append(",");
                fileWriter.append(String.valueOf(eval.getRecall()));
                fileWriter.append(",");
                fileWriter.append(String.valueOf(eval.getAuc()));
                fileWriter.append(",");
                fileWriter.append(String.valueOf(eval.getKappa()));
                fileWriter.append(",");
                fileWriter.append(String.valueOf(eval.getTp()));
                fileWriter.append(",");
                fileWriter.append(String.valueOf(eval.getFp()));
                fileWriter.append(",");
                fileWriter.append(String.valueOf(eval.getTn()));
                fileWriter.append(",");
                fileWriter.append(String.valueOf(eval.getFn()));
                fileWriter.append(",");
                fileWriter.append(String.valueOf(eval.getNpofb()));
                fileWriter.append("\n");
            }
        }catch (Exception e){
            logger.log(Level.SEVERE, e.getMessage());
            exit(0);
        }
    }

    public static void acumeCSV(String projName, List<AcumeEntry> acumeEntries){
        File subfolder=new File("output/acumeFiles");
        if(!subfolder.mkdirs() && !subfolder.exists()){
            logger.log(Level.SEVERE, "Errore nella creazione della cartella di output");
        }

        String outName = subfolder+File.separator+"Acume.csv";
        try (FileWriter fileWriter = new FileWriter(outName)) {
            fileWriter.append("ID,Size,prob,actual\n");
            for(AcumeEntry acumeEntry:acumeEntries){
                fileWriter.append(String.valueOf(acumeEntry.getId()));
                fileWriter.append(",");
                fileWriter.append(String.valueOf(acumeEntry.getSize()));
                fileWriter.append(",");
                fileWriter.append(String.valueOf(acumeEntry.getPredictedProbability()));
                fileWriter.append(",");
                fileWriter.append(acumeEntry.getActualStringValue());
                fileWriter.append("\n");
            }
        }catch (Exception e){
            logger.log(Level.SEVERE, e.getMessage());
            exit(0);
        }
    }
}
