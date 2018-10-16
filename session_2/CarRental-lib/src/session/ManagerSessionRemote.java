package session;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.ejb.Remote;
import rental.CarType;

@Remote
public interface ManagerSessionRemote {
    Collection<CarType> getCarTypesByCompany(String companyName);
    Map<CarType,Integer> getCarTypeReservationAmountByCompany(String companyName);
    String getBestCustomer();
    int getNumberOfReservationsBy(String clientName) 
            throws Exception;
    int getNumberOfReservationsForCarType(String carRentalName, String carType) 
            throws Exception;    
}
