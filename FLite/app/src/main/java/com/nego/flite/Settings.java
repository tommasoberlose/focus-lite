package com.nego.flite;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

public class Settings extends AppCompatActivity {

    private static String KEY_SELECTED_NEW_STYLE = "KEY_SELECTED_NEW_STYLE";
    private static String KEY_SELECTED_NEW_STYLE_WIDGET = "KEY_SELECTED_NEW_STYLE_WIDGET";
    private static String KEY_SELECTED_PREF = "KEY_SELECTED_PREF";

    private int selected_new_style;
    private int selected_new_style_widget;
    private boolean[] selected_not_pref = new boolean[3];

    private SharedPreferences SP;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_settings);
        toolbar = (Toolbar) findViewById(R.id.my_awesome_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        if (savedInstanceState != null) {
            selected_new_style = savedInstanceState.getInt(KEY_SELECTED_NEW_STYLE);
            selected_new_style_widget = savedInstanceState.getInt(KEY_SELECTED_NEW_STYLE_WIDGET);
            selected_not_pref = savedInstanceState.getBooleanArray(KEY_SELECTED_PREF);
        }


        SP = getSharedPreferences(Costants.PREFERENCES_COSTANT, Context.MODE_PRIVATE);

        // POPUP STYLE

        switch (SP.getString(Costants.PREFERENCE_STYLE_POPUP, Costants.PREFERENCE_STYLE_POPUP_DEFAULT)) {
            case Costants.PREFERENCE_STYLE_POPUP_MD:
                ((TextView) findViewById(R.id.popup_style_subtitle)).setText(getString(R.string.style_material_dark));
                break;
            case Costants.PREFERENCE_STYLE_POPUP_ML:
                ((TextView) findViewById(R.id.popup_style_subtitle)).setText(getString(R.string.style_material_light));
                break;
            default:
                ((TextView) findViewById(R.id.popup_style_subtitle)).setText(getString(R.string.style_deafult));
                break;
        }

        findViewById(R.id.action_popup_style).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selected_style = 0;
                switch (SP.getString(Costants.PREFERENCE_STYLE_POPUP, Costants.PREFERENCE_STYLE_POPUP_DEFAULT)) {
                    case Costants.PREFERENCE_STYLE_POPUP_MD:
                        selected_style = 1;
                        break;
                    case Costants.PREFERENCE_STYLE_POPUP_ML:
                        selected_style = 2;
                        break;
                }
                new AlertDialog.Builder(Settings.this, R.style.mDialog)
                        .setTitle(getString(R.string.title_popup_style))
                        .setSingleChoiceItems(new String[]{getString(R.string.style_deafult), getString(R.string.style_material_dark), getString(R.string.style_material_light)}, selected_style, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                selected_new_style = which;
                            }
                        })
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (selected_new_style) {
                                    case 0:
                                        SP.edit().putString(Costants.PREFERENCE_STYLE_POPUP, Costants.PREFERENCE_STYLE_POPUP_DEFAULT).apply();
                                        break;
                                    case 1:
                                        SP.edit().putString(Costants.PREFERENCE_STYLE_POPUP, Costants.PREFERENCE_STYLE_POPUP_MD).apply();
                                        break;
                                    case 2:
                                        SP.edit().putString(Costants.PREFERENCE_STYLE_POPUP, Costants.PREFERENCE_STYLE_POPUP_ML).apply();
                                        break;
                                }
                                switch (SP.getString(Costants.PREFERENCE_STYLE_POPUP, Costants.PREFERENCE_STYLE_POPUP_DEFAULT)) {
                                    case Costants.PREFERENCE_STYLE_POPUP_MD:
                                        ((TextView) findViewById(R.id.popup_style_subtitle)).setText(getString(R.string.style_material_dark));
                                        break;
                                    case Costants.PREFERENCE_STYLE_POPUP_ML:
                                        ((TextView) findViewById(R.id.popup_style_subtitle)).setText(getString(R.string.style_material_light));
                                        break;
                                    default:
                                        ((TextView) findViewById(R.id.popup_style_subtitle)).setText(getString(R.string.style_deafult));
                                        break;
                                }
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        });

        // WIDGET STYLE

        switch (SP.getString(Costants.PREFERENCE_STYLE_WIDGET, Costants.PREFERENCE_STYLE_POPUP_DEFAULT)) {
            case Costants.PREFERENCE_STYLE_WIDGET_ML:
                ((TextView) findViewById(R.id.widget_style_subtitle)).setText(getString(R.string.style_material_light));
                break;
            default:
                ((TextView) findViewById(R.id.widget_style_subtitle)).setText(getString(R.string.style_material_dark));
                break;
        }

        findViewById(R.id.action_widget_style).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selected_style = 0;
                switch (SP.getString(Costants.PREFERENCE_STYLE_WIDGET, Costants.PREFERENCE_STYLE_POPUP_DEFAULT)) {
                    case Costants.PREFERENCE_STYLE_WIDGET_MD:
                        selected_style = 0;
                        break;
                    case Costants.PREFERENCE_STYLE_WIDGET_ML:
                        selected_style = 1;
                        break;
                }
                new AlertDialog.Builder(Settings.this, R.style.mDialog)
                        .setTitle(getString(R.string.title_widget_style))
                        .setSingleChoiceItems(new String[]{getString(R.string.style_material_dark), getString(R.string.style_material_light)}, selected_style, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                selected_new_style_widget = which;
                            }
                        })
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (selected_new_style_widget) {
                                    case 0:
                                        SP.edit().putString(Costants.PREFERENCE_STYLE_WIDGET, Costants.PREFERENCE_STYLE_WIDGET_MD).apply();
                                        break;
                                    case 1:
                                        SP.edit().putString(Costants.PREFERENCE_STYLE_WIDGET, Costants.PREFERENCE_STYLE_WIDGET_ML).apply();
                                        break;
                                }

                                switch (SP.getString(Costants.PREFERENCE_STYLE_WIDGET, Costants.PREFERENCE_STYLE_WIDGET_MD)) {
                                    case Costants.PREFERENCE_STYLE_WIDGET_ML:
                                        ((TextView) findViewById(R.id.widget_style_subtitle)).setText(getString(R.string.style_material_light));
                                        break;
                                    default:
                                        ((TextView) findViewById(R.id.widget_style_subtitle)).setText(getString(R.string.style_material_dark));
                                        break;
                                }
                                Utils.updateWidget(Settings.this);
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        });

        // NOTIFICATION PREFERENCES

        String text_not = getString(R.string.text_no_preferences);
        if (SP.getBoolean(Costants.PREFERENCES_NOTIFICATION_VIBRATE, true)) {
            text_not = getString(R.string.text_vibrate);
        }
        if (SP.getBoolean(Costants.PREFERENCES_NOTIFICATION_SOUND, true)) {
            if (SP.getBoolean(Costants.PREFERENCES_NOTIFICATION_VIBRATE, true)) {
                text_not = text_not + ", " + getString(R.string.text_sound);
            } else {
                text_not = getString(R.string.text_sound);
            }
        }
        if (SP.getBoolean(Costants.PREFERENCES_NOTIFICATION_LED, true)) {
            if (SP.getBoolean(Costants.PREFERENCES_NOTIFICATION_VIBRATE, true) || SP.getBoolean(Costants.PREFERENCES_NOTIFICATION_SOUND, true)) {
                text_not = text_not + ", " + getString(R.string.text_led);
            } else {
                text_not = getString(R.string.text_led);
            }
        }
        ((TextView) findViewById(R.id.notification_subtitle)).setText(text_not);

        findViewById(R.id.action_notification_settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean[] actual_not_pref = new boolean[]{SP.getBoolean(Costants.PREFERENCES_NOTIFICATION_VIBRATE, true), SP.getBoolean(Costants.PREFERENCES_NOTIFICATION_SOUND, true), SP.getBoolean(Costants.PREFERENCES_NOTIFICATION_LED, true)};
                selected_not_pref = actual_not_pref;
                new AlertDialog.Builder(Settings.this, R.style.mDialog)
                        .setTitle(getString(R.string.title_widget_style))
                        .setMultiChoiceItems(new String[]{getString(R.string.text_vibrate), getString(R.string.text_sound), getString(R.string.text_led)},
                                actual_not_pref, new DialogInterface.OnMultiChoiceClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                        selected_not_pref[which] = isChecked;
                                    }
                                })
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SP.edit().putBoolean(Costants.PREFERENCES_NOTIFICATION_VIBRATE, selected_not_pref[0]).apply();
                                SP.edit().putBoolean(Costants.PREFERENCES_NOTIFICATION_SOUND, selected_not_pref[1]).apply();
                                SP.edit().putBoolean(Costants.PREFERENCES_NOTIFICATION_LED, selected_not_pref[2]).apply();

                                String text_not = getString(R.string.text_no_preferences);
                                if (SP.getBoolean(Costants.PREFERENCES_NOTIFICATION_VIBRATE, true)) {
                                    text_not = getString(R.string.text_vibrate);
                                }
                                if (SP.getBoolean(Costants.PREFERENCES_NOTIFICATION_SOUND, true)) {
                                    if (SP.getBoolean(Costants.PREFERENCES_NOTIFICATION_VIBRATE, true)) {
                                        text_not = text_not + ", " + getString(R.string.text_sound);
                                    } else {
                                        text_not = getString(R.string.text_sound);
                                    }
                                    findViewById(R.id.action_show_add_notification).setVisibility(View.VISIBLE);
                                } else {
                                    findViewById(R.id.action_show_add_notification).setVisibility(View.GONE);
                                }
                                if (SP.getBoolean(Costants.PREFERENCES_NOTIFICATION_LED, true)) {
                                    if (SP.getBoolean(Costants.PREFERENCES_NOTIFICATION_VIBRATE, true) || SP.getBoolean(Costants.PREFERENCES_NOTIFICATION_SOUND, true)) {
                                        text_not = text_not + ", " + getString(R.string.text_led);
                                    } else {
                                        text_not = getString(R.string.text_led);
                                    }
                                }
                                ((TextView) findViewById(R.id.notification_subtitle)).setText(text_not);
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        });

        // VIEW INTRO
        findViewById(R.id.action_view_intro).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Settings.this, Intro.class));
            }
        });

        // ORDER NOTIFICATIONS
        final AppCompatCheckBox order_check = (AppCompatCheckBox) findViewById(R.id.order_check);
        order_check.setChecked(SP.getBoolean(Costants.PREFERENCE_ORDER_NOTIFICATIONS, true));
        order_check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SP.edit().putBoolean(Costants.PREFERENCE_ORDER_NOTIFICATIONS, isChecked).apply();
                Utils.notification_add_update(Settings.this);
            }
        });
        findViewById(R.id.action_order).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                order_check.setChecked(!order_check.isChecked());
            }
        });

        // SHARE BUTTON
        final AppCompatCheckBox share_check = (AppCompatCheckBox) findViewById(R.id.share_check);
        share_check.setChecked(SP.getBoolean(Costants.PREFERENCE_BUTTON_SHARE, true));
        share_check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SP.edit().putBoolean(Costants.PREFERENCE_BUTTON_SHARE, isChecked).apply();
                Utils.notification_add_update(Settings.this);
            }
        });
        findViewById(R.id.action_switch_share).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                share_check.setChecked(!share_check.isChecked());
            }
        });

        // ONGOING NOTIFICATIONS
        final AppCompatCheckBox ongoing_check = (AppCompatCheckBox) findViewById(R.id.ongoing_check);
        ongoing_check.setChecked(SP.getBoolean(Costants.PREFERENCE_BUTTON_DELETE, true));
        ongoing_check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SP.edit().putBoolean(Costants.PREFERENCE_BUTTON_DELETE, isChecked).apply();
                Utils.notification_add_update(Settings.this);
            }
        });
        findViewById(R.id.action_swicth_ongoing).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ongoing_check.setChecked(!ongoing_check.isChecked());
            }
        });

        // HIDE ADD NOTIFICATION
        if (SP.getBoolean(Costants.PREFERENCES_NOTIFICATION_SOUND, true)) {
            final AppCompatCheckBox hide_new_check = (AppCompatCheckBox) findViewById(R.id.show_add_notification_check);
            hide_new_check.setChecked(SP.getBoolean(Costants.PREFERENCE_SHOW_ADD_NOTIFICATION, false));
            hide_new_check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SP.edit().putBoolean(Costants.PREFERENCE_SHOW_ADD_NOTIFICATION, isChecked).apply();
                    Utils.notification_add_update(Settings.this);
                }
            });
            findViewById(R.id.action_show_add_notification).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hide_new_check.setChecked(!hide_new_check.isChecked());
                }
            });
        } else {
            findViewById(R.id.action_show_add_notification).setVisibility(View.GONE);
        }

    }


    @Override
    public void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_SELECTED_NEW_STYLE, selected_new_style);
        outState.putInt(KEY_SELECTED_NEW_STYLE_WIDGET, selected_new_style_widget);
        outState.putBooleanArray(KEY_SELECTED_PREF, selected_not_pref);
    }

}
