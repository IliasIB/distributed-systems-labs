package session;

import java.util.Date;
import java.util.List;
import java.util.Set;
import javax.ejb.Remote;
import rental.CarRentalCompany;
import rental.CarType;

@Remote
public interface ManagerSessionRemote {
    
    public Set<CarType> getCarTypes(String company);
    
    public Set<Integer> getCarIds(String company,String type);
    
    public int getNumberOfReservations(String company, String type, int carId);
    
    public int getNumberOfReservations(String company, String type);
    
    public void loadRental(String name);
    
    public List<CarRentalCompany> getCompanies();
    
    public Set<String> getBestClients();
    
    public CarType getMostPopularCarTypeIn(String carRentalCompanyName, int year);
    
    public String getCheapestCarType(Date start, Date end, String region);

    public int getNumberOfReservationsBy(String clientName) throws Exception;
}