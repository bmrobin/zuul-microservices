package bmrobin.service;

import bmrobin.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
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
public class MyServiceImpl implements MyService {

    private final MyRepository repository;

    @Autowired
    public MyServiceImpl(MyRepository repository) {
        this.repository = repository;
    }

    public Person savePerson(Person person) {
        return repository.save(person);
    }

    public Person findPerson(Long id) throws NotFoundException {
        Person person = repository.findOne(id);
        if (person == null) {
            throw new NotFoundException();
        }
        return person;
    }

    public List<Person> findPeople() {
        List<Person> people = new ArrayList<Person>();

        for (Person person : repository.findAll()) {
            people.add(person);
        }

        return people;
    }

    public void deletePeople() {
        repository.deleteAll();
    }
}
