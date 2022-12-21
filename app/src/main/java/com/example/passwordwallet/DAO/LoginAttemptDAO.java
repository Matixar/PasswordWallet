package com.example.passwordwallet.DAO;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.Calendar;
import java.util.List;

@Dao
public interface LoginAttemptDAO {
    @Insert
    void insertLoginAttempt(LoginAttempt login);

    @Update
    void updateLoginAttempt(LoginAttempt login);

    @Query("SELECT * FROM loginattempt WHERE ip_address = :ipAddress")
    List<LoginAttempt> getLoginAttempts(String ipAddress);

    @Query("SELECT login_time FROM loginattempt WHERE ip_address = :ipAddress AND user_login = :login")
    Calendar getAttemptTime(String ipAddress, String login);

    @Query("SELECT login_time FROM loginattempt WHERE ip_address = :ipAddress AND user_login = :login AND login_result = :successful ORDER BY 1 DESC LIMIT 1")
    Calendar getLastLoginTime(String ipAddress, String login, boolean successful);


}
