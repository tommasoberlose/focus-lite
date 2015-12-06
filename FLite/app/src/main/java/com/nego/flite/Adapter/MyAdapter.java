package com.nego.flite.Adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.DragEvent;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nego.flite.Costants;
import com.nego.flite.R;

import java.util.ArrayList;
import java.util.List;


public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    private List<String[]> mDataset = new ArrayList<>();
    private Context mContext;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public View mView;
        public ViewHolder(View v) {
            super(v);
            mView = v;
        }

        public CheckBox checkBox;
        public ImageView img;
        public EditText text;
        public ImageView action_remove;
        public ViewHolder(View v, CheckBox checkBox, ImageView img, EditText text, ImageView action_remove) {
            super(v);
            mView = v;
            this.checkBox = checkBox;
            this.img = img;
            this.text = text;
            this.action_remove = action_remove;
        }

    }

    public MyAdapter(Context mContext, String list) {
        this.mContext = mContext;
        generate_list(list);
    }

    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {

        ViewHolder vh;
        View v;

        v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item, parent, false);
        vh = new ViewHolder(v,
                (CheckBox) v.findViewById(R.id.checkbox),
                (ImageView) v.findViewById(R.id.action_drag),
                (EditText) v.findViewById(R.id.text),
                (ImageView) v.findViewById(R.id.action_remove));

        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        if (!mDataset.get(position)[0].equals("")) {

            holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    mDataset.get(position)[0] = isChecked ? "1" : "0";
                }
            });

            holder.checkBox.setChecked(mDataset.get(position)[0].equals("1"));
            holder.text.setText(mDataset.get(position)[1]);
            if (mDataset.get(position)[0].equals("1"))
                holder.text.setTextColor(ContextCompat.getColor(mContext, R.color.primary_dark));
            else
                holder.text.setTextColor(ContextCompat.getColor(mContext, android.R.color.white));

            holder.text.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    mDataset.get(position)[1] = s.toString().replace(Costants.LIST_COSTANT, "").replace(Costants.LIST_ORDER_SEPARATOR, "").replace(Costants.LIST_ITEM_SEPARATOR, "");
                }
            });
            holder.action_remove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDataset.remove(position);
                    notifyItemRemoved(position);
                }
            });
            // TODO img drag
            holder.action_remove.setVisibility(View.VISIBLE);
            holder.img.setVisibility(View.VISIBLE);
            holder.checkBox.setVisibility(View.VISIBLE);
        } else {
            holder.action_remove.setVisibility(View.GONE);
            holder.img.setVisibility(View.INVISIBLE);
            holder.checkBox.setVisibility(View.INVISIBLE);
            holder.text.setHint(mContext.getString(R.string.title_activity_add_item));
            holder.text.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (s.toString().contains("\n")) {
                        mDataset.add(1, new String[]{"0", s.toString().trim()});
                        notifyItemInserted(1);
                        s.clear();
                    }
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    // GENERATE LIST
    public void generate_list(String list) {
        mDataset.clear();
        list = list.replace(Costants.LIST_COSTANT, "");
        String[] content_split = list.split(Costants.LIST_ITEM_SEPARATOR, -1);
        for (String i : content_split) {

            mDataset.add(new String[] {i.split(Costants.LIST_ORDER_SEPARATOR, -1)[0], i.split(Costants.LIST_ORDER_SEPARATOR, -1)[1]});

        }
        mDataset.add(new String[]{""});
    }

    public String getData() {
        String text = Costants.LIST_COSTANT;
        for (String[] s : mDataset) {
            if (!s[0].equals(""))
                text += s[0] + Costants.LIST_ORDER_SEPARATOR + s[1] + Costants.LIST_ITEM_SEPARATOR;
        }
        text = text.substring(0, text.length() - Costants.LIST_ITEM_SEPARATOR.length());
        return text;
    }
}
