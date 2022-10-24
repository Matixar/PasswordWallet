package com.example.passwordwallet.Fragments;

import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.example.passwordwallet.ContentActivity;
import com.example.passwordwallet.DAO.Password;
import com.example.passwordwallet.DAO.UserDatabase;
import com.example.passwordwallet.R;
import com.example.passwordwallet.crypto.HashFunctions;
import com.example.passwordwallet.databinding.AddNewPasswordFBinding;

import java.security.Key;
import java.util.Arrays;

/**
 * Fragment with new password adding view
 */
public class AddNewPasswordFragment extends Fragment {

    AddNewPasswordFBinding binding;
    private String nick;
    private String password;
    public AddNewPasswordFragment() {
        // Required empty public constructor
    }

    /**
     * @return A new instance of fragment AddNewPasswordFragment.
     */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void initButton() {
        //init action on add button
        binding.newPasswordFAddPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //check if added successfully
                if(addNewPassword())
                    Toast.makeText(getContext(),"Password added",Toast.LENGTH_SHORT).show();
                //return to list view
                Bundle bundle = new Bundle();
                bundle.putString("login",nick);
                bundle.putString("passwd",password);
                Navigation.findNavController(binding.getRoot()).navigate(R.id.action_addNewPasswordFragment_to_passwordFragment, bundle);
            }
        });
    }

    @Override
    public void onDestroyView() {
        ContentActivity activity = (ContentActivity) getActivity();
        activity.floatingActionButton.setVisibility(View.VISIBLE);
        super.onDestroyView();

    }

    private boolean addNewPassword() {
        //get database instance and run async task to access data from database
        UserDatabase database = UserDatabase.getInstance(getContext());
        try {
            new AddPasswordAsync(database, getStringFromEditText(binding.newPasswordFPasswordEditText),
                    getStringFromEditText(binding.newPasswordFWebsiteEditText),
                    getStringFromEditText(binding.newPasswordFLoginEditText),
                    getStringFromEditText(binding.newPasswordFDescriptionEditText),
                    password).execute();
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private String getStringFromEditText(EditText editText) {
        //get string from user's input
        return editText.getText().toString();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = AddNewPasswordFBinding.inflate(inflater, container, false);
        //get variables from previous fragment
        nick = this.getArguments().getString("login");
        password = this.getArguments().getString("passwd");
        initButton();
        return binding.getRoot();
    }

    //class used to connect to database
    private class AddPasswordAsync extends AsyncTask<Void,Void,Boolean> {

        private final UserDatabase database;
        private final String mPassword;
        private final String mWebsite;
        private final String mLogin;
        private final String mDescription;
        private final String password;

        AddPasswordAsync(UserDatabase database, String password, String website, String login, String description, String passwd) {
            this.database = database;
            this.mPassword = password;
            this.mWebsite = website;
            this.mDescription = description;
            this.mLogin = login;
            this.password = passwd;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                //get users id
                int id = database.userDao().getUser(nick).stream().findFirst().get().getId();
                //generate key and encrypt password
                Key key = HashFunctions.AESenc.generateKey(this.password);
                String encryptedPassword = HashFunctions.AESenc.encrypt(mPassword, key);
                //save new password in database
                Password password = new Password(encryptedPassword, id, mWebsite, mDescription, mLogin);
                database.passwordDao().insertPassword(password);
            }
            catch (Exception e) {
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
        }
    }
}