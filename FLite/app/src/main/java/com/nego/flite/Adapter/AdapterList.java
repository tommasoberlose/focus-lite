package com.nego.flite.Adapter;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
import android.widget.TextView;

import com.nego.flite.Costants;
import com.nego.flite.Functions.ReminderService;
import com.nego.flite.Main;
import com.nego.flite.MyDialog;
import com.nego.flite.R;
import com.nego.flite.Reminder;
import com.nego.flite.User;
import com.nego.flite.Utils;
import com.nego.flite.database.DbAdapter;

import java.util.ArrayList;
import java.util.Calendar;
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
        public View bottom_divider;
        public CardView card_reminder;
        public TextView reminder_title;
        public TextView reminder_subtitle;
        public LinearLayout reminder_icon;
        public CardView container_option;
        public ImageView action_contact;
        public ImageView action_attach;
        public ImageView action_browser;
        public ImageView action_address;
        public ImageView action_password;
        public ViewHolder(View v, View top_divider, View bottom_divider, CardView card_reminder, TextView reminder_title, TextView reminder_subtitle, LinearLayout reminder_icon, CardView container_option, ImageView action_contact, ImageView action_attach, ImageView action_browser, ImageView action_address, ImageView action_password) {
            super(v);
            mView = v;
            this.top_divider = top_divider;
            this.bottom_divider = bottom_divider;
            this.card_reminder = card_reminder;
            this.reminder_title = reminder_title;
            this.reminder_subtitle = reminder_subtitle;
            this.reminder_icon = reminder_icon;
            this.container_option = container_option;
            this.action_contact = action_contact;
            this.action_attach = action_attach;
            this.action_browser = action_browser;
            this.action_address = action_address;
            this.action_password = action_password;
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
                .inflate(R.layout.mainlist_item, parent, false);
        vh = new ViewHolder(v,
                v.findViewById(R.id.top_divider),
                v.findViewById(R.id.bottom_divider),
                (CardView) v.findViewById(R.id.card_reminder),
                (TextView) v.findViewById(R.id.reminder_title),
                (TextView) v.findViewById(R.id.reminder_subtitle),
                (LinearLayout) v.findViewById(R.id.reminder_icon),
                (CardView) v.findViewById(R.id.container_options),
                (ImageView) v.findViewById(R.id.action_contact),
                (ImageView) v.findViewById(R.id.action_attach),
                (ImageView) v.findViewById(R.id.action_browser),
                (ImageView) v.findViewById(R.id.action_address),
                (ImageView) v.findViewById(R.id.action_password));

        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        final Reminder r = mDataset.get(position);

        // Top and Bottom divider visibility first element
        holder.top_divider.setVisibility(position == 0 ? View.GONE : View.VISIBLE);
        holder.bottom_divider.setVisibility(position != mDataset.size() - 1 ? View.GONE : View.VISIBLE);

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
        if (r.getDate_archived() == 0) {
            if (r.getPriority() == 1) {
                holder.reminder_icon.setBackground(ContextCompat.getDrawable(mContext, R.drawable.circle_back_starred));
            } else {
                if (r.getAlarm() != 0 && r.getDate_reminded() == 0) {
                    holder.reminder_icon.setBackground(ContextCompat.getDrawable(mContext, R.drawable.circle_back_primary_dark));
                } else {
                    holder.reminder_icon.setBackground(ContextCompat.getDrawable(mContext, R.drawable.circle_back_light));
                }
            }
        } else {
            holder.reminder_icon.setBackground(ContextCompat.getDrawable(mContext, R.drawable.circle_back_grey));
        }

        // Shortcuts
        if (r.getPasw().equals("")) {
            boolean link_browser;
            String url = Utils.checkURL(r.getTitle());
            if (url.equals(""))
                url = Utils.checkURL(Utils.getBigContentList(mContext, r.getContent()));
            if (!url.equals("")) {
                holder.action_browser.setVisibility(View.VISIBLE);
                final Intent url_intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                holder.action_browser.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mContext.startActivity(url_intent);
                    }
                });
                link_browser = true;
            } else {
                holder.action_browser.setVisibility(View.GONE);
                link_browser = false;
            }

            boolean contact;
            if (!r.getAction_info().equals("")) {
                holder.action_contact.setVisibility(View.VISIBLE);
                switch (r.getAction_type()) {
                    case Costants.ACTION_CALL:
                        final Intent call_intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + r.getAction_info()));
                        holder.action_contact.setImageResource(R.drawable.ic_action_communication_call);
                        holder.action_contact.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mContext.startActivity(call_intent);
                            }
                        });
                        break;
                    case Costants.ACTION_SMS:
                        final Intent sms_intent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + r.getAction_info()));
                        holder.action_contact.setImageResource(R.drawable.ic_action_communication_messenger);
                        holder.action_contact.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mContext.startActivity(sms_intent);
                            }
                        });
                        break;
                    case Costants.ACTION_MAIL:
                        final Intent mail_intent = new Intent(Intent.ACTION_VIEW, Uri.parse("mailto:" + r.getAction_info()));
                        holder.action_contact.setImageResource(R.drawable.ic_action_communication_email);
                        holder.action_contact.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mContext.startActivity(mail_intent);
                            }
                        });
                        break;
                    case Costants.ACTION_CONTACT:
                        Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, String.valueOf(r.getAction_info()));
                        final Intent contact_intent = new Intent(Intent.ACTION_VIEW, uri);
                        holder.action_contact.setImageResource(R.drawable.ic_person);
                        holder.action_contact.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mContext.startActivity(contact_intent);
                            }
                        });
                        break;
                }
                contact = true;
            } else {
                holder.action_contact.setVisibility(View.GONE);
                contact = false;
            }

            boolean attach;
            if (!r.getImg().equals("")) {
                holder.action_attach.setVisibility(View.VISIBLE);

                String[] imgs = r.getImg().split(Costants.LIST_IMG_SEPARATOR);
                if (imgs.length == 1) {
                    final Intent img_intent = new Intent(Intent.ACTION_VIEW, Uri.parse(imgs[0]));
                    img_intent.setDataAndType(Uri.parse(imgs[0]), "image/*");
                    holder.action_attach.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mContext.startActivity(img_intent);
                        }
                    });
                } else {
                    holder.action_attach.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(mContext, MyDialog.class);
                            i.setAction(Costants.ACTION_EDIT_ITEM);
                            i.putExtra(Costants.EXTRA_REMINDER, r);
                            mContext.startActivity(i);
                        }
                    });
                }
                attach = true;
            } else {
                holder.action_attach.setVisibility(View.GONE);
                attach = false;
            }

            boolean address;
            if (!r.getAddress().equals("")) {
                holder.action_address.setVisibility(View.VISIBLE);
                final Intent address_intent = new Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=" + r.getAddress()));
                holder.action_address.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mContext.startActivity(address_intent);
                    }
                });
                address = true;
            } else {
                holder.action_address.setVisibility(View.GONE);
                address = false;
            }

            if (!attach && !contact && !link_browser && !address)
                holder.container_option.setVisibility(View.GONE);
            else
                holder.container_option.setVisibility(View.VISIBLE);

            holder.action_password.setVisibility(View.GONE);
        } else {
            holder.action_contact.setVisibility(View.GONE);
            holder.action_attach.setVisibility(View.GONE);
            holder.action_browser.setVisibility(View.GONE);
            holder.action_address.setVisibility(View.GONE);
            holder.action_password.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    // GENERATE LIST
    public void generate_list(DbAdapter dbAdapter, String query) {
        mDataset.clear();
        Cursor c = dbAdapter.fetchAllRemindersFilterByTitle(SP.getBoolean(Costants.PREFERENCE_ORDER_ALARM_FIRST, true), query, new User(mContext).getId());
        while (c.moveToNext()) {
            Reminder r = new Reminder(c);
            if (!((!SP.getBoolean(Costants.PREFERENCES_LIST_ARCHIVED, false) && r.getDate_archived() != 0) ||
                    (!SP.getBoolean(Costants.PREFERENCES_LIST_STARRED, true) && r.getPriority() == 1 && r.getDate_archived() == 0) ||
                    (!SP.getBoolean(Costants.PREFERENCES_LIST_REMINDERS, true) && r.getAlarm() != 0 && r.getDate_archived() == 0) ||
                    (!SP.getBoolean(Costants.PREFERENCES_LIST_NOTE, true) && r.getAlarm() == 0 && r.getPriority() == 0 && r.getDate_archived() == 0)))
                mDataset.add(r);
        }
    }

    public void deleteElement(int position) {
        ReminderService.startAction(mContext, Costants.ACTION_ARCHIVE, mDataset.get(position));
        if (!SP.getBoolean(Costants.PREFERENCES_LIST_ARCHIVED, false)) {
            update(Costants.ACTION_DELETE, mDataset.get(position));
        } else {
            mDataset.get(position).setDate_archived(Calendar.getInstance().getTimeInMillis());
            notifyItemChanged(position);
        }
    }

    public void update(String action, Reminder r) {
        int pos = 0;
        switch (action) {
            case Costants.ACTION_CREATE:
                mDataset.add(0, r);
                notifyItemInserted(0);
                ((Main) mContext).recyclerGoUp();
                break;
            case Costants.ACTION_DELETE:
                for (Reminder reminder : mDataset) {
                    if (reminder.getId() == r.getId())
                        break;
                    else
                        pos++;
                }
                mDataset.remove(pos);
                notifyItemRemoved(pos);
                break;
            case Costants.ACTION_UPDATE:
                for (Reminder reminder : mDataset) {
                    if (reminder.getId() == r.getId())
                        break;
                    else
                        pos++;
                }
                mDataset.set(pos, r);
                notifyItemChanged(pos);
                break;
            case Costants.ACTION_UPDATE_DATE:
                for (Reminder reminder : mDataset) {
                    if (reminder.getId() == r.getId())
                        break;
                    else
                        pos++;
                }
                mDataset.set(pos, r);
                notifyItemChanged(pos);
                break;
        }
    }
}
