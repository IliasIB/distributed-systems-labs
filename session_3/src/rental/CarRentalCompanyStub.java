package rental;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

public interface CarRentalCompanyStub extends Remote{
    Quote createQuote(ReservationConstraints constraints, String client)
            throws Exception;
    Reservation confirmQuote(Quote quote)
            throws Exception;
    Set<CarType> getAvailableCarTypes(Date start, Date end)
            throws Exception;
    Collection<CarType> getCarTypes() throws Exception;
    List<Car> getCars() throws Exception;
    void cancelReservation(Reservation res) throws Exception;
    boolean hasRegion(String region) throws Exception;
    CarType getType(String carTypeName) throws Exception;
}
