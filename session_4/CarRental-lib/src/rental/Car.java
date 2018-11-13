package rental;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;
import static javax.persistence.CascadeType.PERSIST;
import static javax.persistence.CascadeType.REMOVE;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

@Entity
public class Car implements Serializable{


    private int id;
    private CarType type;
    
    private String company;

    private Set<Reservation> reservations;

    /***************
     * CONSTRUCTOR *
     ***************/
    
    public Car() {

    }

    /******
     * ID *
     ******/
    
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    public int getId() {
    	return id;
    }
   
    public void setId(int id){
        this.id = id;
    }
    /************
     * CAR TYPE *
     ************/
    
    @ManyToOne
    public CarType getType() {
        return type;
    }
	
	public void setType(CarType type) {
		this.type = type;
	}
    /****************
     * RESERVATIONS *
     ****************/

    public boolean isAvailable(Date start, Date end) {
        if(!start.before(end))
            throw new IllegalArgumentException("Illegal given period");

        for(Reservation reservation : reservations) {
            if(reservation.getEndDate().before(start) || reservation.getStartDate().after(end))
                continue;
            return false;
        }
        return true;
    }
    
    public void addReservation(Reservation res) {
        reservations.add(res);
    }
    
    public void removeReservation(Reservation reservation) {
        // equals-method for Reservation is required!
        reservations.remove(reservation);
    }

    @OneToMany(cascade={REMOVE, PERSIST})
    public Set<Reservation> getReservations() {
        return reservations;
    }

    public String getCompany() {
        return company;
    }
    
    public void setCompany(String company) {
        this.company = company;
    }

    public void setReservations(Set<Reservation> reservations) {
        this.reservations = reservations;
    }
    
    
}