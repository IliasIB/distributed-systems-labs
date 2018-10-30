package rental;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.List;

public interface ReservationSessionStub extends Session {
    void createQuote(ReservationConstraints constraints, String guest)
            throws Exception;
    List<Reservation> confirmQuotes()
            throws Exception;
    List<Quote> getCurrentQuotes() throws Exception;
    void checkForAvailableCarTypes(Date start, Date end)
            throws Exception;
    String getCheapestCarType(Date start, Date end, String region) throws Exception;
}
