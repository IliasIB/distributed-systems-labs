package rental;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

public class SessionManager implements SessionManagerStub {
    Map<String, Session> sessions = new HashMap<>();

    public SessionManager() {
        try {
            SessionManagerStub stub = (SessionManagerStub) UnicastRemoteObject.exportObject(this, 0);

            if (System.getSecurityManager() == null){
                System.setSecurityManager(null);
            }

            // Bind the remote object's stub in the registry
            Registry registry = LocateRegistry.getRegistry();
            registry.bind("sessionManager", stub);

            System.err.println("SessionManager ready");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }

    @Override
    public synchronized ManagerSessionStub newManagerSession(String sessionName) throws Exception {
        ManagerSession managerSession= new ManagerSession();
        sessions.put(sessionName, managerSession);
        makeTimer(sessionName);

        return (ManagerSessionStub) UnicastRemoteObject.exportObject(managerSession, 0);
    }

    @Override
    public synchronized ReservationSessionStub newReservationSession(String sessionName) throws Exception {
        ReservationSession reservationSession = new ReservationSession();
        sessions.put(sessionName, reservationSession);
        makeTimer(sessionName);

        return (ReservationSessionStub) UnicastRemoteObject.exportObject(reservationSession, 0);
    }

    @Override
    public synchronized Session getSession(String sessionName) throws Exception{
        return sessions.get(sessionName);
    }

    @Override
    public synchronized void removeSession(String sessionName) throws Exception{
        sessions.remove(sessionName);
    }

    private void makeTimer(String sessionName){
        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        try {
                            removeSession(sessionName);
                        }catch (Exception e){}
                    }
                },
                120000
        );
    }
}
