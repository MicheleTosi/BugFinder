package it.uniroma2.tosi.utils;

import it.uniroma2.tosi.entities.Ticket;
import it.uniroma2.tosi.jira.RetrieveTickets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class CalculateProportion {
    private static int movingWindowSize;
    private static double proportion;
    private static final List<Ticket> listTicketProportion=new ArrayList<>();

    private CalculateProportion() {
        throw new IllegalStateException("Utility class");
    }

    public static void proportion(List<Ticket> tickets) throws IOException {
        int numTickets=tickets.size();
        movingWindowSize=numTickets/100;
        proportion=0;
        for (Ticket ticket: tickets) {
            if (ticket.getIV() != null ) {
                addTicketToList(ticket);
            } else {
                if (listTicketProportion.size() < movingWindowSize) {
                    if (proportion==0) {
                        proportion = coldStartProportional();
                    }
                } else {
                    proportion = movingWindowProportional();
                }
                setTicketIv(ticket);
            }
        }
    }

    private static void setTicketIv(Ticket ticket) {
        int ov = ticket.getOV();
        int fv = ticket.getFV();
        if (fv == ov) {
            ticket.setIV((int) Math.floor(fv-proportion));
        }else {
            ticket.setIV((int) (Math.floor(fv - (fv - ov) * proportion)));
        }
        if (ticket.getIV()<=0) {
            ticket.setIV(1);
        }
    }

    public static double movingWindowProportional(){
        int index=0;
        proportion=0;
        for (Ticket t : listTicketProportion) {
            if(!Objects.equals(t.getFV(), t.getOV())) {
                proportion += (float) (t.getFV() - t.getIV()) / (t.getFV() - t.getOV());
            }else{
                proportion+=t.getFV() - t.getIV();
            }
            index++;

        }
        if (index!=0) {
            proportion = proportion / index;
        }

        return proportion;
    }

    public static double coldStartProportional() throws IOException {
        List<Ticket> tickets = new ArrayList<>();
        List<Double> proportions = new ArrayList<>();
        int index;
        double prop=0;
        for (ProjectsEnum proj : ProjectsEnum.values()) {
            index=0;
            tickets.clear();
            tickets = RetrieveTickets.getTickets(proj.name());
            for (Ticket ticket : tickets){
                if(ticket.getIV()!=null && ticket.getOV()!=null && ticket.getFV()!=null
                        && !Objects.equals(ticket.getOV(), ticket.getFV())){
                    if((float) (ticket.getFV()- ticket.getIV())/(ticket.getFV()- ticket.getOV())>1 ) {
                        prop += (float) (ticket.getFV() - ticket.getIV()) / (ticket.getFV() - ticket.getOV());
                    }else{
                        prop+=1;
                    }
                    index++;
                }
            }
            if(index!=0) {
                prop = prop / index;
                proportions.add(prop);
            }
        }


        proportions.sort(Comparator.naturalOrder());
        if(proportions.size()%2!=0){
            return proportions.get(proportions.size()/2);
        }
        return (proportions.get(proportions.size()/2)+proportions.get(proportions.size()/2+1))/2;
    }

    public static void addTicketToList(Ticket ticket) {
        if(ticket.getIV()!=null) {
            if (listTicketProportion.size() >= movingWindowSize) {
                listTicketProportion.remove(0);
            }
            listTicketProportion.add(ticket);
        }
    }
}
