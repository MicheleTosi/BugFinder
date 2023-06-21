package it.uniroma2.tosi.control;

import it.uniroma2.tosi.entities.ClassifierEvaluation;
import it.uniroma2.tosi.entities.Release;
import it.uniroma2.tosi.entities.Ticket;
import it.uniroma2.tosi.utils.CSV;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static it.uniroma2.tosi.github.Metrics.checkBuggyness;
import static it.uniroma2.tosi.github.RetrieveCommits.retrieveCommits;
import static it.uniroma2.tosi.jira.ReleaseInfo.*;
import static it.uniroma2.tosi.jira.RetrieveTickets.*;
import static it.uniroma2.tosi.utils.CalculateProportion.proportion;
import static it.uniroma2.tosi.weka.WekaInfo.retrieveWekaInfo;

public class ExecutionFlow {

    private static final Logger logger = Logger.getLogger(ExecutionFlow.class.getName());

    private ExecutionFlow() {
        throw new IllegalStateException("Utility class");
    }

    public static void evaluate(String projName){
        try {
            File currentDirectory = new File(System.getProperty("user.dir"));
            String parentDirectoryPath = currentDirectory.getParent();
            String path = parentDirectoryPath+File.separator+ projName.toLowerCase() + "/.git";
            //il progetto da analizzare sta nella stessa cartella del progetto corrente

            List<Release> releases = getReleaseInfo(projName); //ottengo la lista delle release
            List<RevCommit> commits = retrieveCommits(path);//ottengo la lista dei commit relativo al progetto

            List<Ticket> tickets = getTickets(projName); //ottengo la lista dei tickets da jira

            linkCommits2Ticket(commits, tickets); //lego i commit ai ticket il cui ID compare nel loro commento
            removeTicketsWithoutCommits(tickets); //rimuovo i ticket senza commit (se non ci sono commit che li
            //risolvono è come se il problema descritto non fosse mai esistito)

            removeInconsistentTickets(tickets); //rimuovo i ticket che presentano inconsistenze
            deleteHalfRelease(releases); //cancello la metà delle release per eliminare lo snoring presente nelle ultime

            //PROPORTION
            Collections.reverse(tickets); //inverto l'ordine dei tickets perché per la proportion
            //con MovingWindow mi serve lavorare sui ticket in ordine temporale
            proportion(tickets); //calcolo la proportion
            removeInconsistentTickets(tickets); //rimuovo possibili incongruenze nei ticket che possono essersi formate
            setTicketsAV(tickets); //genero la lista degli IV

            linkCommits2Release(commits, releases, path); //lego commit e release in modo da poter capire quali sono i
            //file modificati in ogni release

            linkFiles2Release(releases, path); //lego i file modificati alla release di interesse

            //per fare walk forward devo fare un ciclo e utilizzare le prime release come training
            //e quella successiva come testing (training parte dei ticket, testing tutti i ticket)
            for (int i = 1; i < releases.size(); i++) {
                List<Release> releaseList = new ArrayList<>(); //lista delle release che fanno parte del training set
                List<Ticket> ticketList = new ArrayList<>(); //lista dei ticket che fanno parte del training set
                for (Release release : releases) {
                    if (release.getIndex() <= i) { //al passo 'i' fanno parte del training set tutte le release fino
                        //alla i-esima compresa
                        releaseList.add(release);
                    }
                }
                for (Ticket ticket : tickets) {
                    if (ticket.getFV() <= i) {
                        ticketList.add(ticket);
                    }
                }
                checkBuggyness(releaseList, ticketList, path);
                CSV.buginessCSVCreator(i, projName, releaseList);

                //testing set
                checkBuggyness(releases.subList(i, i + 1), tickets, path);
                CSV.buginessCSVCreator(i + 100, projName, releases.subList(i, i + 1));
            }

            //calcolo i valori di precision recall ecc. con weka
            List<ClassifierEvaluation> classifierEvaluations = retrieveWekaInfo(projName, releases.size());
            CSV.evaluationCsv(projName, classifierEvaluations);
        }catch(Exception e){
            logger.log(Level.SEVERE, e.getMessage());
            e.printStackTrace();
        }
    }

}
