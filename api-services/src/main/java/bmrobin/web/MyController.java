package bmrobin.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import bmrobin.service.MyService;
import bmrobin.data.Person;

import java.util.List;

/**
 * @author brobinson
 */
@Controller
public class MyController {

    @Autowired
    private MyService service;

    @RequestMapping("/")
    public String index(Model model) {
        String myTextVariable = "Hello MCJUG!";
        Person person = new Person();
        List<Person> people = service.findPeople();
        model.addAttribute("people", people);
        model.addAttribute("person", person);
        return "index";
    }

    @RequestMapping(value = "/person/{id}", method = RequestMethod.GET)
    @ResponseBody
    public String getPerson(@PathVariable Long id) {
        Person retrievedPerson = service.findPerson(id);
        return "Meet " + retrievedPerson.getFirstName() + " " + retrievedPerson.getLastName();
    }

    @RequestMapping(value = "/person/new", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Person savePerson(@ModelAttribute Person person) {
        return service.savePerson(person);
    }

    @RequestMapping(value = "/person/delete", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public void deletePeople() {
        service.deletePeople();
    }

}
