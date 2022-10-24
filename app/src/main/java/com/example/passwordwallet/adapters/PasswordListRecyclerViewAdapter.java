package com.example.passwordwallet.adapters;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.passwordwallet.crypto.HashFunctions;
import com.example.passwordwallet.databinding.PasswordsPBinding;
import com.example.passwordwallet.placeholder.PasswordList.PasswordListItem;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link PasswordListItem}.
 *
 */
public class PasswordListRecyclerViewAdapter extends RecyclerView.Adapter<PasswordListRecyclerViewAdapter.ViewHolder> {

    private final List<PasswordListItem> mValues;
    private static int mExpandedPosition = -1;
    private final String mainPassword;

    public PasswordListRecyclerViewAdapter(List<PasswordListItem> items, String mainPassword) {
        mValues = items;
        this.mainPassword = mainPassword;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(PasswordsPBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.mItem = mValues.get(position);

        //set password information text
        holder.mDescription.setText(holder.mItem.description);
        holder.mLogin.setText(holder.mItem.login);
        holder.mWebAddress.setText(holder.mItem.webAddress);
        holder.mPassword.setText("******");

        //expand details when pressing on a list item
        final boolean isExpanded = position==mExpandedPosition;
        holder.mDetails.setVisibility(isExpanded? ViewGroup.VISIBLE: View.GONE);
        holder.itemView.setActivated(isExpanded);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mExpandedPosition = isExpanded ? -1:position;
                notifyItemChanged(position);
            }
        });

        //set show switch to decrypt password when checked and show stars when not
        holder.mButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) {
                    try {
                        holder.mPassword.setText(HashFunctions.AESenc.decrypt(holder.mItem.password,
                                HashFunctions.AESenc.generateKey(mainPassword)));
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else {
                    holder.mPassword.setText("******");
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView mWebAddress;
        public final TextView mDescription;
        public final TextView mLogin;
        public final TextView mPassword;
        public final LinearLayout mDetails;
        public final SwitchMaterial mButton;
        public PasswordListItem mItem;

        public ViewHolder(PasswordsPBinding binding) {
            super(binding.getRoot());
            mWebAddress = binding.passwordsPWebAddress;
            mDescription = binding.passwordsPDescription;
            mLogin = binding.passwordsPLogin;
            mPassword = binding.passwordsPPassword;
            mDetails = binding.passwordsPDetails;
            mButton = binding.passwordsPButtonPassword;
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mDescription.getText() + "'";
        }
    }
}