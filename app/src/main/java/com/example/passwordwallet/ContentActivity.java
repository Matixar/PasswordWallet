package com.example.passwordwallet;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.passwordwallet.DAO.Password;
import com.example.passwordwallet.DAO.User;
import com.example.passwordwallet.DAO.UserDatabase;
import com.example.passwordwallet.Fragments.ChangePasswordDialogFragment;
import com.example.passwordwallet.crypto.HashFunctions;
import com.example.passwordwallet.databinding.ActivityContentBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class ContentActivity extends AppCompatActivity implements ChangePasswordDialogFragment.ChangePasswordDialogListener {

    private AppBarConfiguration appBarConfiguration;
    private ActivityContentBinding binding;
    private String userNickname;
    private String passwordHash;
    public FloatingActionButton floatingActionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //get nickname and password from login activity
        Intent intent = getIntent();
        userNickname = intent.getStringExtra("nick");
        passwordHash = intent.getStringExtra("passwd");
        String lastLogin = intent.getStringExtra("successfulLogin");
        String lastFailedLogin = intent.getStringExtra("unsuccessfulLogin");

        binding = ActivityContentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //setup Navigation
        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_content);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        Bundle bundle = new Bundle();
        bundle.putString("login",userNickname);
        bundle.putString("passwd",passwordHash);
        navController.setGraph(R.navigation.nav_graph, bundle);

        //setup floating action button
        floatingActionButton = binding.fab;

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putString("login",userNickname);
                bundle.putString("passwd",passwordHash);
                binding.fab.setVisibility(View.GONE);
                navController.navigate(R.id.action_passwordFragment_to_addNewPasswordFragment, bundle);
            }
        });
        Snackbar.make(binding.getRoot(),"Last successful login: \n" + lastLogin + "\nLast unsuccessful login: \n" + lastFailedLogin , Snackbar.LENGTH_LONG).setTextMaxLines(8).show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_content);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.wallet_options_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //setup change password options menu click
        if (item.getItemId() == R.id.options_main_password_change_password) {
            ChangePasswordDialogFragment dialogFragment = new ChangePasswordDialogFragment();
            dialogFragment.show(getSupportFragmentManager(),"change");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog, String newPassword) {
        //check if user entered new password
        if(newPassword.length() > 0) {
            //change password in database
            UserDatabase database = UserDatabase.getInstance(this);
            new OptionsMenuAsync(database,newPassword).execute();
        }
    }


    private class OptionsMenuAsync extends AsyncTask<Void,Void,Boolean> {
        private UserDatabase database;
        private String newPassword;
        private final String hmacKey = "HMACKey";

        OptionsMenuAsync(UserDatabase database, String newPassword) {
            this.database = database;
            this.newPassword = newPassword;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                //get old users data
                User oldUser = database.userDao().getUser(userNickname).stream().findFirst().get();
                String newPasswordHash;
                User newUser;
                //check which algorithm was used to store password
                if(oldUser.isPasswordKeptAsHash()) {
                    final String pepper = "pepper";
                    final String salt = "newSalt";
                    newPasswordHash = HashFunctions.calculateSHA512(pepper + salt + newPassword);
                    newUser = new User(oldUser.getId(),oldUser.getLogin(),newPasswordHash,salt,true);
                }
                else {
                    newPasswordHash = HashFunctions.calculateHMAC(newPassword, hmacKey);
                    newUser = new User(oldUser.getId(), oldUser.getLogin(), newPasswordHash,"",false);
                }
                //update password in database
                database.userDao().updateUser(newUser);
                //get list of old passwords and encrypt them with new master password
                List<Password> oldPasswords = database.passwordDao().getPasswordsList(oldUser.getId());
                for(Password p : oldPasswords) {
                    String decryptedPassword = HashFunctions.AESenc.decrypt(p.getPassword(),HashFunctions.AESenc.generateKey(oldUser.getPassword_hash()));
                    String encryptedPassword = HashFunctions.AESenc.encrypt(decryptedPassword,HashFunctions.AESenc.generateKey(newUser.getPassword_hash()));
                    database.passwordDao().updatePassword(new Password(p.getId(),encryptedPassword,p.getId_user(),p.getWeb_adress(),p.getDescription(),p.getLogin()));
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            //show confirmation
            if(aBoolean)
                Toast.makeText(ContentActivity.this, "Password changed successfully", Toast.LENGTH_SHORT).show();
        }
    }

}