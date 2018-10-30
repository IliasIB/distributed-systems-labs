package client;

import rental.*;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class Main extends AbstractTestManagement<ReservationSessionStub, ManagerSessionStub>{
    private SessionManagerStub sessionManagerStub;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Main main = new Main("trips");
        try {
            main.run();
        } catch (Exception e) {
        }
    }

    public Main(String scriptFile){
        super(scriptFile);
        try {
            Registry registry = LocateRegistry.getRegistry("localhost");
            sessionManagerStub = (SessionManagerStub) registry.lookup("sessionManager");
        } catch (Exception e) {
            System.err.println("SessionManager exception: " + e.toString());
            e.printStackTrace();
        }
        loadCompanies();
    }

    private void loadCompanies(){
        try {
            ManagerSessionStub session = sessionManagerStub.newManagerSession("loadCompany");
            session.createCompany("Hertz", "hertz.csv");
            session.createCompany("Dockx", "dockx.csv");
            sessionManagerStub.removeSession("loadCompany");
        } catch (Exception e){
            System.err.println("Error creating companies: " + e.toString());
            e.printStackTrace();
        }
    }

    @Override
    public Set<String> getBestClients(ManagerSessionStub session) throws Exception{
        return session.getBestClients();
    }

    @Override
    public String getCheapestCarType(ReservationSessionStub session, Date start,
                                     Date end, String region) throws Exception{
        return session.getCheapestCarType(start, end, region);
    }

    @Override
    public CarType getMostPopularCarTypeIn(ManagerSessionStub session, String carRentalCompanyName, int year)
            throws Exception{
        return session.getMostPopularCarTypeIn(session, carRentalCompanyName, year);
    }

    @Override
    public ReservationSessionStub getNewReservationSession(String name) throws Exception{
        return sessionManagerStub.newReservationSession(name);
    }

    @Override
    public ManagerSessionStub getNewManagerSession(String name, String carRentalName) throws Exception{
        return sessionManagerStub.newManagerSession(name);
    }

    @Override
    public void checkForAvailableCarTypes(ReservationSessionStub session, Date start, Date end) throws Exception{
        session.checkForAvailableCarTypes(start, end);
    }

    @Override
    public void addQuoteToSession(ReservationSessionStub session, String name,
                                  Date start, Date end, String carType, String region) throws Exception{
        ReservationConstraints constraint = new ReservationConstraints(start, end, carType, region);
        session.createQuote(constraint, name);
    }

    @Override
    public List<Reservation> confirmQuotes(ReservationSessionStub session, String name) throws Exception{
        return session.confirmQuotes();
    }

    @Override
    public int getNumberOfReservationsBy(ManagerSessionStub ms, String clientName) throws Exception{
        return ms.getNumberOfReservationsBy(clientName);
    }

    @Override
    public int getNumberOfReservationsForCarType(ManagerSessionStub ms, String carRentalName, String carType) throws Exception{
        return ms.getNumberOfReservationsForCarType(carRentalName, carType);
    }
}
