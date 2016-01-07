package com.nego.flite.Adapter;


import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.v4.content.ContextCompat;
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

        public CheckBox checkBox;
        public ImageView img;
        public ImageView add_icon;
        public EditText text;
        public ImageView action_remove;
        public ViewHolder(View v, CheckBox checkBox, ImageView img, ImageView add_icon, EditText text, ImageView action_remove) {
            super(v);
            mView = v;
            this.checkBox = checkBox;
            this.img = img;
            this.add_icon = add_icon;
            this.text = text;
            this.action_remove = action_remove;
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
                .inflate(R.layout.list_item, parent, false); // TODO creare layout item con cardview per elevation e per farlo scorrere poi
        vh = new ViewHolder(v,
                (CheckBox) v.findViewById(R.id.checkbox),
                (ImageView) v.findViewById(R.id.action_drag),
                (ImageView) v.findViewById(R.id.add_icon),
                (EditText) v.findViewById(R.id.text),
                (ImageView) v.findViewById(R.id.action_remove));

        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {



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
