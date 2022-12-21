package com.example.passwordwallet.DAO;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Calendar;


@Entity(tableName = "LoginAttempt",
        foreignKeys = {@ForeignKey(entity = User.class, parentColumns = "login", childColumns = "user_login"),
                    @ForeignKey(entity = IpAddress.class, parentColumns = "ip_address", childColumns = "ip_address")},
        indices = {@Index(value = "ip_address"), @Index(value = "user_login")})
public class LoginAttempt {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "login_time")
    private Calendar login_time;

    @ColumnInfo(name = "login_result")
    private boolean login_result;

    @ColumnInfo(name = "ip_address")
    private String ip_address;

    @ColumnInfo(name = "user_login")
    private String user_login;

    public LoginAttempt(int id, Calendar login_time, boolean login_result, String ip_address, String user_login) {
        this.id = id;
        this.login_time = login_time;
        this.login_result = login_result;
        this.ip_address = ip_address;
        this.user_login = user_login;
    }

    @Ignore
    public LoginAttempt(Calendar login_time, boolean login_result, String ip_address, String user_login) {
        this.login_time = login_time;
        this.login_result = login_result;
        this.ip_address = ip_address;
        this.user_login = user_login;
    }

    public int getId() {
        return id;
    }

    public Calendar getLogin_time() {
        return login_time;
    }

    public boolean isLogin_result() {
        return login_result;
    }

    public String getIp_address() {
        return ip_address;
    }

    public String getUser_login() {
        return user_login;
    }
}
