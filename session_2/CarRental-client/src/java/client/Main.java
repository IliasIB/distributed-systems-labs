package client;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.naming.InitialContext;
import rental.Reservation;
import rental.ReservationConstraints;
import session.CarRentalSessionRemote;
import session.ManagerSessionRemote;
import session.ReservationSessionRemote;

public class Main extends AbstractTestAgency<ReservationSessionRemote, ManagerSessionRemote> {
    
    @EJB
    static CarRentalSessionRemote session;
    
    Map<String, ReservationSessionRemote> reservationSessions = new HashMap<String, ReservationSessionRemote>();
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.out.println("found rental companies: "+session.getAllRentalCompanies());
        Main main = new Main("simpleTrips");
        try {
            main.run();            
        } catch (Exception e) {
        }
    }
    
    public Main(String scriptFile){
        super(scriptFile);
    }
    
    @Override
    public ReservationSessionRemote getNewReservationSession(String name) throws Exception{
        if (reservationSessions.get(name) == null) {
            InitialContext context = new InitialContext();
            ReservationSessionRemote newNession = (ReservationSessionRemote) context.lookup(ReservationSessionRemote.class.getName());
            reservationSessions.put(name, newNession);
            return newNession;
        }
        else{
            return reservationSessions.get(name);
        }
    }
    
    @Override
    public ManagerSessionRemote getNewManagerSession(String name, String carRentalName) throws Exception{
        InitialContext context = new InitialContext();
        ManagerSessionRemote newNession = (ManagerSessionRemote) context.lookup(ManagerSessionRemote.class.getName());
        return newNession;
    }
    
    @Override
    public void checkForAvailableCarTypes(ReservationSessionRemote session, Date start, Date end) throws Exception{
        session.checkForAvailableCarTypes(start, end);
    }
    
    @Override
    public void addQuoteToSession(ReservationSessionRemote session, String name,
            Date start, Date end, String carType, String region) throws Exception{
        ReservationConstraints constraint = new ReservationConstraints(start, end, carType, region);
        session.createQuote(constraint, name);
    }
    
    @Override
    public List<Reservation> confirmQuotes(ReservationSessionRemote session, String name) throws Exception{
        return session.confirmQuotes();
    }
    
    @Override
    public int getNumberOfReservationsBy(ManagerSessionRemote ms, String clientName) throws Exception{
        return ms.getNumberOfReservationsBy(clientName);
    }
    
    @Override
    public int getNumberOfReservationsForCarType(ManagerSessionRemote ms, String carRentalName, String carType) throws Exception{
        return ms.getNumberOfReservationsForCarType(carRentalName, carType);
    }
}
