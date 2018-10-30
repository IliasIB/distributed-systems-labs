package rental;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface ManagerSessionStub extends Session {
    Collection<CarType> getCarTypesByCompany(String companyName) throws Exception;
    Map<CarType,Integer> getCarTypeReservationAmountByCompany(String companyName) throws Exception;
    Set<String> getBestClients() throws Exception;
    int getNumberOfReservationsBy(String clientName)
            throws Exception;
    int getNumberOfReservationsForCarType(String carRentalName, String carType)
            throws Exception;
    CarType getMostPopularCarTypeIn(ManagerSessionStub session, String carRentalCompanyName, int year)
        throws Exception;
    void createCompany(String companyName, String fileLocation)
            throws Exception;
}
