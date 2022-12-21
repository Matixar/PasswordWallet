package com.example.passwordwallet;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.passwordwallet.DAO.IpAddress;
import com.example.passwordwallet.DAO.LoginAttempt;
import com.example.passwordwallet.DAO.User;
import com.example.passwordwallet.DAO.UserDatabase;
import com.example.passwordwallet.crypto.HashFunctions;
import com.example.passwordwallet.databinding.ActivityMainBinding;
import com.google.android.material.snackbar.Snackbar;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.concurrent.TimeUnit;

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
        System.out.println(getLocalIpAddress());
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
        binding.ipReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetIP();
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
    protected boolean dataValidation() {
        return (isEditTextNotNull(binding.username) && isEditTextNotNull(binding.password));
    }

    //check if text inputs are empty
    private boolean isEditTextNotNull(EditText editText) {
        return editText.getText() != null && editText.getText().length() > 0;
    }

    protected void login() {
        try {
            //create database connection and execute login check in async task
            UserDatabase userDatabase = UserDatabase.getInstance(this);
            new LoginAsync(this).execute(userDatabase);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void resetIP() {
        UserDatabase userDatabase = UserDatabase.getInstance(this);
        new ResetIpAsync(userDatabase,getLocalIpAddress()).execute();
    }

    public static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    protected class LoginAsync extends AsyncTask<UserDatabase,Void,Boolean> {

        MainActivity activity;
        UserDatabase userDatabase;
        private final String hmacKey = "HMACKey";
        private String password;
        private boolean passwordKeptAsHash;
        private String lastLogin;
        private String lastFailedLogin;
        private boolean isBlocked;

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
                boolean loggedIn = userDatabase.userDao().login(getUsername(), passwordHash);
                String ipAddress = getLocalIpAddress();
                if(loggedIn) {
                    if(userDatabase.ipAddressDAO().getIpAddress(ipAddress).isEmpty()) {
                        IpAddress ipAddressEntity = new IpAddress(ipAddress, 1, 0, false);
                        userDatabase.ipAddressDAO().insertIpAddress(ipAddressEntity);
                    }
                    else {
                        IpAddress ip = userDatabase.ipAddressDAO().getIpAddress(ipAddress).stream().findFirst().get();
                        if(ip.isBlocked()) {
                            isBlocked = true;
                            return true;
                        }
                        ip.setSuccessful_logins(ip.getSuccessful_logins() + 1);
                        ip.setUnsuccessful_logins(0);
                        ip.setBlocked(false);
                        userDatabase.ipAddressDAO().updateIpAddress(ip);
                    }
                    lastLogin = userDatabase.loginAttemptDAO().getLastLoginTime(ipAddress,getUsername(),true) == null? "" : SimpleDateFormat.getDateTimeInstance().format(userDatabase.loginAttemptDAO().getLastLoginTime(ipAddress,getUsername(),true).getTime());
                    lastFailedLogin = userDatabase.loginAttemptDAO().getLastLoginTime(ipAddress,getUsername(),false) == null? "" : SimpleDateFormat.getDateTimeInstance().format(userDatabase.loginAttemptDAO().getLastLoginTime(ipAddress,getUsername(),false).getTime());
                    LoginAttempt loginAttempt = new LoginAttempt(Calendar.getInstance(), true,ipAddress, getUsername());
                    userDatabase.loginAttemptDAO().insertLoginAttempt(loginAttempt);
                    return true;
                }
                else {
                    if(userDatabase.ipAddressDAO().getIpAddress(ipAddress).isEmpty()) {
                        IpAddress ipAddressEntity = new IpAddress(ipAddress, 0, 1, false);
                        userDatabase.ipAddressDAO().insertIpAddress(ipAddressEntity);
                    }
                    else {
                        IpAddress ip = userDatabase.ipAddressDAO().getIpAddress(ipAddress).stream().findFirst().get();
                        if(ip.getUnsuccessful_logins() == 2) {
                            long diff = new Date().getTime() - userDatabase.loginAttemptDAO().getLastLoginTime(ipAddress,getUsername(),false).getTime().getTime();
                            if(TimeUnit.MILLISECONDS.toSeconds(diff) < 5) {
                                Snackbar.make(binding.getRoot(),"Wait for 5 seconds",Snackbar.LENGTH_SHORT).show();
                                return false;
                            }
                        }
                        if(ip.getUnsuccessful_logins() == 3) {
                            long diff = new Date().getTime() - userDatabase.loginAttemptDAO().getLastLoginTime(ipAddress,getUsername(),false).getTime().getTime();
                            if(TimeUnit.MILLISECONDS.toSeconds(diff) < 10) {
                                Snackbar.make(binding.getRoot(),"Wait for 10 seconds", Snackbar.LENGTH_SHORT).show();
                                return false;
                            }
                        }
                        ip.setUnsuccessful_logins(ip.getUnsuccessful_logins() + 1);
                        if(ip.getUnsuccessful_logins() > 3) {
                            ip.setBlocked(true);
                            isBlocked = true;
                        }
                        userDatabase.ipAddressDAO().updateIpAddress(ip);
                    }

                    LoginAttempt loginAttempt = new LoginAttempt(Calendar.getInstance(), false,ipAddress, getUsername());

                    userDatabase.loginAttemptDAO().insertLoginAttempt(loginAttempt);
                    return false;
                }

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
                if(!isBlocked) {
                    Toast.makeText(MainActivity.this, "Successfuly logged in", Toast.LENGTH_SHORT).show();
                    //make intent and start new activity
                    Intent intent = new Intent(MainActivity.this, ContentActivity.class);
                    intent.putExtra("passwd", password);
                    intent.putExtra("nick", getUsername());
                    intent.putExtra("algorithm", passwordKeptAsHash);
                    intent.putExtra("successfulLogin", lastLogin);
                    intent.putExtra("unsuccessfulLogin", lastFailedLogin);
                    startActivity(intent);
                    activity.finish();
                }
                else
                    Toast.makeText(MainActivity.this, "This ip address is blocked", Toast.LENGTH_SHORT).show();

            }
            else
                Toast.makeText(MainActivity.this,"Unsuccessful login", Toast.LENGTH_SHORT).show();
        }
    }

    protected class ResetIpAsync extends AsyncTask<Void,Void,Boolean> {

        UserDatabase userDatabase;
        String ipAddress;


        ResetIpAsync(UserDatabase userDatabase, String ipAddress) {
            this.userDatabase = userDatabase;
            this.ipAddress = ipAddress;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            if(!userDatabase.ipAddressDAO().getIpAddress(ipAddress).isEmpty()) {
                if(userDatabase.ipAddressDAO().isAddressBlocked(ipAddress)) {
                    IpAddress ip = userDatabase.ipAddressDAO().getIpAddress(ipAddress).stream().findFirst().get();
                    ip.setBlocked(false);
                    ip.setUnsuccessful_logins(0);
                    userDatabase.ipAddressDAO().updateIpAddress(ip);
                    Snackbar.make(binding.getRoot(),"Unblocked ip address " + ip.getIp_address(), Snackbar.LENGTH_SHORT).show();
                }
                else
                    Snackbar.make(binding.getRoot(), "Ip address isn't blocked", Snackbar.LENGTH_SHORT).show();
            }
            else
                Snackbar.make(binding.getRoot(), "No logins from such ip address", Snackbar.LENGTH_SHORT).show();
            return true;
        }
    }


}