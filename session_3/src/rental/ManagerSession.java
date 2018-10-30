package rental;

import java.util.*;

public class ManagerSession implements ManagerSessionStub {
    @Override
    public Collection<CarType> getCarTypesByCompany(String companyName) throws Exception{
        CarRentalCompanyStub rental = RentalStore.getRental(companyName);
        return rental.getCarTypes();
    }

    @Override
    public Map<CarType,Integer> getCarTypeReservationAmountByCompany(String companyName) throws Exception{
        CarRentalCompanyStub rental = RentalStore.getRental(companyName);
        Collection<CarType> carTypes = rental.getCarTypes();
        Map<CarType,Integer> carTypeMap = new HashMap<CarType,Integer>();
        for (CarType carType : carTypes) {
            carTypeMap.put(carType, 0);
        }

        List<Car> cars = new ArrayList<Car>();
        for (Car car : cars) {
            int amount = car.getAllReservations().size();
            CarType carType = car.getType();
            carTypeMap.put(carType, carTypeMap.get(carType) + amount);
        }

        return carTypeMap;
    }

    @Override
    public int getNumberOfReservationsBy(String clientName)
            throws Exception{
        List<Reservation> reservations = getAllReservations();

        int count = 0;
        for (Reservation reservation : reservations) {
            if (reservation.getCarRenter().equals(clientName)) {
                count += 1;
            }
        }
        return count;
    }

    @Override
    public Set<String> getBestClients() throws Exception{
        List<Reservation> reservations = getAllReservations();

        Map<String, Integer> customerCount = new HashMap<String, Integer>();
        for (Reservation reservation : reservations) {
            String customerName = reservation.getCarRenter();
            if (customerCount.get(customerName) == null) {
                customerCount.put(customerName, 0);
            }
            else
                customerCount.put(customerName, customerCount.get(customerName) + 1);
        }

        List<Integer> reservationAmounts = new ArrayList<Integer>(customerCount.values());
        Set<String> bestCustomers = new HashSet<>();
        Collections.sort(reservationAmounts);
        int highestAmount = reservationAmounts.get(reservationAmounts.size() - 1);

        for (Map.Entry<String, Integer> customer : customerCount.entrySet())  {
            if (customer.getValue() == highestAmount){
                bestCustomers.add(customer.getKey());
            }
        }
        return bestCustomers;
    }

    @Override
    public int getNumberOfReservationsForCarType(String carRentalName, String carType)
            throws Exception{
        CarRentalCompanyStub rental = RentalStore.getRental(carRentalName);
        List<Car> cars = rental.getCars();
        List<Reservation> reservations = new ArrayList<Reservation>();
        for (Car car : cars) {
            if (car.getType().getName().equals(carType)){
                reservations.addAll(car.getAllReservations());
            }
        }
        return reservations.size();
    }

    @Override
    public CarType getMostPopularCarTypeIn(ManagerSessionStub session, String carRentalCompanyName, int year)
            throws Exception{
        CarRentalCompanyStub rentalCompanyStub = RentalStore.getRental(carRentalCompanyName);
        List<Reservation> reservations = getReservations(rentalCompanyStub);

        List<Reservation> reservationsInYear = new ArrayList<>();
        for (Reservation reservation : reservations){
            Calendar calendar = new GregorianCalendar();
            calendar.setTime(reservation.getStartDate());
            int reservationYear = calendar.get(Calendar.YEAR);
            if (reservationYear == year) {
                reservationsInYear.add(reservation);
            }
        }

        Map<CarType,Integer> carTypeResAmount = new HashMap<>();
        for (Reservation reservation : reservationsInYear){
            CarType carType = rentalCompanyStub.getType(reservation.getCarType());
            if (carTypeResAmount.get(carType) == null){
                carTypeResAmount.put(carType,1);
            }
            else{
                carTypeResAmount.put(carType, carTypeResAmount.get(carType) + 1);
            }
        }

        List<Integer> reservationAmounts = new ArrayList<Integer>(carTypeResAmount.values());
        Collections.sort(reservationAmounts);
        int highestAmount = reservationAmounts.get(reservationAmounts.size() - 1);

        for (Map.Entry<CarType, Integer> carType : carTypeResAmount.entrySet())  {
            if (carType.getValue() == highestAmount){
                return carType.getKey();
            }
        }
        return null;
    }

    @Override
    public void createCompany(String companyName, String fileLocation)
            throws Exception{
        RentalStore.createCompany(companyName, fileLocation);
    }

    private List<Reservation> getAllReservations() throws Exception{
        List<Reservation> reservations = new ArrayList<Reservation>();
        Map<String, CarRentalCompanyStub> rentals = RentalStore.getRentals();
        for (Map.Entry<String, CarRentalCompanyStub> rental : rentals.entrySet())  {
            reservations.addAll(getReservations(rental.getValue()));
        }
        return reservations;
    }

    private List<Reservation> getReservations(CarRentalCompanyStub rental) throws Exception{
        List<Reservation> reservations = new ArrayList<>();
        List<Car> cars = rental.getCars();
        for (Car car : cars) {
            reservations.addAll(car.getAllReservations());
        }
        return reservations;
    }
}
