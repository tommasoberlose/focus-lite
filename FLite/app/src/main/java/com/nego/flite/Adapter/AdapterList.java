package com.nego.flite.Adapter;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.nego.flite.Costants;
import com.nego.flite.MyDialog;
import com.nego.flite.R;
import com.nego.flite.Reminder;
import com.nego.flite.Utils;
import com.nego.flite.database.DbAdapter;

import java.util.ArrayList;
import java.util.List;

public class AdapterList extends RecyclerView.Adapter<AdapterList.ViewHolder> {
    private List<Reminder> mDataset = new ArrayList<>();
    private Context mContext;
    private SharedPreferences SP;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public View mView;
        public ViewHolder(View v) {
            super(v);
            mView = v;
        }

        public View top_divider;
        public CardView card_reminder;
        public TextView reminder_title;
        public TextView reminder_subtitle;
        public ViewHolder(View v, View top_divider, CardView card_reminder, TextView reminder_title, TextView reminder_subtitle) {
            super(v);
            mView = v;
            this.top_divider = top_divider;
            this.card_reminder = card_reminder;
            this.reminder_title = reminder_title;
            this.reminder_subtitle = reminder_subtitle;
        }

    }

    public AdapterList(DbAdapter dbAdapter, String query, Context mContext) {
        this.mContext = mContext;
        SP = mContext.getSharedPreferences(Costants.PREFERENCES_COSTANT, Context.MODE_PRIVATE);
        generate_list(dbAdapter, query);
    }

    @Override
    public AdapterList.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {

        ViewHolder vh;
        View v;

        v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.mainlist_item, parent, false); // TODO creare layout item con cardview per elevation e per farlo scorrere poi
        vh = new ViewHolder(v, v.findViewById(R.id.top_divider),
                (CardView) v.findViewById(R.id.card_reminder),
                (TextView) v.findViewById(R.id.reminder_title),
                (TextView) v.findViewById(R.id.reminder_subtitle));

        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        final Reminder r = mDataset.get(position);

        // Top divider visibility first element
        holder.top_divider.setVisibility(position == 0 ? View.GONE : View.VISIBLE);

        holder.card_reminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(mContext, MyDialog.class);
                i.setAction(Costants.ACTION_EDIT_ITEM);
                i.putExtra(Costants.EXTRA_REMINDER, r);
                mContext.startActivity(i);
            }
        });

        // Title
        holder.reminder_title.setText(mDataset.get(position).getTitle());

        // Subtitle
        if (r.getPasw().equals("")) {
            if (r.getContent().equals("")) {
                if (r.getAlarm() == 0) {
                    holder.reminder_subtitle.setText(Utils.getDate(mContext, r.getDate_create()));
                } else {
                    holder.reminder_subtitle.setText(Utils.getAlarm(mContext, r.getAlarm(), r.getAlarm_repeat(), r.getDate_reminded()));
                }
            } else {
                holder.reminder_subtitle.setText(Utils.getContentList(mContext, r.getContent()));
            }
        } else {
            holder.reminder_subtitle.setText(mContext.getString(R.string.text_locked_note));
        }

        // Icon
        /*if (r.getPriority() == 1) {
            holder.reminder_icon.setImageResource(R.drawable.ic_stat_action_bookmark_star);
        } else {
            if (!r.getAlarm_repeat().equals("")) {
                holder.reminder_icon.setImageResource(R.drawable.ic_stat_action_bookmark_snoozed);
            } else {
                if (r.getAlarm() != 0) {
                    if (r.getDate_reminded() != 0)
                        holder.reminder_icon.setImageResource(R.drawable.ic_not_bookmark_check);
                    else
                        holder.reminder_icon.setImageResource(R.drawable.ic_stat_action_bookmark_snoozed);
                } else {
                    holder.reminder_icon.setImageResource(R.drawable.ic_not_action_bookmark);
                }
            }
        }*/

    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    // GENERATE LIST
    public void generate_list(DbAdapter dbAdapter, String query) {
        mDataset.clear();
        // TODO search, filter from side nav e fare gli header??
        Cursor c = dbAdapter.fetchAllReminders(SP.getBoolean(Costants.PREFERENCE_ORDER_ALARM_FIRST, true));
        while (c.moveToNext()) {
            mDataset.add(new Reminder(c));
        }
    }

    // TODO mancano anche tutte le operazione di selezione e manca la gestione del menu
}
