package com.example.smartcontactmanager;

import com.example.smartcontactmanager.entities.Contact;
import com.example.smartcontactmanager.entities.User;
import com.example.smartcontactmanager.helper.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ContactRepository contactRepository;
    //Method for Adding Common data to Response
    @ModelAttribute
    public void addCommonData(Model model,Principal principal){
        String userName= principal.getName();
        System.out.println("USERNAME "+userName);
        //get the user using userName(email)
        User user= userRepository.getUserByUserName(userName);
        System.out.println("USER "+user);
        model.addAttribute("user",user);


    }
    // DashBoard Home
    @RequestMapping("/index")
    public String dashboard(Model model, Principal principal){
        model.addAttribute("title","User DashBoard");

        return "normal/user_dashboard";
    }
    //open add form controller
    @RequestMapping("/add-contact")
    public String openAddContactForm(Model model){
        model.addAttribute("title","Add Contact");
        model.addAttribute("contact",new Contact());
        return "normal/add_contact_form";
    }

    //processing add contact form
    @PostMapping("/process-contact")
    public String processContact(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file, Principal principal ,RedirectAttributes redirectAttributes){
        try {
            String name = principal.getName();
            User user = userRepository.getUserByUserName(name);

            //processing and uploading file..
            if(file.isEmpty()){
                System.out.println("File is empty!!");
                contact.setImage("contact.png");
            }else {
                contact.setImage(file.getOriginalFilename());
               File saveFile= new ClassPathResource("static/image").getFile();
                Path path= Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
                Files.copy(file.getInputStream(),path, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Images is uploaded!!!");
            }

            contact.setUser(user);
            user.getContacts().add(contact);
            this.userRepository.save(user);

            System.out.println("DATA" + Arrays.toString(new Contact[]{contact}));
            System.out.println("Added To Databases");
            //Message Success...
            redirectAttributes.addFlashAttribute("success","Contact Added Successfully");
            return "redirect:/user/add-contact";
        }catch (Exception e){
            System.out.println("Error "+e.getMessage());
            e.printStackTrace();
            //Error Message..
            redirectAttributes.addFlashAttribute("message","Something Went to Wrong!!" );
            return "redirect:/user/add_contact";
        }

    }
    //Show Contact Handler...
    @GetMapping("/show_contacts/{page}")
    public String showContacts(@PathVariable("page") Integer page,Model model,Principal principal){
        model.addAttribute("title","Show User Contacts");
        //contact ki list ko Bhejna hai.....
       String userName=principal.getName();
       User user=this.userRepository.getUserByUserName(userName);
       //currentPage=page
       //Contact Per Page=5
        Pageable pageable =PageRequest.of(page,8);
        Page<Contact> contacts=this.contactRepository.findContactByUser(user.getId(),pageable);
       model.addAttribute("contacts",contacts);
       model.addAttribute("currentPage",page);
       model.addAttribute("totalPages",contacts.getTotalPages());
        return "normal/show_contacts";
    }
    //Showing Particular Contact Details..
    @RequestMapping("/contact/{cId}")
    public  String showContactDetail(@PathVariable("cId") Integer cId,Model model, Principal principal){
        Optional<Contact> contactOptional=this.contactRepository.findById(cId);
        Contact contact=contactOptional.get();
        String userName=principal.getName();
        User user=this.userRepository.getUserByUserName(userName);


      if(user.getId()==contact.getUser().getId()){
          model.addAttribute("contact",contact);
          model.addAttribute("title",contact.getName());
      }

        return "normal/contact_detail";
    }
//Delete contact handler
    @GetMapping("/delete/{cid}")
    public  String deleteContact(@PathVariable("cid")Integer cId,Model model,RedirectAttributes redirectAttributes){
        Optional<Contact> contactOptional=this.contactRepository.findById(cId);
        Contact contact=contactOptional.get();
        //unlink between contact and user
                contact.setUser(null);
        this.contactRepository.delete(contact);


        // Here We Use a new method to delete because in database value not delete


        redirectAttributes.addFlashAttribute("success","Contact Deleted Successfully");
        return "redirect:/user/show_contacts/0";
    }
    // Open Update form
    @PostMapping("/update-contact/{cid}")
    public String updateForm(@PathVariable("cid") Integer cId,Model model){
        model.addAttribute("title","Update Contact");
        Contact contact=this.contactRepository.findById(cId).get();
        model.addAttribute("contact",contact);
        return "normal/update_form";
    }
    //update contact Handler
    @RequestMapping(value = "/process-update",method = RequestMethod.POST)
    public String updateHandler(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file,Model model,RedirectAttributes redirectAttributes,Principal principal){
        try{
            Contact oldContactDetail=this.contactRepository.findById(contact.getcId()).get();
            //image check
            if(!file.isEmpty()){
          //delete old photo....
                File deleteFile= new ClassPathResource("static/image").getFile();
                File file1=new File(deleteFile,oldContactDetail.getImage());

            //update new photo
                File saveFile= new ClassPathResource("static/image").getFile();
                Path path= Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
                Files.copy(file.getInputStream(),path, StandardCopyOption.REPLACE_EXISTING);
                contact.setImage(file.getOriginalFilename());
            }
            else{
                contact.setImage(oldContactDetail.getImage());
            }


            User user=this.userRepository.getUserByUserName(principal.getName());
            contact.setUser(user);
            this.contactRepository.save(contact);
            redirectAttributes.addFlashAttribute("success","Contact is Updated Successfully");
        }catch (Exception e){

        }
        return "redirect:/user/show_contacts/0";
    }

    //  Your Profile Handler...
    @GetMapping("/profile")
    public String yourProfile(Model model){
        model.addAttribute("title","Profile Page");
        return "normal/profile";
    }


}
