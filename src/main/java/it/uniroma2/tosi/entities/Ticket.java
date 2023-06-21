package it.uniroma2.tosi.entities;

import org.eclipse.jgit.revwalk.RevCommit;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Ticket {

    private Integer FV;
    private List<Integer> AV;
    private Integer IV;
    private String id;
    private LocalDateTime openingDate;
    private Integer OV;
    private LocalDateTime resolutionDate;
    private List<RevCommit> commits=new ArrayList<>();

    public Ticket(String id, LocalDateTime openingDate, Integer IV) {
        this.id=id;
        this.openingDate=openingDate;
        this.IV=IV;
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

    public void setAV(List<Integer> AV) {
        this.AV = AV;
    }

    public List<Integer> getAV() {
        return AV;
    }

    public void setOV(Integer OV) {
        this.OV = OV;
    }

    public Integer getOV() {
        return OV;
    }

    public Integer getIV() {
        return this.IV;
    }

    public void setFV(Integer fv) {
        this.FV = fv;
    }

    public Integer getFV() {
        return FV;
    }

    public void setIV(Integer IV) {
        this.IV = IV;
    }
}
