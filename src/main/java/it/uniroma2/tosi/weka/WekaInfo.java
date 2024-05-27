package it.uniroma2.tosi.weka;

import it.uniroma2.tosi.entities.ClassifierEvaluation;
import it.uniroma2.tosi.utils.CSV2Arff;
import weka.attributeSelection.CfsSubsetEval;
import weka.attributeSelection.GreedyStepwise;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.CostMatrix;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.lazy.IBk;
import weka.classifiers.meta.CostSensitiveClassifier;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.supervised.instance.SpreadSubsample;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static it.uniroma2.tosi.acume.AcumeInfo.getNpofb;

public class WekaInfo {

    private static final Logger logger = Logger.getLogger(WekaInfo.class.getName());

    private WekaInfo() {
        throw new IllegalStateException("Utility class");
    }

    public static List<ClassifierEvaluation> retrieveWekaInfo(String projName, int size) {

        List<ClassifierEvaluation> evaluationList=new ArrayList<>();

        //lista dei classificatori utilizzati
        Classifier[] classifiers = new Classifier[]{
                new RandomForest(),
                new NaiveBayes(),
                new IBk()
        };
        try {
            //itero su tutte le release
            for (int i = 1; i < size; i++) {
                int j = i + 100;
                //genero un file per il training set
                String fileName = projName + "_" + i + "_" + "BuginessInfo.csv";
                //genero un file per il testing set
                String fileName2 = projName + "_" + j + "_" + "BuginessInfo.csv";
                String path = Paths.get("output", fileName).toString();
                String path2 = Paths.get("output", fileName2).toString();
                //converto il file csv in formato arff da utilizzare come input per Weka
                String trainingPath=CSV2Arff.csv2Arff(path);
                String testingPath=CSV2Arff.csv2Arff(path2);

                ConverterUtils.DataSource source1 = new ConverterUtils.DataSource(trainingPath);
                ConverterUtils.DataSource source2 = new ConverterUtils.DataSource(testingPath);

                Files.delete(new File(trainingPath).toPath());
                Files.delete(new File(testingPath).toPath());

                Instances training = source1.getDataSet();
                Instances testing = source2.getDataSet();

                int numAttr = training.numAttributes();
                training.setClassIndex(numAttr - 1);
                testing.setClassIndex(numAttr - 1);

                Evaluation eval = new Evaluation(testing);

                for (Classifier classifier : classifiers) {
                    classifier.buildClassifier(training);
                    eval.evaluateModel(classifier, testing);

                    //SIMPLE CLASSIFIER
                    ClassifierEvaluation simpleClassifier = new ClassifierEvaluation(projName, i,
                            classifier.getClass().getSimpleName(), false, false, false);
                    simpleClassifier.setTrainingPercent(100.0 * training.numInstances() / (training.numInstances() + testing.numInstances()));
                    simpleClassifier.setPrecision(eval.precision(0));
                    simpleClassifier.setRecall(eval.recall(0));
                    simpleClassifier.setAuc(eval.areaUnderROC(0));
                    simpleClassifier.setKappa(eval.kappa());
                    simpleClassifier.setTp(eval.numTruePositives(0));
                    simpleClassifier.setFp(eval.numFalsePositives(0));
                    simpleClassifier.setTn(eval.numTrueNegatives(0));
                    simpleClassifier.setFn(eval.numFalseNegatives(0));
                    simpleClassifier.setNpofb(getNpofb(projName,testing, (AbstractClassifier) classifier));
                    evaluationList.add(simpleClassifier);

                    //VALIDATION WITH FEATURE SELECTION (GREEDY BACKWARD SEARCH) AND WITHOUT SAMPLING
                    CfsSubsetEval subsetEval = new CfsSubsetEval();
                    GreedyStepwise search = new GreedyStepwise();
                    search.setSearchBackwards(true);

                    AttributeSelection filter = new AttributeSelection();
                    filter.setEvaluator(subsetEval);
                    filter.setSearch(search);
                    filter.setInputFormat(training);

                    Instances filteredTraining = Filter.useFilter(training, filter);
                    Instances filteredTesting = Filter.useFilter(testing, filter);

                    int numAttrFiltered = filteredTraining.numAttributes();
                    filteredTraining.setClassIndex(numAttrFiltered - 1);

                    classifier.buildClassifier(filteredTraining);
                    eval.evaluateModel(classifier, filteredTesting);
                    ClassifierEvaluation featureSelClassifier = new ClassifierEvaluation(projName, i,
                            classifier.getClass().getSimpleName(), true, false, false);
                    featureSelClassifier.setTrainingPercent(100.0*filteredTraining.numInstances()/
                            (filteredTraining.numInstances()+filteredTesting.numInstances()));
                    featureSelClassifier.setPrecision(eval.precision(0));
                    featureSelClassifier.setRecall(eval.recall(0));
                    featureSelClassifier.setAuc(eval.areaUnderROC(0));
                    featureSelClassifier.setKappa(eval.kappa());
                    featureSelClassifier.setNpofb(getNpofb(projName,filteredTesting, (AbstractClassifier) classifier));
                    featureSelClassifier.setTp(eval.numTruePositives(0));
                    featureSelClassifier.setFp(eval.numFalsePositives(0));
                    featureSelClassifier.setTn(eval.numTrueNegatives(0));
                    featureSelClassifier.setFn(eval.numFalseNegatives(0));
                    evaluationList.add(featureSelClassifier);

                    //VALIDATION WITH FEATURE SELECTION (GREEDY BACKWARD SEARCH) AND WITH SAMPLING (UNDERSAMPLING)
                    SpreadSubsample spreadSubsample = new SpreadSubsample();
                    spreadSubsample.setInputFormat(filteredTraining);
                    spreadSubsample.setOptions(new String[] {"-M", "1.0"});

                    FilteredClassifier fc = new FilteredClassifier();
                    fc.setFilter(spreadSubsample);

                    fc.setClassifier(classifier);
                    fc.buildClassifier(filteredTraining);
                    eval.evaluateModel(fc, filteredTesting);
                    ClassifierEvaluation samplingClassifier = new ClassifierEvaluation(projName, i,
                            classifier.getClass().getSimpleName(), true, true, false);
                    samplingClassifier.setTrainingPercent(100.0*filteredTraining.numInstances()/
                            (filteredTraining.numInstances()+filteredTesting.numInstances()));
                    samplingClassifier.setPrecision(eval.precision(0));
                    samplingClassifier.setRecall(eval.recall(0));
                    samplingClassifier.setNpofb(getNpofb(projName,filteredTesting, (AbstractClassifier) classifier));
                    samplingClassifier.setAuc(eval.areaUnderROC(0));
                    samplingClassifier.setKappa(eval.kappa());
                    samplingClassifier.setTp(eval.numTruePositives(0));
                    samplingClassifier.setFp(eval.numFalsePositives(0));
                    samplingClassifier.setTn(eval.numTrueNegatives(0));
                    samplingClassifier.setFn(eval.numFalseNegatives(0));
                    evaluationList.add(samplingClassifier);

                    //VALIDATION WITH FEATURE SELECTION (GREEDY BACKWARD SEARCH) AND WITH SENSITIVE LEARNING (CFN = 10*CFP)
                    CostMatrix costMatrix = new CostMatrix(2);
                    costMatrix.setCell(0, 0, 0.0);
                    costMatrix.setCell(1, 0, 10.0);
                    costMatrix.setCell(0, 1, 1.0);
                    costMatrix.setCell(1, 1, 0.0);

                    CostSensitiveClassifier csc = new CostSensitiveClassifier();

                    csc.setClassifier(classifier);
                    csc.setCostMatrix(costMatrix);
                    csc.buildClassifier(filteredTraining);
                    eval.evaluateModel(csc, filteredTesting);
                    ClassifierEvaluation costSensClassifier = new ClassifierEvaluation(projName, i,
                            classifier.getClass().getSimpleName(), true, false, true);
                    costSensClassifier.setTrainingPercent(100.0*filteredTraining.numInstances()/(filteredTraining.numInstances()+filteredTesting.numInstances()));
                    costSensClassifier.setPrecision(eval.precision(0));
                    costSensClassifier.setRecall(eval.recall(0));
                    costSensClassifier.setAuc(eval.areaUnderROC(0));
                    costSensClassifier.setKappa(eval.kappa());
                    costSensClassifier.setTp(eval.numTruePositives(0));
                    costSensClassifier.setFp(eval.numFalsePositives(0));
                    costSensClassifier.setNpofb(getNpofb(projName,filteredTesting, (AbstractClassifier) classifier));
                    costSensClassifier.setTn(eval.numTrueNegatives(0));
                    costSensClassifier.setFn(eval.numFalseNegatives(0));
                    evaluationList.add(costSensClassifier);
                }

            }
        }catch(Exception e){
            logger.log(Level.INFO, e.getMessage());
            e.printStackTrace();
        }

        return evaluationList;

    }

}
