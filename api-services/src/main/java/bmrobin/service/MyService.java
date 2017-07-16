package bmrobin.service;

import bmrobin.data.Person;

import java.util.List;

/**
 * @author brobinson
 */
public interface MyService {

    Person savePerson(Person person);
    Person findPerson(Long id);
    List<Person> findPeople();
    void deletePeople();

}
