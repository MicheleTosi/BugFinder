package it.uniroma2.tosi.entities;

public class AcumeEntry {

    private int id;
    private double size;
    private double predictedProbability;
    private boolean actualValue;

    public AcumeEntry(int id,double size, double predictedProbability, boolean actualValue){
        this.id=id;
        this.size=size;
        this.predictedProbability=predictedProbability;
        this.actualValue=actualValue;
    }


    public int getId() {
        return id;
    }


    public double getSize() {
        return size;
    }

    public void setSize(double size) {
        this.size = size;
    }

    public double getPredictedProbability() {
        return predictedProbability;
    }

    public void setPredictedProbability(double predictedProbability) {
        this.predictedProbability = predictedProbability;
    }

    public boolean isActualValue() {
        return actualValue;
    }

    public String getActualStringValue() {
        return actualValue ? "Yes":"No";
    }

    public void setActualValue(boolean actualValue) {
        this.actualValue = actualValue;
    }
}

