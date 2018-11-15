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
import javax.persistence.TemporalType;
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
        return (Set<String>) new HashSet<>(query.getResultList());
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
    public Quote createQuote(String name, ReservationConstraints constraints) throws ReservationException {
        Query query = em.createQuery(
        "SELECT company FROM CarRentalCompany company");
        List<CarRentalCompany> companies = new ArrayList<>(query.getResultList());
        for (CarRentalCompany rental : companies){
            try{
                Quote out = rental.createQuote(constraints, name);
                quotes.add(out);
                return out;
            }
            catch(ReservationException e){

            }
            catch(IllegalArgumentException e){

            }
        }
        throw new ReservationException("No cmpanies satisfied with request");
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
                Reservation reservation = rental.confirmQuote(quote);
                done.add(reservation);
                em.persist(reservation);
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
        Query query = em.createQuery(
            "SELECT cars.type.name FROM CarRentalCompany comp, Car cars "
                + "WHERE cars.company = comp.name AND "
                + "NOT EXISTS ("
                    + "SELECT cars FROM Reservation res "
                    + "WHERE res.car = cars AND "
                    + "((res.startDate>=:start AND res.startDate<=:end)"
                    + "OR (res.endDate>=:start AND res.endDate<=:end)) "
                    + "AND :region_s MEMBER OF comp.regions) "
                + "AND :region_s MEMBER OF comp.regions "
                + "ORDER BY cars.type.rentalPricePerDay ASC")
            .setParameter("start", start, TemporalType.DATE)
            .setParameter("end", end, TemporalType.DATE)
            .setParameter("region_s", region);
        List<String> types = (List<String>) query.getResultList();
        if (types.size() > 0){
            return types.get(0);
        }
        else{
            return null;
        }
    }
    

}