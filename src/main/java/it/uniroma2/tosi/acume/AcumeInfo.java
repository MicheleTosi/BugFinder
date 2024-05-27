package it.uniroma2.tosi.acume;

import it.uniroma2.tosi.entities.AcumeEntry;
import weka.classifiers.AbstractClassifier;
import weka.core.Instance;
import weka.core.Instances;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static it.uniroma2.tosi.utils.CSV.acumeCSV;

public class AcumeInfo {

    private static List<AcumeEntry> acumeInputList;
    private static String acumeScriptPath;
    private static String acumeOutputPath;

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
                System.err.println("Colonna 'Npofb' non trovata nel file CSV.");
                throw new RuntimeException();
            }

            // Leggi il resto del file CSV e stampa i valori della colonna "Npofb"
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                if (fields.length > npofbIndex) {
                    npofbValue = fields[npofbIndex];
                    System.out.println("Valore Npofb: " + npofbValue);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
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
            System.out.println("attesa rpocesso");

            // Leggere l'output dello script Python
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }

            in.close();

            // Leggi lo stream di errore dello script Python
            BufferedReader err = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            while ((line = err.readLine()) != null) {
                System.err.println(line);
            }
            err.close();

            int exitCode = p.waitFor();
            if (exitCode == 0) {
                System.out.println("Script Python eseguito con successo.");
            } else {
                System.out.println("Errore nell'esecuzione dello script Python. Codice di uscita: " + exitCode);
                return; // Esce dal programma se c'è un errore
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    //prende per l'istanza passata in input la percentuale che sia buggy
    private static double getPredictionPercForYesLabel(Instance inst, AbstractClassifier classifier) throws Exception {
        double[] predDist=classifier.distributionForInstance(inst);

        System.out.println("Funzione invocata prediction perc, "+ Arrays.toString(predDist));

        System.out.println(inst.value(0));
        System.out.println(inst.toString(inst.numAttributes()-1).equals("Yes"));

        for(int i=0;i<predDist.length;i++){
            if (inst.classAttribute().value(i).equals("Yes")){
                System.out.println(predDist[i]);
                System.out.println("Entrato");
                return predDist[i];
            }
        }

        throw new Exception();
    }

    private static void getFilePath(){
        // Ottiene il percorso della directory corrente
        String currentDirectory = System.getProperty("user.dir");
        System.out.println("Current working directory: " + currentDirectory);

        // Esempio di utilizzo del percorso corrente per eseguire uno script Python e leggere un file CSV
        acumeScriptPath = Paths.get(currentDirectory, "ACUME/main.py").toString();
        acumeOutputPath = Paths.get(currentDirectory, "ACUME/EAM_NEAM_output.csv").toString();

        System.out.println("Script path: " + acumeScriptPath);
        System.out.println("CSV file path: " + acumeOutputPath);

    }

}
