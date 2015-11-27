package com.nego.flite.Functions;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import com.nego.flite.Costants;
import com.nego.flite.Main;
import com.nego.flite.MyDialog;
import com.nego.flite.R;
import com.nego.flite.Reminder;
import com.nego.flite.Utils;

import java.io.FileNotFoundException;
import java.io.IOException;

public class NotificationF {


    public static void Notification(Context context, Reminder r) {
        Intent i=new Intent(context,MyDialog.class);
        i.setAction(Costants.ACTION_EDIT_ITEM);
        i.putExtra(Costants.EXTRA_REMINDER, r);
        PendingIntent pi= PendingIntent.getActivity(context, r.getId(), i, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent clear_i=new Intent(context,MyDialog.class);
        clear_i.setAction(Costants.ACTION_UPDATE_LIST);
        PendingIntent pi_clear= PendingIntent.getActivity(context, r.getId(), clear_i, PendingIntent.FLAG_UPDATE_CURRENT);

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
                .setColor(context.getResources().getColor(R.color.primary))
                .setPriority(Notification.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender();

        if (r.getContent().equals("")) {
            if (r.getAlarm() == 0) {
                n.setContentText(Utils.getDate(context, r.getDate_create()));
            } else {
                n.setContentText(Utils.getDateAlarm(context, r.getAlarm()));
            }
        } else {
            if (r.getPasw().equals("")) {
                n.setContentText(Utils.getContentList(context, r.getContent()));
                n.setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(Utils.getBigContentList(context, r.getContent())));
            } else {
                n.setContentText(context.getString(R.string.text_locked_note));
            }
        }

        SharedPreferences SP = context.getSharedPreferences(Costants.PREFERENCES_COSTANT, Context.MODE_PRIVATE);
        if (SP.getBoolean(Costants.PREFERENCE_BUTTON_DELETE, true)) {
            n.setDeleteIntent(pi_clear);
        } else {
            n.setDeleteIntent(pi_delete);
        }

        if (SP.getBoolean(Costants.PREFERENCES_NOTIFICATION_SOUND, true)) {
            n.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        }
        if (SP.getBoolean(Costants.PREFERENCES_NOTIFICATION_LED, true)) {
            n.setLights(context.getResources().getColor(R.color.primary_dark), 3000, 3000);
        }
        if (SP.getBoolean(Costants.PREFERENCES_NOTIFICATION_VIBRATE, true)) {
            n.setVibrate(new long[]{0, 400, 400, 400});
        }

        if (!r.getImg().equals("")) {
            try {
                n.setStyle(new android.support.v4.app.NotificationCompat.BigPictureStyle().bigPicture(MediaStore.Images.Media.getBitmap(context.getContentResolver(), Uri.parse(r.getImg()))));
            } catch (Exception e) {

            }
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

        String url = Utils.checkURL(r.getTitle());
        if (url != "") {
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
            n.addAction(R.drawable.abc_ic_menu_share_mtrl_alpha, context.getString(R.string.action_share), share_pi);
        }

        if (SP.getBoolean(Costants.PREFERENCE_BUTTON_DELETE, true)) {
            n.addAction(R.drawable.ic_action_delete, context.getString(R.string.action_delete), pi_delete);
            delete_i.putExtra(Costants.FROM_WEAR, true);
            PendingIntent pi_delete_wear = PendingIntent.getActivity(context, r.getId(), delete_i, PendingIntent.FLAG_UPDATE_CURRENT);
            wearableExtender.addAction(new NotificationCompat.Action.Builder(R.drawable.ic_action_delete, context.getString(R.string.action_delete), pi_delete_wear).build());
        }

        Intent snooze_i = new Intent(context, MyDialog.class);
        snooze_i.setAction(Costants.ACTION_SNOOZE);
        snooze_i.putExtra(Costants.EXTRA_REMINDER, r);
        PendingIntent pi_snooze = PendingIntent.getActivity(context, r.getId(), snooze_i, PendingIntent.FLAG_UPDATE_CURRENT);
        n.addAction(R.drawable.ic_action_time_small, context.getString(R.string.action_snooze), pi_snooze);
        snooze_i.putExtra(Costants.FROM_WEAR, true);
        PendingIntent pi_snooze_wear = PendingIntent.getActivity(context, r.getId(), snooze_i, PendingIntent.FLAG_UPDATE_CURRENT);
        wearableExtender.addAction(new NotificationCompat.Action.Builder(R.drawable.ic_action_time_small, context.getString(R.string.action_snooze), pi_snooze_wear).build());


        if (r.getTitle().length() >= 40) {
            // Create a big text style for the second page
            NotificationCompat.BigTextStyle secondPageStyle = new NotificationCompat.BigTextStyle();
            secondPageStyle.setBigContentTitle(context.getString(R.string.text_big_notification))
                    .bigText(r.getTitle());

            // Create second page notification
            Notification secondPageNotification =
                    new NotificationCompat.Builder(context)
                            .setStyle(secondPageStyle)
                            .build();

            wearableExtender.addPage(secondPageNotification);
        }

        n.extend(wearableExtender);

        notificationManager.notify(r.getId(), n.build());
    }

    // NOTIFICATION FIXED
    public static void NotificationFixed(Context context, Reminder r) {
        Intent i=new Intent(context,MyDialog.class);
        i.setAction(Costants.ACTION_EDIT_ITEM);
        i.putExtra(Costants.EXTRA_REMINDER, r);
        PendingIntent pi= PendingIntent.getActivity(context, r.getId(), i, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent delete_i = new Intent(context, MyDialog.class);
        delete_i.setAction(Costants.ACTION_DELETE);
        delete_i.putExtra(Costants.EXTRA_REMINDER, r);
        PendingIntent pi_delete = PendingIntent.getActivity(context, r.getId(), delete_i, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder n  = new NotificationCompat.Builder(context)
                .setContentTitle(r.getTitle())
                .setContentIntent(pi)
                .setPriority(-1)
                .setColor(context.getResources().getColor(R.color.primary))
                .setPriority(Notification.PRIORITY_MIN)
                .setAutoCancel(false);

        if (r.getAlarm() == 0) {
            n.setSmallIcon(R.drawable.ic_notification_note);
        } else {
            n.setSmallIcon(R.drawable.ic_action_note_reminded);
        }

        if (r.getContent().equals("")) {
            if (r.getAlarm() == 0) {
                n.setContentText(Utils.getDate(context, r.getDate_create()));
            } else {
                n.setContentText(Utils.getDateAlarm(context, r.getAlarm()));
            }
        } else {
            if (r.getPasw().equals("")) {
                n.setContentText(Utils.getContentList(context, r.getContent()));
                n.setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(Utils.getBigContentList(context, r.getContent())));
            } else {
                n.setContentText(context.getString(R.string.text_locked_note));
            }
        }

        SharedPreferences SP = context.getSharedPreferences(Costants.PREFERENCES_COSTANT, Context.MODE_PRIVATE);
        if (SP.getBoolean(Costants.PREFERENCE_BUTTON_DELETE, true)) {
            n.setOngoing(true);
        } else {
            n.setOngoing(false);
            n.setDeleteIntent(pi_delete);
        }

        if (!r.getImg().equals("")) {
            try {
                n.setStyle(new android.support.v4.app.NotificationCompat.BigPictureStyle().bigPicture(MediaStore.Images.Media.getBitmap(context.getContentResolver(), Uri.parse(r.getImg()))));
            } catch (Exception e) {

            }
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

        String url = Utils.checkURL(r.getTitle());
        if (url != "") {
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
        Intent i=new Intent(context,MyDialog.class);
        i.setAction(Costants.ACTION_ADD_ITEM);
        PendingIntent pi= PendingIntent.getActivity(context, -1, i, PendingIntent.FLAG_UPDATE_CURRENT);

        String item_to_do = context.getString(R.string.no_items);
        int count = Utils.itemsToDo(context);
        if (count > 0)
            item_to_do = context.getResources().getString(R.string.num_items_todo, count);

        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder n  = new NotificationCompat.Builder(context)
                .setContentTitle(context.getString(R.string.title_activity_add_item))
                .setContentText(item_to_do)
                .setSmallIcon(R.drawable.ic_stat_bookmark_plus)
                .setContentIntent(pi)
                .setOngoing(true)
                .setPriority(-1)
                .setPriority(Notification.PRIORITY_MIN)
                .setAutoCancel(false);


        SharedPreferences SP = context.getSharedPreferences(Costants.PREFERENCES_COSTANT, Context.MODE_PRIVATE);
        if (count > 0) {
            if (SP.getBoolean(Costants.PREFERENCES_VIEW_ALL, true)) {
                PendingIntent pi_hide = PendingIntent.getBroadcast(context, -2, new Intent(Costants.ACTION_HIDE_ALL), PendingIntent.FLAG_UPDATE_CURRENT);

                n.setColor(context.getResources().getColor(R.color.primary));
                n.addAction(R.drawable.ic_action_hide_all, context.getString(R.string.action_hide_all), pi_hide);
            } else {
                PendingIntent pi_view = PendingIntent.getBroadcast(context, -2, new Intent(Costants.ACTION_VIEW_ALL), PendingIntent.FLAG_UPDATE_CURRENT);

                n.setColor(context.getResources().getColor(R.color.accent));
                n.addAction(R.drawable.ic_action_view_all, context.getString(R.string.action_view_all), pi_view);
            }
        }

        notificationManager.notify(-1, n.build());
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
