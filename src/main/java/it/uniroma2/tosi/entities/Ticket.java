package it.uniroma2.tosi.entities;

import org.eclipse.jgit.revwalk.RevCommit;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Ticket {

    private Integer fixedVersion;
    private List<Integer> affectedVersion;
    private Integer injectedVersion;
    private String id;
    private LocalDateTime openingDate;
    private Integer openingVersion;
    private LocalDateTime resolutionDate;
    private List<RevCommit> commits=new ArrayList<>();

    public Ticket(String id, LocalDateTime openingDate, Integer injectedVersion) {
        this.id=id;
        this.openingDate=openingDate;
        this.injectedVersion=injectedVersion;
    }

    public void setCommits(List<RevCommit> commits) {
        this.commits = commits;
    }

    public List<RevCommit> getCommits() {
        return commits;
    }

    public void setResolutionDate(LocalDateTime resolutionDate) {
        this.resolutionDate = resolutionDate;
    }

    public LocalDateTime getResolutionDate() {
        return resolutionDate;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setOpeningDate(LocalDateTime openingDate) {
        this.openingDate = openingDate;
    }

    public LocalDateTime getOpeningDate() {
        return openingDate;
    }

    public void setAV(List<Integer> affectedVersion) {
        this.affectedVersion = affectedVersion;
    }

    public List<Integer> getAV() {
        return affectedVersion;
    }

    public void setOV(Integer openingVersion) {
        this.openingVersion = openingVersion;
    }

    public Integer getOV() {
        return openingVersion;
    }

    public Integer getIV() {
        return this.injectedVersion;
    }

    public void setFV(Integer fv) {
        this.fixedVersion = fv;
    }

    public Integer getFV() {
        return fixedVersion;
    }

    public void setIV(Integer injectedVersion) {
        this.injectedVersion = injectedVersion;
    }
}
