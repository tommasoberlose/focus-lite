package com.nego.flite.Widget;


import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
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
        Cursor c = dbHelper.fetchAllReminders(SP.getBoolean(Costants.PREFERENCE_ORDER_ALARM_FIRST, false));
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
        Cursor c = dbHelper.fetchAllReminders(SP.getBoolean(Costants.PREFERENCE_ORDER_ALARM_FIRST, false));
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
            SharedPreferences SP = mContext.getSharedPreferences(Costants.PREFERENCES_COSTANT, Context.MODE_PRIVATE);
            RemoteViews rv;

            if (SP.getString(Costants.PREFERENCE_STYLE_WIDGET, Costants.PREFERENCE_STYLE_WIDGET_MD).equals(Costants.PREFERENCE_STYLE_WIDGET_ML)) {
                rv = new RemoteViews(mContext.getPackageName(), R.layout.widget_row_ml);
            } else {
                rv = new RemoteViews(mContext.getPackageName(), R.layout.widget_row);
            }

            Intent i = new Intent();
            Bundle extras = new Bundle();
            extras.putParcelable(Costants.EXTRA_REMINDER, mWidgetItems.get(position));
            i.putExtras(extras);
            i.setAction(Costants.ACTION_EDIT_ITEM);
            rv.setOnClickFillInIntent(R.id.row, i);

            rv.setTextViewText(R.id.title, mWidgetItems.get(position).getTitle());

            if (mWidgetItems.get(position).getPasw().equals("")) {
                if (mWidgetItems.get(position).getContent().equals("")) {
                    if (mWidgetItems.get(position).getAlarm() == 0) {
                        rv.setTextViewText(R.id.subtitle, Utils.getDate(mContext, mWidgetItems.get(position).getDate_create()));
                    } else {
                        rv.setTextViewText(R.id.subtitle, Utils.getDateAlarm(mContext, mWidgetItems.get(position).getAlarm()));
                    }
                } else {
                    rv.setTextViewText(R.id.subtitle, Utils.getContentList(mContext, mWidgetItems.get(position).getContent()));
                }
            } else {
                rv.setTextViewText(R.id.subtitle, mContext.getString(R.string.text_locked_note));
            }

            if (mWidgetItems.get(position).getPasw().equals("")) {

                boolean link_browser;
                String url = Utils.checkURL(mWidgetItems.get(position).getTitle());
                if (url.equals(""))
                    url = Utils.checkURL(Utils.getBigContentList(mContext, mWidgetItems.get(position).getContent()));
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
                if (!mWidgetItems.get(position).getAction_info().equals("")) {
                    rv.setViewVisibility(R.id.action_contact, View.VISIBLE);
                    switch (mWidgetItems.get(position).getAction_type()) {
                        case Costants.ACTION_CALL:
                            Intent call_intent = new Intent(Intent.ACTION_DIAL).putExtra(Costants.EXTRA_ACTION_TYPE, "tel:" + mWidgetItems.get(position).getAction_info());
                            rv.setViewVisibility(R.id.action_contact, View.VISIBLE);
                            rv.setImageViewResource(R.id.action_contact, R.drawable.ic_action_communication_call);
                            rv.setOnClickFillInIntent(R.id.action_contact, call_intent);
                            break;
                        case Costants.ACTION_SMS:
                            Intent sms_intent = new Intent(Intent.ACTION_VIEW).putExtra(Costants.EXTRA_ACTION_TYPE, "sms:" + mWidgetItems.get(position).getAction_info());
                            rv.setViewVisibility(R.id.action_contact, View.VISIBLE);
                            rv.setImageViewResource(R.id.action_contact, R.drawable.ic_action_communication_messenger);
                            rv.setOnClickFillInIntent(R.id.action_contact, sms_intent);
                            break;
                        case Costants.ACTION_MAIL:
                            Intent mail_intent = new Intent(Intent.ACTION_VIEW).putExtra(Costants.EXTRA_ACTION_TYPE, "mailto:" + mWidgetItems.get(position).getAction_info());
                            rv.setViewVisibility(R.id.action_contact, View.VISIBLE);
                            rv.setImageViewResource(R.id.action_contact, R.drawable.ic_action_communication_email);
                            rv.setOnClickFillInIntent(R.id.action_contact, mail_intent);
                            break;
                        case Costants.ACTION_CONTACT:
                            Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, String.valueOf(mWidgetItems.get(position).getAction_info()));
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
                if (!mWidgetItems.get(position).getImg().equals("")) {
                    rv.setViewVisibility(R.id.action_attach, View.VISIBLE);

                    String[] imgs = mWidgetItems.get(position).getImg().split(Costants.LIST_IMG_SEPARATOR);
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

                if (!attach && !contact && !link_browser)
                    rv.setViewVisibility(R.id.container_options, View.GONE);
                else
                    rv.setViewVisibility(R.id.container_options, View.VISIBLE);

                rv.setViewVisibility(R.id.action_password, View.GONE);
            } else {
                rv.setViewVisibility(R.id.action_contact, View.GONE);
                rv.setViewVisibility(R.id.action_attach, View.GONE);
                rv.setViewVisibility(R.id.action_browser, View.GONE);
                rv.setViewVisibility(R.id.action_password, View.VISIBLE);
            }

            if (mWidgetItems.get(position).getPriority() == 1) {
                rv.setViewVisibility(R.id.action_priority, View.VISIBLE);
            } else {
                rv.setViewVisibility(R.id.action_priority, View.GONE);
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
