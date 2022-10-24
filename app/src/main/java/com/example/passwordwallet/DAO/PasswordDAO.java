package com.example.passwordwallet.DAO;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface PasswordDAO {
    //add new password
    @Insert
     void insertPassword(Password password);

    //get all users passwords
    @Query("SELECT * FROM passwords WHERE id_user = :user_id")
    List<Password> getPasswordsList(int user_id);

    //update password (after changing main password)
    @Update
    void updatePassword(Password password);
}
