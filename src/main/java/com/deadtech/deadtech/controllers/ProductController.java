package com.deadtech.deadtech.controllers;

import com.deadtech.deadtech.models.Manufacturer;
import com.deadtech.deadtech.models.Product;
import com.deadtech.deadtech.models.User;
import com.deadtech.deadtech.models.data.ManufacturerDao;
import com.deadtech.deadtech.models.data.ProductDao;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

@Controller
@RequestMapping(value = "product")
public class ProductController {

    private MongoClientURI uri = new MongoClientURI(
            "mongodb+srv://dmmahala:<PASSWORD>@deadtech-eirhc.gcp.mongodb.net/test?retryWrites=true");

    private MongoClient mongoClient = new MongoClient(uri);
    private MongoDatabase database = mongoClient.getDatabase("DeadTech");

    @Autowired
    private ProductDao productDao;

    @Autowired
    private ManufacturerDao manufacturerDao;

    @Autowired
    private HttpSession httpSession;

    @RequestMapping(value = "")
    public String index(Model model) {

        model.addAttribute("title", "Products");
        model.addAttribute("products", productDao.findAll());

        return "product/index";
    }

    @RequestMapping(value = "view/{id}", method = RequestMethod.GET)
    public String displayProductPage(Model model, @PathVariable("id") int id) {

        Product product = productDao.findOne(id);
        Manufacturer manufacturer = product.getManufacturer();
        model.addAttribute("product", product);
        model.addAttribute("manufacturer", manufacturer);
        model.addAttribute("title", product.getName());

        User logged = (User) httpSession.getAttribute("user");

        if (!httpSession.isNew()) {

            if (!(logged == null)) {

                if (logged.isAdmin()) {
                    model.addAttribute("admin", true);
                }
            }

        }

        return "product/view";
    }

    @RequestMapping(value = "/add", method = RequestMethod.GET)
    public String displayAddProductForm(Model model) {

        if (httpSession.isNew() ) {

            httpSession.setAttribute("error", "You must be Logged in to add products.");

            return "redirect:/user/login";
        }

        if (httpSession.getAttribute("user") == null) {

            httpSession.setAttribute("error", "You must be Logged in to add products.");

            return "redirect:/user/login";
        }


        model.addAttribute("title", "Add a Product");
        model.addAttribute(new Product());
        model.addAttribute("manufacturers", manufacturerDao.findAll());

        return "product/add";
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public String processAddProductForm(@ModelAttribute @Valid Product newProduct, @RequestParam int manufacturerId, Errors errors, Model model) {

        if(errors.hasErrors()) {

            model.addAttribute("title", "Add a Product");
            model.addAttribute(new Product());

            return "product/add";
        }

        User user = (User) httpSession.getAttribute("user");

        if (user.isAdmin() == true) {

            Manufacturer manufacturer = manufacturerDao.findOne(manufacturerId);
            newProduct.setManufacturer(manufacturer);
            newProduct.setPending(false);
            productDao.save(newProduct);

            return "redirect:/product/view/" + newProduct.getId();
        }

        Manufacturer manufacturer = manufacturerDao.findOne(manufacturerId);
        newProduct.setManufacturer(manufacturer);
        newProduct.setPending(true);
        productDao.save(newProduct);

        return "redirect:/product/view/" + newProduct.getId();
    }

    @RequestMapping(value = "approve", method = RequestMethod.POST)
    public String approveProduct(Model model, HttpServletRequest request) {

        int productId = Integer.parseInt(request.getParameter("productId"));
        Product product = productDao.findOne(productId);
        product.setPending(false);
        productDao.save(product);

        return "redirect:/product/view/" + product.getId();
    }

}
