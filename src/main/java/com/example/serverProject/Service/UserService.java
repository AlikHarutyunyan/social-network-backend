package com.example.serverProject.Service;

import com.example.serverProject.DbUtils;
import com.example.serverProject.Exceptions.UserAlreadyExistsException;
import com.example.serverProject.Objects.Post;
import com.example.serverProject.Objects.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    @Autowired
    private DbUtils dbUtils;
    private final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

    public boolean signUp(String login, String password) {
        if (login == null || password == null) {
            return false;
        }
        password = bCryptPasswordEncoder.encode(password);
        String token = bCryptPasswordEncoder.encode(login + password);
        try {
            return dbUtils.registerUser(login, password, token);
        } catch (UserAlreadyExistsException e) {
            return false;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public User signIn(String login, String password) {
        return dbUtils.getUserToken(login, password, bCryptPasswordEncoder);
    }

    public boolean follow(String followerId, String followeName) {
        try {
            int followerIdNum = Integer.parseInt(followerId);
            int followeIdNum = dbUtils.getUserIdByName(followeName);
            return dbUtils.addFollower(followerIdNum, followeIdNum);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public boolean checkToken(String token) {
        return dbUtils.checkToken(token);
    }

    public List<String> searchUsers(String name) {
        return dbUtils.searchByName(name);
    }

    public boolean publishPost(String text, String userId) {
        try {
            return dbUtils.publishPost(text, Integer.parseInt(userId));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public List<Post> getFeed(List<String> names) {
        return dbUtils.getFeed(names);
    }

    public User getUserByName(String name) {
        return dbUtils.getUserByName(name);
    }

    public List<String> getUserFollowers(int id) {
        return dbUtils.getUserRelations(id, true);
    }

    public List<String> getUserFollowes(int id) {
        return dbUtils.getUserRelations(id, false);
    }

    public boolean updateImageUrl(String url, String userId) {
        try {
            return dbUtils.updateImageUrl(url, Integer.parseInt(userId.trim()));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }
}
