package com.example.passwordwallet.DAO;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

//database class, database made with Android Room
@Database(entities = {User.class, Password.class, IpAddress.class, LoginAttempt.class},exportSchema = false, version = 2)
@TypeConverters({Converters.class})
public abstract class UserDatabase extends RoomDatabase {
    //name of database
    private static final String DB_NAME = "user_db";
    private static UserDatabase instance;

    //create new database instance
    public static synchronized UserDatabase getInstance(Context context) {
        if(instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(), UserDatabase.class,DB_NAME)
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }

    //dao methods to use database in application
    public abstract UserDAO userDao();
    public abstract PasswordDAO passwordDao();
    public abstract IpAddressDAO ipAddressDAO();
    public abstract LoginAttemptDAO loginAttemptDAO();

}
