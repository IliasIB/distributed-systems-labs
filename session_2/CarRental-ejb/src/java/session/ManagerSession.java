package session;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.Stateless;
import rental.Car;
import rental.CarRentalCompany;
import rental.CarType;
import rental.RentalStore;
import rental.Reservation;

@Stateless
public class ManagerSession implements ManagerSessionRemote{
    @Override
    public Collection<CarType> getCarTypesByCompany(String companyName){
        CarRentalCompany rental = RentalStore.getRental(companyName);
        return rental.getCarTypes();
    }
    
    @Override
    public Map<CarType,Integer> getCarTypeReservationAmountByCompany(String companyName){
        CarRentalCompany rental = RentalStore.getRental(companyName);
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
        List<Reservation> reservations = new ArrayList<Reservation>();
        Map<String, CarRentalCompany> rentals = RentalStore.getRentals();
        for (Map.Entry<String, CarRentalCompany> rental : rentals.entrySet())  {
            List<Car> cars = rental.getValue().getCars();
            for (Car car : cars) {
                reservations.addAll(car.getAllReservations());
            }
        }
        
        int count = 0;
        for (Reservation reservation : reservations) {
            if (reservation.getCarRenter().equals(clientName)) {
                count += 1;
            }
        }
        return count;
    }
    
    @Override
    public String getBestCustomer(){
        List<Reservation> reservations = new ArrayList<Reservation>();
        Map<String, CarRentalCompany> rentals = RentalStore.getRentals();
        for (Map.Entry<String, CarRentalCompany> rental : rentals.entrySet())  {
            List<Car> cars = rental.getValue().getCars();
            for (Car car : cars) {
                reservations.addAll(car.getAllReservations());
            }
        }
        
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
        Collections.sort(reservationAmounts);
        int highestAmount = reservationAmounts.get(reservationAmounts.size() - 1);
        
        for (Map.Entry<String, Integer> customer : customerCount.entrySet())  {
            if (customer.getValue() == highestAmount){
                return customer.getKey();
            }
        }
        return null;
    }
    
    @Override
    public int getNumberOfReservationsForCarType(String carRentalName, String carType) 
            throws Exception{
        CarRentalCompany rental = RentalStore.getRental(carRentalName);
        List<Car> cars = rental.getCars();
        List<Reservation> reservations = new ArrayList<Reservation>();
        for (Car car : cars) {
            if (car.getType().getName().equals(carType)){
                reservations.addAll(car.getAllReservations());
            }
        }
        return reservations.size();
    }
}
