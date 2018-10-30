package rental;

import java.util.*;

public class ReservationSession implements ReservationSessionStub {
    List<Quote> quotes = new ArrayList<Quote>();

    @Override
    public void createQuote(ReservationConstraints constraints, String guest)
            throws Exception {
        Map<String, CarRentalCompanyStub> rentals = RentalStore.getRentals();
        for (Map.Entry<String, CarRentalCompanyStub> rental : rentals.entrySet()) {
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
            throws Exception {
        List<Reservation> tempReservations = new ArrayList<Reservation>();
        try {
            for (Quote quote : quotes) {
                CarRentalCompanyStub rental = RentalStore.getRental(quote.getRentalCompany());
                Reservation reservation = rental.confirmQuote(quote);
                tempReservations.add(reservation);
            }
            quotes.clear();
        } catch (ReservationException e) {
            for (Reservation tempReservation : tempReservations) {
                CarRentalCompanyStub rental = RentalStore.getRental(tempReservation.getRentalCompany());
                rental.cancelReservation(tempReservation);
            }
            throw new ReservationException("Reservation failed");
        }
        return tempReservations;
    }

    @Override
    public List<Quote> getCurrentQuotes() throws Exception{
        return quotes;
    }

    @Override
    public void checkForAvailableCarTypes(Date start, Date end)
            throws Exception{
        Set<CarType> carTypes = new HashSet<CarType>();

        Map<String, CarRentalCompanyStub> rentals = RentalStore.getRentals();
        for (Map.Entry<String, CarRentalCompanyStub> rental : rentals.entrySet()) {
            carTypes.addAll(rental.getValue().getAvailableCarTypes(start, end));
        }
        System.out.println(carTypes);
    }

    @Override
    public String getCheapestCarType(Date start, Date end, String region) throws Exception{
        Map<String, CarRentalCompanyStub> rentals = RentalStore.getRentals();
        List<CarRentalCompanyStub> regionalCompanies = new ArrayList<>();
        for (Map.Entry<String, CarRentalCompanyStub> rental : rentals.entrySet()) {
            if (rental.getValue().hasRegion(region)){
                regionalCompanies.add(rental.getValue());
            }
        }

        List<CarType> availableCarTypes = new ArrayList<>();
        for (CarRentalCompanyStub companyStub : regionalCompanies){
            availableCarTypes.addAll(companyStub.getAvailableCarTypes(start, end));
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
