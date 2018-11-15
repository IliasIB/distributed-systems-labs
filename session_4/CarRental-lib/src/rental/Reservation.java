package rental;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
public class Reservation extends Quote implements Serializable {

    private int id;
    
    private Car car;
    
    private int carId;
    
    /***************
     * CONSTRUCTOR *
     ***************/

    public Reservation() {
           
    }

    /******
     * ID *
     ******/
    
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    public int getId() {
    	return id;
    }

    
    public void setId(int id) {
        this.id = id;
    }

    public int getCarId() {
        return carId;
    }
    
    public void setCarId(int carId) {
        this.carId = carId;
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
    
    @Temporal(TemporalType.DATE)
    @Override
    public Date getStartDate() {
        return super.getStartDate();
    }

    @Temporal(TemporalType.DATE)
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

    @ManyToOne
    public Car getCar() {
        return car;
    }

    public void setCar(Car car) {
        this.car = car;
    }
}