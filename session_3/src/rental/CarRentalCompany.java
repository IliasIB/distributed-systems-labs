package rental;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CarRentalCompany implements CarRentalCompanyStub{

	private static Logger logger = Logger.getLogger(CarRentalCompany.class.getName());

	private List<String> regions;
	private String name;
	private List<Car> cars;
	private Map<String,CarType> carTypes;


	/***************
	 * CONSTRUCTOR *
	 ***************/

	public CarRentalCompany(String name, List<String> regions, List<Car> cars) {
		logger.log(Level.INFO, "<{0}> Car Rental Company {0} starting up...", name);
		setName(name);
		setRegions(regions);
		this.cars = cars;

		carTypes = new HashMap<String, CarType>();
		for(Car car:cars)
			carTypes.put(car.getType().getName(), car.getType());
	}

	/********
	 * NAME *
	 ********/

	public String getName() {
		return name;
	}

	private void setName(String name) {
		this.name = name;
	}

	/*************
	 * CAR TYPES *
	 *************/

	@Override
	public Collection<CarType> getCarTypes() throws Exception{
		return carTypes.values();
	}

	@Override
	public CarType getType(String carTypeName) throws Exception{
		if(carTypes.containsKey(carTypeName))
			return carTypes.get(carTypeName);
		throw new IllegalArgumentException("<" + carTypeName + "> No cartype of name " + carTypeName);
	}

	public boolean isAvailable(String carTypeName, Date start, Date end) throws Exception {
		logger.log(Level.INFO, "<{0}> Checking availability for car type {1}", new Object[]{name, carTypeName});
		return getAvailableCarTypes(start, end).contains(carTypes.get(carTypeName));
	}

	@Override
	public synchronized Set<CarType> getAvailableCarTypes(Date start, Date end) throws Exception {
		Set<CarType> availableCarTypes = new HashSet<CarType>();
		for (Car car : cars) {
			if (car.isAvailable(start, end)) {
				availableCarTypes.add(car.getType());
			}
		}
		return availableCarTypes;
	}

	/***********
	 * Regions *
	 **********/
	private void setRegions(List<String> regions) {
		this.regions = regions;
	}

	public List<String> getRegions() {
		return this.regions;
	}

	@Override
	public boolean hasRegion(String region) {
		return this.regions.contains(region);
	}

	/*********
	 * CARS *
	 *********/

	@Override
	public List<Car> getCars() throws Exception{
		return cars;
	}

	private Car getCar(int uid) {
		for (Car car : cars) {
			if (car.getId() == uid)
				return car;
		}
		throw new IllegalArgumentException("<" + name + "> No car with uid " + uid);
	}

	private List<Car> getAvailableCars(String carType, Date start, Date end) {
		List<Car> availableCars = new LinkedList<Car>();
		for (Car car : cars) {
			if (car.getType().getName().equals(carType) && car.isAvailable(start, end)) {
				availableCars.add(car);
			}
		}
		return availableCars;
	}

	/****************
	 * RESERVATIONS *
	 ****************/

	@Override
	public synchronized Quote createQuote(ReservationConstraints constraints, String guest)
			throws Exception {
		logger.log(Level.INFO, "<{0}> Creating tentative reservation for {1} with constraints {2}",
				new Object[]{name, guest, constraints.toString()});


		if(!this.regions.contains(constraints.getRegion()) || !isAvailable(constraints.getCarType(), constraints.getStartDate(), constraints.getEndDate()))
			throw new ReservationException("<" + name
					+ "> No cars available to satisfy the given constraints.");

		CarType type = getType(constraints.getCarType());

		double price = calculateRentalPrice(type.getRentalPricePerDay(),constraints.getStartDate(), constraints.getEndDate());

		return new Quote(guest, constraints.getStartDate(), constraints.getEndDate(), getName(), constraints.getCarType(), price);
	}

	// Implementation can be subject to different pricing strategies
	private double calculateRentalPrice(double rentalPricePerDay, Date start, Date end) {
		return rentalPricePerDay * Math.ceil((end.getTime() - start.getTime())
				/ (1000 * 60 * 60 * 24D));
	}

	@Override
	public synchronized Reservation confirmQuote(Quote quote) throws Exception {
		logger.log(Level.INFO, "<{0}> Reservation of {1}", new Object[]{name, quote.toString()});
		List<Car> availableCars = getAvailableCars(quote.getCarType(), quote.getStartDate(), quote.getEndDate());
		if(availableCars.isEmpty())
			throw new ReservationException("Reservation failed, all cars of type " + quote.getCarType()
					+ " are unavailable from " + quote.getStartDate() + " to " + quote.getEndDate());
		Car car = availableCars.get((int)(Math.random()*availableCars.size()));

		Reservation res = new Reservation(quote, car.getId());
		car.addReservation(res);
		return res;
	}

	@Override
	public synchronized void cancelReservation(Reservation res) throws Exception{
		logger.log(Level.INFO, "<{0}> Cancelling reservation {1}", new Object[]{name, res.toString()});
		getCar(res.getCarId()).removeReservation(res);
	}

	public Set<Reservation> getReservationsBy(String renter) {
		logger.log(Level.INFO, "<{0}> Retrieving reservations by {1}", new Object[]{name, renter});
		Set<Reservation> out = new HashSet<Reservation>();
		for(Car c : cars) {
			for(Reservation r : c.getAllReservations()) {
				if(r.getCarRenter().equals(renter))
					out.add(r);
			}
		}
		return out;
	}
}