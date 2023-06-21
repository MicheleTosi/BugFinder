package it.uniroma2.tosi.entities;

import org.eclipse.jgit.revwalk.RevCommit;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Release {

    private String releaseName;
    private LocalDateTime releaseDate;
    private int id;
    private int index;
    private List<RevCommit> commits=new ArrayList<>();
    private List<JavaFile> javaFiles=new ArrayList<>();

    public Release(int id, LocalDateTime releaseDate, String releaseName){
        this.id=id;
        this.releaseDate=releaseDate;
        this.releaseName=releaseName;
    }

    public List<RevCommit> getCommits() {
        return commits;
    }

    public void setCommits(List<RevCommit> commits) {
        this.commits = commits;
    }

    public int getIndex() {
        return index;
    }

    public LocalDateTime getReleaseDate() {
        return releaseDate;
    }

    public String getReleaseName() {
        return releaseName;
    }

    public void setReleaseName(String releaseName){
        this.releaseName=releaseName;
    }

    public void setIndex(int index){
        this.index=index;
    }

    public void setReleaseDate(LocalDateTime releaseDate){
        this.releaseDate=releaseDate;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public List<JavaFile> getJavaFiles() {
        return javaFiles;
    }

    public void setJavaFiles(List<JavaFile> javaFiles) {
        this.javaFiles = javaFiles;
    }
}
