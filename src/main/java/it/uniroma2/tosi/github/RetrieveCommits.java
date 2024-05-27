package it.uniroma2.tosi.github;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.TreeWalk;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class RetrieveCommits {

    private RetrieveCommits(){
        throw new IllegalStateException("RetrieveCommits class");
    }

    public static List<RevCommit> retrieveCommits(String path){
        Iterable<RevCommit> commits;
        List<RevCommit> commitList=new ArrayList<>();
        try (Git git = Git.open((Path.of(path).toFile()))) {
            commits = git.log().all().call();
            for (RevCommit commit : commits) {
                commitList.add(commit);
            }
        } catch (IOException | GitAPIException e) {
            throw new RuntimeException(e);
        }
        return commitList;
    }



    public static List<String> getJavaFilesChangedInCommit(RevCommit commit, Repository repository) throws Exception {

        List<String> javaFilesNames = new ArrayList<>();
        TreeWalk treeWalk = new TreeWalk(repository);
        treeWalk.addTree(commit.getTree());
        treeWalk.setRecursive(true);
        while (treeWalk.next()) {
            if (treeWalk.getPathString().endsWith(".java") && !treeWalk.getPathString().contains("/test/")) {
                javaFilesNames.add(treeWalk.getPathString());
            }
        }
        return javaFilesNames;
    }

}
