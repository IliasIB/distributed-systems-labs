package rental;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NamingService implements NamingServiceStub {
    private static Map<String, CarRentalCompanyStub> namedRemotes = new HashMap<>();
    private static Map<String,CarRentalCompany> rentals = new HashMap<>();
    private SessionManager sessionManager = new SessionManager();

    public static void main(String[] args){

        try {
            NamingService obj = new NamingService();
            NamingServiceStub stub = (NamingServiceStub) UnicastRemoteObject.exportObject(obj, 0);

            if (System.getSecurityManager() == null){
                System.setSecurityManager(null);
            }

            // Bind the remote object's stub in the registry
            Registry registry = LocateRegistry.getRegistry();
            registry.bind("namingService", stub);

            System.err.println("NamingService ready");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }

    @Override
    public synchronized Map<String, CarRentalCompanyStub> getAllRegistered() {
        return namedRemotes;
    }

    @Override
    public synchronized void register(String name, String fileLocation) {
        loadRental(name, fileLocation);
    }

    @Override
    public synchronized void unregister(String name) {
        namedRemotes.remove(name);
    }

    public static void loadRental(String name, String datafile) {
        try {
            CrcData data = loadData(datafile);
            CarRentalCompany company = new CarRentalCompany(data.name, data.regions, data.cars);
            rentals.put(name, company);
            namedRemotes.put(name, (CarRentalCompanyStub)UnicastRemoteObject.exportObject(company, 0));
            Logger.getLogger(RentalStore.class.getName()).log(Level.INFO, "Loaded {0} from file {1}", new Object[]{data.name, datafile});
        } catch (NumberFormatException ex) {
            Logger.getLogger(RentalStore.class.getName()).log(Level.SEVERE, "bad file", ex);
        } catch (IOException ex) {
            Logger.getLogger(RentalStore.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static CrcData loadData(String datafile)
            throws NumberFormatException, IOException {

        CrcData out = new CrcData();
        StringTokenizer csvReader;
        int nextuid = 0;

        //open file from jar
        BufferedReader in = new BufferedReader(new FileReader(datafile));

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
                    CarType type = new CarType(csvReader.nextToken(),
                            Integer.parseInt(csvReader.nextToken()),
                            Float.parseFloat(csvReader.nextToken()),
                            Double.parseDouble(csvReader.nextToken()),
                            Boolean.parseBoolean(csvReader.nextToken()));
                    //create N new cars with given type, where N is the 5th field
                    for (int i = Integer.parseInt(csvReader.nextToken()); i > 0; i--) {
                        out.cars.add(new Car(nextuid++, type));
                    }
                }
            }
        } finally {
            in.close();
        }

        return out;
    }

    static class CrcData {
        public List<Car> cars = new LinkedList<Car>();
        public String name;
        public List<String> regions =  new LinkedList<String>();
    }
}
