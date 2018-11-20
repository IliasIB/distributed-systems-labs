package ds.gae;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import ds.gae.entities.Car;
import ds.gae.entities.CarRentalCompany;
import ds.gae.entities.CarType;
import ds.gae.entities.Quote;
import ds.gae.entities.Reservation;
import ds.gae.entities.ReservationConstraints;

public class CarRentalModel {
		
	private static CarRentalModel instance;
	
	public static CarRentalModel get() {
		if (instance == null)
			instance = new CarRentalModel();
		return instance;
	}
		
	/**
	 * Get the car types available in the given car rental company.
	 *
	 * @param 	crcName
	 * 			the car rental company
	 * @return	The list of car types (i.e. name of car type), available
	 * 			in the given car rental company.
	 */
	public Set<String> getCarTypesNames(String crcName) {
		EntityManager em =
				EMF.get().createEntityManager(); 
		
		try {
			TypedQuery<String> query =
					em.createQuery("cType.name FROM CarType cType", String.class);
			List<String> names = query.getResultList();
			return new HashSet<>(names); 
		} finally {
			em.close(); 
	
		}	
	}

    /**
     * Get all registered car rental companies
     *
     * @return	the list of car rental companies
     */
    public Collection<String> getAllRentalCompanyNames() {
		EntityManager em =
				EMF.get().createEntityManager(); 
		
		try {
			TypedQuery<String> query =
					em.createQuery("comp.name FROM Company comp", String.class);
			List<String> names = query.getResultList();
			return new HashSet<>(names); 
		} finally {
			em.close(); 
	
		}	
    }
	
	/**
	 * Create a quote according to the given reservation constraints (tentative reservation).
	 * 
	 * @param	company
	 * 			name of the car renter company
	 * @param	renterName 
	 * 			name of the car renter 
	 * @param 	constraints
	 * 			reservation constraints for the quote
	 * @return	The newly created quote.
	 *  
	 * @throws ReservationException
	 * 			No car available that fits the given constraints.
	 */
    public Quote createQuote(String company, String renterName, ReservationConstraints constraints) throws ReservationException {
		// FIXME: use persistence instead
    	
		EntityManager em =
				EMF.get().createEntityManager(); 
		
		try {
			CarRentalCompany crc = em.find(CarRentalCompany.class, company);
			
			if (crc != null) {
		           Quote out = crc.createQuote(constraints, renterName);
		           return out;
		    } 
			else{
		          throw new ReservationException("CarRentalCompany not found.");    	
		       }
		} finally {
			em.close(); 
	
		}	
    }

    
	/**
	 * Confirm the given quote.
	 *
	 * @param 	q
	 * 			Quote to confirm
	 * 
	 * @throws ReservationException
	 * 			Confirmation of given quote failed.	
	 */
	public Reservation confirmQuote(Quote q) throws ReservationException {


		EntityManager em =
				EMF.get().createEntityManager(); 
	
		try {
		TypedQuery<CarRentalCompany> query =
				em.createQuery("comp FROM Company comp" 
						+ "WHERE comp.name=:quoteN",
						CarRentalCompany.class)
				.setParameter("quoteN", q.getCarRenter());
		List<CarRentalCompany> comps = query.getResultList();
		CarRentalCompany crc = comps.get(0);

        return crc.confirmQuote(q);
      
		} finally {
			em.close();
		}
	
    }

	
    /**
	 * Confirm the given list of quotes
	 * 
	 * @param 	quotes 
	 * 			the quotes to confirm
	 * @return	The list of reservations, resulting from confirming all given quotes.
	 * 
	 * @throws 	ReservationException
	 * 			One of the quotes cannot be confirmed. 
	 * 			Therefore none of the given quotes is confirmed.
	 */
    public List<Reservation> confirmQuotes(List<Quote> quotes) throws ReservationException {    	
		List<Reservation> reservations = new ArrayList<>();
    	for (Quote q : quotes) {
			Reservation res = confirmQuote(q);
			reservations.add(res);
			
		}
    	return reservations;
    }
	
    /**
	 * Get all reservations made by the given car renter.
	 *
	 * @param 	renter
	 * 			name of the car renter
	 * @return	the list of reservations of the given car renter
	 */
	public List<Reservation> getReservations(String renter) {

		EntityManager em =
				EMF.get().createEntityManager(); 
	
		try {
			TypedQuery<Reservation> query =
				em.createQuery("res FROM Reservation res, CarRentalCompany comp, Car c " 
						+ "WHERE comp.name=res.rentalCompany AND res.carRenter=:renter",
						Reservation.class)
				.setParameter("renter", renter);
	
			List<Reservation> reservations = query.getResultList();
			return reservations;
      
		} finally {
			em.close();
		}
    	

    }

    /**
     * Get the car types available in the given car rental company.
     *
     * @param 	crcName
     * 			the given car rental company
     * @return	The list of car types in the given car rental company.
     */
    public Collection<CarType> getCarTypesOfCarRentalCompany(String crcName) {
		EntityManager em =
				EMF.get().createEntityManager(); 
	
		try {
			TypedQuery<CarType> query =
				em.createQuery("cType FROM CarType cType" 
						+ "WHERE cTye.companyName=compName",
						CarType.class)
				.setParameter("compName",crcName);
	
			List<CarType> types = query.getResultList();
			return types;
      
		} finally {
			em.close();
		}
    	
    }
	
    /**
     * Get the list of cars of the given car type in the given car rental company.
     *
     * @param	crcName
	 * 			name of the car rental company
     * @param 	carType
     * 			the given car type
     * @return	A list of car IDs of cars with the given car type.
     */
    public Collection<Integer> getCarIdsByCarType(String crcName, CarType carType) {
    	Collection<Integer> out = new ArrayList<Integer>();
    	for (Car c : getCarsByCarType(crcName, carType)) {
    		out.add((int)c.getId().getId());
    	}
    	return out;
    }
    
    /**
     * Get the amount of cars of the given car type in the given car rental company.
     *
     * @param	crcName
	 * 			name of the car rental company
     * @param 	carType
     * 			the given car type
     * @return	A number, representing the amount of cars of the given car type.
     */
    public int getAmountOfCarsByCarType(String crcName, CarType carType) {
    	return this.getCarsByCarType(crcName, carType).size();
    }

	/**
	 * Get the list of cars of the given car type in the given car rental company.
	 *
	 * @param	crcName
	 * 			name of the car rental company
	 * @param 	carType
	 * 			the given car type
	 * @return	List of cars of the given car type
	 */
	private List<Car> getCarsByCarType(String crcName, CarType carType) {				
		// FIXME: use persistence instead

		EntityManager em =
				EMF.get().createEntityManager(); 
	
		try {
			TypedQuery<Car> query =
				em.createQuery("ctype.cars FROM CarType ctype "
						+ "WHERE cTye.compName=:compName",
						Car.class)
				.setParameter("compName",crcName);
	
			List<Car> cars = query.getResultList();
			return cars;
      
		} finally {
			em.close();
		}
	}

	/**
	 * Check whether the given car renter has reservations.
	 *
	 * @param 	renter
	 * 			the car renter
	 * @return	True if the number of reservations of the given car renter is higher than 0.
	 * 			False otherwise.
	 */
	public boolean hasReservations(String renter) {
		return this.getReservations(renter).size() > 0;		
	}	

}