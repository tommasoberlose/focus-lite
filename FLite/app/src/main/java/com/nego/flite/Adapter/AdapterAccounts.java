package com.nego.flite.Adapter;


import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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
        public LinearLayout active_user;
        public LinearLayout other_user;
        public TextView title;
        public TextView subtitle;
        public TextView title_active;
        public TextView subtitle_active;
        public ImageView photo_active;
        public ImageView remove_account;

        public ViewHolder(LinearLayout v,
                          LinearLayout active_user,
                          LinearLayout other_user,
                          TextView title,
                          TextView subtitle,
                          TextView title_active,
                          TextView subtitle_active,
                          ImageView photo_active,
                          ImageView remove_account) {
            super(v);
            this.v = v;
            this.active_user = active_user;
            this.other_user = other_user;
            this.title = title;
            this.subtitle = subtitle;
            this.title_active = title_active;
            this.subtitle_active = subtitle_active;
            this.photo_active = photo_active;
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
                (LinearLayout) v.findViewById(R.id.active_user),
                (LinearLayout) v.findViewById(R.id.other_user),
                (TextView) v.findViewById(R.id.title),
                (TextView) v.findViewById(R.id.subtitle),
                (TextView) v.findViewById(R.id.title_active),
                (TextView) v.findViewById(R.id.subtitle_active),
                (ImageView) v.findViewById(R.id.photo_active),
                (ImageView) v.findViewById(R.id.remove_account));
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        final User user = mDataset.get(position);

        if (position == 0) {
            holder.active_user.setVisibility(View.VISIBLE);
            holder.other_user.setVisibility(View.GONE);

            // NAME
            holder.title_active.setText(user.getName());

            // MAIL
            holder.subtitle_active.setText(user.getEmail());

            // PHOTO
            try {
                holder.photo_active.setImageBitmap(BitmapFactory.decodeStream(new URL(user.getPhoto()).openStream()));
            } catch (Exception e) {
                e.printStackTrace();
            }


        } else {
            holder.active_user.setVisibility(View.GONE);
            holder.other_user.setVisibility(View.VISIBLE);

            // NAME
            holder.title.setText(user.getName());

            // MAIL
            holder.subtitle.setText(user.getEmail());

            // REMOVE ACCOUNT
            holder.remove_account.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((SignInActivity) mContext).deleteUserInfo(user);
                }
            });
        }


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
            if (user.getActive() == 0)
                mDataset.add(user);
            else
                mDataset.add(0, user);
        }
        c.close();
    }

}


