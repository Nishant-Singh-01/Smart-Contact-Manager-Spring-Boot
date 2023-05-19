package com.example.smartcontactmanager;

import com.example.smartcontactmanager.entities.Contact;
import com.example.smartcontactmanager.entities.User;
import com.example.smartcontactmanager.helper.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;


@Controller
public class HomeController {
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

//    @Autowired
//    private UserRepository userRepository;
//    @GetMapping("/test")
//    @ResponseBody
//    public String test(){
//        User user=new User();
//        user.setName("Nishant Singh");
//        user.setEmail("nishant@123gmail.com");
//
//        Contact contact=new Contact();
//        user.getContacts().add(contact);
//        userRepository.save(user);
//     return "Working";
   // }

   @Autowired
    private UserRepository userRepository;

    @RequestMapping("/")
    public String home(Model model){
        model.addAttribute("title","Home-smart contact Manager");
        return "home";
    }

    @RequestMapping("/about")
    public String about(Model model){
        model.addAttribute("title","About-smart contact Manager");
        return "about";
    }
    @RequestMapping("/signup")
    public String signup(Model model){
        model.addAttribute("title","Register-smart contact Manager");
        model.addAttribute("user",new User());
        return "signup";
    }
    //handler for registering user
//    @RequestMapping(value = "/do_register",method = RequestMethod.POST)
    @PostMapping("/do_register")
    public String registerUser(@Valid @ModelAttribute("user") User user,BindingResult result1, @RequestParam(value = "agreement",defaultValue = "false")boolean agreement, Model model, RedirectAttributes redirectAttributes
                               ){

     try{
         if(!agreement){
             System.out.println("You have not agreed the terms and condition");
             throw new Exception("You have not agreed the terms and condition");
         }
         if(result1.hasErrors()){
             model.addAttribute("user",user);
             return "signup";
         }


         user.setRole("ROLE_USER");
         user.setEnabled(true);
         user.setImageUrl("contact.png");
         user.setPassword(passwordEncoder.encode(user.getPassword()));//for encoded password in database::

         System.out.println("Agreement "+agreement);
         System.out.println("User "+user);

         User result=this.userRepository.save(user);

       //  model.addAttribute("user",new User());session.setAttribute("message",new Message("Successfully Register!!","alert-succees"));
         redirectAttributes.addFlashAttribute("success","Register Successfully");
         return "redirect:/signup";
     }
     catch (Exception e){
         e.printStackTrace();
         model.addAttribute("user",user);
         //in session attribute it takes message and type from helper package -- Message is the Entity Name
       //session.setAttribute("message",new Message("Something went wrong!!"+e.getMessage(),"alert-danger"));
         redirectAttributes.addFlashAttribute("success","Register Not Successfully");

         return "redirect:/signup";
     }


    }
    //handler for custom login
    @RequestMapping("/customLogin")
    public  String customLogin(Model model){
        model.addAttribute("title","Login Page");
        return "login";
}
}
