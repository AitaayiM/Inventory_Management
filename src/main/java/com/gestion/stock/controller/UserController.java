package com.gestion.stock.controller;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.gestion.stock.Repository.UserRepository;
import com.gestion.stock.entity.User;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@CrossOrigin(origins = "https://localhost:8081")
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private final UserRepository repository;

    public static final String LOGGED_IN_ATTRIBUTE = "loggedIn";

    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @GetMapping("/logout")
    public ModelAndView showLogout(HttpSession session) {
        session.setAttribute(LOGGED_IN_ATTRIBUTE, false);
        return new ModelAndView("login");
    }

    @GetMapping("/login")
    public ModelAndView showLogin() {
        return new ModelAndView("login");
    }

    @PostMapping("/login")
    public ModelAndView login(@RequestParam("username") String username,
                            @RequestParam("password") String password,
                            HttpSession session,
                            Model model) {
        User user = repository.findByUsername(username);
        if (user != null && passwordEncoder.matches(password, user.getPassword()) && user.isStatus()) {
            session.setAttribute(LOGGED_IN_ATTRIBUTE, true);
            return showUsers(model);
        } else {
            model.addAttribute("msg", "Invalid username or password");
            return new ModelAndView("login");
        }
    }

    @GetMapping("/signup")
    public ModelAndView showSignUp() {
        return new ModelAndView("signup");
    }

    @PostMapping("/signup")
    public ModelAndView signUp(@RequestParam("username") String username,
                            @RequestParam("password") String password,
                            @RequestParam("fullName") String fullName,
                            @RequestParam("email") String email,
                            Model model) {

        User user = repository.findByUsername(username);
        if (user != null) {
            model.addAttribute("msg", "Invalid username");
            return new ModelAndView("login");
        } else {
            String encodedPassword = passwordEncoder.encode(password);
            User newUser = new User(null, username, encodedPassword, fullName, email, false);
            repository.save(newUser);
            model.addAttribute("msg", "User ajouté avec succès");
            return new ModelAndView("login");
        }
    }

    @GetMapping("/users/{id}/toggleStatus")
    public ModelAndView toggleUserStatus(@PathVariable("id") int id, Model model) {
        User user = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));
        user.setStatus(!user.isStatus());
        repository.save(user);
        return showUsers(model);
    }

    @GetMapping("/users/{id}/toggleDelete")
    public ModelAndView toggleUserDelete(@PathVariable("id") int id, Model model) {
        User user = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));
        repository.delete(user);
        return showUsers(model);
    }


    @GetMapping("/users")
    public ModelAndView showUsers(Model model) {
        List<User> users = repository.findAll();
        model.addAttribute("users", users);
        return new ModelAndView("users-list");
    }
}
