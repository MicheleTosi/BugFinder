package it.uniroma2.tosi.github;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class RetrieveCommits {

    public static List<RevCommit> retrieveCommits(String path){
        Iterable<RevCommit> commits;
        List<RevCommit> commitList=new ArrayList<>();
        try (Git git = Git.open((Path.of(path).toFile()))) {
            commits = git.log().all().call();
            //commitList=stream(commits.spliterator(),false).collect(toList());
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
//        List<String> javaFilesNames=new ArrayList<>();
//        try (Git git = new Git(repository)) {
//            if (commit.getParents().length == 0) {
//                return javaFilesNames; // il commit è la radice del repository, restituisce una lista vuota
//            }
//            ObjectId oldHead = commit.getParent(0).getId();
//            ObjectId head = commit.getId();
//            DiffFormatter formatter = new DiffFormatter(DisabledOutputStream.INSTANCE);
//            formatter.setRepository(git.getRepository());
//            formatter.setDiffComparator(RawTextComparator.DEFAULT);
//            formatter.setDetectRenames(true);
//            List<DiffEntry> diffs = formatter.scan(oldHead, head);
//            for (DiffEntry diff : diffs) {
//                if (diff.getNewPath().endsWith(".java") && !diff.getNewPath().contains("/test/")) {
//                    javaFilesNames.add(diff.getNewPath());
//                }
//            }
//        }
//        return javaFilesNames;
//    }
    /*

        List<JavaFile> javaFiles = new ArrayList<>();

        // Ottenere l'oggetto del commit
        RevWalk revWalk = new RevWalk(repository);

        // Creare un filtro per limitare la ricerca ai file Java
        TreeFilter filter = PathFilter.create("*.java");

        // Ottenere l'albero dei file associato al commit
        try (TreeWalk treeWalk = new TreeWalk(repository)) {
            treeWalk.addTree(commit.getTree());
            treeWalk.setRecursive(true);
            treeWalk.setFilter(filter);

            // Aggiungere i file Java modificati o aggiunti alla lista
            while (treeWalk.next()) {
                JavaFile javaFile=new JavaFile(treeWalk.getPathString());
                javaFiles.add(javaFile);
            }
        }

        // Chiudere il RevWalk per rilasciare le risorse
        revWalk.close();



        return javaFiles;
    }/*/

}
