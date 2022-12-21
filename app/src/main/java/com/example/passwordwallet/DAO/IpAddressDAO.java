package com.example.passwordwallet.DAO;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface IpAddressDAO {

    @Insert
    void insertIpAddress(IpAddress ipAddress);

    @Update
    void updateIpAddress(IpAddress ipAddress);

    @Query("SELECT * FROM ipAddress WHERE ip_address = :ipAddress")
    List<IpAddress> getIpAddress(String ipAddress);

    @Query("SELECT successful_logins FROM ipAddress WHERE ip_address = :ipAddress")
    int getSuccessfulLogins(String ipAddress);

    @Query("SELECT unsuccessful_logins FROM ipAddress WHERE ip_address = :ipAddress")
    int getUnsuccessfulLogins(String ipAddress);

    @Query("SELECT blocked FROM ipAddress WHERE ip_address = :ipAddress")
    boolean isAddressBlocked(String ipAddress);


}
