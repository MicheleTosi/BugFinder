package it.uniroma2.tosi.utils;


import static java.lang.System.exit;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CSV2Arff {
    private static final java.util.logging.Logger logger = Logger.getLogger(CSV2Arff.class.getName());

    private CSV2Arff() {
        throw new IllegalStateException("Utility class");
    }

    public static String csv2Arff(String fileName) {

        String arffName=null;

        try {
            String outName =fileName.substring(0, fileName.length() - 3) + "arff";

            Path path = Paths.get(outName);

            //load CSV
            CSVLoader loader = new CSVLoader();
            loader.setSource(new File(fileName));
            Instances data = loader.getDataSet();

            data.deleteAttributeAt(1);
            data.deleteAttributeAt(0);

            ArffSaver saver = new ArffSaver();
            saver.setInstances(data);
            saver.setFile(new File(outName));
            saver.setDestination(new File(outName));
            saver.writeBatch();

            List<String> lines = Files.readAllLines(path);
            for (String line : lines) {
                if (line.contains("@attribute BUGGYNESS")) {
                    lines.set(lines.indexOf(line), "@attribute BUGGYNESS {Yes,No}");
                }
            }
            Files.write(path, lines, StandardCharsets.UTF_8);
            arffName=path.toString();

            File csv=new File(fileName);
            Files.delete(csv.toPath());

        }catch(IOException e){
            logger.log(Level.SEVERE,"Errore nella conversione del file");
            exit(0);
        }
        return arffName;
    }

}
