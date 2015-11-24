package com.nego.flite;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.PopupMenu;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.Explode;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nego.flite.Functions.AlarmF;
import com.nego.flite.Functions.NotificationF;
import com.nego.flite.Functions.ReminderService;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MyDialog extends AppCompatActivity {
    private boolean from_notifications = false;
    private Reminder r_snooze;

    private Reminder r;
    private boolean edit = false;
    public String img = "";
    public String pasw = "";
    public String action = "";
    public String action_info = "";
    public long alarm = 0;
    public String alarm_repeat = "";

    public EditText title;
    public EditText content;
    private TextView save_button;
    private ImageView action_attach;
    private LinearLayout action_camera;
    private LinearLayout action_gallery;
    private ImageView selected_img;
    private ImageView cancel_img;
    public LinearLayout action_contact;
    private ImageView action_reminder;
    private CardView img_card;
    private ImageView action_menu;
    private PopupMenu control_menu;
    private CardView card_attach;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        super.onCreate(savedInstanceState);

        final Intent intent = getIntent();

        if (intent != null && intent.getAction() != null && !intent.getAction().equals("")) {
            if (intent.getAction().equals(Intent.ACTION_VIEW) || intent.getAction().equals(Intent.ACTION_DIAL)) {
                from_notifications = true;
                // TODO check if file is image
                startActivity(new Intent(intent.getAction(), Uri.parse(intent.getStringExtra(Costants.EXTRA_ACTION_TYPE))));
                finish();
            } else if (intent.getAction().equals(Costants.ACTION_DELETE)) {
                from_notifications = true;
                if (intent.getBooleanExtra(Costants.FROM_WEAR, false)) {
                    ReminderService.startAction(MyDialog.this, Costants.ACTION_DELETE, (Reminder) intent.getParcelableExtra(Costants.EXTRA_REMINDER));
                    NotificationF.CancelNotification(MyDialog.this, "" + r_snooze.getId());
                    finish();
                } else {
                    new AlertDialog.Builder(this)
                            .setTitle(getResources().getString(R.string.attention))
                            .setMessage(getResources().getString(R.string.ask_delete_reminder) + "?")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    ReminderService.startAction(MyDialog.this, Costants.ACTION_DELETE, (Reminder) intent.getParcelableExtra(Costants.EXTRA_REMINDER));
                                }
                            })
                            .setNegativeButton(android.R.string.cancel, null)
                            .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    finish();
                                }
                            })
                            .show();
                }
            } else if (intent.getAction().equals(Costants.ACTION_SNOOZE)) {
                from_notifications = true;
                r_snooze = intent.getParcelableExtra(Costants.EXTRA_REMINDER);
                if (intent.getBooleanExtra(Costants.FROM_WEAR, false)) {
                    AlarmF.addAlarm(MyDialog.this, r_snooze.getId(), r_snooze.getAlarm() + 10 * 60 * 1000, r_snooze.getAlarm_repeat());
                    NotificationF.CancelNotification(MyDialog.this, "" + r_snooze.getId());
                    finish();
                } else {
                    ReminderDialog r_dialog = new ReminderDialog(this, r_snooze.getAlarm(), r_snooze.getAlarm_repeat());
                    r_dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            finish();
                        }
                    });
                    r_dialog.show();
                }
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
                content = (EditText) findViewById(R.id.content);
                save_button = (TextView) findViewById(R.id.action_save);
                action_attach = (ImageView) findViewById(R.id.action_attach);
                action_camera = (LinearLayout) findViewById(R.id.action_camera);
                action_gallery = (LinearLayout) findViewById(R.id.action_gallery);
                selected_img = (ImageView) findViewById(R.id.selected_img);
                cancel_img = (ImageView) findViewById(R.id.action_cancel_image);
                action_contact = (LinearLayout) findViewById(R.id.action_contact);
                action_reminder = (ImageView) findViewById(R.id.action_reminder);
                img_card = (CardView) findViewById(R.id.card_img);
                action_menu = (ImageView) findViewById(R.id.control_menu);
                card_attach = (CardView) findViewById(R.id.card_attach);


                if (intent.getAction() != null && Costants.ACTION_EDIT_ITEM.equals(intent.getAction())) {
                    r = intent.getParcelableExtra(Costants.EXTRA_REMINDER);

                    pasw = r.getPasw();
                    controlPasw();

                    title.setText(r.getTitle());
                    content.setText(r.getContent());
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
                        action_info = r.getAction_info();/*
                        TODO visualizzare contatto selezionato
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
                        }*/
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
                    content.setText(savedInstanceState.getString(Costants.KEY_DIALOG_CONTENT));
                    img = savedInstanceState.getString(Costants.KEY_DIALOG_IMG);
                    pasw = savedInstanceState.getString(Costants.KEY_DIALOG_PASW);
                    action = savedInstanceState.getString(Costants.KEY_DIALOG_ACTION);
                    action_info = savedInstanceState.getString(Costants.KEY_DIALOG_ACTION_INFO);/*
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
                    }*/

                    setAlarm(Long.parseLong(savedInstanceState.getString(Costants.KEY_DIALOG_ALARM)), savedInstanceState.getString(Costants.KEY_DIALOG_ALARM_REPEAT));

                    checkImg();
                }

                save_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        saveAll();
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
                            action = action_to_do;/*
                            if (action_info.equals(""))
                                action_contact.setImageDrawable(ContextCompat.getDrawable(MyDialog.this, R.drawable.ic_action_contacts));
                            action_contact.setVisibility(View.VISIBLE);*/
                        } else {
                            action = "";
                            action_info = "";/*
                            action_contact.setVisibility(View.GONE);
                            action_contact.setAlpha(0.6f);*/
                        }
                    }
                });

                action_contact.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent contact_intent = new Intent(Intent.ACTION_PICK, Uri.parse("content://contacts/people"));
                        contact_intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
                        startActivityForResult(contact_intent, Costants.CODE_REQUEST_CONTACT);
                        setVisibilityAttachCard(false);
                    }
                });

                action_attach.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setVisibilityAttachCard(true);
                    }
                });

                action_camera.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                            try {
                                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                                String imageFileName = "JPEG_" + timeStamp;
                                File storageDir = Environment.getExternalStoragePublicDirectory(
                                        Environment.DIRECTORY_PICTURES);
                                File image = File.createTempFile(
                                        imageFileName,  /* prefix */
                                        ".jpg",         /* suffix */
                                        storageDir      /* directory */
                                );
                                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                        Uri.fromFile(image));
                                startActivityForResult(takePictureIntent, Costants.CODE_REQUEST_CAMERA);
                                img = Uri.fromFile(new File(image.getAbsolutePath())).toString();
                                Log.i("IMG_PATH", img);
                                setVisibilityAttachCard(false);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });

                action_gallery.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent getIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        getIntent.setType("image/*");
                        Intent chooserIntent = Intent.createChooser(getIntent, getString(R.string.choose_img));

                        startActivityForResult(chooserIntent, Costants.CODE_REQUEST_IMG);
                        setVisibilityAttachCard(false);
                    }
                });

                action_reminder.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ReminderDialog r_dialog = new ReminderDialog(MyDialog.this, alarm, alarm_repeat);
                        r_dialog.show();
                    }
                });

                ContextThemeWrapper mContextPicker = new ContextThemeWrapper(this, R.style.Theme_AppCompat_Light);
                control_menu = new PopupMenu(mContextPicker, action_menu, GravityCompat.END);
                control_menu.inflate(R.menu.control_menu);
                control_menu.getMenu().getItem(1).setVisible(r != null);
                checkPasw();
                action_menu.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        control_menu.show();
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
                                //action_contact.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_action_communication_call));
                                //action_contact.setAlpha(1f);
                                title.setText(title.getText() + " " + name);
                            } else {
                                Toast.makeText(this, R.string.error_contact_not_found, Toast.LENGTH_SHORT).show();
                            }
                            break;
                        case Costants.ACTION_SMS:
                            if (!phone.equals("")) {
                                action_info = phone;
                                //action_contact.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_action_communication_messenger));
                                //action_contact.setAlpha(1f);
                                title.setText(title.getText() + " " + name);
                            } else {
                                Toast.makeText(this, R.string.error_contact_not_found, Toast.LENGTH_SHORT).show();
                            }
                            break;
                        case Costants.ACTION_MAIL:
                            if (!phone.equals("")) {
                                action_info = email;
                                //action_contact.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_action_communication_email));
                                //action_contact.setAlpha(1f);
                                title.setText(title.getText() + " " + name);
                            } else {
                                Toast.makeText(this, R.string.error_contact_not_found, Toast.LENGTH_SHORT).show();
                            }
                            break;
                    }

                }
            }
            cursor.close();
        } else if (requestCode == Costants.CODE_REQUEST_PASW) {
            if (resultCode == RESULT_OK)
                findViewById(R.id.back_to_dismiss).setVisibility(View.VISIBLE);
            else
                finish();
        } else if (requestCode == Costants.CODE_REQUEST_CAMERA && resultCode == RESULT_OK) {
            if (data != null) {
                checkImg();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (from_notifications || (r == null && Utils.isEmpty(title) && img.equals("") && Utils.isEmpty(content)) || (r != null && r.getTitle().equals(title.getText().toString()) && r.getContent().equals(content.getText().toString()) && r.getImg().equals(img))) {
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
            outState.putString(Costants.KEY_DIALOG_CONTENT, content.toString());
            outState.putString(Costants.KEY_DIALOG_IMG, img);
            outState.putString(Costants.KEY_DIALOG_PASW, pasw);
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
        String titleN = title.getText().toString();
        titleN = titleN.trim();
        String contentT = content.getText().toString();
        contentT.trim();
        if (!edit) {
            Calendar c = Calendar.getInstance();
            long dateC = c.getTimeInMillis();
            // TODO fare i controlli su content
            r = new Reminder(titleN, contentT, action, action_info, img, pasw, dateC, alarm, alarm_repeat);
        } else {
            r.setTitle(titleN);
            r.setContent(contentT);
            r.setImg(img);
            r.setPasw(pasw);
            r.setAction_type(action);
            r.setAction_info(action_info);
            r.setAlarm(alarm);
            r.setAlarm_repeat(alarm_repeat);
        }
    }

    public void checkImg() {
        if (!img.equals("")) {
            selected_img.setImageURI(Uri.parse(img));
            img_card.setVisibility(View.VISIBLE);
            selected_img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse(img), "image/*");
                    startActivity(intent);
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
            img_card.setVisibility(View.GONE);
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
                action_reminder.setImageDrawable(ContextCompat.getDrawable(MyDialog.this, R.drawable.ic_action_notifications_on));
            } else {
                action_reminder.setImageDrawable(ContextCompat.getDrawable(MyDialog.this, R.drawable.ic_action_social_notifications));
            }
        }
    }

    @Override
     public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    public void checkPasw() {
        if (pasw.equals("")) {
            control_menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.action_pasw:
                            setPasw();
                            return true;
                        case R.id.action_delete:
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
                            return true;
                    }
                    return false;
                }
            });
            control_menu.getMenu().getItem(0).setTitle(getString(R.string.action_lock));
        } else {
            control_menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.action_pasw:
                            pasw = "";
                            checkPasw();
                            return true;
                        case R.id.action_delete:
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
                            return true;
                    }
                    return false;
                }
            });
            control_menu.getMenu().getItem(0).setTitle(getString(R.string.action_unlock));

        }
    }

    public void setPasw() {
        final View paswView = LayoutInflater.from(this).inflate(R.layout.pin_dialog, null);
        final EditText pasw_text = (EditText) paswView.findViewById(R.id.pasw);
        pasw_text.setText(pasw);
        new android.support.v7.app.AlertDialog.Builder(this, R.style.Theme_AppCompat_Light_Dialog)
                .setView(paswView)
                .setPositiveButton(R.string.action_lock, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        pasw = pasw_text.getText().toString();
                        checkPasw();
                        dialog.dismiss();
                    }
                })
                .show();
    }

    public void controlPasw() {
        if (!pasw.equals("")) {
            findViewById(R.id.back_to_dismiss).setVisibility(View.INVISIBLE);
            Intent requestPasw = new Intent(MyDialog.this, PasswordCheck.class);
            requestPasw.putExtra(Costants.EXTRA_REMINDER, r);
            startActivityForResult(requestPasw, Costants.CODE_REQUEST_PASW);
        }
    }

    public void setVisibilityAttachCard(boolean b) {
        if (b && card_attach.getVisibility() != View.VISIBLE) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                Animator anim =
                        ViewAnimationUtils.createCircularReveal(card_attach, (int) getResources().getDimension(R.dimen.activity_horizontal_margin), 0, 0, card_attach.getWidth());
                card_attach.setVisibility(View.VISIBLE);
                anim.start();
            } else {
                card_attach.setVisibility(View.VISIBLE);
            }
        } else {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                Animator anim =
                        ViewAnimationUtils.createCircularReveal(card_attach, (int) getResources().getDimension(R.dimen.activity_horizontal_margin), 0, card_attach.getWidth(), 0);
                anim.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        card_attach.setVisibility(View.GONE);
                    }
                });
                anim.start();
            } else {
                card_attach.setVisibility(View.GONE);
            }
        }
    }
}
