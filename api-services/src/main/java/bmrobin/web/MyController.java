package bmrobin.web;

import bmrobin.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import bmrobin.service.MyService;
import bmrobin.data.Person;

import java.util.List;

/**
 * @author brobinson
 */
@RestController
@RequestMapping(value = "/person")
public class MyController {

    private final MyService service;

    @Autowired
    public MyController(MyService service) {
        this.service = service;
    }

    @RequestMapping("/")
    public List<Person> index() {
        return service.findPeople();
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Person getPerson(@PathVariable Long id) throws NotFoundException {
        return service.findPerson(id);
    }

    @RequestMapping(value = "/new",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public Person savePerson(@RequestBody Person person) {
        return service.savePerson(person);
    }

    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK)
    public void deletePeople() {
        service.deletePeople();
    }

}
