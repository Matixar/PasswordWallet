package com.example.passwordwallet.DAO;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "user", indices = {@Index(value = {"login"}, unique = true)})

public class User {
    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name = "login")
    private final String login;
    @ColumnInfo(name = "password_hash")
    private final String password_hash;
    @ColumnInfo(name = "salt")
    private final String salt;
    @ColumnInfo(name = "isPasswordKeptAsHash")
    private final boolean isPasswordKeptAsHash;

    public User(int id, String login, String password_hash, String salt, Boolean isPasswordKeptAsHash) {
        this.id = id;
        this.login = login;
        this.password_hash = password_hash;
        this.salt = salt;
        this.isPasswordKeptAsHash = isPasswordKeptAsHash;
    }

    @Ignore
    public User(String login, String password_hash, String salt, Boolean isPasswordKeptAsHash) {
        this.login = login;
        this.password_hash = password_hash;
        this.salt = salt;
        this.isPasswordKeptAsHash = isPasswordKeptAsHash;
    }

    public int getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword_hash() {
        return password_hash;
    }

    public String getSalt() {
        return salt;
    }

    public boolean isPasswordKeptAsHash() {
        return isPasswordKeptAsHash;
    }
}
