package com.nego.flite.Pages;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.nego.flite.Costants;
import com.nego.flite.Intro;
import com.nego.flite.PasswordCheck;
import com.nego.flite.R;
import com.nego.flite.Utils;

public class SettingsFragment extends Fragment {

    private static String KEY_SELECTED_NEW_STYLE = "KEY_SELECTED_NEW_STYLE";
    private static String KEY_SELECTED_NEW_STYLE_WIDGET = "KEY_SELECTED_NEW_STYLE_WIDGET";
    private static String KEY_SELECTED_PREF = "KEY_SELECTED_PREF";

    private int selected_new_style;
    private int selected_new_style_widget;
    private boolean[] selected_not_pref = new boolean[3];

    private View view;
    private SharedPreferences SP;

    public SettingsFragment() {
    }

    @Override
    public void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_SELECTED_NEW_STYLE, selected_new_style);
        outState.putInt(KEY_SELECTED_NEW_STYLE_WIDGET, selected_new_style_widget);
        outState.putBooleanArray(KEY_SELECTED_PREF, selected_not_pref);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            selected_new_style = savedInstanceState.getInt(KEY_SELECTED_NEW_STYLE);
            selected_new_style_widget = savedInstanceState.getInt(KEY_SELECTED_NEW_STYLE_WIDGET);
            selected_not_pref = savedInstanceState.getBooleanArray(KEY_SELECTED_PREF);
        }


        SP = getActivity().getSharedPreferences(Costants.PREFERENCES_COSTANT, Context.MODE_PRIVATE);
        view = inflater.inflate(R.layout.page_settings, container, false);

        // POPUP STYLE

        switch (SP.getString(Costants.PREFERENCE_STYLE_POPUP, Costants.PREFERENCE_STYLE_POPUP_DEFAULT)) {
            case Costants.PREFERENCE_STYLE_POPUP_MD:
                ((TextView) view.findViewById(R.id.popup_style_subtitle)).setText(getString(R.string.style_material_dark));
                break;
            case Costants.PREFERENCE_STYLE_POPUP_ML:
                ((TextView) view.findViewById(R.id.popup_style_subtitle)).setText(getString(R.string.style_material_light));
                break;
            default:
                ((TextView) view.findViewById(R.id.popup_style_subtitle)).setText(getString(R.string.style_deafult));
                break;
        }

        view.findViewById(R.id.action_popup_style).setOnClickListener(new View.OnClickListener() {
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
                new AlertDialog.Builder(getActivity(), R.style.mDialog)
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
                                        ((TextView) view.findViewById(R.id.popup_style_subtitle)).setText(getString(R.string.style_material_dark));
                                        break;
                                    case Costants.PREFERENCE_STYLE_POPUP_ML:
                                        ((TextView) view.findViewById(R.id.popup_style_subtitle)).setText(getString(R.string.style_material_light));
                                        break;
                                    default:
                                        ((TextView) view.findViewById(R.id.popup_style_subtitle)).setText(getString(R.string.style_deafult));
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

        switch (SP.getString(Costants.PREFERENCE_STYLE_WIDGET, Costants.PREFERENCE_STYLE_WIDGET_MD)) {
            case Costants.PREFERENCE_STYLE_WIDGET_ML:
                ((TextView) view.findViewById(R.id.widget_style_subtitle)).setText(getString(R.string.style_material_light));
                break;
            default:
                ((TextView) view.findViewById(R.id.widget_style_subtitle)).setText(getString(R.string.style_material_dark));
                break;
        }

        view.findViewById(R.id.action_widget_style).setOnClickListener(new View.OnClickListener() {
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
                new AlertDialog.Builder(getActivity(), R.style.mDialog)
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
                                        ((TextView) view.findViewById(R.id.widget_style_subtitle)).setText(getString(R.string.style_material_light));
                                        break;
                                    default:
                                        ((TextView) view.findViewById(R.id.widget_style_subtitle)).setText(getString(R.string.style_material_dark));
                                        break;
                                }
                                Utils.updateWidget(getActivity());
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
        ((TextView) view.findViewById(R.id.notification_subtitle)).setText(text_not);

        view.findViewById(R.id.action_notification_settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean[] actual_not_pref = new boolean[]{SP.getBoolean(Costants.PREFERENCES_NOTIFICATION_VIBRATE, true), SP.getBoolean(Costants.PREFERENCES_NOTIFICATION_SOUND, true), SP.getBoolean(Costants.PREFERENCES_NOTIFICATION_LED, true)};
                selected_not_pref = actual_not_pref;
                new AlertDialog.Builder(getActivity(), R.style.mDialog)
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
                                }
                                if (SP.getBoolean(Costants.PREFERENCES_NOTIFICATION_LED, true)) {
                                    if (SP.getBoolean(Costants.PREFERENCES_NOTIFICATION_VIBRATE, true) || SP.getBoolean(Costants.PREFERENCES_NOTIFICATION_SOUND, true)) {
                                        text_not = text_not + ", " + getString(R.string.text_led);
                                    } else {
                                        text_not = getString(R.string.text_led);
                                    }
                                }
                                ((TextView) view.findViewById(R.id.notification_subtitle)).setText(text_not);
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

        //  LOCK NOTES

        checkPasw();

        // VIEW INTRO
        view.findViewById(R.id.action_view_intro).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), Intro.class));
            }
        });

        // ORDER NOTIFICATIONS
        final CheckBox order_check = (CheckBox) view.findViewById(R.id.order_check);
        order_check.setChecked(SP.getBoolean(Costants.PREFERENCE_ORDER_NOTIFICATIONS, true));
        order_check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SP.edit().putBoolean(Costants.PREFERENCE_ORDER_NOTIFICATIONS, isChecked).apply();
                Utils.notification_add_update(getActivity());
            }
        });
        view.findViewById(R.id.action_order).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                order_check.setChecked(!order_check.isChecked());
            }
        });

        // SHARE BUTTON
        final CheckBox share_check = (CheckBox) view.findViewById(R.id.share_check);
        share_check.setChecked(SP.getBoolean(Costants.PREFERENCE_BUTTON_SHARE, true));
        share_check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SP.edit().putBoolean(Costants.PREFERENCE_BUTTON_SHARE, isChecked).apply();
                Utils.notification_add_update(getActivity());
            }
        });
        view.findViewById(R.id.action_switch_share).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                share_check.setChecked(!share_check.isChecked());
            }
        });

        // ONGOING NOTIFICATIONS
        final CheckBox ongoing_check = (CheckBox) view.findViewById(R.id.ongoing_check);
        ongoing_check.setChecked(SP.getBoolean(Costants.PREFERENCE_BUTTON_DELETE, true));
        ongoing_check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SP.edit().putBoolean(Costants.PREFERENCE_BUTTON_DELETE, isChecked).apply();
                Utils.notification_add_update(getActivity());
            }
        });
        view.findViewById(R.id.action_swicth_ongoing).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ongoing_check.setChecked(!ongoing_check.isChecked());
            }
        });

        return view;
    }

    public void checkPasw() {
        if (SP.getString(Costants.PREFERENCE_PASSWORD, "").equals("")) {
            ((TextView) view.findViewById(R.id.lock_subtitle)).setText(R.string.subtitle_lock_inactive);
            view.findViewById(R.id.action_lock).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setPasw();
                }
            });
            view.findViewById(R.id.action_reset).setVisibility(View.GONE);
            view.findViewById(R.id.action_reset).setOnClickListener(null);
        } else {
            ((TextView) view.findViewById(R.id.lock_subtitle)).setText(R.string.subtitle_lock_active);
            view.findViewById(R.id.action_lock).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivityForResult(new Intent(getActivity(), PasswordCheck.class), 5);
                }
            });
            view.findViewById(R.id.action_reset).setVisibility(View.VISIBLE);
            view.findViewById(R.id.action_reset).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(getActivity(), R.style.mDialog)
                        .setTitle(R.string.title_reset)
                        .setMessage(R.string.ask_reset_pasw)
                        .setPositiveButton(R.string.action_reset, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (Utils.resetPasw(getActivity())) {
                                    checkPasw();
                                    Utils.notification_add_update(getActivity());
                                    Utils.SnackbarC(getActivity(), getString(R.string.text_pasw_reset), view);
                                } else {
                                    Utils.SnackbarC(getActivity(), getString(R.string.error), view);
                                }
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .show();

                }
            });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 5 && resultCode == Activity.RESULT_OK)
            setPasw();
    }

    public void setPasw() {
        final View paswView = LayoutInflater.from(getActivity()).inflate(R.layout.pin_dialog, null);
        final EditText pasw_text = (EditText) paswView.findViewById(R.id.pasw);
        pasw_text.setText(SP.getString(Costants.PREFERENCE_PASSWORD, ""));
        new AlertDialog.Builder(getActivity(), R.style.mDialog)
                .setView(paswView)
                .setPositiveButton(R.string.action_lock, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SP.edit()
                                .putString(Costants.PREFERENCE_PASSWORD, pasw_text.getText().toString())
                                .putBoolean(Costants.PREFERENCES_VIEW_ALL, false)
                                .apply();
                        dialog.dismiss();
                        Utils.SnackbarC(getActivity(), getString(R.string.subtitle_lock_active), view);
                        Utils.notification_add_update(getActivity());
                        checkPasw();
                    }
                })
                .setNegativeButton(R.string.action_unlock, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SP.edit()
                                .putString(Costants.PREFERENCE_PASSWORD, "")
                                .putBoolean(Costants.PREFERENCES_VIEW_ALL, true)
                                .apply();
                        dialog.dismiss();
                        Utils.SnackbarC(getActivity(), getString(R.string.subtitle_lock_inactive), view);
                        Utils.notification_add_update(getActivity());
                        checkPasw();
                    }
                })
                .show();
    }
}