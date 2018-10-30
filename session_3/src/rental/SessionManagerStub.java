package rental;

import java.rmi.Remote;

public interface SessionManagerStub extends Remote {
    ManagerSessionStub newManagerSession(String sessionName) throws Exception;
    ReservationSessionStub newReservationSession(String sessionName) throws Exception;
    Session getSession(String sessionName) throws Exception;
    void removeSession(String sessionName) throws Exception;
}
