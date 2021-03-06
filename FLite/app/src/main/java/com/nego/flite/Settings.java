package com.nego.flite;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class Settings extends AppCompatActivity {

    private static String KEY_SELECTED_NEW_STYLE = "KEY_SELECTED_NEW_STYLE";
    private static String KEY_SELECTED_NEW_STYLE_WIDGET = "KEY_SELECTED_NEW_STYLE_WIDGET";
    private static String KEY_SELECTED_PREF = "KEY_SELECTED_PREF";

    private int selected_new_style;
    private int selected_new_style_widget;
    private boolean[] selected_not_pref = new boolean[3];
    private String section = Costants.SECTION_NOTIFICATION_SETTINGS;
    private int selected_new_ringtone;

    private SharedPreferences SP;

    private LinearLayout section_notification;
    private LinearLayout section_alarm;
    private LinearLayout section_style;
    private LinearLayout section_application;

    private ImageView section_notification_icon;
    private ImageView section_alarm_icon;
    private ImageView section_style_icon;
    private ImageView section_application_icon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_settings);

        section_notification = (LinearLayout) findViewById(R.id.section_notification_settings);
        section_alarm = (LinearLayout) findViewById(R.id.section_alarm_settings);
        section_style = (LinearLayout) findViewById(R.id.section_style_settings);
        section_application = (LinearLayout) findViewById(R.id.section_application_settings);

        section_notification_icon = (ImageView) findViewById(R.id.section_notification_settings_icon);
        section_alarm_icon = (ImageView) findViewById(R.id.section_alarm_settings_icon);
        section_style_icon = (ImageView) findViewById(R.id.section_style_settings_icon);
        section_application_icon = (ImageView) findViewById(R.id.section_application_settings_icon);

        findViewById(R.id.action_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        section_notification_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSection(Costants.SECTION_NOTIFICATION_SETTINGS);
            }
        });

        section_alarm_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSection(Costants.SECTION_ALARM_SETTINGS);
            }
        });

        section_style_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSection(Costants.SECTION_STYLE_SETTINGS);
            }
        });

        section_application_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSection(Costants.SECTION_APPLICATION_SETTINGS);
            }
        });

        if (getIntent() != null && getIntent().getStringExtra(Costants.SECTION_SETTINGS) != null)
            section = getIntent().getStringExtra(Costants.SECTION_SETTINGS);

        if (savedInstanceState != null) {
            selected_new_style = savedInstanceState.getInt(KEY_SELECTED_NEW_STYLE);
            selected_new_style_widget = savedInstanceState.getInt(KEY_SELECTED_NEW_STYLE_WIDGET);
            selected_not_pref = savedInstanceState.getBooleanArray(KEY_SELECTED_PREF);
            section = savedInstanceState.getString(Costants.SECTION_SETTINGS);
        }

        setSection(section);


        SP = getSharedPreferences(Costants.PREFERENCES_COSTANT, Context.MODE_PRIVATE);

        // DEVICE & WIFI PREFERENCES

        findViewById(R.id.action_choose_wifi).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Settings.this, ActiveDevice.class);
                i.putExtra(Costants.EXTRA_ACTION_TYPE, Costants.ALARM_TYPE_WIFI);
                startActivity(i);
            }
        });

        findViewById(R.id.action_choose_device).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Settings.this, ActiveDevice.class);
                i.putExtra(Costants.EXTRA_ACTION_TYPE, Costants.ALARM_TYPE_BLUETOOTH);
                startActivity(i);
            }
        });

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

        // FOCUS NOTIFY TOOGLE
        final AppCompatCheckBox show_notify_check = (AppCompatCheckBox) findViewById(R.id.show_notify_check);
        show_notify_check.setChecked(SP.getBoolean(Costants.PREFERENCE_SHOW_NOTIFY, true));
        show_notify_check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SP.edit().putBoolean(Costants.PREFERENCE_SHOW_NOTIFY, isChecked).apply();
                Utils.notification_add_update(Settings.this);
                updateUi();
            }
        });
        findViewById(R.id.action_show_notify).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                show_notify_check.setChecked(!show_notify_check.isChecked());
            }
        });

        // STARRED NOTES TOOGLE
        final AppCompatCheckBox show_starred_check = (AppCompatCheckBox) findViewById(R.id.show_starred_check);
        show_starred_check.setChecked(SP.getBoolean(Costants.PREFERENCE_SHOW_STARRED, true));
        show_starred_check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SP.edit().putBoolean(Costants.PREFERENCE_SHOW_STARRED, isChecked).apply();
                Utils.notification_add_update(Settings.this);
            }
        });
        findViewById(R.id.action_show_starred).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                show_starred_check.setChecked(!show_starred_check.isChecked());
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
                                    findViewById(R.id.action_custom_sound).setVisibility(View.VISIBLE);
                                } else {
                                    findViewById(R.id.action_custom_sound).setVisibility(View.GONE);
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
        findViewById(R.id.action_order).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean[] actual_not_pref = new boolean[]{SP.getBoolean(Costants.PREFERENCE_ORDER_NOTIFICATIONS, true), SP.getBoolean(Costants.PREFERENCE_ORDER_ALARM_FIRST, true)};
                selected_not_pref = actual_not_pref;
                new AlertDialog.Builder(Settings.this, R.style.mDialog)
                        .setTitle(getString(R.string.title_order))
                        .setMultiChoiceItems(new String[]{getString(R.string.preferences_addnote_first), getString(R.string.preferences_alarm_first)},
                                actual_not_pref, new DialogInterface.OnMultiChoiceClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                        selected_not_pref[which] = isChecked;
                                    }
                                })
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SP.edit().putBoolean(Costants.PREFERENCE_ORDER_NOTIFICATIONS, selected_not_pref[0]).apply();
                                SP.edit().putBoolean(Costants.PREFERENCE_ORDER_ALARM_FIRST, selected_not_pref[1]).apply();
                                Utils.notification_add_update(Settings.this);
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
        findViewById(R.id.action_notification_preference).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean[] actual_not_pref = new boolean[]{SP.getBoolean(Costants.PREFERENCE_BUTTON_SHARE, true), SP.getBoolean(Costants.PREFERENCE_BUTTON_DELETE_NOT_ONGOING, false), SP.getBoolean(Costants.PREFERENCE_BUTTON_DELETE, true), SP.getBoolean(Costants.PREFERENCE_ONGOING_NOTIFICATIONS, true)};
                selected_not_pref = actual_not_pref;
                new AlertDialog.Builder(Settings.this, R.style.mDialog)
                        .setTitle(getString(R.string.title_notification_preference))
                        .setMultiChoiceItems(new String[]{getString(R.string.title_preferences_share), !SP.getBoolean(Costants.PREFERENCE_BUTTON_DELETE_TO_ARCHIVE, false) ? getString(R.string.title_delete_button_not_ongoing_preferences) : getString(R.string.title_archive_button_preferences), !SP.getBoolean(Costants.PREFERENCE_BUTTON_DELETE_TO_ARCHIVE, false) ? getString(R.string.title_delete_button_preferences) : getString(R.string.title_archive_button_not_ongoing_preferences), getString(R.string.title_preferences_ongoing)},
                                actual_not_pref, new DialogInterface.OnMultiChoiceClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                        selected_not_pref[which] = isChecked;
                                    }
                                })
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SP.edit().putBoolean(Costants.PREFERENCE_BUTTON_SHARE, selected_not_pref[0]).apply();
                                SP.edit().putBoolean(Costants.PREFERENCE_BUTTON_DELETE_NOT_ONGOING, selected_not_pref[1]).apply();
                                SP.edit().putBoolean(Costants.PREFERENCE_BUTTON_DELETE, selected_not_pref[2]).apply();
                                SP.edit().putBoolean(Costants.PREFERENCE_ONGOING_NOTIFICATIONS, selected_not_pref[3]).apply();
                                Utils.notification_add_update(Settings.this);
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

        // CUSTOM SOUND NOTIFICATION
        if (SP.getBoolean(Costants.PREFERENCES_NOTIFICATION_SOUND, true)) {
            findViewById(R.id.action_custom_sound).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ContextCompat.checkSelfPermission(Settings.this,
                            Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        int selected_ringtone = 0;
                        final ArrayList<String> ringtone_title = new ArrayList<String>();
                        final ArrayList<String> ringtone_url = new ArrayList<String>();

                        RingtoneManager manager = new RingtoneManager(Settings.this);
                        manager.setType(RingtoneManager.TYPE_RINGTONE);
                        Cursor cursor = manager.getCursor();
                        while (cursor.moveToNext()) {
                            ringtone_title.add(cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX));
                            ringtone_url.add(manager.getRingtoneUri(cursor.getPosition()).toString());
                            if (manager.getRingtoneUri(cursor.getPosition()).toString().equals(SP.getString(Costants.PREFERENCES_NOTIFICATION_RINGTONE, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION).toString())))
                                selected_ringtone = ringtone_title.size() - 1;
                        }

                        ringtone_title.add(0, getString(R.string.style_deafult));
                        ringtone_url.add(0, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION).toString());

                        String[] title_ringtone_array = new String[ringtone_title.size()];
                        title_ringtone_array = ringtone_title.toArray(title_ringtone_array);

                        new AlertDialog.Builder(Settings.this, R.style.mDialog)
                                .setTitle(getString(R.string.title_custom_sound))
                                .setSingleChoiceItems(title_ringtone_array, selected_ringtone, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        selected_new_ringtone = which;
                                    }
                                })
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        SP.edit().putString(Costants.PREFERENCES_NOTIFICATION_RINGTONE, ringtone_url.get(selected_new_ringtone)).apply();
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
                    } else {
                        ActivityCompat.requestPermissions(Settings.this,
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                Costants.CODE_REQUEST_PERMISSION_READ_EXTERNAL_STORAGE);
                    }
                }
            });
        } else {
            findViewById(R.id.action_custom_sound).setVisibility(View.GONE);
        }

        // DELETE TO ARCHIVE
        final AppCompatCheckBox delete_to_archive_check = (AppCompatCheckBox) findViewById(R.id.delete_to_archive_check);
        delete_to_archive_check.setChecked(SP.getBoolean(Costants.PREFERENCE_BUTTON_DELETE_TO_ARCHIVE, false));
        delete_to_archive_check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SP.edit().putBoolean(Costants.PREFERENCE_BUTTON_DELETE_TO_ARCHIVE, isChecked).apply();
            }
        });
        findViewById(R.id.action_delete_to_archive).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delete_to_archive_check.setChecked(!delete_to_archive_check.isChecked());
            }
        });

        // SHOW ADD NOTIFICATION
        final AppCompatCheckBox hide_new_check = (AppCompatCheckBox) findViewById(R.id.show_add_notification_check);
        hide_new_check.setChecked(SP.getBoolean(Costants.PREFERENCE_SHOW_ADD_NOTIFICATION, true));
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

        // 12-HOUR FORMAT
        final AppCompatCheckBox twelve_hour_check = (AppCompatCheckBox) findViewById(R.id.twelve_hour_check);
        twelve_hour_check.setChecked(SP.getBoolean(Costants.PREFERENCE_TWELVE_HOUR_FORMAT, false));
        twelve_hour_check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SP.edit().putBoolean(Costants.PREFERENCE_TWELVE_HOUR_FORMAT, isChecked).apply();
                Utils.notification_add_update(Settings.this);
            }
        });
        findViewById(R.id.action_twelve_hour).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                twelve_hour_check.setChecked(!twelve_hour_check.isChecked());
            }
        });

        // CLOSE NOTES AFTER ACTIONS
        final AppCompatCheckBox close_notes__check = (AppCompatCheckBox) findViewById(R.id.close_note_after_actions_check);
        close_notes__check.setChecked(SP.getBoolean(Costants.PREFERENCE_CLOSE_NOTE_AFTER_ACTIONS, true));
        close_notes__check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SP.edit().putBoolean(Costants.PREFERENCE_CLOSE_NOTE_AFTER_ACTIONS, isChecked).apply();
            }
        });
        findViewById(R.id.action_close_note_after_actions).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                close_notes__check.setChecked(!close_notes__check.isChecked());
            }
        });

        updateUi();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home)
            onBackPressed();

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_SELECTED_NEW_STYLE, selected_new_style);
        outState.putInt(KEY_SELECTED_NEW_STYLE_WIDGET, selected_new_style_widget);
        outState.putBooleanArray(KEY_SELECTED_PREF, selected_not_pref);
        outState.putString(Costants.SECTION_SETTINGS, section);
    }

    public void setSection(String section) {
        section_notification.setVisibility(section.equals(Costants.SECTION_NOTIFICATION_SETTINGS) ? View.VISIBLE : View.GONE);
        section_alarm.setVisibility(section.equals(Costants.SECTION_ALARM_SETTINGS) ? View.VISIBLE : View.GONE);
        section_style.setVisibility(section.equals(Costants.SECTION_STYLE_SETTINGS) ? View.VISIBLE : View.GONE);
        section_application.setVisibility(section.equals(Costants.SECTION_APPLICATION_SETTINGS) ? View.VISIBLE : View.GONE);

        section_notification_icon.setColorFilter(ContextCompat.getColor(this, section.equals(Costants.SECTION_NOTIFICATION_SETTINGS) ? R.color.primary : R.color.secondary_text));
        section_alarm_icon.setColorFilter(ContextCompat.getColor(this, section.equals(Costants.SECTION_ALARM_SETTINGS) ? R.color.primary : R.color.secondary_text));
        section_style_icon.setColorFilter(ContextCompat.getColor(this, section.equals(Costants.SECTION_STYLE_SETTINGS) ? R.color.primary : R.color.secondary_text));
        section_application_icon.setColorFilter(ContextCompat.getColor(this, section.equals(Costants.SECTION_APPLICATION_SETTINGS) ? R.color.primary : R.color.secondary_text));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case Costants.CODE_REQUEST_PERMISSION_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                }
                break;
        }
    }

    public void updateUi() {
        findViewById(R.id.show_add_notification_title).setEnabled(SP.getBoolean(Costants.PREFERENCE_SHOW_NOTIFY, true));
        findViewById(R.id.show_add_notification_subtitle).setEnabled(SP.getBoolean(Costants.PREFERENCE_SHOW_NOTIFY, true));
        findViewById(R.id.show_add_notification_check).setEnabled(SP.getBoolean(Costants.PREFERENCE_SHOW_NOTIFY, true));
        findViewById(R.id.order_title).setEnabled(SP.getBoolean(Costants.PREFERENCE_SHOW_NOTIFY, true));
        findViewById(R.id.order_subtitle).setEnabled(SP.getBoolean(Costants.PREFERENCE_SHOW_NOTIFY, true));
        findViewById(R.id.notification_preference_title).setEnabled(SP.getBoolean(Costants.PREFERENCE_SHOW_NOTIFY, true));
        findViewById(R.id.notification_preference_subtitle).setEnabled(SP.getBoolean(Costants.PREFERENCE_SHOW_NOTIFY, true));
        findViewById(R.id.show_starred_title).setEnabled(!SP.getBoolean(Costants.PREFERENCE_SHOW_NOTIFY, true));
        findViewById(R.id.show_starred_subtitle).setEnabled(!SP.getBoolean(Costants.PREFERENCE_SHOW_NOTIFY, true));
        findViewById(R.id.show_starred_check).setEnabled(!SP.getBoolean(Costants.PREFERENCE_SHOW_NOTIFY, true));
    }

}
