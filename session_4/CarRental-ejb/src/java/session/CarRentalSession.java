package session;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.ejb.EJBException;
import javax.ejb.Stateful;
import javax.ejb.TransactionAttribute;
import static javax.ejb.TransactionAttributeType.REQUIRED;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import rental.CarRentalCompany;
import rental.CarType;
import rental.Quote;
import rental.Reservation;
import rental.ReservationConstraints;
import rental.ReservationException;

@Stateful
public class CarRentalSession implements CarRentalSessionRemote {

    @PersistenceContext(unitName="CarRental-ejbPU")
    EntityManager em;
        
    private String renter;
    private List<Quote> quotes = new LinkedList<Quote>();

    @Override
    public Set<String> getAllRentalCompanies() {
        Query query = em.createQuery("SELECT e.name FROM CarRentalCompany e");
        return (Set<String>) query.getResultList();
    }
    
    @Override
    public List<CarType> getAvailableCarTypes(Date start, Date end) {
        List<CarType> availableCarTypes = new LinkedList<CarType>();
        for(String crc : getAllRentalCompanies()) {
            CarRentalCompany rental = em.find(CarRentalCompany.class, crc);
            for(CarType ct : rental.getAvailableCarTypes(start, end)) {
                if(!availableCarTypes.contains(ct))
                    availableCarTypes.add(ct);
            }
        }
        return availableCarTypes;
    }

    @Override
    public Quote createQuote(String company, ReservationConstraints constraints) throws ReservationException {
        try {
            CarRentalCompany rental = em.find(CarRentalCompany.class, company);
            Quote out = rental.createQuote(constraints, renter);
            quotes.add(out);
            return out;
        } catch(Exception e) {
            throw new ReservationException(e);
        }
    }

    @Override
    public List<Quote> getCurrentQuotes() {
        return quotes;
    }

    @TransactionAttribute(REQUIRED)
    @Override
    public List<Reservation> confirmQuotes() throws ReservationException {
        List<Reservation> done = new LinkedList<Reservation>();

        try {
            for (Quote quote : quotes) {
                CarRentalCompany rental = em.find(CarRentalCompany.class, quote.getRentalCompany());
                
                done.add(rental.confirmQuote(quote));

            }
        } catch (Exception e) {
            throw new EJBException(e);
        }
        return done;
    }

    @Override
    public void setRenterName(String name) {
        if (renter != null) {
            throw new IllegalStateException("name already set");
        }
        renter = name;
    }
    
     @Override
    public String getCheapestCarType(Date start, Date end, String region) {

        List<CarRentalCompany> regionalCompanies = new ArrayList<>();
        Query query = em.createQuery(
            "SELECT comp FROM CarRentalCompany comp ");
        List<CarRentalCompany> rentals = (List<CarRentalCompany>) query.getResultList();
        for (CarRentalCompany rental : rentals) {
            if ( rental.getRegions().contains(region)){
                regionalCompanies.add(rental);
            }
        }

        List<CarType> availableCarTypes = new ArrayList<>();
        for (CarRentalCompany company: regionalCompanies){
            availableCarTypes.addAll(company.getAvailableCarTypes(start, end));
        }

        double cheapestPrice = Double.MAX_VALUE;
        String cheapestCarType = null;
        for (CarType carType : availableCarTypes){
            if (carType.getRentalPricePerDay() < cheapestPrice){
                cheapestCarType = carType.getName();
                cheapestPrice = carType.getRentalPricePerDay();
            }
        }
        return cheapestCarType;
    }
    

}