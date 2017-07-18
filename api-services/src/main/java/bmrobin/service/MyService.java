package bmrobin.service;

import bmrobin.data.Person;
import bmrobin.exception.NotFoundException;

import java.util.List;

/**
 * @author brobinson
 */
public interface MyService {

    Person savePerson(Person person);
    Person findPerson(Long id) throws NotFoundException;
    List<Person> findPeople();
    void deletePeople();

}
