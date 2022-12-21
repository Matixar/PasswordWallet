package com.example.passwordwallet.DAO;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "ipAddress",indices = {@Index(value = "ip_address", unique = true)})
public class IpAddress {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "ip_address")
    private String ip_address;

    @ColumnInfo(name = "successful_logins")
    private int successful_logins;

    @ColumnInfo(name = "unsuccessful_logins")
    private int unsuccessful_logins;

    @ColumnInfo(name = "blocked")
    private boolean blocked;

    public IpAddress(int id, String ip_address, int successful_logins, int unsuccessful_logins, boolean blocked) {
        this.id = id;
        this.ip_address = ip_address;
        this.successful_logins = successful_logins;
        this.unsuccessful_logins = unsuccessful_logins;
        this.blocked = blocked;
    }

    @Ignore
    public IpAddress(String ip_address, int successful_logins, int unsuccessful_logins, boolean blocked) {
        this.ip_address = ip_address;
        this.successful_logins = successful_logins;
        this.unsuccessful_logins = unsuccessful_logins;
        this.blocked = blocked;
    }

    public int getId() {
        return id;
    }

    public String getIp_address() {
        return ip_address;
    }

    public int getSuccessful_logins() {
        return successful_logins;
    }

    public int getUnsuccessful_logins() {
        return unsuccessful_logins;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setSuccessful_logins(int successful_logins) {
        this.successful_logins = successful_logins;
    }

    public void setUnsuccessful_logins(int unsuccessful_logins) {
        this.unsuccessful_logins = unsuccessful_logins;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }
}
