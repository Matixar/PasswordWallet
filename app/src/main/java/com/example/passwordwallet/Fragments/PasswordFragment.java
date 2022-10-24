package com.example.passwordwallet.Fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.passwordwallet.DAO.UserDatabase;
import com.example.passwordwallet.R;
import com.example.passwordwallet.adapters.PasswordListRecyclerViewAdapter;
import com.example.passwordwallet.placeholder.PasswordList;

/**
 * A fragment representing a list of password items.
 */
public class PasswordFragment extends Fragment {

    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;

    private PasswordList list;
    private String mainPassword;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PasswordFragment() {
    }

    @SuppressWarnings("unused")
    public static PasswordFragment newInstance(int columnCount, String login, String passwd) {
        PasswordFragment fragment = new PasswordFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        args.putString("login",login);
        args.putString("passwd",passwd);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UserDatabase database = UserDatabase.getInstance(getContext());

        //get passwords list and password given in login activity
        if (getArguments() != null) {
            if(getArguments().getString("login") != null) {
                list = new PasswordList(database, getArguments().getString("login"));
                mainPassword = getArguments().getString("passwd");
            }
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_passwords_list, container, false);


        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            recyclerView.setAdapter(new PasswordListRecyclerViewAdapter(list != null? list.getItems() : PasswordList.ITEMS, mainPassword));
        }
        return view;
    }
}