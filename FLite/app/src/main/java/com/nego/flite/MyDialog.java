package com.nego.flite;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.Explode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nego.flite.Functions.ReminderService;

import java.util.Calendar;

public class MyDialog extends AppCompatActivity {
    private boolean from_notifications = false;
    private Reminder r_snooze;

    private Reminder r;
    private boolean edit = false;
    public String img = "";
    public String action = "";
    public String action_info = "";
    public long alarm = 0;
    public String alarm_repeat = "";

    public EditText title;
    private ImageView action_delete;
    private TextView save_button;
    private ImageView add_img_button;
    private ImageView selected_img;
    private ImageView cancel_img;
    public ImageView action_contact;
    private ImageView action_reminder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        super.onCreate(savedInstanceState);

        final Intent intent = getIntent();

        if (intent != null && intent.getAction() != null && !intent.getAction().equals("")) {
            if (intent.getAction().equals(Intent.ACTION_VIEW) || intent.getAction().equals(Intent.ACTION_DIAL)) {
                from_notifications = true;
                startActivity(new Intent(intent.getAction(), Uri.parse(intent.getStringExtra(Costants.EXTRA_ACTION_TYPE))));
                finish();
            } else if (intent.getAction().equals(Costants.ACTION_DELETE)) {
                from_notifications = true;
                new AlertDialog.Builder(this)
                        .setTitle(getResources().getString(R.string.attention))
                        .setMessage(getResources().getString(R.string.ask_delete_reminder) + "?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                ReminderService.startAction(MyDialog.this, Costants.ACTION_DELETE, (Reminder) intent.getParcelableExtra(Costants.EXTRA_REMINDER));
                                finish();
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null).show();
            } else if (intent.getAction().equals(Costants.ACTION_SNOOZE)) {
                from_notifications = true;
                r_snooze = intent.getParcelableExtra(Costants.EXTRA_REMINDER);
                ReminderDialog r_dialog = new ReminderDialog(this, r_snooze.getAlarm(), r_snooze.getAlarm_repeat());
                r_dialog.show();
            } else if (intent.getAction().equals(Costants.ACTION_UPDATE_LIST)) {
                from_notifications = true;
                Utils.notification_add_update(this);
                finish();
            } else {
                SharedPreferences SP = getSharedPreferences(Costants.PREFERENCES_COSTANT, Context.MODE_PRIVATE);
                switch (SP.getString(Costants.PREFERENCE_STYLE_POPUP, Costants.PREFERENCE_STYLE_POPUP_DEFAULT)) {
                    case Costants.PREFERENCE_STYLE_POPUP_MD:
                        setContentView(R.layout.add_item_dialog_md);
                        break;
                    case Costants.PREFERENCE_STYLE_POPUP_ML:
                        setContentView(R.layout.add_item_dialog_ml);
                        break;
                    default:
                        setContentView(R.layout.add_item_dialog);
                        break;
                }

                findViewById(R.id.back_to_dismiss).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onBackPressed();
                    }
                });

                title = (EditText) findViewById(R.id.editText);
                action_delete = (ImageView) findViewById(R.id.action_delete);
                save_button = (TextView) findViewById(R.id.action_save);
                add_img_button = (ImageView) findViewById(R.id.add_img);
                selected_img = (ImageView) findViewById(R.id.selected_img);
                cancel_img = (ImageView) findViewById(R.id.action_cancel_image);
                action_contact = (ImageView) findViewById(R.id.action_contact);
                action_reminder = (ImageView) findViewById(R.id.action_reminder);


                if (intent.getAction() != null && Costants.ACTION_EDIT_ITEM.equals(intent.getAction())) {
                    action_delete.setVisibility(View.VISIBLE);

                    r = intent.getParcelableExtra(Costants.EXTRA_REMINDER);
                    title.setText(r.getTitle());

                    img = r.getImg();

                    if (!img.equals("")) {
                        selected_img.setImageURI(Uri.parse(img));
                        if (selected_img.getDrawable() != null) {
                            checkImg();
                        }
                    }

                    save_button.setTextColor(getResources().getColor(android.R.color.white));

                    edit = true;

                    if (!r.getAction_type().equals("")) {
                        action = r.getAction_type();
                        action_info = r.getAction_info();
                        action_contact.setVisibility(View.VISIBLE);
                        action_contact.setAlpha(1f);
                        switch (action) {
                            case Costants.ACTION_CALL:
                                action_contact.setImageDrawable(ContextCompat.getDrawable(MyDialog.this, R.drawable.ic_action_communication_call));
                                break;
                            case Costants.ACTION_SMS:
                                action_contact.setImageDrawable(ContextCompat.getDrawable(MyDialog.this, R.drawable.ic_action_communication_messenger));
                                break;
                            case Costants.ACTION_MAIL:
                                action_contact.setImageDrawable(ContextCompat.getDrawable(MyDialog.this, R.drawable.ic_action_communication_email));
                                break;
                        }
                    }

                    if (r.getAlarm() != 0) {
                        setAlarm(r.getAlarm(), r.getAlarm_repeat());
                    }
                } else if (intent.getAction() != null && Intent.ACTION_SEND.equals(intent.getAction())) {
                    if ("text/plain".equals(intent.getType())) {
                        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                        if (sharedText != null) {
                            title.setText(sharedText);
                            save_button.setTextColor(getResources().getColor(android.R.color.white));
                        }
                    } else if (intent.getType().startsWith("image/")) {
                        final Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                        if (imageUri != null) {
                            img = imageUri.toString();
                            checkImg();
                        }
                    }
                }

                if (savedInstanceState != null) {
                    title.setText(savedInstanceState.getString(Costants.KEY_DIALOG_TITLE));
                    img = savedInstanceState.getString(Costants.KEY_DIALOG_IMG);
                    action = savedInstanceState.getString(Costants.KEY_DIALOG_ACTION);
                    action_info = savedInstanceState.getString(Costants.KEY_DIALOG_ACTION_INFO);
                    if (!action.equals("")) {
                        action_contact.setVisibility(View.VISIBLE);
                        if (!action_info.equals(""))
                            action_contact.setAlpha(1f);
                        switch (action) {
                            case Costants.ACTION_CALL:
                                action_contact.setImageDrawable(ContextCompat.getDrawable(MyDialog.this, R.drawable.ic_action_communication_call));
                                break;
                            case Costants.ACTION_SMS:
                                action_contact.setImageDrawable(ContextCompat.getDrawable(MyDialog.this, R.drawable.ic_action_communication_messenger));
                                break;
                            case Costants.ACTION_MAIL:
                                action_contact.setImageDrawable(ContextCompat.getDrawable(MyDialog.this, R.drawable.ic_action_communication_email));
                                break;
                        }
                    }

                    setAlarm(Long.parseLong(savedInstanceState.getString(Costants.KEY_DIALOG_ALARM)), savedInstanceState.getString(Costants.KEY_DIALOG_ALARM_REPEAT));

                    checkImg();
                }

                save_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        saveAll();
                    }
                });

                action_delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new AlertDialog.Builder(MyDialog.this)
                                .setTitle(getResources().getString(R.string.attention))
                                .setMessage(getResources().getString(R.string.ask_delete_reminder) + "?")
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        ReminderService.startAction(MyDialog.this, Costants.ACTION_DELETE, r);
                                        finish();
                                    }
                                })
                                .setNegativeButton(android.R.string.cancel, null).show();
                    }
                });

                title.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                        SharedPreferences SP = getSharedPreferences(Costants.PREFERENCES_COSTANT, Context.MODE_PRIVATE);
                        if (s.length() > 0) {
                            save_button.setTextColor(getResources().getColor(android.R.color.white));
                        } else {
                            save_button.setTextColor(getResources().getColor(R.color.white_back));
                        }

                        String action_to_do = Utils.checkAction(MyDialog.this, s.toString());
                        if (!action_to_do.equals("")) {
                            action = action_to_do;
                            if (action_info.equals(""))
                                action_contact.setImageDrawable(ContextCompat.getDrawable(MyDialog.this, R.drawable.ic_action_contacts));
                            action_contact.setVisibility(View.VISIBLE);
                        } else {
                            action = "";
                            action_info = "";
                            action_contact.setVisibility(View.GONE);
                            action_contact.setAlpha(0.6f);
                        }
                    }
                });

                action_contact.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent contact_intent = new Intent(Intent.ACTION_PICK, Uri.parse("content://contacts/people"));
                        contact_intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
                        startActivityForResult(contact_intent, Costants.CODE_REQUEST_CONTACT);

                    }
                });

                add_img_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent getIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        getIntent.setType("image/*");
                        Intent chooserIntent = Intent.createChooser(getIntent, getString(R.string.choose_img));

                        startActivityForResult(chooserIntent, Costants.CODE_REQUEST_IMG);
                    }
                });

                action_reminder.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ReminderDialog r_dialog = new ReminderDialog(MyDialog.this, alarm, alarm_repeat);
                        r_dialog.show();
                    }
                });

            }
        }

    }

    @Override
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Costants.CODE_REQUEST_IMG && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri selectedImageURI = data.getData();
                img = selectedImageURI.toString();
                checkImg();
            }
        } else if (requestCode == Costants.CODE_REQUEST_CONTACT && resultCode == Activity.RESULT_OK) {
            Uri contactData = data.getData();

            String name = "";
            String email = "";
            String phone = "";

            // Get the name
            Cursor cursor = getContentResolver().query(contactData,
                    new String[]{ContactsContract.Contacts.DISPLAY_NAME},
                    null, null, null);
            if (cursor.moveToFirst()) {
                name = cursor.getString(cursor.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME));

                // Set up the projection
                String[] projection = {
                        ContactsContract.Data.DISPLAY_NAME,
                        ContactsContract.Contacts.Data.DATA1,
                        ContactsContract.Contacts.Data.MIMETYPE };

                // Query ContactsContract.Data
                cursor = getContentResolver().query(
                        ContactsContract.Data.CONTENT_URI, projection,
                        ContactsContract.Data.DISPLAY_NAME + " = ?",
                        new String[] { name },
                        null);

                if (cursor.moveToFirst()) {
                    // Get the indexes of the MIME type and data
                    int mimeIdx = cursor.getColumnIndex(
                            ContactsContract.Contacts.Data.MIMETYPE);
                    int dataIdx = cursor.getColumnIndex(
                            ContactsContract.Contacts.Data.DATA1);

                    // Match the data to the MIME type, store in variables
                    do {
                        String mime = cursor.getString(mimeIdx);
                        if (ContactsContract.CommonDataKinds.Email
                                .CONTENT_ITEM_TYPE.equalsIgnoreCase(mime)) {
                            email = cursor.getString(dataIdx);
                        }
                        if (ContactsContract.CommonDataKinds.Phone
                                .CONTENT_ITEM_TYPE.equalsIgnoreCase(mime)) {
                            phone = cursor.getString(dataIdx);
                        }
                    } while (cursor.moveToNext());

                    switch (action) {
                        case Costants.ACTION_CALL:
                            if (!phone.equals("")) {
                                action_info = phone;
                                action_contact.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_action_communication_call));
                                title.setText(title.getText() + " " + name);
                                action_contact.setAlpha(1f);
                            } else {
                                Toast.makeText(this, R.string.error_contact_not_found, Toast.LENGTH_SHORT).show();
                            }
                            break;
                        case Costants.ACTION_SMS:
                            if (!phone.equals("")) {
                                action_info = phone;
                                action_contact.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_action_communication_messenger));
                                title.setText(title.getText() + " " + name);
                                action_contact.setAlpha(1f);
                            } else {
                                Toast.makeText(this, R.string.error_contact_not_found, Toast.LENGTH_SHORT).show();
                            }
                            break;
                        case Costants.ACTION_MAIL:
                            if (!phone.equals("")) {
                                action_info = email;
                                action_contact.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_action_communication_email));
                                title.setText(title.getText() + " " + name);
                                action_contact.setAlpha(1f);
                            } else {
                                Toast.makeText(this, R.string.error_contact_not_found, Toast.LENGTH_SHORT).show();
                            }
                            break;
                    }

                }
            }
            cursor.close();
        }
    }

    @Override
    public void onBackPressed() {
        if ((r == null && Utils.isEmpty(title) && img.equals("")) || (r != null && r.getTitle().equals(title.getText().toString()) && r.getImg().equals(img))) {
            finish();
        } else {
            new AlertDialog.Builder(MyDialog.this)
                    .setTitle(getResources().getString(R.string.attention))
                    .setMessage(getResources().getString(R.string.ask_exit) + "?")
                    .setPositiveButton(R.string.action_exit_editor, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            finish();
                        }
                    })
                    .setNegativeButton(android.R.string.no, null).show();
        }
    }

    @Override
    protected void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);
        if (!from_notifications) {
            outState.putString(Costants.KEY_DIALOG_TITLE, title.toString());
            outState.putString(Costants.KEY_DIALOG_IMG, img);
            outState.putString(Costants.KEY_DIALOG_ACTION, action);
            outState.putString(Costants.KEY_DIALOG_ACTION_INFO, action_info);
            outState.putString(Costants.KEY_DIALOG_ALARM, "" + alarm);
            outState.putString(Costants.KEY_DIALOG_ALARM_REPEAT, alarm_repeat);
        }
    }

    public void saveAll() {
        if (Utils.isEmpty(title)) {
            title.setError(getResources().getString(R.string.error));
        } else {
            generateReminder();
            if (!edit)
                ReminderService.startAction(MyDialog.this, Costants.ACTION_CREATE, r);
            else
                ReminderService.startAction(MyDialog.this, Costants.ACTION_UPDATE, r);
            finish();
        }
    }

    public void generateReminder() {
        if (!edit) {
            Calendar c = Calendar.getInstance();
            long dateC = c.getTimeInMillis();
            String titleN = title.getText().toString();
            titleN = titleN.trim();
            r = new Reminder(titleN, action, action_info, img, dateC, alarm, alarm_repeat);
        } else {
            String titleN = title.getText().toString();
            titleN = titleN.trim();
            r.setTitle(titleN);
            r.setImg(img);
            r.setAction_type(action);
            r.setAction_info(action_info);
            r.setAlarm(alarm);
            r.setAlarm_repeat(alarm_repeat);
        }
    }

    public void checkImg() {
        if (!img.equals("")) {
            selected_img.setImageURI(Uri.parse(img));
            selected_img.setVisibility(View.VISIBLE);
            cancel_img.setVisibility(View.VISIBLE);
            selected_img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(img)));
                }
            });

            cancel_img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(MyDialog.this)
                            .setTitle(getResources().getString(R.string.attention))
                            .setMessage(getResources().getString(R.string.ask_delete_img) + "?")
                            .setPositiveButton(R.string.action_remove, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    img = "";
                                    checkImg();
                                }
                            })
                            .setNegativeButton(android.R.string.no, null).show();
                }
            });
        } else {
            selected_img.setVisibility(View.GONE);
            cancel_img.setVisibility(View.GONE);
        }
    }

    public void setAlarm(long alarm, String alarm_repeat) {
        if (from_notifications) {
            r_snooze.setAlarm(alarm);
            r_snooze.setAlarm_repeat(alarm_repeat);
            ReminderService.startAction(this, Costants.ACTION_UPDATE, r_snooze);
            finish();
        } else {
            this.alarm = alarm;
            this.alarm_repeat = alarm_repeat;
            if (alarm != 0) {
                action_reminder.setImageDrawable(ContextCompat.getDrawable(MyDialog.this, R.drawable.ic_action_alarm_on));
            } else {
                action_reminder.setImageDrawable(ContextCompat.getDrawable(MyDialog.this, R.drawable.ic_action_alarm_add));
            }
        }
    }

    @Override
     public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
