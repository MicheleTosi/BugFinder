package it.uniroma2.tosi.entities;

import java.util.ArrayList;
import java.util.List;

public class JavaFile {
    private final ArrayList<Integer> chgSet;
    private String name;
    private int locAdded;
    private List<Integer> locAddedList;
    private int churn;
    private List<Integer> churnList;
    private int loc;
    private List<String> authors;
    private int numR;
    private String buggy;

    public JavaFile(String name){
        this.name=name;
        this.locAdded=0;
        this.churn=0;
        this.locAddedList= new ArrayList<>();
        this.loc=0;
        this.authors=new ArrayList<>();
        this.numR=0;
        this.chgSet=new ArrayList<>();
        this.churnList=new ArrayList<>();
        this.buggy="No";
    }

    public int getLocAdded() {
        return locAdded;
    }

    public void setLocAdded(int locAdded) {
        this.locAdded = locAdded;
    }

    public int getChurn() {
        return churn;
    }

    public void setChurn(int churn) {
        this.churn = churn;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public List<Integer> getLocAddedList() {
        return locAddedList;
    }

    public void setLocAddedList(List<Integer> locAddedList) {
        this.locAddedList = locAddedList;
    }

    public int getLoc() {
        return loc;
    }

    public void setLoc(int loc) {
        this.loc = loc;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public void setAuthors(List<String> authors) {
        this.authors = authors;
    }

    public int getNumR() {
        return numR;
    }

    public void setNumR(int numR) {
        this.numR = numR;
    }

    public List<Integer> getChgSet() {
        return chgSet;
    }

    public List<Integer> getChurnList() {
        return churnList;
    }

    public void setChurnList(List<Integer> churnList) {
        this.churnList = churnList;
    }

    public void setBuggy(String buggy) {
        this.buggy=buggy;
    }

    public String getBuggy() {
        return this.buggy;
    }
}
