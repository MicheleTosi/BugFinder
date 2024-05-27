package it.uniroma2.tosi.jira;

import it.uniroma2.tosi.entities.Release;
import it.uniroma2.tosi.entities.Ticket;
import static it.uniroma2.tosi.jira.ReleaseInfo.getReleaseInfo;
import static it.uniroma2.tosi.utils.Json.readJsonFromUrl;
import static java.util.stream.IntStream.range;
import org.eclipse.jgit.revwalk.RevCommit;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class RetrieveTickets {

    private static final String FIELDS="fields";
    
    private RetrieveTickets() {
        throw new IllegalStateException("Utility class");
    }
    
    public static List<Ticket> getTickets(String projName) throws JSONException, IOException {
        int j;
        int issuesNum;
        int i=0;
        List<Ticket> tickets =new ArrayList<>();
        List<Release> releases = getReleaseInfo(projName);
        HashMap<Integer, Release> releasesId=new HashMap<>();
        for (Release release : releases){
            releasesId.put(release.getId(), release);
        }
        //Get JSON API for closed bugs w/ affectedVersion in the project
        do {
            //Only gets a max of 1000 at a time, so must do this multiple times if bugs >1000
            j = i + 1000;
            String url = "https://issues.apache.org/jira/rest/api/2/search?jql=project=%22"
                    + projName + "%22AND%22issueType%22=%22Bug%22AND(%22status%22=%22closed%22OR"
                    + "%22status%22=%22resolved%22)AND%22resolution%22=%22fixed%22&fields=key," +
                    "resolutiondate,versions,created&startAt=" + i + "&maxResults=" + j;
            JSONObject json = readJsonFromUrl(url);
            JSONArray issues = json.getJSONArray("issues");
            issuesNum = json.getInt("total");
            for (; i < issuesNum && i < j; i++) {
                //Iterate through each bug
                JSONObject issue = issues.getJSONObject(i%1000);
                String key = issue.get("key").toString();
                LocalDateTime openingDate=LocalDateTime.parse(issue.getJSONObject(FIELDS)
                        .getString("created").substring(0,16));
                JSONArray affectedVersion = issue.getJSONObject(FIELDS).getJSONArray("versions");
                Integer iv=null;
                if(affectedVersion.length()>0){
                    iv=affectedVersion.getJSONObject(0).getInt("id");
                    if(releasesId.containsKey(iv)) {
                        iv = releasesId.get(iv).getIndex(); //se injectedVersion è l'indice va bene, se è l'ID commentare questa riga
                    }else{
                        continue;
                    }
                }
                Ticket ticket=new Ticket(key, openingDate, iv);
                LocalDateTime resolutionDate=LocalDateTime.parse(issue.getJSONObject(FIELDS)
                                .get("resolutiondate").toString().substring(0,16));
                ticket.setResolutionDate(resolutionDate);
                tickets.add(ticket);
            }
        } while (i < issuesNum);


        for (Ticket ticket :
             tickets) {
            ticket.setOV(getOpeningVersion(ticket, releases));
            ticket.setFV(getfixedVersion(ticket, releases));
        }

        return tickets;
    }

    public static void linkCommits2Ticket(List<RevCommit> commits, List<Ticket> tickets){
        for (Ticket ticket: tickets) {
            for (RevCommit commit: commits) {
                if (commit.getShortMessage().matches(".*\\b" + Pattern.quote(ticket.getId()) + "\\b.*")){
                    ticket.getCommits().add(commit);
                }
            }
        }
    }

    private static Integer getOpeningVersion(Ticket ticket, List<Release> releases) {
        for (Release release : releases){
            if(ticket.getOpeningDate().compareTo(release.getReleaseDate())<0){
                return release.getIndex();
            }
        }
        return null;
    }

    private static Integer getfixedVersion(Ticket ticket, List<Release> releases){
        for (Release release : releases){
            if(ticket.getResolutionDate().compareTo(release.getReleaseDate())<0){
                return release.getIndex();
            }
        }
        return null;
    }

    public static void removeInconsistentTickets(List<Ticket> ticketList){
        Iterator<Ticket> tickets= ticketList.iterator();
        while(tickets.hasNext()){
            Ticket ticket=tickets.next();
            Integer iv=ticket.getIV();
            Integer ov=ticket.getOV();
            Integer fv=ticket.getFV();

            if(fv==null || ov==null || ov>fv){
                tickets.remove();
                continue;
            }

            if(iv!=null && iv>ov){
                ticket.setIV(null);
                iv=null;
            }

            if(Objects.equals(iv, fv)){
                tickets.remove();
            }
        }
    }

    public static void setTicketsAV(List<Ticket> ticketList){
        for (Ticket ticket: ticketList) {
            ticket.setAV(range(ticket.getIV(), ticket.getFV()).boxed().collect(Collectors.toList()));
        }
    }

    public static void removeTicketsWithoutCommits(List<Ticket> tickets){
        tickets.removeIf(ticket -> ticket.getCommits().isEmpty());
    }

}


