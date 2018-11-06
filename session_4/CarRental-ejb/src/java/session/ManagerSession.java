package session;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import rental.Car;
import rental.CarRentalCompany;
import rental.CarType;
import rental.Reservation;

@Stateless
public class ManagerSession implements ManagerSessionRemote {
    
    @PersistenceContext
    EntityManager em;
    
    
    @Override
    public List<CarRentalCompany> getCompanies() {
        Query query = em.createQuery("SELECT e FROM CarRentalCompany e");
        return (List<CarRentalCompany>) query.getResultList();
    }
    
    @Override
    public Set<CarType> getCarTypes(String company) {
        Query query = em.createQuery(
                "SELECT comp.carTypes FROM CarRentalCompany comp WHERE comp.name=:company")
                .setParameter("company", company);
        
                
        return (Set<CarType>) query.getResultList();
    }

    @Override
    public Set<Integer> getCarIds(String company, String type) {
        Set<Integer> out = new HashSet<Integer>();
        try {
            CarRentalCompany comp = em.find(CarRentalCompany.class, company);
            for(Car c: comp.getCars(type)){
                out.add(c.getId());
            }
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(ManagerSession.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        return out;
    }

    @Override
    public int getNumberOfReservations(String company, String type, int id) {

        Query query = em.createQuery(
            "SELECT car.reservations FROM Car car "
                + "WHERE company=:company AND car.type=:cartype AND car.id=:id")             
        .setParameter("company", company)
        .setParameter("id", id)               
        .setParameter("cartype", type);      
                
        return ((Set<CarType>) query.getResultList()).size();
    }
    
    @Override
    public Set<String> getBestClients(){
         Query query = em.createQuery(
            "SELECT res FROM Reservation res ");
        List<Reservation> reservations = (List<Reservation>) query.getResultList();
        
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
     public CarType getMostPopularCarTypeIn(String carRentalCompanyName, int year){
         Query query = em.createQuery(
            "SELECT res FROM Reservation res");
        List<Reservation> reservations = (List<Reservation>) query.getResultList();

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
            CarType carType = em.find(CarType.class, reservation.getCarType());
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

    public int getNumberOfReservationsBy(String clientName) throws Exception {
         Query query = em.createQuery(
            "SELECT res FROM Reservation res");
        List<Reservation> reservations = (List<Reservation>) query.getResultList();

        int count = 0;
        for (Reservation reservation : reservations) {
            if (reservation.getCarRenter().equals(clientName)) {
                count += 1;
            }
        }
        return count;
    }
        
        
    @Override
    public int getNumberOfReservations(String company, String type) {

        Query query = em.createQuery(
            "SELECT car.reservations FROM Car car "
                + "WHERE company=:company AND car.type=:cartype")             
        .setParameter("company", company)             
        .setParameter("cartype", type);      
                
        return ((Set<CarType>) query.getResultList()).size();
    }
    
    public void addCompany(CarRentalCompany carRentalCompany){
        em.persist(carRentalCompany);
    }
    
    public void addCarTypes(Set<CarType> carTypes, CarRentalCompany carRentalCompany){
        carRentalCompany.setTypes(carTypes);
        em.persist(carTypes);
        
    }
        
    public void addCars(List<Car> cars, CarRentalCompany carRentalCompany){
        for (Car car : cars){
            car.setCompany(carRentalCompany.getName());
        }
        carRentalCompany.setCars(cars);
        em.persist(cars);
    }
    
    @Override
    public synchronized void loadRental(String datafile) {
        try {
            CrcData data = loadData(datafile);
            
            CarRentalCompany company = new CarRentalCompany();
            
            company.setRegions(data.regions);
            company.setName(data.name);
            
            Set<CarType> types = new HashSet<>();
            for (Car car : data.cars){
                types.add(car.getType());
            }
            
            this.addCarTypes(types, company);
            
            this.addCars(data.cars, company);
            
            this.addCompany(company);
            
  
            Logger.getLogger(ManagerSession.class.getName()).log(Level.INFO, "Loaded {0} from file {1}", new Object[]{data.name, datafile});
        } catch (NumberFormatException ex) {
            Logger.getLogger(ManagerSession.class.getName()).log(Level.SEVERE, "bad file", ex);
        } catch (IOException ex) {
            Logger.getLogger(ManagerSession.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public CrcData loadData(String datafile)
            throws NumberFormatException, IOException {

        CrcData out = new CrcData();
        StringTokenizer csvReader;
        int nextuid = 0;

        //open file from jar
        BufferedReader in = new BufferedReader(new InputStreamReader(ManagerSession.class.getClassLoader().getResourceAsStream(datafile)));

        try {
            while (in.ready()) {
                String line = in.readLine();

                if (line.startsWith("#")) {
                    // comment -> skip					
                } else if (line.startsWith("-")) {
                    csvReader = new StringTokenizer(line.substring(1), ",");
                    out.name = csvReader.nextToken();
                    out.regions = Arrays.asList(csvReader.nextToken().split(":"));
                } else {
                    csvReader = new StringTokenizer(line, ",");
                    //create new car type from first 5 fields
                    
                    CarType type = new CarType();
                             
                    type.setName(csvReader.nextToken());
                    type.setNbOfSeats(Integer.parseInt(csvReader.nextToken()));
                    type.setTrunkSpace(Float.parseFloat(csvReader.nextToken()));
                    type.setRentalPricePerDay( Double.parseDouble(csvReader.nextToken()));
                    type.setSmokingAllowed(Boolean.parseBoolean(csvReader.nextToken()));
                    
                    //create N new cars with given type, where N is the 5th field
                    for (int i = Integer.parseInt(csvReader.nextToken()); i > 0; i--) {
                        Car car = new Car();
                        car.setId(nextuid++);
                        car.setType(type);
                        out.cars.add(car);
                    }        
                }
            } 
        } finally {
            in.close();
        }

        return out;
    }
     
    class CrcData {
            public List<Car> cars = new LinkedList<Car>();
            public String name;
            public List<String> regions =  new LinkedList<String>();
    }

}