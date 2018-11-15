package session;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLIntegrityConstraintViolationException;
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
import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TemporalType;
import rental.Car;
import rental.CarRentalCompany;
import rental.CarType;
import rental.Reservation;

@Stateless
public class ManagerSession implements ManagerSessionRemote {
    
    @PersistenceContext(unitName="CarRental-ejbPU")
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
            "SELECT COUNT(res) FROM Car cars JOIN cars.reservations as res "
                + "WHERE cars.company=:comp AND cars.type=:cartype AND cars.id=:id")             
        .setParameter("comp", company)
        .setParameter("id", id)               
        .setParameter("cartype", type);      
                
        return ((Set<CarType>) query.getResultList()).size();
    }
    
    @Override
    public Set<String> getBestClients(){
         Query query = em.createQuery(
            "SELECT res.carRenter, COUNT(res.carRenter) FROM Reservation res "
                    + "GROUP BY res.carRenter "
                    + "ORDER BY COUNT(res.carRenter) DESC");
       List<Object[]> customers = (List<Object[]>)query.getResultList();
       Set<String> bestCustomers = new HashSet<>();
       long startValue = -1;
        if (customers.size() > 0){
            for (Object[] customer : customers) {
                if (startValue == -1){
                    startValue = (long) customer[1];
                }
                if ((long) customer[1] == startValue){
                    bestCustomers.add((String) customer[0]);
                }else{
                    break;
                }
            
           }
            return bestCustomers;
        }
        else{
            return null;
        }
    }

     @Override
     public CarType getMostPopularCarTypeIn(String carRentalCompanyName, int year){
        Query query = em.createQuery(
            "SELECT cars.type "
                    + "FROM Reservation res, Car cars, CarRentalCompany comp "
                    + "WHERE FUNCTION('YEAR' ,res.startDate)=:year "
                    + "AND res.car = cars AND cars.company = comp.name "
                    + "AND comp.name=:company "
                    + "GROUP BY cars.type"
                    + " ORDER BY COUNT(res) DESC")
            .setParameter("year", year)
            .setParameter("company", carRentalCompanyName);
        
        List<CarType> types = (List<CarType>) query.getResultList();
        if (types.size() > 0){
            return types.get(0);
        }
        else{
            return null;
        }
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
                    + "OR (res.endDate>=:start AND res.endDate<=:end))) "
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
            "SELECT res FROM Car cars JOIN cars.reservations AS res "
                + "WHERE cars.company=:companyName AND cars.type.name=:cartype")             
        .setParameter("companyName", company)             
        .setParameter("cartype", type);
        return ((Set<CarType>) new HashSet<>(query.getResultList())).size();
    }
    
    public void addCompany(CarRentalCompany carRentalCompany){
        em.persist(carRentalCompany);
    }
    
    public void addCarTypes(List<CarType> carTypes, CarRentalCompany carRentalCompany){
        carRentalCompany.setAllTypes(carTypes);
        for (CarType carType : carTypes){
            em.persist(carType);
        }
    }
        
    public void addCars(List<Car> cars, CarRentalCompany carRentalCompany){
        for (Car car : cars){
            car.setCompany(carRentalCompany.getName());
            em.persist(car);
        }
        carRentalCompany.setCars(cars);        
    }
    
    @Override
    public synchronized void loadRental(String datafile) {
        try {
            CrcData data = loadData(datafile);
            
            CarRentalCompany company = new CarRentalCompany();
            
            company.setRegions(data.regions);
            company.setName(data.name);
            
            List<CarType> types = new ArrayList<>();
            for (Car car : data.cars){
                CarType type = car.getType();
                type.setCompany(company);
                types.add(type);
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