package com.nego.flite.Functions;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.nego.flite.Costants;
import com.nego.flite.Main;
import com.nego.flite.MyDialog;
import com.nego.flite.R;
import com.nego.flite.Reminder;
import com.nego.flite.Utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;

public class NotificationF {


    public static void Notification(Context context, Reminder r) {
        Intent i=new Intent(context,MyDialog.class);
        i.setAction(Costants.ACTION_EDIT_ITEM);
        i.putExtra(Costants.EXTRA_REMINDER, r);
        PendingIntent pi= PendingIntent.getActivity(context, r.getId(), i, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent archive_i = new Intent(context, MyDialog.class);
        archive_i.setAction(Costants.ACTION_ARCHIVE);
        archive_i.putExtra(Costants.EXTRA_REMINDER, r);
        PendingIntent pi_archive = PendingIntent.getActivity(context, r.getId(), archive_i, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent delete_i = new Intent(context, MyDialog.class);
        delete_i.setAction(Costants.ACTION_DELETE);
        delete_i.putExtra(Costants.EXTRA_REMINDER, r);
        PendingIntent pi_delete = PendingIntent.getActivity(context, r.getId(), delete_i, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder n  = new NotificationCompat.Builder(context)
                .setContentTitle(r.getTitle())
                .setSmallIcon(R.drawable.ic_not_flite)
                .setContentIntent(pi)
                .setColor(ContextCompat.getColor(context, R.color.primary))
                .setPriority(Notification.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender();
        try {
            wearableExtender.setBackground(BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.background_wear));
        } catch (Exception e) {
            e.printStackTrace();
        }



        if (r.getPasw().equals("")) {
            if (r.getContent().equals("")) {
                if (r.getAlarm() == 0) {
                    n.setContentText(Utils.getDate(context, r.getDate_create()));
                } else {
                    n.setContentText(Utils.getAlarm(context, r.getAlarm(), r.getAlarm_repeat(), r.getDate_reminded()));
                }
            } else {
                n.setContentText(Utils.getContentList(context, r.getContent()));
                n.setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(Utils.getBigContentList(context, r.getContent())));
            }
        } else {
            n.setContentText(context.getString(R.string.text_locked_note));
        }

        SharedPreferences SP = context.getSharedPreferences(Costants.PREFERENCES_COSTANT, Context.MODE_PRIVATE);
        if ((r.getAlarm_repeat().equals("") || r.getAlarm() == Costants.ALARM_TYPE_BLUETOOTH || r.getAlarm() == Costants.ALARM_TYPE_WIFI) && SP.getBoolean(Costants.PREFERENCE_BUTTON_DELETE, true)) {
            if (SP.getBoolean(Costants.PREFERENCE_BUTTON_DELETE_TO_ARCHIVE, false)) {
                n.setDeleteIntent(pi_archive);
            } else {
                n.setDeleteIntent(pi_delete);
            }
        }

        if (SP.getBoolean(Costants.PREFERENCE_BUTTON_DELETE_NOT_ONGOING, false)) {

            if (SP.getBoolean(Costants.PREFERENCE_BUTTON_DELETE_TO_ARCHIVE, false)) {
                n.addAction(R.drawable.ic_action_check, context.getString(R.string.action_archive), pi_archive);
            } else {
                n.addAction(R.drawable.ic_action_delete, context.getString(R.string.action_delete), pi_delete);
            }
        }

        if (SP.getBoolean(Costants.PREFERENCES_NOTIFICATION_SOUND, true)) {
            n.setSound(Uri.parse(SP.getString(Costants.PREFERENCES_NOTIFICATION_RINGTONE, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION).toString())));
        }
        if (SP.getBoolean(Costants.PREFERENCES_NOTIFICATION_LED, true)) {
            n.setLights(ContextCompat.getColor(context, R.color.primary_dark), 3000, 3000);
        }
        if (SP.getBoolean(Costants.PREFERENCES_NOTIFICATION_VIBRATE, true)) {
            n.setVibrate(new long[]{0, 300, 200, 300});
        }


        if (!r.getImg().equals("")) {
            String[] imgs = r.getImg().split(Costants.LIST_IMG_SEPARATOR);
            if (imgs.length == 1) {
                try {
                    n.setStyle(new android.support.v4.app.NotificationCompat.BigPictureStyle().bigPicture(MediaStore.Images.Media.getBitmap(context.getContentResolver(), Uri.parse(imgs[0]))));

                    Intent img_intent = new Intent(Intent.ACTION_VIEW, Uri.parse(imgs[0]));
                    img_intent.putExtra(Costants.EXTRA_IS_PHOTO, true);
                    PendingIntent img_pi = PendingIntent.getActivity(context, r.getId(), img_intent, 0);
                    n.addAction(R.drawable.ic_action_gallery, context.getString(R.string.action_view), img_pi);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if (!r.getAddress().equals("")) {
            Intent address_intent = new Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=" + r.getAddress()));
            PendingIntent address_pi = PendingIntent.getActivity(context, r.getId(), address_intent, 0);
            n.addAction(R.drawable.ic_action_maps_directions, context.getString(R.string.action_navigate), address_pi);
        }

        if (!r.getAction_type().equals("")) {
            switch (r.getAction_type()) {
                case Costants.ACTION_CALL:
                    Intent call_intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+r.getAction_info()));
                    PendingIntent call_pi = PendingIntent.getActivity(context, r.getId(), call_intent, 0);
                    n.addAction(R.drawable.ic_action_communication_call, context.getString(R.string.action_call), call_pi);
                    wearableExtender.addAction(new NotificationCompat.Action.Builder(R.drawable.ic_action_communication_call, context.getString(R.string.action_call), call_pi).build());
                    break;
                case Costants.ACTION_SMS:
                    Intent sms_intent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:"+r.getAction_info()));
                    PendingIntent sms_pi = PendingIntent.getActivity(context, r.getId(), sms_intent, 0);
                    n.addAction(R.drawable.ic_action_communication_messenger, context.getString(R.string.action_sms), sms_pi);
                    wearableExtender.addAction(new NotificationCompat.Action.Builder(R.drawable.ic_action_communication_messenger, context.getString(R.string.action_sms), sms_pi).build());
                    break;
                case Costants.ACTION_MAIL:
                    Intent mail_intent = new Intent(Intent.ACTION_VIEW, Uri.parse("mailto:"+r.getAction_info()));
                    PendingIntent mail_pi = PendingIntent.getActivity(context, r.getId(), mail_intent, 0);
                    n.addAction(R.drawable.ic_action_communication_email, context.getString(R.string.action_mail), mail_pi);
                    wearableExtender.addAction(new NotificationCompat.Action.Builder(R.drawable.ic_action_communication_email, context.getString(R.string.action_mail), mail_pi).build());
                    break;
            }
        }

        String url_title = Utils.checkURL(r.getTitle());
        String url_content = Utils.checkURL(Utils.getBigContentList(context, r.getContent()));
        String url = url_title;
        if (url.equals(""))
            url = url_content;
        if (!url.equals("")) {
            Intent url_intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            PendingIntent url_pi = PendingIntent.getActivity(context, r.getId(), url_intent, 0);
            n.addAction(R.drawable.ic_action_open_in_browser, context.getString(R.string.open_in_browser), url_pi);
            wearableExtender.addAction(new NotificationCompat.Action.Builder(R.drawable.ic_action_open_in_browser, context.getString(R.string.open_in_browser), url_pi).build());
        }


        if (SP.getBoolean(Costants.PREFERENCE_BUTTON_SHARE, true)) {
            Intent share_intent = new Intent(Intent.ACTION_SEND);
            share_intent.putExtra(Intent.EXTRA_TEXT, r.getTitle() + Utils.getBigContentList(context, r.getContent()));
            share_intent.setType("text/plain");
            PendingIntent share_pi = PendingIntent.getActivity(context, r.getId(), share_intent, 0);
            n.addAction(R.drawable.ic_action_social_share, context.getString(R.string.action_share), share_pi);
        }

        if (SP.getBoolean(Costants.PREFERENCE_BUTTON_DELETE, true)) {
            if (SP.getBoolean(Costants.PREFERENCE_BUTTON_DELETE_TO_ARCHIVE, false)) {
                Intent archive_i_wear = new Intent(context, MyDialog.class);
                archive_i_wear.setAction(Costants.ACTION_ARCHIVE_WEAR);
                archive_i_wear.putExtra(Costants.EXTRA_REMINDER, r);
                PendingIntent pi_archive_wear = PendingIntent.getActivity(context, r.getId(), archive_i_wear, PendingIntent.FLAG_UPDATE_CURRENT);
                wearableExtender.addAction(new NotificationCompat.Action.Builder(R.drawable.ic_action_check, context.getString(R.string.action_archive), pi_archive_wear).build());
            } else {
                Intent delete_i_wear = new Intent(context, MyDialog.class);
                delete_i_wear.setAction(Costants.ACTION_DELETE_WEAR);
                delete_i_wear.putExtra(Costants.EXTRA_REMINDER, r);
                PendingIntent pi_delete_wear = PendingIntent.getActivity(context, r.getId(), delete_i_wear, PendingIntent.FLAG_UPDATE_CURRENT);
                wearableExtender.addAction(new NotificationCompat.Action.Builder(R.drawable.ic_action_delete, context.getString(R.string.action_delete), pi_delete_wear).build());
            }

        }

        Intent snooze_i = new Intent(context, MyDialog.class);
        snooze_i.setAction(Costants.ACTION_SNOOZE);
        snooze_i.putExtra(Costants.EXTRA_REMINDER, r);
        PendingIntent pi_snooze = PendingIntent.getActivity(context, r.getId(), snooze_i, PendingIntent.FLAG_UPDATE_CURRENT);
        n.addAction(R.drawable.ic_action_time_small, context.getString(R.string.action_snooze), pi_snooze);

        Intent snooze_i_wear = new Intent(context, MyDialog.class);
        snooze_i_wear.setAction(Costants.ACTION_SNOOZE_WEAR);
        snooze_i_wear.putExtra(Costants.EXTRA_REMINDER, r);
        PendingIntent pi_snooze_wear = PendingIntent.getActivity(context, r.getId(), snooze_i_wear, PendingIntent.FLAG_UPDATE_CURRENT);
        wearableExtender.addAction(new NotificationCompat.Action.Builder(R.drawable.ic_action_time_small, context.getString(R.string.action_snooze), pi_snooze_wear).build());

        n.extend(wearableExtender);

        notificationManager.notify(r.getId() + Costants.PLUS_NOTIFICATION, n.build());

        // Aggiorno il promemoria con il fatto di aver fatto suonare l'allarme
        r.setDate_reminded(Calendar.getInstance().getTimeInMillis());
        ReminderService.startAction(context, Costants.ACTION_UPDATE_DATE, r);
    }

    // NOTIFICATION FIXED
    public static void NotificationFixed(Context context, Reminder r) {
        Intent i=new Intent(context,MyDialog.class);
        i.setAction(Costants.ACTION_EDIT_ITEM);
        i.putExtra(Costants.EXTRA_REMINDER, r);
        PendingIntent pi= PendingIntent.getActivity(context, r.getId(), i, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent delete_i = new Intent(context, MyDialog.class);
        delete_i.setAction(Costants.ACTION_ARCHIVE);
        delete_i.putExtra(Costants.EXTRA_REMINDER, r);
        PendingIntent pi_delete = PendingIntent.getActivity(context, r.getId(), delete_i, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder n  = new NotificationCompat.Builder(context)
                .setContentTitle(r.getTitle())
                .setContentIntent(pi)
                .setPriority(-1)
                .setColor(ContextCompat.getColor(context, R.color.primary))
                .setPriority(Notification.PRIORITY_MIN)
                .setAutoCancel(false);

        // ICONS
        if (r.getPriority() == 1) {
            n.setSmallIcon(R.drawable.ic_stat_toggle_star);
        } else {
            if (r.getDate_reminded() != 0) {
                n.setSmallIcon(R.drawable.ic_not_flite);
            } else {
                if (r.getAlarm() == 0) {
                    n.setSmallIcon(R.drawable.ic_stat_generic);
                } else {
                    if (!r.getAlarm_repeat().equals("")) {
                        if (r.getAlarm() == Costants.ALARM_TYPE_WIFI)
                            n.setSmallIcon(R.drawable.ic_stat_wifi);
                        else if (r.getAlarm() == Costants.ALARM_TYPE_BLUETOOTH)
                            n.setSmallIcon(R.drawable.ic_stat_bluetooth);
                        else
                            n.setSmallIcon(R.drawable.ic_stat_av_replay);
                    } else {
                        n.setSmallIcon(R.drawable.ic_stat_av_snooze);
                    }
                }
            }
        }


        if (r.getPasw().equals("")) {
            if (r.getContent().equals("")) {
                if (r.getAlarm() == 0) {
                    n.setContentText(Utils.getDate(context, r.getDate_create()));
                } else {
                    n.setContentText(Utils.getAlarm(context, r.getAlarm(), r.getAlarm_repeat(), r.getDate_reminded()));
                }
            } else {
                n.setContentText(Utils.getContentList(context, r.getContent()));
                n.setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(Utils.getBigContentList(context, r.getContent())));
            }
        } else {
            n.setContentText(context.getString(R.string.text_locked_note));
        }

        SharedPreferences SP = context.getSharedPreferences(Costants.PREFERENCES_COSTANT, Context.MODE_PRIVATE);
        if (SP.getBoolean(Costants.PREFERENCE_BUTTON_DELETE, true)) {
            n.setDeleteIntent(pi_delete);
        }

        if (SP.getBoolean(Costants.PREFERENCE_BUTTON_DELETE_NOT_ONGOING, false)) {
            n.addAction(R.drawable.ic_action_check, context.getString(R.string.action_archive), pi_delete);
        }

        if (SP.getBoolean(Costants.PREFERENCE_ONGOING_NOTIFICATIONS, true)) {
            n.setOngoing(true);
        } else {
            n.setOngoing(false);
        }

        if (!r.getImg().equals("")) {
            String[] imgs = r.getImg().split(Costants.LIST_IMG_SEPARATOR);
            if (imgs.length == 1) {
                try {
                    n.setStyle(new android.support.v4.app.NotificationCompat.BigPictureStyle().bigPicture(MediaStore.Images.Media.getBitmap(context.getContentResolver(), Uri.parse(imgs[0]))));

                    Intent img_intent = new Intent(Intent.ACTION_VIEW, Uri.parse(imgs[0]));
                    img_intent.putExtra(Costants.EXTRA_IS_PHOTO, true);
                    PendingIntent img_pi = PendingIntent.getActivity(context, r.getId(), img_intent, 0);
                    n.addAction(R.drawable.ic_action_gallery, context.getString(R.string.action_view), img_pi);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if (!r.getAddress().equals("")) {
            Intent address_intent = new Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=" + r.getAddress()));
            PendingIntent address_pi = PendingIntent.getActivity(context, r.getId(), address_intent, 0);
            n.addAction(R.drawable.ic_action_maps_directions, context.getString(R.string.action_navigate), address_pi);
        }

        if (!r.getAction_type().equals("")) {
            switch (r.getAction_type()) {
                case Costants.ACTION_CALL:
                    Intent call_intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+r.getAction_info()));
                    PendingIntent call_pi = PendingIntent.getActivity(context, r.getId(), call_intent, 0);
                    n.addAction(R.drawable.ic_action_communication_call, context.getString(R.string.action_call), call_pi);
                    break;
                case Costants.ACTION_SMS:
                    Intent sms_intent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:"+r.getAction_info()));
                    PendingIntent sms_pi = PendingIntent.getActivity(context, r.getId(), sms_intent, 0);
                    n.addAction(R.drawable.ic_action_communication_messenger, context.getString(R.string.action_sms), sms_pi);
                    break;
                case Costants.ACTION_MAIL:
                    Intent mail_intent = new Intent(Intent.ACTION_VIEW, Uri.parse("mailto:"+r.getAction_info()));
                    PendingIntent mail_pi = PendingIntent.getActivity(context, r.getId(), mail_intent, 0);
                    n.addAction(R.drawable.ic_action_communication_email, context.getString(R.string.action_mail), mail_pi);
                    break;
                case Costants.ACTION_CONTACT:
                    Intent contact_intent = new Intent(Intent.ACTION_VIEW);
                    Uri contact_uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, String.valueOf(r.getAction_info()));
                    contact_intent.setData(contact_uri);
                    PendingIntent contact_pi = PendingIntent.getActivity(context, r.getId(), contact_intent, 0);
                    n.addAction(R.drawable.ic_action_account_circle_small, context.getString(R.string.text_contact), contact_pi);
                    break;
            }
        }

        String url_title = Utils.checkURL(r.getTitle());
        String url_content = Utils.checkURL(Utils.getBigContentList(context, r.getContent()));
        String url = url_title;
        if (url.equals(""))
            url = url_content;
        if (!url.equals("")) {
            Intent url_intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            PendingIntent url_pi = PendingIntent.getActivity(context, r.getId(), url_intent, 0);
            n.addAction(R.drawable.ic_action_open_in_browser, context.getString(R.string.open_in_browser), url_pi);
        }

        if (SP.getBoolean(Costants.PREFERENCE_BUTTON_SHARE, true)) {
            Intent share_intent = new Intent(Intent.ACTION_SEND);
            share_intent.putExtra(Intent.EXTRA_TEXT, r.getTitle() + Utils.getBigContentList(context, r.getContent()));
            share_intent.setType("text/plain");
            PendingIntent share_pi = PendingIntent.getActivity(context, r.getId(), share_intent, 0);
            n.addAction(R.drawable.abc_ic_menu_share_mtrl_alpha, context.getString(R.string.action_share), share_pi);
        }


        notificationManager.notify(r.getId(), n.build());
    }

    // NOTIFICATION ADD
    public static void NotificationAdd(Context context) {
        SharedPreferences SP = context.getSharedPreferences(Costants.PREFERENCES_COSTANT, Context.MODE_PRIVATE);

        int count = Utils.itemsToDo(context);
        if (!(!SP.getBoolean(Costants.PREFERENCE_SHOW_ADD_NOTIFICATION, true) && count == 0)) {
            String item_to_do = context.getString(R.string.no_items);
            if (count > 0)
                item_to_do = context.getResources().getString(R.string.num_items_todo, count);

            Intent i = new Intent(context, MyDialog.class);
            i.setAction(Costants.ACTION_ADD_ITEM);
            PendingIntent pi = PendingIntent.getActivity(context, -1, i, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationManager notificationManager = (NotificationManager)
                    context.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationCompat.Builder n = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.ic_stat_flite_plus)
                    .setContentIntent(pi)
                    .setOngoing(true)
                    .setPriority(-1)
                    .setPriority(Notification.PRIORITY_MIN)
                    .setAutoCancel(false);

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                    R.layout.add_notification_layout);
            remoteViews.setTextViewText(R.id.notification_title, context.getString(R.string.title_activity_add_item));
            remoteViews.setTextViewText(R.id.notification_subtitle, item_to_do);

            if (count > 0) {
                if (SP.getBoolean(Costants.PREFERENCES_VIEW_ALL, true)) {
                    PendingIntent pi_hide = PendingIntent.getBroadcast(context, -2, new Intent(Costants.ACTION_HIDE_ALL), PendingIntent.FLAG_UPDATE_CURRENT);
                    remoteViews.setOnClickPendingIntent(R.id.toggle_icon, pi_hide);
                    remoteViews.setImageViewResource(R.id.toggle_icon, R.drawable.ic_action_hide_all);
                    remoteViews.setOnClickFillInIntent(R.id.toggle_icon, new Intent(Costants.ACTION_HIDE_ALL));
                } else {
                    PendingIntent pi_view = PendingIntent.getBroadcast(context, -2, new Intent(Costants.ACTION_VIEW_ALL), PendingIntent.FLAG_UPDATE_CURRENT);
                    remoteViews.setOnClickPendingIntent(R.id.toggle_icon, pi_view);
                    remoteViews.setImageViewResource(R.id.toggle_icon, R.drawable.ic_action_view_all);
                    remoteViews.setOnClickFillInIntent(R.id.toggle_icon, new Intent(Costants.ACTION_VIEW_ALL));
                }
                remoteViews.setViewVisibility(R.id.toggle_icon, View.VISIBLE);
            } else {
                remoteViews.setViewVisibility(R.id.toggle_icon, View.GONE);
            }

            n.setContent(remoteViews);

            notificationManager.notify(-1, n.build());
        }
    }

    public static void CancelAllNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    public static void CancelNotification(Context context, String id) {
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(Integer.parseInt(id));
    }

}
