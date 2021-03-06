package com.nego.flite.Widget;


import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import android.widget.Toast;

import com.nego.flite.Costants;
import com.nego.flite.Main;
import com.nego.flite.MyDialog;
import com.nego.flite.R;
import com.nego.flite.Reminder;
import com.nego.flite.User;
import com.nego.flite.Utils;
import com.nego.flite.database.DbAdapter;

import java.util.ArrayList;
import java.util.List;

public class WidgetViewsFactory implements
        RemoteViewsService.RemoteViewsFactory {
    private List<Reminder> mWidgetItems = new ArrayList<>();
    private Context mContext;
    private int mAppWidgetId;

    public WidgetViewsFactory(Context context, Intent intent) {
        SharedPreferences SP = context.getSharedPreferences(Costants.PREFERENCES_COSTANT, Context.MODE_PRIVATE);
        mContext = context;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);

        DbAdapter dbHelper = new DbAdapter(mContext);
        dbHelper.open();
        Cursor c = dbHelper.fetchAllReminders(SP.getBoolean(Costants.PREFERENCE_ORDER_ALARM_FIRST, false), Utils.getActiveUserId(dbHelper));
        while (c.moveToNext()) {
            mWidgetItems.add(new Reminder(c));
        }
        c.close();
        dbHelper.close();
    }

    public void onCreate() {
    }

    @Override
    public void onDataSetChanged() {
        SharedPreferences SP = mContext.getSharedPreferences(Costants.PREFERENCES_COSTANT, Context.MODE_PRIVATE);

        mWidgetItems.clear();

        DbAdapter dbHelper = new DbAdapter(mContext);
        dbHelper.open();
        Cursor c = dbHelper.fetchAllReminders(SP.getBoolean(Costants.PREFERENCE_ORDER_ALARM_FIRST, false), Utils.getActiveUserId(dbHelper));
        while (c.moveToNext()) {
            mWidgetItems.add(new Reminder(c));
        }
        c.close();
        dbHelper.close();
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return mWidgetItems.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        if (position < mWidgetItems.size()) {
            Reminder r = mWidgetItems.get(position);
            SharedPreferences SP = mContext.getSharedPreferences(Costants.PREFERENCES_COSTANT, Context.MODE_PRIVATE);
            RemoteViews rv;

            if (SP.getString(Costants.PREFERENCE_STYLE_WIDGET, Costants.PREFERENCE_STYLE_WIDGET_MD).equals(Costants.PREFERENCE_STYLE_WIDGET_ML)) {
                rv = new RemoteViews(mContext.getPackageName(), R.layout.widget_row_ml);
            } else {
                rv = new RemoteViews(mContext.getPackageName(), R.layout.widget_row);
            }

            Intent i = new Intent();
            Bundle extras = new Bundle();
            extras.putParcelable(Costants.EXTRA_REMINDER, r);
            i.putExtras(extras);
            i.setAction(Costants.ACTION_EDIT_ITEM);
            rv.setOnClickFillInIntent(R.id.row, i);

            rv.setTextViewText(R.id.title, r.getTitle());

            if (r.getPasw().equals("")) {
                if (r.getContent().equals("")) {
                    if (r.getAlarm() == 0) {
                        rv.setTextViewText(R.id.subtitle, Utils.getDate(mContext, r.getDate_create()));
                    } else {
                        rv.setTextViewText(R.id.subtitle, Utils.getAlarm(mContext, r.getAlarm(), r.getAlarm_repeat(), r.getDate_reminded()));
                    }
                } else {
                    rv.setTextViewText(R.id.subtitle, Utils.getContentList(mContext, r.getContent()));
                }
            } else {
                rv.setTextViewText(R.id.subtitle, mContext.getString(R.string.text_locked_note));
            }

            // Icon
            if (r.getDate_archived() == 0) {
                if (r.getPriority() == 1) {
                    rv.setInt(R.id.reminder_icon, "setBackgroundResource",
                            R.drawable.circle_back_starred);
                } else {
                    if (r.getColor().equals("")) {
                        if (r.getAlarm() != 0 && r.getDate_reminded() == 0) {
                            rv.setInt(R.id.reminder_icon, "setBackgroundResource",
                                    R.drawable.circle_back_primary_dark);
                        } else {
                            rv.setInt(R.id.reminder_icon, "setBackgroundResource",
                                    R.drawable.circle_back_light);
                        }
                    } else {
                        rv.setInt(R.id.reminder_icon, "setBackgroundResource",
                                Utils.getCustomColorBackground(r.getColor()));
                    }
                }
            } else {
                rv.setInt(R.id.reminder_icon, "setBackgroundResource",
                        R.drawable.circle_back_grey);
            }

            if (r.getPasw().equals("")) {

                boolean link_browser;
                String url = Utils.checkURL(r.getTitle());
                if (url.equals(""))
                    url = Utils.checkURL(Utils.getBigContentList(mContext, r.getContent()));
                if (!url.equals("")) {
                    rv.setViewVisibility(R.id.action_browser, View.VISIBLE);
                    Intent url_intent = new Intent(Intent.ACTION_VIEW).putExtra(Costants.EXTRA_ACTION_TYPE, url);
                    rv.setOnClickFillInIntent(R.id.action_browser, url_intent);
                    link_browser = true;
                } else {
                    rv.setViewVisibility(R.id.action_browser, View.GONE);
                    link_browser = false;
                }

                boolean contact;
                if (!r.getAction_info().equals("")) {
                    rv.setViewVisibility(R.id.action_contact, View.VISIBLE);
                    switch (r.getAction_type()) {
                        case Costants.ACTION_CALL:
                            Intent call_intent = new Intent(Intent.ACTION_DIAL).putExtra(Costants.EXTRA_ACTION_TYPE, "tel:" + r.getAction_info());
                            rv.setViewVisibility(R.id.action_contact, View.VISIBLE);
                            rv.setImageViewResource(R.id.action_contact, R.drawable.ic_action_communication_call);
                            rv.setOnClickFillInIntent(R.id.action_contact, call_intent);
                            break;
                        case Costants.ACTION_SMS:
                            Intent sms_intent = new Intent(Intent.ACTION_VIEW).putExtra(Costants.EXTRA_ACTION_TYPE, "sms:" + r.getAction_info());
                            rv.setViewVisibility(R.id.action_contact, View.VISIBLE);
                            rv.setImageViewResource(R.id.action_contact, R.drawable.ic_action_communication_messenger);
                            rv.setOnClickFillInIntent(R.id.action_contact, sms_intent);
                            break;
                        case Costants.ACTION_MAIL:
                            Intent mail_intent = new Intent(Intent.ACTION_VIEW).putExtra(Costants.EXTRA_ACTION_TYPE, "mailto:" + r.getAction_info());
                            rv.setViewVisibility(R.id.action_contact, View.VISIBLE);
                            rv.setImageViewResource(R.id.action_contact, R.drawable.ic_action_communication_email);
                            rv.setOnClickFillInIntent(R.id.action_contact, mail_intent);
                            break;
                        case Costants.ACTION_CONTACT:
                            Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, String.valueOf(r.getAction_info()));
                            Intent contact_intent = new Intent(Intent.ACTION_VIEW).putExtra(Costants.EXTRA_ACTION_TYPE, uri.toString());
                            rv.setViewVisibility(R.id.action_contact, View.VISIBLE);
                            rv.setImageViewResource(R.id.action_contact, R.drawable.ic_person);
                            rv.setOnClickFillInIntent(R.id.action_contact, contact_intent);
                            break;
                    }
                    contact = true;
                } else {
                    rv.setViewVisibility(R.id.action_contact, View.GONE);
                    contact = false;
                }

                boolean attach;
                if (!r.getImg().equals("")) {
                    rv.setViewVisibility(R.id.action_attach, View.VISIBLE);

                    String[] imgs = r.getImg().split(Costants.LIST_IMG_SEPARATOR);
                    if (imgs.length == 1) {
                        Intent img_intent = new Intent(Intent.ACTION_VIEW).putExtra(Costants.EXTRA_ACTION_TYPE, imgs[0]);
                        img_intent.putExtra(Costants.EXTRA_IS_PHOTO, true);
                        rv.setOnClickFillInIntent(R.id.action_attach, img_intent);
                    } else {
                        rv.setOnClickFillInIntent(R.id.action_attach, i);
                    }
                    attach = true;
                } else {
                    rv.setViewVisibility(R.id.action_attach, View.GONE);
                    attach = false;
                }

                boolean address;
                if (!r.getAddress().equals("")) {
                    rv.setViewVisibility(R.id.action_address, View.VISIBLE);
                    Intent address_intent = new Intent(Intent.ACTION_VIEW).putExtra(Costants.EXTRA_ACTION_TYPE, "google.navigation:q=" + r.getAddress());
                    rv.setOnClickFillInIntent(R.id.action_address, address_intent);
                    address = true;
                } else {
                    rv.setViewVisibility(R.id.action_address, View.GONE);
                    address = false;
                }

                if (!attach && !contact && !link_browser && !address)
                    rv.setViewVisibility(R.id.container_options, View.GONE);
                else
                    rv.setViewVisibility(R.id.container_options, View.VISIBLE);

                rv.setViewVisibility(R.id.action_password, View.GONE);
            } else {
                rv.setViewVisibility(R.id.action_contact, View.GONE);
                rv.setViewVisibility(R.id.action_attach, View.GONE);
                rv.setViewVisibility(R.id.action_browser, View.GONE);
                rv.setViewVisibility(R.id.action_address, View.GONE);
                rv.setViewVisibility(R.id.action_password, View.VISIBLE);
            }
            
            return rv;
        } else {
            return null;
        }
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }
}
