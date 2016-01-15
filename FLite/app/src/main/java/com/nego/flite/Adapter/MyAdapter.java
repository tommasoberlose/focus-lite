package com.nego.flite.Adapter;

import android.content.Context;
import android.content.Intent;
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
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nego.flite.Costants;
import com.nego.flite.MyDialog;
import com.nego.flite.R;

import java.util.ArrayList;
import java.util.List;


public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    private List<Item> mDataset = new ArrayList<>();
    private Context mContext;
    private SharedPreferences SP;
    private int max_id = 0;

    private int newFromEnter = -1;

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
        SP = mContext.getSharedPreferences(Costants.PREFERENCES_COSTANT, Context.MODE_PRIVATE);
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

        final Item item = mDataset.get(position);

        // THEME
        if (SP.getString(Costants.PREFERENCE_STYLE_POPUP, Costants.PREFERENCE_STYLE_POPUP_DEFAULT).equals(Costants.PREFERENCE_STYLE_POPUP_ML))
            holder.text.setTextColor(ContextCompat.getColor(mContext, R.color.primary_text));
        else
            holder.text.setTextColor(ContextCompat.getColor(mContext, android.R.color.white));

        holder.text.setText(item.getText());
        if (item.getChecked())
            holder.text.setAlpha(0.5f);
        else
            holder.text.setAlpha(1f);

        holder.text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                saveText(s.toString().replace(Costants.LIST_COSTANT, "").replace(Costants.LIST_ORDER_SEPARATOR, "").replace(Costants.LIST_ITEM_SEPARATOR, "").trim(), item.getId());
            }
        });

        holder.text.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    addNextElement(v, item.getId());
                }
                return false;
            }
        });
        if (item.getId() == newFromEnter) {
            newFromEnter = -1;
            holder.text.requestFocus();
        }

        // CHECKBOX
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                checkItem(isChecked, item.getId());
                if (isChecked)
                    holder.text.setAlpha(0.5f);
                else
                    holder.text.setAlpha(1f);
                ((MyDialog) mContext).updateHeight();
            }
        });

        holder.checkBox.setChecked(item.getChecked());

        // ACTION REMOVE
        holder.action_remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteElement(item.getId());
            }
        });

    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    // GENERATE LIST
    public void generate_list(String list) {
        int id = max_id;
        mDataset.clear();
        list = list.replace(Costants.LIST_COSTANT, "");
        String[] content_split = list.split(Costants.LIST_ITEM_SEPARATOR, -1);
        for (String i : content_split) {
            mDataset.add(new Item(id, i.split(Costants.LIST_ORDER_SEPARATOR, -1)[1], i.split(Costants.LIST_ORDER_SEPARATOR, -1)[0].equals("1")));
            id++;
        }
        max_id = id;
    }

    public String getData() {
        String text = Costants.LIST_COSTANT;
        for (Item s : mDataset) {
            text += s.getCheckedString() + Costants.LIST_ORDER_SEPARATOR + s.getText() + Costants.LIST_ITEM_SEPARATOR;
        }
        text = text.substring(0, text.length() - Costants.LIST_ITEM_SEPARATOR.length());
        return text;
    }

    public boolean doneSomeItems() {
        int n = 0;
        for (Item s : mDataset) {
            if (s.getChecked())
                n++;
        }
        return (n != 0);
    }

    public void addElement(TextView v) {
        max_id++;
        mDataset.add(new Item(max_id, v.getText().toString().replace(Costants.LIST_COSTANT, "").replace(Costants.LIST_ORDER_SEPARATOR, "").replace(Costants.LIST_ITEM_SEPARATOR, "").trim(), false));
        notifyItemInserted(mDataset.size() - 1);
        v.setText("");
        ((MyDialog) mContext).updateHeight();
    }

    public void deleteElement(int id) {
        int pos = 0;
        for (Item strings : mDataset) {
            if (strings.getId() == id) {
                mDataset.remove(pos);
                notifyItemRemoved(pos);
                break;
            }
            pos++;
        }
        ((MyDialog) mContext).updateHeight();
    }

    public void addNextElement(TextView text, int id) {
        max_id++;
        int pos = 0;
        for (Item strings : mDataset) {
            if (strings.getId() == id) {
                mDataset.add(pos + 1, new Item(max_id, "", false));
                notifyItemInserted(pos + 1);
                text.clearFocus();
                newFromEnter = max_id;
                break;
            }
            pos++;
        }
        ((MyDialog) mContext).updateHeight();
    }

    public void saveText(String text, int id) {
        int pos = 0;
        for (Item strings : mDataset) {
            if (strings.getId() == id) {
                mDataset.get(pos).setText(text);
                break;
            }
            pos++;
        }
    }

    public void checkItem(boolean check, int id) {
        int pos = 0;
        for (Item strings : mDataset) {
            if (strings.getId() == id) {
                mDataset.get(pos).setChecked(check);
                break;
            }
            pos++;
        }
    }

    public void swapElement(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            Item item = mDataset.remove(fromPosition);
            mDataset.add(toPosition, item);
        } else {
            Item item = mDataset.remove(fromPosition);
            mDataset.add(toPosition, item);
        }
        notifyItemMoved(fromPosition, toPosition);
    }

    public class Item {

        private String text;
        private boolean checked;
        private int id;

        public Item(int id, String text, boolean checked) {
            this.id = id;
            this.text = text;
            this.checked = checked;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public boolean getChecked() {
            return checked;
        }

        public String getCheckedString() {
            if (checked)
                return "1";
            else
                return "0";
        }

        public void setChecked(boolean checked) {
            this.checked = checked;
        }
    }
}
