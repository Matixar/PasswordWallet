package com.example.passwordwallet.placeholder;

import android.os.AsyncTask;

import com.example.passwordwallet.DAO.Password;
import com.example.passwordwallet.DAO.UserDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * class containing Password items
 */
public class PasswordList {

    public static final List<PasswordListItem> ITEMS = new ArrayList<PasswordListItem>();

    private List<PasswordListItem> items;

    public PasswordList(UserDatabase database, String login) {
        //init items list and get it from database
        items = new ArrayList<>();
        new PasswordListAsync(database, login).execute();
    }

    public PasswordList(List<PasswordListItem> items) {
        this.items = items;
    }

    public List<PasswordListItem> getItems() {
        return items;
    }


    //item class for displaying data
    public static class PasswordListItem {
        public final String webAddress;
        public final String description;
        public final String login;
        public final String password;

        public PasswordListItem(String webAddress, String description, String login, String password) {
            this.webAddress = webAddress;
            this.description = description;
            this.login = login;
            this.password = password;
        }

    }

    //class to get password list from database
    private class PasswordListAsync extends AsyncTask<Void,Void,Boolean> {
        private final UserDatabase database;
        private final String username;

        PasswordListAsync(UserDatabase database, String nick) {
            this.database = database;
            this.username = nick;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                //get user id and passwords list from database
                int id = database.userDao().getUserId(this.username);
                List<Password> list = database.passwordDao().getPasswordsList(id);
                //create new display items
                for (Password password : list) {
                    PasswordListItem item = new PasswordListItem(password.getWeb_adress(), password.getDescription(), password.getLogin(), password.getPassword());
                    items.add(item);
                }
                return true;
            }
            catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }
}