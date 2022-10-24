package com.example.passwordwallet.DAO;



import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(foreignKeys = @ForeignKey(entity = User.class, parentColumns = "id", childColumns = "id_user"), tableName = "passwords", indices = {@Index(value = "id_user")})
public class Password {
    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name = "password")
    private final String password;
    @ColumnInfo
    private final int id_user;
    @ColumnInfo
    private final String web_adress;
    @ColumnInfo
    private final String description;
    @ColumnInfo
    private final String login;

    @Ignore
    public Password(String password, int id_user, String web_adress, String description, String login) {
        this.password = password;
        this.id_user = id_user;
        this.web_adress = web_adress;
        this.description = description;
        this.login = login;
    }

    public Password(int id, String password, int id_user, String web_adress, String description, String login) {
        this.id = id;
        this.password = password;
        this.id_user = id_user;
        this.web_adress = web_adress;
        this.description = description;
        this.login = login;
    }

    public int getId() {
        return id;
    }

    public String getPassword() {
        return password;
    }

    public int getId_user() {
        return id_user;
    }

    public String getWeb_adress() {
        return web_adress;
    }

    public String getDescription() {
        return description;
    }

    public String getLogin() {
        return login;
    }
}
