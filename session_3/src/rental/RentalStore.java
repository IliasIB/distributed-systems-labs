package rental;

import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Map;

public class RentalStore {
    private static Map<String, CarRentalCompanyStub> rentals;

    public static synchronized CarRentalCompanyStub getRental(String company) {
        CarRentalCompanyStub out = RentalStore.getRentals().get(company);
        if (out == null) {
            throw new IllegalArgumentException("Company doesn't exist!: " + company);
        }
        return out;
    }

    public static synchronized void createCompany(String company, String fileLocation) {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost");
            NamingServiceStub namingService = (NamingServiceStub) registry.lookup("namingService");

            namingService.register(company, fileLocation);
        } catch (Exception e) {
            System.err.println("RentalStore exception: " + e.toString());
            e.printStackTrace();
        }
    }

    public static synchronized Map<String, CarRentalCompanyStub> getRentals(){
        if(rentals == null){
            rentals = new HashMap<>();
            try {
                Registry registry = LocateRegistry.getRegistry("localhost");
                NamingServiceStub namingService = (NamingServiceStub) registry.lookup("namingService");

                Map<String, CarRentalCompanyStub> registered = namingService.getAllRegistered();
                for (Map.Entry<String, CarRentalCompanyStub> entry :
                        registered.entrySet()) {
                        rentals.put(entry.getKey(), entry.getValue());
                }
            } catch (Exception e) {
                System.err.println("RentalStore exception: " + e.toString());
                e.printStackTrace();
            }
        }
        return rentals;
    }



}
