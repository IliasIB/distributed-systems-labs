package session;

import java.util.Date;
import java.util.List;
import rental.Quote;
import rental.Reservation;
import rental.ReservationConstraints;
import rental.ReservationException;


public interface ReservationSessionRemote {
    void createQuote(ReservationConstraints constraints, String guest)
            throws ReservationException;
    List<Reservation> confirmQuotes() 
            throws ReservationException;
    List<Quote> getCurrentQuotes();
    void checkForAvailableCarTypes(Date start, Date end) 
           throws Exception;
}
