package bmrobin.data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * @author brobinson
 */
@Entity
public class Person {

    @Id
    @GeneratedValue
    private Long id;

    private String myFirstName;

    private String myLastName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return myFirstName;
    }

    public void setFirstName(String myFirstName) {
        this.myFirstName = myFirstName;
    }

    public String getLastName() {
        return myLastName;
    }

    public void setLastName(String myLastName) {
        this.myLastName = myLastName;
    }
}
