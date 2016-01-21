package com.nego.flite.Adapter;


import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nego.flite.Costants;
import com.nego.flite.Functions.UserService;
import com.nego.flite.R;
import com.nego.flite.SignInActivity;
import com.nego.flite.User;
import com.nego.flite.database.DbAdapter;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class AdapterAccounts extends RecyclerView.Adapter<AdapterAccounts.ViewHolder> {
    private List<User> mDataset = new ArrayList<>();
    private Context mContext;


    public static class ViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout v;
        public LinearLayout active;
        public TextView title;
        public TextView subtitle;
        public ImageView photo;
        public ImageView remove_account;

        public ViewHolder(LinearLayout v,
                          LinearLayout active,
                          TextView title,
                          TextView subtitle,
                          ImageView photo,
                          ImageView remove_account) {
            super(v);
            this.v = v;
            this.active = active;
            this.title = title;
            this.subtitle = subtitle;
            this.photo = photo;
            this.remove_account = remove_account;
        }

    }

    public AdapterAccounts(Context mContext, DbAdapter dbAdapter) {
        this.mContext = mContext;
        generate_list(dbAdapter);
    }

    @Override
    public AdapterAccounts.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                  int viewType) {

        ViewHolder vh;
        View v;

        v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_account, parent, false);

        vh = new ViewHolder((LinearLayout) v,
                (LinearLayout) v.findViewById(R.id.active),
                (TextView) v.findViewById(R.id.title),
                (TextView) v.findViewById(R.id.subtitle),
                (ImageView) v.findViewById(R.id.photo),
                (ImageView) v.findViewById(R.id.remove_account));
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        final User user = mDataset.get(position);

        // NAME
        holder.title.setText(user.getName());

        // MAIL
        holder.subtitle.setText(user.getEmail());

        // PHOTO
        holder.photo.setImageURI(Uri.parse(user.getPhoto()));

        // ACTIVE USER
        holder.active.setVisibility(user.getActive() == 1 ? View.VISIBLE : View.GONE);

        // REMOVE ACCOUNT
        holder.remove_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserService.startAction(mContext, Costants.ACTION_DELETE, user);
            }
        });

        // CLICK
        holder.v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserService.startAction(mContext, Costants.ACTION_SET_ACTIVE_USER, user);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    // GENERATE LIST
    public void generate_list(DbAdapter dbAdapter) {
        Cursor c = dbAdapter.fetchAllUsers();
        while (c.moveToNext()) {
            User user = new User(c);
            mDataset.add(user);
        }
        c.close();
    }

}


