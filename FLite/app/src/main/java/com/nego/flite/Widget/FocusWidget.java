package com.nego.flite.Widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.nego.flite.Costants;
import com.nego.flite.Main;
import com.nego.flite.MyDialog;
import com.nego.flite.R;
import com.nego.flite.Utils;

public class FocusWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int i = 0; i < appWidgetIds.length; i++) {
            Intent intent = new Intent(context, WidgetService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

            // Instantiate the RemoteViews object for the app widget layout.
            SharedPreferences SP = context.getSharedPreferences(Costants.PREFERENCES_COSTANT, Context.MODE_PRIVATE);
            RemoteViews rv;
            switch (SP.getString(Costants.PREFERENCE_STYLE_WIDGET, Costants.PREFERENCE_STYLE_WIDGET_MD)) {
                case Costants.PREFERENCE_STYLE_WIDGET_ML:
                    rv = new RemoteViews(context.getPackageName(), R.layout.focus_widget_ml);
                    break;
                default:
                    rv = new RemoteViews(context.getPackageName(), R.layout.focus_widget);
                    break;
            }
            //Elimino la view di loading
            rv.removeAllViews(R.id.loading);
            // aggiungo adapter
            rv.setRemoteAdapter(R.id.list, intent);
            rv.setEmptyView(R.id.list, R.id.no_items);
            // visualizzo il numero di elementi
            rv.setTextViewText(R.id.count_item, "" + Utils.itemsToDo(context));
            rv.setViewVisibility(R.id.count_item, View.VISIBLE);


            Intent new_item =new Intent(context,MyDialog.class);
            new_item.setAction(Costants.ACTION_ADD_ITEM);
            PendingIntent pi= PendingIntent.getActivity(context, -1, new_item, PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setOnClickPendingIntent(R.id.action_new, pi);

            Intent open_main =new Intent(context,Main.class);
            PendingIntent pi_open_main = PendingIntent.getActivity(context, -1, open_main, PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setOnClickPendingIntent(R.id.toolbar, pi_open_main);

            PendingIntent clickPI = PendingIntent.getActivity(context, 0, new Intent(context, MyDialog.class), PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setPendingIntentTemplate(R.id.list, clickPI);

            appWidgetManager.updateAppWidget(appWidgetIds[i], rv);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE)) {
            ComponentName thisWidget = new ComponentName(context, FocusWidget.class);
            int[] appWidgetIds = AppWidgetManager.getInstance(context).getAppWidgetIds(thisWidget);
            if (appWidgetIds.length > 0)
                onUpdate(context, AppWidgetManager.getInstance(context), appWidgetIds);
        }
    }

}

