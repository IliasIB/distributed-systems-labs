package session;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ejb.Stateful;
import rental.CarRentalCompany;
import rental.CarType;
import rental.Quote;
import rental.RentalStore;
import rental.Reservation;
import rental.ReservationConstraints;
import rental.ReservationException;

@Stateful
public class ReservationSession implements ReservationSessionRemote{
    List<Quote> quotes = new ArrayList<Quote>();

    @Override
    public void createQuote(ReservationConstraints constraints, String guest)
            throws ReservationException{
        Map<String, CarRentalCompany> rentals = RentalStore.getRentals();
        for (Map.Entry<String, CarRentalCompany> rental : rentals.entrySet()) {
            try {
                Quote quote = rental.getValue().createQuote(constraints, guest);
                quotes.add(quote);
                return;
            } catch (ReservationException e) {}
        }
        throw new ReservationException("No cars available to satisfy the given constraints.");
    }
    
    @Override
    public List<Reservation> confirmQuotes() 
            throws ReservationException {
        List<Reservation> tempReservations = new ArrayList<Reservation>();
        try {
            for (Quote quote : quotes) {
                CarRentalCompany rental = RentalStore.getRental(quote.getRentalCompany());
                Reservation reservation = rental.confirmQuote(quote);
                tempReservations.add(reservation);
            }
            quotes.clear();
        } catch (ReservationException e) {
            for (Reservation tempReservation : tempReservations) {
                CarRentalCompany rental = RentalStore.getRental(tempReservation.getRentalCompany());
                rental.cancelReservation(tempReservation);
            }
            throw new ReservationException("Reservation failed");
        }
        return tempReservations;
    }
    
    @Override
    public List<Quote> getCurrentQuotes(){
        return quotes;
    }
    
    @Override
    public void checkForAvailableCarTypes(Date start, Date end) 
           throws Exception{
        Set<CarType> carTypes = new HashSet<CarType>();
        
        Map<String, CarRentalCompany> rentals = RentalStore.getRentals();
        for (Map.Entry<String, CarRentalCompany> rental : rentals.entrySet()) {
            carTypes.addAll(rental.getValue().getAvailableCarTypes(start, end));
        }
        System.out.println(carTypes);
    }

}
