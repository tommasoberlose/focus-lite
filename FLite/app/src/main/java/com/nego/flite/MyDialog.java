package com.nego.flite;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.Explode;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nego.flite.Adapter.ImgViewPagerAdapter;
import com.nego.flite.Adapter.MyAdapter;
import com.nego.flite.Adapter.ViewPagerAdapter;
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
    public int priority = 0;

    public EditText title;
    public EditText content;
    private TextView save_button;
    private ImageView action_attach;
    private ImageView action_reminder;
    private ViewPager img_card;
    private ImageView action_menu;
    private PopupMenu control_menu;
    private CardView contact_card;
    private ImageView action_list;
    private RecyclerView content_list;
    private ImageView action_priority;
    private CardView url_card;

    private MyAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        super.onCreate(savedInstanceState);

        final Intent intent = getIntent();

        if (intent.getAction() != null) {
            if (intent.getAction().equals(Intent.ACTION_VIEW) || intent.getAction().equals(Intent.ACTION_DIAL)) {
                from_notifications = true;
                Intent i = new Intent(intent.getAction(), Uri.parse(intent.getStringExtra(Costants.EXTRA_ACTION_TYPE)));
                if (intent.getBooleanExtra(Costants.EXTRA_IS_PHOTO, false))
                    i.setDataAndType(Uri.parse(intent.getStringExtra(Costants.EXTRA_ACTION_TYPE)), "image/*");
                startActivity(i);
                finish();
            } else if (intent.getAction().equals(Costants.ACTION_DELETE) || intent.getAction().equals(Costants.ACTION_DELETE_WEAR)) {
                from_notifications = true;
                if (intent.getAction().equals(Costants.ACTION_DELETE_WEAR)) {
                    Reminder r_delete = intent.getParcelableExtra(Costants.EXTRA_REMINDER);
                    ReminderService.startAction(MyDialog.this, Costants.ACTION_DELETE, r_delete);
                    NotificationF.CancelNotification(MyDialog.this, "" + r_delete.getId());
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
            } else if (intent.getAction().equals(Costants.ACTION_SNOOZE) || intent.getAction().equals(Costants.ACTION_SNOOZE_WEAR)) {
                from_notifications = true;
                r_snooze = intent.getParcelableExtra(Costants.EXTRA_REMINDER);
                if (intent.getAction().equals(Costants.ACTION_SNOOZE_WEAR)) {
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
                action_reminder = (ImageView) findViewById(R.id.action_reminder);
                img_card = (ViewPager) findViewById(R.id.card_img);
                action_menu = (ImageView) findViewById(R.id.control_menu);
                contact_card = (CardView) findViewById(R.id.card_contact);
                action_list = (ImageView) findViewById(R.id.action_list);
                content_list = (RecyclerView) findViewById(R.id.content_list);
                action_priority = (ImageView) findViewById(R.id.action_priority);
                url_card = (CardView) findViewById(R.id.card_browser);

                // TODO fix list
                action_list.setVisibility(View.GONE);

                content_list.setHasFixedSize(true);
                LinearLayoutManager llm = new LinearLayoutManager(this);
                llm.setOrientation(LinearLayoutManager.VERTICAL);
                content_list.setLayoutManager(llm);
                content_list.setNestedScrollingEnabled(false);

                if (intent.getAction() != null && Costants.ACTION_EDIT_ITEM.equals(intent.getAction())) {
                    r = intent.getParcelableExtra(Costants.EXTRA_REMINDER);

                    pasw = r.getPasw();
                    controlPasw();

                    title.setText(r.getTitle());
                    setContent(r.getContent());
                    img = r.getImg();
                    checkImg();
                    setPriority(r.getPriority());

                    save_button.setAlpha(1f);

                    edit = true;

                    if (!r.getAction_type().equals("")) {
                        action = r.getAction_type();
                        action_info = r.getAction_info();
                        setContact();
                    }

                    if (r.getAlarm() != 0) {
                        setAlarm(r.getAlarm(), r.getAlarm_repeat());
                    }
                } else if (intent.getAction() != null && Intent.ACTION_SEND.equals(intent.getAction())) {
                    if ("text/plain".equals(intent.getType())) {
                        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                        if (sharedText != null) {
                            title.setText(sharedText);
                            save_button.setAlpha(1f);
                        }
                    } else if (intent.getType().startsWith("image/")) {
                        final Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                        if (imageUri != null) {
                            img = imageUri.toString();
                            checkImg();
                        }
                    }
                } else if (intent.getAction() != null && intent.getAction().equals("com.google.android.gm.action.AUTO_SEND")) {
                    if ("text/plain".equals(intent.getType())) {
                        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                        if (sharedText != null) {
                            title.setText(sharedText);
                            saveAll();
                        }
                    }
                }

                if (savedInstanceState != null) {
                    title.setText(savedInstanceState.getString(Costants.KEY_DIALOG_TITLE));
                    setContent(savedInstanceState.getString(Costants.KEY_DIALOG_CONTENT));
                    img = savedInstanceState.getString(Costants.KEY_DIALOG_IMG);
                    checkImg();
                    pasw = savedInstanceState.getString(Costants.KEY_DIALOG_PASW);
                    controlPasw();
                    action = savedInstanceState.getString(Costants.KEY_DIALOG_ACTION);
                    action_info = savedInstanceState.getString(Costants.KEY_DIALOG_ACTION_INFO);
                    setContact();
                    setPriority(savedInstanceState.getInt(Costants.KEY_DIALOG_PRIORITY));

                    if (savedInstanceState.getString(Costants.KEY_DIALOG_ALARM) != null)
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
                            save_button.setAlpha(1f);
                        } else {
                            save_button.setAlpha(0.5f);
                        }
                        updateUrl();
                        /* TODO rendere dinamico l'inserimento di un contatto
                        String action_to_do = Utils.checkAction(MyDialog.this, s.toString());
                        if (!action_to_do.equals("")) {
                            action = action_to_do;
                        }*/
                    }
                });

                content.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        updateUrl();
                        /* TODO rendere dinamico l'inserimento di un contatto
                        String action_to_do = Utils.checkAction(MyDialog.this, s.toString());
                        if (!action_to_do.equals("")) {
                            action = action_to_do;
                        }*/
                    }
                });

                action_attach.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setVisibilityAttachCard();
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
                control_menu.getMenu().getItem(2).setVisible(r != null);
                control_menu.getMenu().getItem(3).setVisible(r != null);
                checkPasw();
                action_menu.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        control_menu.show();
                    }
                });

                setPriority(priority);
                updateUrl();
                updateUrl();

                action_list.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        switchContent();
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
                addImg(selectedImageURI.toString());
            }
        } else if (requestCode == Costants.CODE_REQUEST_CONTACT && resultCode == Activity.RESULT_OK) {
            Uri contactData = data.getData();

            String name;

            // Get the name
            Cursor cursor = getContentResolver().query(contactData,
                    new String[]{ContactsContract.Contacts.DISPLAY_NAME},
                    null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                name = cursor.getString(cursor.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME));

                final String[] contact = Utils.fetchContacts(MyDialog.this, name);
                if (contact != null) {
                    action = Costants.ACTION_CONTACT;
                    action_info = contact[0];
                    setContact();
                } else {
                    Toast.makeText(this, R.string.error_contact_not_found, Toast.LENGTH_SHORT).show();
                }
                cursor.close();
            }
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
        if (from_notifications || (r == null && Utils.isEmpty(title) && img.equals("") && getContent().equals("")) || (r != null && r.getTitle().equals(title.getText().toString()) && r.getContent().equals(getContent()) && r.getImg().equals(img))) {
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
            outState.putString(Costants.KEY_DIALOG_CONTENT, getContent());
            outState.putString(Costants.KEY_DIALOG_IMG, img);
            outState.putString(Costants.KEY_DIALOG_PASW, pasw);
            outState.putString(Costants.KEY_DIALOG_ACTION, action);
            outState.putString(Costants.KEY_DIALOG_ACTION_INFO, action_info);
            outState.putString(Costants.KEY_DIALOG_ALARM, "" + alarm);
            outState.putString(Costants.KEY_DIALOG_ALARM_REPEAT, alarm_repeat);
            outState.putInt(Costants.KEY_DIALOG_PRIORITY, priority);
        }
    }

    public void saveAll() {
        if (Utils.isEmpty(title)) {
            Toast.makeText(this, R.string.error_title, Toast.LENGTH_SHORT).show();
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
        titleN = titleN.replace(Costants.LIST_COSTANT, "").replace(Costants.LIST_ORDER_SEPARATOR, "").replace(Costants.LIST_ITEM_SEPARATOR, "").trim();
        String contentT = getContent();
        if (!edit) {
            Calendar c = Calendar.getInstance();
            long dateC = c.getTimeInMillis();
            r = new Reminder(titleN, contentT, action, action_info, img, pasw, dateC, 0, 0, alarm, alarm_repeat, priority);
        } else {
            r.setTitle(titleN);
            r.setContent(contentT);
            r.setImg(img);
            r.setPasw(pasw);
            r.setAction_type(action);
            r.setAction_info(action_info);
            r.setLast_changed(Calendar.getInstance().getTimeInMillis());
            r.setAlarm(alarm);
            r.setAlarm_repeat(alarm_repeat);
            r.setPriority(priority);
        }
    }

    public void checkImg() {
        if (!img.equals("")) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                ImgViewPagerAdapter mPageAdapter = new ImgViewPagerAdapter(getSupportFragmentManager());
                String[] imgs = img.split(Costants.LIST_IMG_SEPARATOR);
                int n = 1;
                for (String im : imgs) {
                    mPageAdapter.addFrag(im, n + "/" + imgs.length);
                    n++;
                }
                img_card.setAdapter(mPageAdapter);
                img_card.setVisibility(View.VISIBLE);
            } else {
                requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        } else {
            img_card.setVisibility(View.GONE);
        }
    }

    public void addImg(String i) {
        if (i.contains(Costants.LIST_IMG_SEPARATOR)) {
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
        } else {
            String separator = "";
            if (!img.equals(""))
                separator = Costants.LIST_IMG_SEPARATOR;
            img += separator + i;
            checkImg();
            if (title.getText().toString().equals("")) {
                String[] name = i.split("/", -1);
                title.setText(name[name.length-1]);
            }
        }
    }

    public void removeImg(String i) {
        String[] imgs = img.split(Costants.LIST_IMG_SEPARATOR);
        img = "";
        String separator = "";
        for (String im : imgs) {
            if (!img.equals("")) {
                separator = Costants.LIST_IMG_SEPARATOR;
            }
            if (!im.equals(i))
                img += separator + im;
        }
        checkImg();
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
                        case R.id.action_info:
                            showInfo();
                            return true;
                        case R.id.action_share:
                            Intent share_intent = new Intent(Intent.ACTION_SEND);
                            share_intent.putExtra(Intent.EXTRA_TEXT, r.getTitle() + "\n" + Utils.getBigContentList(MyDialog.this, r.getContent()));
                            share_intent.setType("text/plain");
                            startActivity(share_intent);
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
                        case R.id.action_info:
                            showInfo();
                            return true;
                        case R.id.action_share:
                            Intent share_intent = new Intent(Intent.ACTION_SEND);
                            share_intent.putExtra(Intent.EXTRA_TEXT, r.getTitle() + "\n" + Utils.getBigContentList(MyDialog.this, r.getContent()));
                            share_intent.setType("text/plain");
                            startActivity(share_intent);
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
        new android.support.v7.app.AlertDialog.Builder(this, R.style.mDialog)
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

    public void setVisibilityAttachCard() {
        final View attachView = LayoutInflater.from(this).inflate(R.layout.attach_dialog, null);
        LinearLayout action_camera = (LinearLayout) attachView.findViewById(R.id.action_camera);
        LinearLayout action_gallery = (LinearLayout) attachView.findViewById(R.id.action_gallery);
        LinearLayout action_contact = (LinearLayout) attachView.findViewById(R.id.action_contact);

        final Dialog attachDialog = new Dialog(this, R.style.Theme_AppCompat_Light_Dialog);

        action_contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(MyDialog.this,
                        Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                    final Intent contact_intent = new Intent(Intent.ACTION_PICK, Uri.parse("content://contacts/people"));
                    contact_intent.setType(ContactsContract.Contacts.CONTENT_TYPE);

                    if (!action.equals("")) {
                        new AlertDialog.Builder(MyDialog.this)
                                .setTitle(getResources().getString(R.string.attention))
                                .setMessage(getResources().getString(R.string.ask_replace_contact) + "?")
                                .setPositiveButton(R.string.action_replace, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        startActivityForResult(contact_intent, Costants.CODE_REQUEST_CONTACT);
                                        attachDialog.dismiss();
                                    }
                                })
                                .setNegativeButton(android.R.string.no, null).show();
                    } else {
                        startActivityForResult(contact_intent, Costants.CODE_REQUEST_CONTACT);
                        attachDialog.dismiss();
                    }
                } else {
                    requestPermission(Manifest.permission.READ_CONTACTS);
                }
            }
        });

        action_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(MyDialog.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    final Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                        try {
                            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                            String imageFileName = "JPEG_" + timeStamp;
                            File storageDir = Environment.getExternalStoragePublicDirectory(
                                    Environment.DIRECTORY_PICTURES);
                            final File image = File.createTempFile(
                                    imageFileName,
                                    ".jpg",
                                    storageDir
                            );
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                    Uri.fromFile(image));

                            startActivityForResult(takePictureIntent, Costants.CODE_REQUEST_CAMERA);
                            addImg(Uri.fromFile(new File(image.getAbsolutePath())).toString());
                            attachDialog.dismiss();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }
            }
        });

        action_gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(MyDialog.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    Intent getIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    getIntent.setType("image/*");
                    final Intent chooserIntent = Intent.createChooser(getIntent, getString(R.string.choose_img));

                    startActivityForResult(chooserIntent, Costants.CODE_REQUEST_IMG);
                    attachDialog.dismiss();
                } else {
                    requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
                }
            }
        });

        attachDialog.setContentView(attachView);
        attachDialog.show();
    }

    public void showInfo() {
        final View infoView = LayoutInflater.from(this).inflate(R.layout.info_dialog, null);
        ((TextView) infoView.findViewById(R.id.creation_date)).setText(Utils.getDay(this, r.getDate_create()));
        ((TextView) infoView.findViewById(R.id.reminded_date)).setText(Utils.getDay(this, r.getDate_reminded()));
        ((TextView) infoView.findViewById(R.id.last_changed)).setText(Utils.getDay(this, r.getLast_changed()));
        new android.support.v7.app.AlertDialog.Builder(this, R.style.mDialog)
                .setView(infoView)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    public void setContact() {
        if (action.equals("")) {
            ((ImageView) findViewById(R.id.contact_img_replace)).setImageResource(R.drawable.ic_action_account_circle);
            findViewById(R.id.contact_img).setVisibility(View.GONE);
            collapse(contact_card);
        } else {
            findViewById(R.id.contact_img).setVisibility(View.GONE);
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                switch (action) {
                    case Costants.ACTION_CALL:
                        ((TextView) findViewById(R.id.contact_name)).setText(getString(R.string.action_call) + " " + action_info);
                        ((ImageView) findViewById(R.id.contact_img_replace)).setImageResource(R.drawable.ic_action_communication_call);
                        contact_card.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent call_intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + action_info));
                                startActivity(call_intent);
                            }
                        });
                        break;
                    case Costants.ACTION_SMS:
                        ((TextView) findViewById(R.id.contact_name)).setText(getString(R.string.action_sms) + " " + action_info);
                        ((ImageView) findViewById(R.id.contact_img_replace)).setImageResource(R.drawable.ic_action_communication_messenger);
                        contact_card.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent sms_intent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + action_info));
                                startActivity(sms_intent);
                            }
                        });
                        break;
                    case Costants.ACTION_MAIL:
                        ((TextView) findViewById(R.id.contact_name)).setText(getString(R.string.action_mail) + " " + action_info);
                        ((ImageView) findViewById(R.id.contact_img_replace)).setImageResource(R.drawable.ic_action_communication_email);
                        contact_card.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent mail_intent = new Intent(Intent.ACTION_VIEW, Uri.parse("mailto:" + action_info));
                                startActivity(mail_intent);
                            }
                        });
                        break;
                    case Costants.ACTION_CONTACT:
                        ((TextView) findViewById(R.id.contact_name)).setText(getString(R.string.text_contact));
                        final String[] contact = Utils.fetchContacts(MyDialog.this, action_info);
                        try {
                            if (contact[1] != null) {
                                ((TextView) findViewById(R.id.contact_name)).setText(contact[1]);
                            }
                            if (contact[2] != null) {
                                ((ImageView) findViewById(R.id.contact_img)).setImageURI(Uri.parse(contact[2]));
                                findViewById(R.id.contact_img).setVisibility(View.VISIBLE);
                            }
                            contact_card.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, String.valueOf(action_info));
                                    intent.setData(uri);
                                    startActivity(intent);
                                }
                            });
                        } catch (Exception e) {
                            new AlertDialog.Builder(MyDialog.this)
                                    .setTitle(getResources().getString(R.string.attention))
                                    .setMessage(getResources().getString(R.string.ask_remove_contact) + "?")
                                    .setPositiveButton(R.string.action_remove, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            action = "";
                                            action_info = "";
                                            setContact();
                                        }
                                    })
                                    .setNegativeButton(android.R.string.no, null).show();
                        }
                        break;
                }
                contact_card.findViewById(R.id.remove_contact).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        action = "";
                        action_info = "";
                        setContact();
                    }
                });
                expand(contact_card);
            } else {
                requestPermission(Manifest.permission.READ_CONTACTS);
            }
        }
    }

    public void setContent(final String c) {
        boolean list = Utils.checkList(c);
        if (!list) {
            content.setText(c);
        } else {
            final Handler mHandler = new Handler();
            new Thread(new Runnable() {
                public void run() {

                    mAdapter = new MyAdapter(MyDialog.this, c);

                    mHandler.post(new Runnable() {
                        public void run() {
                            content_list.setAdapter(mAdapter);
                            updateHeight();
                        }
                    });
                }
            }).start();
        }

        content.setVisibility(!list ? View.VISIBLE : View.GONE);
        content_list.setVisibility(list ? View.VISIBLE : View.GONE);
        action_list.setImageResource(!list ? R.drawable.ic_action_ic_playlist_add_check_white_24dp : R.drawable.ic_action_ic_playlist_remove_white_48dp);

    }

    public void updateHeight() {
        content_list.getLayoutParams().height =
                mAdapter.getItemCount() * getResources().getDimensionPixelSize(R.dimen.list_item_height) + getResources().getDimensionPixelSize(R.dimen.activity_vertical_margin) / 2;
    }

    public String getContent() {
        if (content.getVisibility() == View.VISIBLE)
            return content.getText().toString();
        else {
            if (mAdapter != null)
                return mAdapter.getData();
            else
                return content.getText().toString();
        }
    }

    private String text = "";
    public void switchContent() {
        String actual_content = getContent();
        actual_content = actual_content.trim();
        if (Utils.checkList(actual_content)) {
            text = "";
            actual_content = actual_content.replace(Costants.LIST_COSTANT, "");
            final String[] content_split = actual_content.split(Costants.LIST_ITEM_SEPARATOR, -1);
            if (mAdapter.doneSomeItems()) {
                new AlertDialog.Builder(MyDialog.this)
                        .setTitle(getResources().getString(R.string.attention))
                        .setMessage(getResources().getString(R.string.ask_keep_checked_item) + "?")
                        .setPositiveButton(R.string.action_keep, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                for (String i : content_split) {
                                    text += i.split(Costants.LIST_ORDER_SEPARATOR, -1)[1] + "\n";
                                }
                                text = text.trim();
                                setContent(text);
                            }
                        })
                        .setNegativeButton(R.string.action_delete, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                for (String i : content_split) {
                                    if (i.split(Costants.LIST_ORDER_SEPARATOR, -1)[0].equals("0"))
                                        text += i.split(Costants.LIST_ORDER_SEPARATOR, -1)[1] + "\n";
                                }
                                text = text.trim();
                                setContent(text);
                            }
                        }).show();
            } else {
                for (String i : content_split) {
                    text += i.split(Costants.LIST_ORDER_SEPARATOR, -1)[1] + "\n";
                }
                text = text.trim();
                setContent(text);
            }
        } else {
            String text = Costants.LIST_COSTANT;
            String[] content_split = actual_content.split("\n", -1);
            for (String s : content_split) {
                text += "0" + Costants.LIST_ORDER_SEPARATOR + s + Costants.LIST_ITEM_SEPARATOR;
            }
            text = text.substring(0, text.length() - Costants.LIST_ITEM_SEPARATOR.length());
            setContent(text);
        }
    }

    public void requestPermission(String code) {
        switch (code) {
            case Manifest.permission.READ_EXTERNAL_STORAGE:
                ActivityCompat.requestPermissions(MyDialog.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        Costants.CODE_REQUEST_PERMISSION_READ_EXTERNAL_STORAGE);
                break;
            case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                ActivityCompat.requestPermissions(MyDialog.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        Costants.CODE_REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE);
                break;
            case Manifest.permission.READ_CONTACTS:
                ActivityCompat.requestPermissions(MyDialog.this,
                        new String[]{Manifest.permission.READ_CONTACTS},
                        Costants.CODE_REQUEST_PERMISSION_READ_CONTACTS);
                break;
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case Costants.CODE_REQUEST_PERMISSION_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkImg();
                }
                break;
            case Costants.CODE_REQUEST_PERMISSION_READ_CONTACTS:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setContact();
                }
                break;
        }
    }

    public void setPriority(int p) {
        priority = p;
        if (p == 1) {
            action_priority.setImageResource(R.drawable.ic_action_toggle_star);
            action_priority.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setPriority(0);
                }
            });
        } else {
            action_priority.setImageResource(R.drawable.ic_action_toggle_star_outline);
            action_priority.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setPriority(1);
                }
            });
        }
    }

    public void updateUrl() {
        String url_from_title = Utils.checkURL(title.getText().toString());
        String url_from_content = Utils.checkURL(Utils.getBigContentList(this, getContent()));

        String toGo = url_from_title;
        if (toGo.equals(""))
            toGo = url_from_content;

        final String url_toGo = toGo;
        if (!url_toGo.equals("")) {
            expand(url_card);
            ((TextView) findViewById(R.id.title_url_to_open)).setText(url_toGo);
            url_card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent url_intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url_toGo));
                    startActivity(url_intent);
                }
            });
        } else {
            collapse(url_card);
        }
    }

    public static void expand(final View v) {
        v.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final int targetHeight = v.getMeasuredHeight();

        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        v.getLayoutParams().height = 1;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? ViewGroup.LayoutParams.WRAP_CONTENT
                        : (int)(targetHeight * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int)(targetHeight / v.getContext().getResources().getDisplayMetrics().density)*2);
        a.setInterpolator(new AccelerateDecelerateInterpolator());
        v.startAnimation(a);
    }

    public static void collapse(final View v) {
        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if(interpolatedTime == 1){
                    v.setVisibility(View.GONE);
                }else{
                    v.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int)(initialHeight / v.getContext().getResources().getDisplayMetrics().density)*2);
        a.setInterpolator(new AccelerateDecelerateInterpolator());
        v.startAnimation(a);
    }
}
