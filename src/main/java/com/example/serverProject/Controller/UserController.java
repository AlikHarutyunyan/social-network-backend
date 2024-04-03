package com.example.serverProject.Controller;

import com.example.serverProject.Objects.Post;
import com.example.serverProject.Objects.User;
import com.example.serverProject.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
@CrossOrigin("*")
@RestController
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("signUp")
    public ResponseEntity<HttpStatus> signUp(@RequestBody Map<String, String> requestBody) {
        if (userService.signUp(requestBody.get("login"), requestBody.get("password"))) {
            return new ResponseEntity<>(HttpStatus.CREATED);
        }
        return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
    }

    @PostMapping("signIn")
    public ResponseEntity<User> signIn(@RequestBody Map<String, String> requestBody) {
        User user = userService.signIn(requestBody.get("login"), requestBody.get("password"));
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PostMapping("follow")
    public ResponseEntity<HttpStatus> follow(@RequestHeader("Authorization") String token, @RequestBody Map<String, String> requestBody) {
        if (userService.checkToken(token)) {
            if (userService.follow(requestBody.get("followerId"), requestBody.get("followeName"))) {
                return new ResponseEntity<>(HttpStatus.ACCEPTED);
            }
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }else{
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("search")
    public ResponseEntity<List<String>> searchUsers(@RequestHeader("Authorization") String token,@RequestParam String name){
        if (userService.checkToken(token)) {
           return new ResponseEntity<>(userService.searchUsers(name),HttpStatus.OK);
        }else{
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("updateImage")
    public ResponseEntity<HttpStatus> updateImage(@RequestHeader("Authorization") String token,@RequestBody Map<String, String> requestBody){
        if (userService.checkToken(token)) {
            if(userService.updateImageUrl(requestBody.get("imageUrl"),requestBody.get("id"))){
                return new ResponseEntity<>(HttpStatus.OK);
            }
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }else{
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("publish")
    public ResponseEntity<HttpStatus> publishPost(@RequestHeader("Authorization") String token,@RequestBody Map<String, String> requestBody){
        if (userService.checkToken(token)) {
            if(userService.publishPost(requestBody.get("text"),requestBody.get("userId"))){
                return new ResponseEntity<>(HttpStatus.CREATED);
            }
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }else{
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("feed")
    public ResponseEntity<List<Post>> getFeed(@RequestHeader("Authorization") String token,@RequestBody Map<String, List<String>> requestBody){
        if (userService.checkToken(token)) {
            try{
                return new ResponseEntity<>(userService.getFeed(requestBody.get("names")), HttpStatus.OK);
            }catch (Exception e){
                throw new RuntimeException();
            }
        }else {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping("user/{name}")
    public ResponseEntity<User> getUserByName(@RequestHeader("Authorization") String token,@PathVariable String name){
        if (userService.checkToken(token)) {
            User user = userService.getUserByName(name);
            HttpStatus status = user == null ? HttpStatus.NOT_FOUND : HttpStatus.OK;
            return new ResponseEntity<>(user,status);
        }else{
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping("getUserFollowers/{id}")
    public ResponseEntity<List<String>> getUserFollowers(@RequestHeader("Authorization") String token,@PathVariable int id){
        if (userService.checkToken(token)) {
            return new ResponseEntity<>(userService.getUserFollowers(id),HttpStatus.OK);
        }else{
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }
    @GetMapping("getUserFollowes/{id}")
    public ResponseEntity<List<String>> getUserFollowes(@RequestHeader("Authorization") String token,@PathVariable int id){
        if (userService.checkToken(token)) {
            return new ResponseEntity<>(userService.getUserFollowes(id),HttpStatus.OK);
        }else{
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }
}
