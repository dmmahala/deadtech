package com.deadtech.deadtech.controllers;

import com.deadtech.deadtech.models.Manufacturer;
import com.deadtech.deadtech.models.User;
import com.deadtech.deadtech.models.data.ManufacturerDao;
import com.deadtech.deadtech.models.data.UserDao;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.List;

@Controller
@RequestMapping(value = "manufacturer")
public class ManufacturerController {

    private MongoClientURI uri = new MongoClientURI(
            "mongodb+srv://dmmahala:<PASSWORD>@deadtech-eirhc.gcp.mongodb.net/test?retryWrites=true");

    private MongoClient mongoClient = new MongoClient(uri);
    private MongoDatabase database = mongoClient.getDatabase("DeadTech");

    @Autowired
    private ManufacturerDao manufacturerDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private HttpSession httpSession;

    @RequestMapping(value = "")
    public String index(Model model) {

        String adminError = (String) httpSession.getAttribute("error");
        httpSession.removeAttribute("error");

        model.addAttribute("title", "Manufacturers");
        model.addAttribute("manufacturers", manufacturerDao.findAll());
        model.addAttribute("adminError", adminError);

        return "manufacturer/index";
    }

    @RequestMapping(value = "view/{id}", method = RequestMethod.GET)
    public String displayManufacturerPage(Model model, @PathVariable("id") int id) {

        Manufacturer manufacturer = manufacturerDao.findOne(id);
        model.addAttribute("manufacturer", manufacturer);
        model.addAttribute("title", manufacturer.getName());
        model.addAttribute("products", manufacturer.getProducts());
        return "manufacturer/view";
    }

    @RequestMapping(value = "/add", method = RequestMethod.GET)
    public String displayAddManufacturerForm(Model model) {

        if (httpSession.isNew() ) {

            httpSession.setAttribute("error", "You must be logged in to add manufacturers.");

            return "redirect:/user/login";
        }

        User user = (User) httpSession.getAttribute("user");

        if (user == null) {

            httpSession.setAttribute("error", "You must be logged in to add manufacturers.");

            return "redirect:/user/login";
        }

        if (user.isAdmin() == false) {

            httpSession.setAttribute("error", "You must be an administrator to add manufacturers.");

            return "redirect:/manufacturer";
        }

        model.addAttribute("title", "Add a Manufacturer");
        model.addAttribute(new Manufacturer());

        return "manufacturer/add";
    }

    @RequestMapping(value = "add", method = RequestMethod.POST)
    public String processAddManufacturerForm(@ModelAttribute @Valid Manufacturer newManufacturer, Errors errors, Model model) {

        if (errors.hasErrors()) {

            model.addAttribute("title", "Add a Manufacturer");
            model.addAttribute(new Manufacturer());

            return "manufacturer/add";
        }

        manufacturerDao.save(newManufacturer);

        return "redirect:/manufacturer/view/" + newManufacturer.getId();
    }


}
