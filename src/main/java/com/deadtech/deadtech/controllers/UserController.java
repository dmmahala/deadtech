package com.deadtech.deadtech.controllers;

import com.deadtech.deadtech.models.User;
import com.deadtech.deadtech.models.data.UserDao;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("user")
public class UserController {

    private MongoClientURI uri = new MongoClientURI(
            "mongodb+srv://dmmahala:<PASSWORD>@deadtech-eirhc.gcp.mongodb.net/test?retryWrites=true");

    private MongoClient mongoClient = new MongoClient(uri);
    private MongoDatabase database = mongoClient.getDatabase("DeadTech");

    @Autowired
    private UserDao userDao;

    @Autowired
    private HttpSession httpSession;

    @RequestMapping(value = "")
    public String index(Model model) {

        model.addAttribute("users", userDao.findAll());
        model.addAttribute("title", "User List");

        return "user/index";
    }

    @RequestMapping(value = "view/{id}", method = RequestMethod.GET)
    public String displayUserPage(Model model, @PathVariable("id") int id) {

        if (httpSession.isNew() ) {

            User user = userDao.findOne(id);

            model.addAttribute("user", user);
            model.addAttribute("title", user.getName());

            return "user/view";
        }

        User logged = (User) httpSession.getAttribute("user");

        if (logged == null) {

            User user = userDao.findOne(id);

            model.addAttribute("user", user);
            model.addAttribute("title", user.getName());

            return "user/view";
        }

        if (logged.isAdmin()) {

            User user = userDao.findOne(id);

            model.addAttribute("user", user);
            model.addAttribute("title", user.getName());
            model.addAttribute("admin", true);

            return "user/view";
        }

        User user = userDao.findOne(id);

        model.addAttribute("user", user);
        model.addAttribute("title", user.getName());

        return "user/view";
    }

    @RequestMapping(value = "new_user", method = RequestMethod.GET)
    public String displayNewUserForm(Model model) {

        model.addAttribute("title", "New User");
        model.addAttribute(new User());

        return "user/new";
    }

    @RequestMapping(value = "new_user", method = RequestMethod.POST)
    public String processNewUserForm(@ModelAttribute @Valid User newUser, Errors errors, Model model){

        if(errors.hasErrors()) {
            model.addAttribute("title", "New User");
            model.addAttribute(new User());
            return "user/new";
        }

        userDao.save(newUser);
        httpSession.setAttribute("user", newUser);

        return "redirect:/user/view/" + newUser.getId();
    }

    @RequestMapping(value = "login", method = RequestMethod.GET)
    public String displayLoginUserForm(Model model) {

        String loginError = (String) httpSession.getAttribute("error");
        httpSession.removeAttribute("error");

        model.addAttribute("loginError", loginError);
        model.addAttribute("title", "Login");
        model.addAttribute(new User());

        return "user/login";
    }

    @RequestMapping(value = "login", method = RequestMethod.POST)
    public String processLoginUserForm(@ModelAttribute @Valid User logUser, Errors errors, Model model) {

        if (errors.hasErrors()) {

            model.addAttribute("title", "Login");
            model.addAttribute(new User());

            return "user/login";
        }

        String logName = logUser.getName();
        String logPass = logUser.getPassword();

        Iterable<User> users = userDao.findAll();

        boolean isUser = false;

        for (User user : users) {
            if (user.getName().equals(logName)) {
                if (user.getPassword().equals(logPass)) {
                    logUser = user;
                    isUser = true;
                    break;
                }
            }
        }

        if (isUser) {

            httpSession.setAttribute("user", logUser);

            return "redirect:/user/view/" + logUser.getId();
        } else {

            model.addAttribute("title", "Login");
            model.addAttribute(new User());

            return "user/login";
        }

    }

    @RequestMapping(value = "logout")
    public String processLogout() {

        httpSession.removeAttribute("user");

        return "redirect:/user";
    }

    @RequestMapping(value = "promote", method = RequestMethod.POST)
    public String promoteAdmin(Model model, HttpServletRequest request) {

        int userId = Integer.parseInt(request.getParameter("userId"));
        User user = userDao.findOne(userId);
        user.setAdmin(true);
        userDao.save(user);

        return "redirect:/user/view/" + user.getId();
    }
}
