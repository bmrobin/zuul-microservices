package bmrobin.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import bmrobin.data.MyRepository;
import bmrobin.data.Person;

import java.util.ArrayList;
import java.util.List;

/**
 * @author brobinson
 */
@Service
@Profile("stub")
public class MyServiceImplStub implements MyService {

    // enable with --spring.profiles.active=stub

    @Value("${first}")
    private String firstName;

    @Value("${last}")
    private String lastName;

    @Autowired
    private MyRepository repository;

    public Person savePerson(Person person) {
        System.out.println("saving " + this.firstName + " " + this.lastName);

        return new Person();
    }

    public Person findPerson(Long id) {
        Person test1 = new Person();
        test1.setFirstName(this.firstName);
        test1.setLastName(this.lastName);
        return test1;
    }

    public List<Person> findPeople() {
        List<Person> people = new ArrayList<Person>();

        Person test1 = new Person();
        test1.setFirstName(this.firstName);
        test1.setLastName(this.lastName);
        test1.setId(1L);
        people.add(test1);

        return people;
    }

    public void deletePeople() {
        System.out.println("deleting stub people");
    }
}
