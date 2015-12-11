package com.nego.flite;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v7.widget.AppCompatSpinner;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ReminderDialog extends Dialog {

    private Context mContext;
    private Context mContextPicker;

    public long alarm = 0;
    public String alarm_repeat = "";

    private LinearLayout action_day;
    private LinearLayout action_time;
    private LinearLayout error_date;
    private TextView title_day;
    private TextView title_time;
    private AppCompatSpinner action_repeat;
    private TextView save_button;
    private TextView delete_button;

    private SharedPreferences SP;

    public ReminderDialog(final Context context, final long alarm_i, String alarm_repeat_i) {
        super(context, R.style.mDialog);
        mContext = context;

        SP = context.getSharedPreferences(Costants.PREFERENCES_COSTANT, Context.MODE_PRIVATE);

        if (Utils.isBrokenSamsungDevice()) {
            mContextPicker = new ContextThemeWrapper(context, android.R.style.Theme_Holo_Light_Dialog);
        } else {
            mContextPicker = mContext;
        }

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        final View dialogView = LayoutInflater.from(context).inflate(R.layout.reminder_dialog, null);

        error_date = (LinearLayout) dialogView.findViewById(R.id.error_date);
        action_day = (LinearLayout) dialogView.findViewById(R.id.action_day);
        action_time = (LinearLayout) dialogView.findViewById(R.id.action_time);
        action_repeat = (AppCompatSpinner) dialogView.findViewById(R.id.text_repeat);
        save_button = (TextView) dialogView.findViewById(R.id.action_save);
        delete_button = (TextView) dialogView.findViewById(R.id.action_delete);
        title_day = (TextView) dialogView.findViewById(R.id.text_day);
        title_time = (TextView) dialogView.findViewById(R.id.text_time);

        alarm = alarm_i;
        alarm_repeat = alarm_repeat_i;

        final Calendar c = Calendar.getInstance();
        if (alarm != 0) {
            c.setTimeInMillis(alarm);
        } else {
            alarm = c.getTimeInMillis();
        }

        title_day.setText(Utils.getDay(context, alarm));
        title_time.setText(Utils.getTime(context, alarm));

        String[] titles = new String[] {
                mContext.getString(R.string.alarm_repeat_not),
                mContext.getString(R.string.alarm_repeat_day),
                mContext.getString(R.string.alarm_repeat_week),
                mContext.getString(R.string.alarm_repeat_month),
                mContext.getString(R.string.alarm_repeat_year),};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(mContext, R.layout.simple_item_dropdown, titles);
        action_repeat.setAdapter(spinnerAdapter);
        action_repeat.setSelection(Utils.getRepeat(context, alarm_repeat));

        action_day.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog mDatePicker = new DatePickerDialog(mContextPicker, R.style.mDialog_Picker, new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker datepicker, int selectedYear, final int selectedMonth, final int selectedDay) {
                        c.set(Calendar.DAY_OF_MONTH, selectedDay);
                        c.set(Calendar.MONTH, selectedMonth);
                        c.set(Calendar.YEAR, selectedYear);
                        alarm = c.getTimeInMillis();
                        title_day.setText(Utils.getDay(context, alarm));
                        if (Utils.isOldDate(alarm)) {
                            error_date.setVisibility(View.VISIBLE);
                        } else {
                            error_date.setVisibility(View.GONE);
                        }
                    }
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));

                // Inserisco un calendario fittizzio per sapere la data minima di oggi
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(c.getTimeInMillis());
                cal.set(Calendar.HOUR_OF_DAY, cal.getMinimum(Calendar.HOUR_OF_DAY));
                cal.set(Calendar.MINUTE, cal.getMinimum(Calendar.MINUTE));
                cal.set(Calendar.SECOND, cal.getMinimum(Calendar.SECOND));
                cal.set(Calendar.MILLISECOND, cal.getMinimum(Calendar.MILLISECOND));

                mDatePicker.getDatePicker().setMinDate(cal.getTimeInMillis());
                mDatePicker.setTitle("");
                mDatePicker.show();
            }
        });

        action_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog mTimePicker = new TimePickerDialog(mContextPicker, R.style.mDialog_Picker, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        c.set(Calendar.HOUR_OF_DAY, selectedHour);
                        c.set(Calendar.MINUTE, selectedMinute);
                        alarm = c.getTimeInMillis();
                        title_time.setText(Utils.getTime(context, alarm));
                        if (Utils.isOldDate(alarm)) {
                            error_date.setVisibility(View.VISIBLE);
                        } else {
                            error_date.setVisibility(View.GONE);
                        }
                    }
                }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), !SP.getBoolean(Costants.PREFERENCE_TWELVE_HOUR_FORMAT, false));
                mTimePicker.show();
            }
        });

        save_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utils.isOldDate(alarm)) {
                    error_date.setVisibility(View.VISIBLE);
                } else {
                    error_date.setVisibility(View.GONE);
                    ((MyDialog) mContext).setAlarm(alarm, Utils.getRepeatString(context, action_repeat.getSelectedItemPosition()));
                    dismiss();
                }
            }
        });

        delete_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(mContext)
                        .setTitle(mContext.getResources().getString(R.string.attention))
                        .setMessage(mContext.getResources().getString(R.string.ask_delete_alarm) + "?")
                        .setPositiveButton(R.string.action_remove, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                ((MyDialog) mContext).setAlarm(0, "");
                                dismiss();
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null).show();
            }
        });


        this.setContentView(dialogView);
    }

}