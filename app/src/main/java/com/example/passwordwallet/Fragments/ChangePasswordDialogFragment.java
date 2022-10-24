package com.example.passwordwallet.Fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.passwordwallet.R;

//Dialog fragment to change master password
public class ChangePasswordDialogFragment extends DialogFragment {

    //interface to respond on positive button click in dialog
    public interface ChangePasswordDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog, String newPassword);
    }

    ChangePasswordDialogListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        //check if listener is implemented in fragment
        try {
            listener = (ChangePasswordDialogListener) context;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString() + "must implement NoticeDialogListener!");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        //setup builder and buttons
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = requireActivity().getLayoutInflater();

        View mView = inflater.inflate(R.layout.change_password_dialog, null);
        EditText editText = mView.findViewById(R.id.dialog_password);

        builder.setView(mView)
                .setPositiveButton(R.string.change, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String newPassword = editText.getText().toString();
                        listener.onDialogPositiveClick(ChangePasswordDialogFragment.this, newPassword);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ChangePasswordDialogFragment.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }
}
