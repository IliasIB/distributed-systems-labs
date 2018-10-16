package rental;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.List;
import java.util.Set;

public interface CarRentalCompanyStub extends Remote{
    Quote createQuote(ReservationConstraints constraints, String client)
            throws ReservationException, RemoteException;
    Reservation confirmQuote(Quote quote)
            throws ReservationException, RemoteException;
    Set<CarType> getAvailableCarTypes(Date start, Date end)
            throws RemoteException;
    List<Reservation> getReservationsByRenter(String companyName)
            throws RemoteException;
    int getReservationAmountByCarType(String carType)
            throws RemoteException;
}
