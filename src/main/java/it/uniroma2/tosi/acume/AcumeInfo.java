package it.uniroma2.tosi.acume;

import it.uniroma2.tosi.entities.AcumeEntry;
import weka.classifiers.AbstractClassifier;
import weka.core.Instance;
import weka.core.Instances;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static it.uniroma2.tosi.utils.CSV.acumeCSV;
import static java.lang.System.exit;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;

public class AcumeInfo {

    private static final Logger logger = Logger.getLogger(AcumeInfo.class.getName());

    private static List<AcumeEntry> acumeInputList;
    private static String acumeScriptPath;
    private static String acumeOutputPath;

    private AcumeInfo() {
        throw new IllegalStateException("Utility class");
    }

    public static double getNpofb(String projectName, Instances testing, AbstractClassifier classifier) throws Exception{
        acumeInputList=new ArrayList<>();

        double npofb=0;

        int lastAttrIndex= testing.numAttributes()-1;

        for(int i=0;i< testing.numInstances();i++) {
            Instance currInstance = testing.get(i);

            double size = currInstance.value(0);
            double prediction = getPredictionPercForYesLabel(currInstance, classifier);
            boolean actual = currInstance.toString(lastAttrIndex).equals("Yes");

            AcumeEntry entry = new AcumeEntry(i, size, prediction, actual);

            acumeInputList.add(entry);

        }

        acumeCSV(projectName, acumeInputList);

        getFilePath();
        invokePythonAcume();

        npofb=readNpofb20FromCsv();

        return npofb;

    }

    private static double readNpofb20FromCsv() {

        String npofbValue="";

        try (BufferedReader reader = new BufferedReader(new FileReader(acumeOutputPath))) {
            // Leggi l'intestazione del file CSV
            String headerLine = reader.readLine();
            String[] headers = headerLine.split(",");

            // Trova l'indice della colonna "Npofb"
            int npofbIndex = -1;
            for (int i = 0; i < headers.length; i++) {
                if (headers[i].equals("Npofb20")) {
                    npofbIndex = i;
                    break;
                }
            }

            // Se non trovi la colonna "Npofb", esce
            if (npofbIndex == -1) {

                throw new RuntimeException();
            }

            // Leggi il resto del file CSV e stampa i valori della colonna "Npofb"
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                if (fields.length > npofbIndex) {
                    npofbValue = fields[npofbIndex];
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Colonna 'Npofb' non trovata nel file CSV.");
            exit(1);
        }

        if(npofbValue.isEmpty()){
            throw new RuntimeException();
        }

        return Double.parseDouble(npofbValue);
    }


    private static void invokePythonAcume() {
        try {
            // Costruire il comando per eseguire lo script Python
            String[] cmd = {
                    "python3", acumeScriptPath, "NPofB"
            };

            // Eseguire il comando
            ProcessBuilder pb = new ProcessBuilder(cmd);
            Process p = pb.start();

            // Leggere l'output dello script Python
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                logger.log(INFO,line);
            }

            in.close();

            // Leggi lo stream di errore dello script Python
            BufferedReader err = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            while ((line = err.readLine()) != null) {
                logger.log(SEVERE,line);
            }
            err.close();

            int exitCode = p.waitFor();
            if (exitCode != 0) {
                logger.log(SEVERE,"Errore nell'esecuzione dello script Python.");
            }
        } catch (IOException | InterruptedException e) {
            logger.log(SEVERE,"Errore nell'invocazione di ACUME");
            exit(1);
        }
    }

    //prende per l'istanza passata in input la percentuale che sia buggy
    private static double getPredictionPercForYesLabel(Instance inst, AbstractClassifier classifier) throws Exception {
        double[] predDist=classifier.distributionForInstance(inst);

        for(int i=0;i<predDist.length;i++){
            if (inst.classAttribute().value(i).equals("Yes")){
                return predDist[i];
            }
        }

        throw new Exception();
    }

    private static void getFilePath(){
        // Ottiene il percorso della directory corrente
        String currentDirectory = System.getProperty("user.dir");

        // Esempio di utilizzo del percorso corrente per eseguire uno script Python e leggere un file CSV
        acumeScriptPath = Paths.get(currentDirectory, "ACUME/main.py").toString();
        acumeOutputPath = Paths.get(currentDirectory, "ACUME/EAM_NEAM_output.csv").toString();

    }

}
