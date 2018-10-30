package rental;

import java.rmi.Remote;
import java.util.Map;

public interface NamingServiceStub extends Remote {
    Map<String,CarRentalCompanyStub> getAllRegistered() throws Exception;
    void register(String name, String fileLocation) throws Exception;
    void unregister(String name) throws Exception;
}
