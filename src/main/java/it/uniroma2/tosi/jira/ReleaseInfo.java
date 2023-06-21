package it.uniroma2.tosi.jira;

import it.uniroma2.tosi.entities.JavaFile;
import it.uniroma2.tosi.entities.Release;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static it.uniroma2.tosi.github.Metrics.getMetrics;
import static it.uniroma2.tosi.github.RetrieveCommits.getJavaFilesChangedInCommit;
import static it.uniroma2.tosi.utils.Json.readJsonFromUrl;


public class ReleaseInfo {

    private static Map<LocalDateTime, String> releaseNames;
    private static Map<LocalDateTime, String> releaseID;
    private static ArrayList<LocalDateTime> releasesDate;
    private static final Logger logger = Logger.getLogger(ReleaseInfo.class.getName());

    private ReleaseInfo() {
        throw new IllegalStateException("Utility class");
    }

    public static List<Release> getReleaseInfo(String projName) throws IOException, JSONException {

        //Fills the arraylist with releases dates and orders them
        //Ignores releases with missing dates
        List<Release> releaseList=new ArrayList<>();
        releasesDate = new ArrayList<>();
        int i;
        String url = "https://issues.apache.org/jira/rest/api/2/project/" + projName;
        JSONObject json = readJsonFromUrl(url);
        JSONArray versions = json.getJSONArray("versions");
        releaseNames = new HashMap<>();
        releaseID = new HashMap<> ();
        for (i = 0; i < versions.length(); i++ ) {
            String name = "";
            String id = "";
            if(versions.getJSONObject(i).has("releaseDate")
                    && versions.getJSONObject(i).get("released").equals(true)) {
                if (versions.getJSONObject(i).has("name"))
                    name = versions.getJSONObject(i).get("name").toString();
                if (versions.getJSONObject(i).has("id"))
                    id = versions.getJSONObject(i).get("id").toString();
                addRelease(versions.getJSONObject(i).get("releaseDate").toString(),
                        name,id);
            }
        }
        // order releases by date
        //@Override
        releasesDate.sort(Comparator.naturalOrder());

        int index=1;
        for (LocalDateTime localDateTime : releasesDate) {
            int id=Integer.parseInt(releaseID.get(localDateTime));
            Release release = new Release(
                    id,
                    localDateTime,
                    releaseNames.get(localDateTime));
            release.setIndex(index);
            releaseList.add(release);
            index=index+1;
        }

        return releaseList;
    }

    public static void deleteHalfRelease(List<Release> releaseList){
        int releasesSize=releaseList.size();
        releaseList.removeIf(release -> release.getIndex() > releasesSize / 2);
    }


    public static void addRelease(String strDate, String name, String id) {
        LocalDate date = LocalDate.parse(strDate);
        LocalDateTime dateTime = date.atStartOfDay();
        if (!releasesDate.contains(dateTime))
            releasesDate.add(dateTime);
        releaseNames.put(dateTime, name);
        releaseID.put(dateTime, id);
    }


    public static void linkCommits2Release(List<RevCommit> commits, List<Release> releases, String path) {

        getRepository(path);

        for (RevCommit commit : commits) {
            PersonIdent author = commit.getAuthorIdent();
            LocalDateTime commitDate = author.getWhen().toInstant().atZone(ZoneId.of("UTC")).toLocalDateTime();
            for (Release release : releases) {
                if (commitDate.compareTo(release.getReleaseDate()) < 0) {
                    release.getCommits().add(commit);
                    break;
                }
            }
        }
    }

    public static void linkFiles2Release(List<Release> releases, String path){
        List<String> filesNames;
        Repository repository=getRepository(path);

        for (Release release: releases) {
            filesNames=new ArrayList<>();
            for(RevCommit commit: release.getCommits()) {
                try {
                    for(String name: getJavaFilesChangedInCommit(commit, repository)){
                        if(!filesNames.contains(name)) {
                            filesNames.add(name);
                        }
                    }
                } catch (Exception e) {
                    logger.log(Level.SEVERE, e.getMessage());
                    e.printStackTrace();
                }
            }
            for(String name: filesNames){
                release.getJavaFiles().add(new JavaFile(name));
            }
            getMetrics(release, repository, release.getJavaFiles());

        }
    }

    private static Repository getRepository(String path){
        Path gitDir = Path.of(path);
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        Repository repository=null;
        try {
            repository = builder
                    .setGitDir(gitDir.toFile())
                    .readEnvironment() // Leggere le configurazioni dall'ambiente
                    .build();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "error");
        }
        return repository;
    }
}
