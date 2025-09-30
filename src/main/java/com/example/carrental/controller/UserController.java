package com.example.carrental.controller;
import com.example.carrental.model.UserModel;
import com.example.carrental.services.UserService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/users")


public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserModel> getAllUsers() {
        return userService.getallUsers();
    }

    @PostMapping
    public UserModel createUser(@RequestBody UserModel user){
        return userService.saveUser(user);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id){
        userService.deleteUser(id);
    }

}
