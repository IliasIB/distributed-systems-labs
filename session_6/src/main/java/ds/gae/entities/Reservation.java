package ds.gae.entities;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.google.appengine.api.datastore.Key;

@Entity
public class Reservation extends Quote {

	private Key id;
    private int carId;
    
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
    public Key getId() {
		return id;
	}

	public void setId(Key id) {
		this.id = id;
	}

	public void setCarId(int carId) {
		this.carId = carId;
	}

	/***************
	 * CONSTRUCTOR *
	 ***************/

    Reservation(Quote quote, int carId) {
    	super(quote.getCarRenter(), quote.getStartDate(), quote.getEndDate(), 
    			quote.getRentalCompany(), quote.getCarType(), quote.getRentalPrice());
        this.carId = carId;
    }
    
    /******
     * ID *
     ******/
    
    public int getCarId() {
    	return carId;
    }
    
    @Override
    public Date getStartDate() {
        return super.getStartDate();
    }

    @Override
    public Date getEndDate() {
        return super.getEndDate();
    }

    @Override
    public String getCarRenter() {
        return super.getCarRenter();
    }

    @Override
    public String getRentalCompany() {
        return super.getRentalCompany();
    }

    @Override
    public double getRentalPrice() {
        return super.getRentalPrice();
    }
    
    @Override
    public String getCarType() {
		return super.getCarType();
	}
    
    public void setStartDate(Date startDate) {
		super.setStartDate(startDate);
	}

	public void setEndDate(Date endDate) {
		super.setEndDate(endDate);
	}

	public void setCarRenter(String carRenter) {
		super.setCarRenter(carRenter);
	}

	public void setRentalCompany(String rentalCompany) {
		super.setRentalCompany(rentalCompany);
	}

	public void setCarType(String carType) {
		super.setCarType(carType);
	}

	public void setRentalPrice(double rentalPrice) {
		super.setRentalPrice(rentalPrice);
	}
    
    /*************
     * TO STRING *
     *************/
    
    @Override
    public String toString() {
        return String.format("Reservation for %s from %s to %s at %s\nCar type: %s\tCar: %s\nTotal price: %.2f", 
                getCarRenter(), getStartDate(), getEndDate(), getRentalCompany(), getCarType(), getCarId(), getRentalPrice());
    }
    
    @Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + carId;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (!super.equals(obj))
			return false;
		Reservation other = (Reservation) obj;
		if (carId != other.carId)
			return false;
		return true;
	}
}