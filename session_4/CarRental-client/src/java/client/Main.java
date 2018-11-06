package client;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.naming.InitialContext;
import rental.CarType;
import rental.Reservation;
import rental.ReservationConstraints;
import session.CarRentalSessionRemote;
import session.ManagerSessionRemote;

public class Main extends AbstractTestManagement<CarRentalSessionRemote, ManagerSessionRemote> {

    public Main(String scriptFile) {
        super(scriptFile);
    }

    public static void main(String[] args) throws Exception {
        // TODO: use updated manager interface to load cars into companies
        Main m = new Main("trips");
        m.loadRentals();
        m.run();
        
    }
    
    public void loadRentals() throws Exception{
        
        InitialContext context = new InitialContext();
        ManagerSessionRemote ms = (ManagerSessionRemote) context.lookup(ManagerSessionRemote.class.getName());
        ms.loadRental("hertz.csv");
        ms.loadRental("dockx.csv");
    }
    


    /**
     * Create a new reservation session for the user with the given name.
     *
     * @param name name of the client (renter) owning this session
     * @return the new reservation session
     *
     * @throws Exception if things go wrong, throw exception
     */
    @Override
    public CarRentalSessionRemote getNewReservationSession(String name) throws Exception {
        InitialContext context = new InitialContext();
        CarRentalSessionRemote rs = (CarRentalSessionRemote) context.lookup(CarRentalSessionRemote.class.getName());
        return rs;
    }

    /**
     * Create a new manager session for the user with the given name.
     *
     * @param name name of the user (i.e. manager) using this session
     * @param carRentalName name of the rental company managed by this session
     * @return the new manager session
     *
     * @throws Exception if things go wrong, throw exception
     */
     @Override
     public ManagerSessionRemote getNewManagerSession(String name, String carRentalName) throws Exception{
        InitialContext context = new InitialContext();
        ManagerSessionRemote ms = (ManagerSessionRemote) context.lookup(ManagerSessionRemote.class.getName());
        return ms;
     }
     

    /**
     * Check which car types are available in the given period and print them.
     *
     * @param session the session to do the request from
     * @param start start time of the period
     * @param end end time of the period
     *
     * @throws Exception if things go wrong, throw exception
     */
    @Override
    public void checkForAvailableCarTypes(CarRentalSessionRemote session, Date start, Date end) throws Exception{
        List<CarType> available = session.getAvailableCarTypes(start, end);
        for (CarType type : available){
            System.out.println(type.getName());
        }
    }

    /**
     * Add a quote for a given car type to the session.
     *
     * @param session the session to add the reservation to
     * @param name the name of the client owning the session
     * @param start start time of the reservation
     * @param end end time of the reservation
     * @param carType type of car to be reserved
     * @param region region for which the car shall be reserved
     * should be done
     *
     * @throws Exception if things go wrong, throw exception
     */
    @Override
    public void addQuoteToSession(CarRentalSessionRemote session, String name,
            Date start, Date end, String carType, String region) throws Exception {
        ReservationConstraints constraints = new ReservationConstraints(start, end, carType, region);
        session.createQuote(region, constraints);
    }

    /**
     * Confirm the quotes in the given session.
     *
     * @param session the session to finalize
     * @param name the name of the client owning the session
     *
     * @throws Exception if things go wrong, throw exception
     */
    @Override
    public List<Reservation> confirmQuotes(CarRentalSessionRemote session, String name) throws Exception{
        return session.confirmQuotes();
    }

    /**
     * Get the number of reservations made by the given renter (across whole
     * rental agency).
     *
     * @param	ms manager session
     * @param clientName name of the renter
     * @return	the number of reservations of the given client (across whole
     * rental agency)
     *
     * @throws Exception if things go wrong, throw exception
     */
    @Override
    public int getNumberOfReservationsBy(ManagerSessionRemote ms, String clientName) throws Exception{
        return ms.getNumberOfReservationsBy(clientName);
    }

    /**
     * Get the number of reservations for a particular car type.
     *
     * @param ms manager session
     * @param carRentalName name of the rental company managed by this session
     * @param carType name of the car type
     * @return number of reservations for this car type
     *
     * @throws Exception if things go wrong, throw exception
     */
    @Override
    public int getNumberOfReservationsForCarType(ManagerSessionRemote ms, String carRentalName, String carType) throws Exception {
        return ms.getNumberOfReservations(carType, carType);
    }   
      
    
    /**
     * Find a cheapest car type that is available in the given period and region.
     *
     * @param session the session to do the request from
     * @param start start time of the period
     * @param end end time of the period
     * @param region region of interest (if null, no limitation by region)
     *
     * @return name of a cheapest car type for the given period
     *
     * @throws Exception if things go wrong, throw exception
     */
    @Override
    public String getCheapestCarType(CarRentalSessionRemote session, Date start,
            Date end, String region) throws Exception {
        return session.getCheapestCarType(start, end, region);
    }

    /**
     * Get the most popular car type in the given car rental company.
     *
     * @param ms manager session
     * @param	carRentalCompanyName The name of the car rental company.
     * @param year year in question
     * @return the most popular car type in the given car rental company
     *
     * @throws Exception if things go wrong, throw exception
     */
    @Override
    public CarType getMostPopularCarTypeIn(ManagerSessionRemote ms, String carRentalCompanyName, int year) throws Exception {
        return ms.getMostPopularCarTypeIn(carRentalCompanyName, year);
    }

    
    /**
     * Get the (list of) best clients, i.e. clients that have highest number of
     * reservations (across all rental agencies).
     *
     * @param ms manager session
     * @return set of best clients
     * @throws Exception if things go wrong, throw exception
     */
    @Override
    public Set<String> getBestClients(ManagerSessionRemote ms) throws Exception {
        return ms.getBestClients();
    }


    //protected abstract String getMostPopularCarRentalCompany(ManagerSession ms) throws Exception;
}