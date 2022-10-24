package com.example.passwordwallet.DAO;



import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface UserDAO {
    //create new user
    @Insert
    void insertUser(User user);

    //change users password
    @Update
    void updateUser(User user);

    @Query("SELECT salt FROM user WHERE login = :nick")
    List<String> getSalt(String nick);

    @Query("SELECT * FROM user WHERE login = :nick")
    List<User> getUser(String nick);

    @Query("SELECT id FROM user WHERE login = :nick")
    int getUserId(String nick);

    @Query("SELECT isPasswordKeptAsHash FROM user WHERE login =:nick")
    boolean isPasswordKeptAsHash(String nick);

    @Query("SELECT EXISTS (SELECT * FROM user WHERE login = :nick AND password_hash = :passwordHash)")
    boolean login(String nick, String passwordHash);

}


