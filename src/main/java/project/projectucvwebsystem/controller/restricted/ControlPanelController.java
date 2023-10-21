package project.projectucvwebsystem.controller.restricted;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.RequestContextHolder;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import project.projectucvwebsystem.Routes.Render;
import project.projectucvwebsystem.service.ProductService;
import project.projectucvwebsystem.service.SupplierService;

@Controller
@RequestMapping("/restricted")
public class ControlPanelController {

    @Autowired
    ProductService productService;

    @Autowired
    SupplierService supplierService;

    @Autowired
    HttpServletRequest request;

    @Autowired
    HttpServletResponse response;

    /*
     * Variables from DB:
     * Leyenda: 1Graph: Category, 2Graph: Name Large
     * Variable: Quantity
     */
    
    @GetMapping("/control-panel") //corregir /login-view/control-panel antes era /control-panel
    public String ControlPanel (Model model) {

        List<Object[]> listSupplierData = supplierService.findAllSuppliers();
        String supplierData = listSupplierData.stream()
        .map(supplier -> supplier[0]+"-"+supplier[1]+"-"+supplier[2]+"-"+supplier[3]+"-"+supplier[4])
        .collect(Collectors.joining(","));
        model.addAttribute("SupplierData", supplierData);
        /*for (Object[] supplier : listSupplierData) {
            System.out.println(supplier[1]);
        }*/

        String[] categories = productService.getCategories();
        String Categories = Arrays.stream(categories)
        .map(category -> ""+category+"").collect(Collectors.joining(","));
        model.addAttribute("Categories", Categories);

        List<Object[]> ObjectNameLargeAndQuantities = productService.getNameLargeAndQuantities();
        String joinedString = ObjectNameLargeAndQuantities.stream()
        .map(row -> row[0]+"-"+row[1])
        .collect(Collectors.joining(","));
        model.addAttribute("ObjectNameLargeAndQuantities", joinedString);
        //model.addAttribute("variable", userService.QuantityUsers());
        return Render.CONTROL_PANEL.name();
    }

    @PostMapping("/control-panel/logout")
    public String Logout () {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("token".equals(cookie.getName())) {
                    cookie.setMaxAge(0);
                    cookie.setPath("/restricted");
                    response.addCookie(cookie);
                    break;
                }
            }
        }

        return "redirect:/restricted/login-view";
    }

    @PostMapping("/control-panel/delete-supplier") //@DeleteMapping
    public String DeleteSupplier (
        @RequestParam("supplierID") int ID
    ) {
        Cookie cookie = new Cookie("removed", "true");
        cookie.setMaxAge(60*60);
        cookie.setPath("/restricted");
        response.addCookie(cookie);

        supplierService.DeleteOnlySupplier(ID);

        return "redirect:/restricted/control-panel";
    }

    @PostMapping("/control-panel/update-supplier") //@PutMapping | @PatchMapping
    public String UpdateSupplier (
        /*@RequestParam("input") String value,*/ //ID input create dinamically. Then with .jsx send the value in a cookie
        @RequestParam("supplierID") int ID
    ) throws InterruptedException {

        Cookie[] cookies = request.getCookies();
        String extractedColumn = null;
        String extractedValue = null;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("extractedColumn".equals(cookie.getName())) {
                    extractedColumn = cookie.getValue();
                    break;
                }
            }
            for (Cookie cookie : cookies) {
                if ("extractedValue".equals(cookie.getName())) {
                    extractedValue = cookie.getValue();
                    break;
                }
            }
        }

        //idValue.substring(1) idValue = input-columnIndex
        System.out.println("Column: "+extractedColumn);
        System.out.println("Value: "+extractedValue);
        
        switch(extractedColumn) {
            case "1" -> { //Case = {num of column}
                supplierService.UpdateSupplierDescriptionName(extractedValue, ID);
            }
            case "2" -> {
                supplierService.UpdateSupplierNumber(extractedValue, ID);
            }
            case "3" -> {
                supplierService.UpdateSupplierEmail(extractedValue, ID);
            }
            case "4" -> {
                supplierService.UpdateSupplierAddress(extractedValue, ID);
            }
        }

        //supplierService.UpdateSupplierNumber(attibute, ID);
        //Thread.sleep(3000);

        return "redirect:/restricted/control-panel";
    }

}
