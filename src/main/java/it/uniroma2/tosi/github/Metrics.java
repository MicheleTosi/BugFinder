package it.uniroma2.tosi.github;

import it.uniroma2.tosi.entities.JavaFile;
import it.uniroma2.tosi.entities.Release;
import it.uniroma2.tosi.entities.Ticket;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.eclipse.jgit.diff.DiffEntry.ChangeType.DELETE;
import static org.eclipse.jgit.diff.DiffEntry.ChangeType.MODIFY;

public class Metrics {
    private static final String EXTENSION = ".java";
    private static final Logger logger = Logger.getLogger(Metrics.class.getName());

    private Metrics() {
        throw new IllegalStateException("Utility class");
    }

    public static void getMetrics(Release release, Repository repo, List<JavaFile> javaFiles) {

        List<RevCommit> commits = release.getCommits();
        try (RevWalk rw = new RevWalk(repo)){

            for (RevCommit commit : commits) {

                if (commit.getParentCount() == 0) {
                    continue;
                }
                RevCommit parent = rw.parseCommit(commit.getParent(0).getId());
                DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE);
                df.setRepository(repo);
                df.setDiffComparator(RawTextComparator.DEFAULT);
                df.setDetectRenames(true);
                List<DiffEntry> diffs;
                diffs = df.scan(parent.getTree(), commit.getTree());
                int chgSet = getChangeSetSize(diffs);


                for (DiffEntry diff : diffs) {
                    int addedLines=0;
                    int deletedLines=0;
                    if (diff.getNewPath().contains(EXTENSION)) {
                        // Ottenere i cambiamenti di codice sorgente tra i due commit
                        String sourceCodeChanges = diffEntryToPatch(repo, diff);
                        // Dividi i cambiamenti in righe e controlla ogni riga
                        String[] lines = sourceCodeChanges.split("\r?\n");

                        for (String line : lines) {
                            addedLines += countAddedLines(line);
                            deletedLines += countDeletedLines(line);
                        }

                        calculateMetrics(diff, javaFiles, commit, chgSet, addedLines, deletedLines);
                    }
                }


                setFileLOC(repo, commit, javaFiles);

            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error during metrics calculus");
        }

    }

    private static int countDeletedLines(String line) {
        int deletedLines=0;
        String trimmedLine=line.substring(1).trim();
        if (line.startsWith("-") && !line.startsWith("---") && (!(trimmedLine.startsWith("*")
                || trimmedLine.startsWith("/*") ||
                trimmedLine.isEmpty() ||
                trimmedLine.startsWith("//")))) {
            // La riga aggiunta inizia con "*" o "/*"
            deletedLines++;
        }
        return deletedLines;
    }

    private static int countAddedLines(String line) {
        int addedLines=0;
        String trimmedLine=line.substring(1).trim();
        if (line.startsWith("+") && !line.startsWith("+++") && (!(trimmedLine.startsWith("*")
                || trimmedLine.startsWith("/*") ||
                trimmedLine.isEmpty() ||
                trimmedLine.startsWith("//")))) {
            // La riga aggiunta inizia con "*" o "/*"
            addedLines++;

        }
        return addedLines;
    }

    private static String diffEntryToPatch(Repository repo, DiffEntry diff) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (DiffFormatter df = new DiffFormatter(outputStream)) {
            df.setRepository(repo);
            df.format(diff);
        } // Il DiffFormatter viene chiuso automaticamente alla fine del blocco try-with-resources
        outputStream.flush();
        return outputStream.toString(StandardCharsets.UTF_8);
    }

    private static void calculateMetrics(DiffEntry diff, List<JavaFile> javaFiles, RevCommit commit, int chgSet,
                                         int addedLines, int deletedLines) {
        for (JavaFile javaFile : javaFiles) {
            if (Objects.equals(javaFile.getName(), diff.getNewPath())) {
                javaFile.setLocAdded(javaFile.getLocAdded() + addedLines);
                javaFile.setChurn(javaFile.getChurn() + addedLines-deletedLines);
                javaFile.getLocAddedList().add(addedLines);
                javaFile.getChurnList().add(addedLines-deletedLines);

                if (!javaFile.getAuthors().contains(commit.getAuthorIdent().getEmailAddress())) {
                    javaFile.getAuthors().add(commit.getAuthorIdent().getEmailAddress());
                }
                javaFile.setNumR(javaFile.getNumR() + 1);
                javaFile.getChgSet().add(chgSet);
            }
        }

    }

    private static int getChangeSetSize(List<DiffEntry> diffs) {
        int chgSet=0;
        for (DiffEntry diff : diffs) {
            if (diff.getNewPath().contains(EXTENSION)) {
                chgSet++;
            }
        }
        return chgSet;
    }

    private static void setFileLOC(Repository repo, RevCommit commit, List<JavaFile> javaFiles) throws IOException {

        RevTree tree = commit.getTree();

        for (JavaFile javaFile : javaFiles) {
            String filePath = javaFile.getName();

            TreeWalk treeWalk = new TreeWalk(repo);
            treeWalk.addTree(tree);
            treeWalk.setRecursive(true);
            treeWalk.setFilter(PathFilter.create(filePath));

            if (!treeWalk.next()) {
                continue;
            }

            ObjectId objectId = treeWalk.getObjectId(0);
            ObjectLoader loader = repo.open(objectId);
            try(BufferedReader reader = new BufferedReader(new InputStreamReader(loader.openStream()))) {

                String line;
                int numLines = 0;
                while ((line = reader.readLine()) != null) {
                    String trimmedLine=line.trim();
                    if (!trimmedLine.isEmpty() && !(trimmedLine.startsWith("/*") ||
                            trimmedLine.startsWith("*") || trimmedLine.startsWith("//"))) {
                        numLines++;
                    }
                }
                javaFile.setLoc(numLines);
            }
        }

    }

    public static void checkBuggyness(List<Release> releaseList, List<Ticket> ticketList, String path) throws IOException {
        //buggy definition: classi appartenenti all'insieme [injectedVersion,fixedVersion)
        for (Ticket ticket : ticketList) //prendo elemento ticket appartenente a ticketList
        {
            List<Integer> av = ticket.getAV();
            for (RevCommit commit : ticket.getCommits()) //prendo i commit dalla lista di commit di quel ticket
            {
                List<DiffEntry> diffs = getDiffs(commit, path); //usato per differenze. Rappresenta singolo cambiamento ad un file (add remove modify).
                if (diffs != null) {
                    analyzeDiffEntryBug(diffs, releaseList, av);
                }
            }
        }

    }

    public static List<DiffEntry> getDiffs(RevCommit commit, String path) throws IOException {
        List<DiffEntry> diffs = null;

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

        //SETTING DI DIFF FORMATTER
        DiffFormatter diff = new DiffFormatter(DisabledOutputStream.INSTANCE);                                              //dove viene collocato il diff code. DisableOutput per throws exceptions.
        diff.setRepository(repository);                                                                                    //repo sorgente che mantiene gli oggetti referenziati.
        diff.setContext(0);                                                                                                //param = linee di codice da vedere prima della prima modifica e dopo l'ultima.
        diff.setDetectRenames(true);                                                                                       // prima del rename devo usare setRepository, dopo posso ottenere l'istanza da getRenameDetector.

        if (commit.getParentCount() != 0) //il commit ha un parente, vedo la differenza tra i due
        {
            RevCommit parent = (RevCommit) commit.getParent(0).getId(); //prendo id parent
            diffs = diff.scan(parent.getTree(), commit.getTree()); //differenze tra alberi. E' del tipo DiffEntry[ADD/MODIFY/... pathfile]

        }


        return diffs;
    }


    public static void analyzeDiffEntryBug(List<DiffEntry> diffs, List<Release> releasesList, List<Integer> av)
    {

        for (DiffEntry diff : diffs)
        {
            DiffEntry.ChangeType type = diff.getChangeType(); //prendo i cambiamenti

            if (diff.toString().contains(EXTENSION) && (type.equals(MODIFY) /*|| type.equals(DELETE)*/))
            {

                /*Check BUGGY, releaseCommit è contenuta in affectedVersion? se si file relase è buggy.
                // se affectedVersion vuota -> faccio nulla, file già buggyness = nO.
                ELSE: file buggy se release commit appartiene a affectedVersion del ticket. Quindi prendo nome file, la release dalla lista, e setto buggy. */

                String file;
                if (diff.getChangeType() == DELETE)
                {
                    file = diff.getOldPath(); //file modificato
                }
                else
                { //MODIFY
                    file = diff.getNewPath();
                }

                setBuggyness(file,releasesList, av);

            }
        }
    }

    public static void setBuggyness(String file, List<Release> releasesList, List<Integer> av) {
        for (Release release : releasesList)
        {
            for (JavaFile javaFile : release.getJavaFiles())

            {
                if (    (javaFile.getName().equals(file)) && (av.contains((release.getIndex())))    ) {
                    javaFile.setBuggy("Yes");
                }
            }

        }
    }
}

