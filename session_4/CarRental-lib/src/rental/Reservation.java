package rental;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Reservation extends Quote implements Serializable {

    @Id
    private int id;
    
    
    private int carId;
    
    /***************
     * CONSTRUCTOR *
     ***************/

    public Reservation() {
           
    }
    
    /******
     * ID *
     ******/
    
    public int getCarId() {
    	return carId;
    }
    
    public void setCarId(int carId) {
        this.id = carId;
    }
    
    
    /*************
     * TO STRING *
     *************/
    
    @Override
    public String toString() {
        return String.format("Reservation for %s from %s to %s at %s\nCar type: %s\tCar: %s\nTotal price: %.2f", 
                getCarRenter(), getStartDate(), getEndDate(), getRentalCompany(), getCarType(), getCarId(), getRentalPrice());
    }	
    
    public void setQuote(Quote quote){
        this.setCarRenter(quote.getCarRenter());
        this.setStartDate(quote.getStartDate());
        this.setEndDate(quote.getEndDate());
        this.setRentalCompany(quote.getRentalCompany());
        this.setCarType(quote.getCarType());
        this.setRentalPrice(quote.getRentalPrice());
    }
}