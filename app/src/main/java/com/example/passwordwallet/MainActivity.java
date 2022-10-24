package com.example.passwordwallet;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.example.passwordwallet.DAO.User;
import com.example.passwordwallet.DAO.UserDatabase;
import com.example.passwordwallet.crypto.HashFunctions;
import com.example.passwordwallet.databinding.ActivityMainBinding;
import com.google.android.material.snackbar.Snackbar;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    private final String pepper = "pepper";
    private boolean hashFunction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //creating layout view and binding
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initButton();
    }

    private void initButton() {
        binding.hashFunction.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                hashFunction = b;
                Toast.makeText(MainActivity.this,"Changed hash function", Toast.LENGTH_SHORT).show();
            }
        });
        //init button on click listener
        binding.login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //hide keyboard
                View focusedView = MainActivity.this.getCurrentFocus();
                InputMethodManager imm =(InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(focusedView.getWindowToken(),0);

                //login
                if(dataValidation())
                    login();
                else
                    Toast.makeText(MainActivity.this, "Invalid username/password", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getPassword() {
        //get password given by user
        if(binding.password.getText() != null)
            return binding.password.getText().toString();
        else return "";
    }
    private String getUsername() {
        if(binding.username.getText() != null)
            return binding.username.getText().toString();
        else return "";
    }

    //validate data
    private boolean dataValidation() {
        return (isEditTextNotNull(binding.username) && isEditTextNotNull(binding.password));
    }

    //check if text inputs are empty
    private boolean isEditTextNotNull(EditText editText) {
        return editText.getText() != null && editText.getText().length() > 0;
    }

    private void login() {
        try {
            //create database connection and execute login check in async task
            UserDatabase userDatabase = UserDatabase.getInstance(this);
            new LoginAsync(this).execute(userDatabase);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class LoginAsync extends AsyncTask<UserDatabase,Void,Boolean> {

        MainActivity activity;
        UserDatabase userDatabase;
        private final String hmacKey = "HMACKey";
        private String password;
        private boolean passwordKeptAsHash;

        LoginAsync(MainActivity activity) {
            this.activity = activity;
        }

        @Override
        protected Boolean doInBackground(UserDatabase... userDatabases) {
            try {
                //get the database instance
                userDatabase = Arrays.stream(userDatabases).findFirst().orElse(null);
                //check if user exists, if not then make new one, default password storage is in SHA512
                if (userDatabase.userDao().getUser(getUsername()).isEmpty()) {
                    if(hashFunction) {
                        String salt = "salt";
                        password = HashFunctions.calculateSHA512(pepper + salt + getPassword());
                        //insert new user into database
                        userDatabase.userDao().insertUser(new User(getUsername(), password, salt, true));
                    }
                    else {
                        password = HashFunctions.calculateHMAC(getPassword(),hmacKey);
                        userDatabase.userDao().insertUser(new User(getUsername(),password,"",false));
                    }
                    Snackbar.make(binding.getRoot(),"Successfuly registered", Snackbar.LENGTH_SHORT).show();
                    return true;
                }
                String passwordHash;
                passwordKeptAsHash = userDatabase.userDao().isPasswordKeptAsHash(getUsername());
                //check how the password is kept and get the hash from credentials given by user
                if(passwordKeptAsHash) {
                    passwordHash = HashFunctions.calculateSHA512(pepper +
                            userDatabase.userDao().getSalt(getUsername()).stream().findFirst().orElse("") + getPassword());
                }
                else {
                    passwordHash = HashFunctions.calculateHMAC(getPassword(),hmacKey);
                }
                //return true if successfully logged in (that means if password hash matches the one in database)
                password = passwordHash;
                return userDatabase.userDao().login(getUsername(), passwordHash);
            }
            catch (NullPointerException e) {
                //in case there isn't a database instance
                e.printStackTrace();
                Snackbar.make(binding.getRoot(), "Błąd bazy danych",Snackbar.LENGTH_SHORT).show();
                return false;
            }
            catch (Exception e) {
                //catching all exceptions in case i missed something
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            //check if logged in successfully
            if(aBoolean) {
                Toast.makeText(MainActivity.this,"Successfuly logged in", Toast.LENGTH_SHORT).show();
                //make intent and start new activity
                Intent intent = new Intent(MainActivity.this, ContentActivity.class);
                intent.putExtra("passwd",password);
                intent.putExtra("nick",getUsername());
                intent.putExtra("algorithm",passwordKeptAsHash);
                startActivity(intent);
                activity.finish();
            }
            else
                Toast.makeText(MainActivity.this,"Wrong password", Toast.LENGTH_SHORT).show();
        }
    }


}