package com.nego.flite;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.Explode;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class MyDialog extends AppCompatActivity {
    private boolean from_notifications = false;
    private Reminder r_snooze;
    private Snackbar snackbar_attach;
    private Snackbar snackbar_reminders;
    private Snackbar snackbar_suggestions;
    private SharedPreferences SP;

    private Reminder r;
    private boolean edit = false;
    public String img = "";
    public String pasw = "";
    public String action = "";
    public String action_info = "";
    public long alarm = 0;
    public String alarm_repeat = "";
    public int priority = 0;
    private String tmpImgNameFile = "";
    private String address = "";
    private String voice_note = "";

    public EditText title;
    public EditText content;
    private TextView save_button;
    private ViewPager img_card;
    private CardView contact_card;
    private RecyclerView content_list;
    private CardView url_card;
    private CardView address_card;
    private RelativeLayout action_add_to_list;

    private MyAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        super.onCreate(savedInstanceState);

        final Intent intent = getIntent();
        SP = getSharedPreferences(Costants.PREFERENCES_COSTANT, Context.MODE_PRIVATE);

        if (intent.getAction() != null) {
            if (intent.getAction().equals(Intent.ACTION_VIEW) || intent.getAction().equals(Intent.ACTION_DIAL)) {
                from_notifications = true;
                Intent i = new Intent(intent.getAction(), Uri.parse(intent.getStringExtra(Costants.EXTRA_ACTION_TYPE)));
                if (intent.getBooleanExtra(Costants.EXTRA_IS_PHOTO, false))
                    i.setDataAndType(Uri.parse(intent.getStringExtra(Costants.EXTRA_ACTION_TYPE)), "image/*");
                startActivity(i);
                finish();
            } else if (intent.getAction().equals(Costants.ACTION_ARCHIVE) || intent.getAction().equals(Costants.ACTION_ARCHIVE_WEAR)) {
                from_notifications = true;
                int id = ((Reminder) intent.getParcelableExtra(Costants.EXTRA_REMINDER)).getId();
                Reminder r_delete = Utils.getReminder(this, id);
                ReminderService.startAction(MyDialog.this, Costants.ACTION_ARCHIVE, r_delete);
                NotificationF.CancelNotification(MyDialog.this, "" + r_delete.getId());
                finish();
            } else if (intent.getAction().equals(Costants.ACTION_SNOOZE) || intent.getAction().equals(Costants.ACTION_SNOOZE_WEAR)) {
                from_notifications = true;
                int id = ((Reminder) intent.getParcelableExtra(Costants.EXTRA_REMINDER)).getId();
                r_snooze = Utils.getReminder(this, id);
                if (intent.getAction().equals(Costants.ACTION_SNOOZE_WEAR)) {
                    if (r_snooze.getAlarm() > 0)
                        AlarmF.addAlarm(MyDialog.this, r_snooze.getId(), r_snooze.getAlarm() + 10 * 60 * 1000, "");
                    else
                        AlarmF.addAlarm(MyDialog.this, r_snooze.getId(), Calendar.getInstance().getTimeInMillis() + 10 * 60 * 1000, "");
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

                Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
                setSupportActionBar(toolbar);
                setTitle("");

                findViewById(R.id.back_to_dismiss).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onBackPressed();
                    }
                });

                title = (EditText) findViewById(R.id.editText);
                content = (EditText) findViewById(R.id.content);
                save_button = (TextView) findViewById(R.id.action_save);
                img_card = (ViewPager) findViewById(R.id.card_img);
                contact_card = (CardView) findViewById(R.id.card_contact);
                content_list = (RecyclerView) findViewById(R.id.content_list);
                url_card = (CardView) findViewById(R.id.card_browser);
                address_card = (CardView) findViewById(R.id.card_address);
                action_add_to_list = (RelativeLayout) findViewById(R.id.action_add_to_list);

                content_list.setHasFixedSize(true);
                LinearLayoutManager llm = new LinearLayoutManager(this);
                llm.setOrientation(LinearLayoutManager.VERTICAL);
                content_list.setLayoutManager(llm);
                content_list.setNestedScrollingEnabled(false);

                ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.DOWN | ItemTouchHelper.UP, 0) {
                    @Override
                    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                        mAdapter.swapElement(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                        return true;
                    }

                    @Override
                    public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                    }
                };

                ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
                itemTouchHelper.attachToRecyclerView(content_list);

                if (intent.getAction() != null && Costants.ACTION_EDIT_ITEM.equals(intent.getAction())) {
                    int id = ((Reminder) intent.getParcelableExtra(Costants.EXTRA_REMINDER)).getId();
                    r = Utils.getReminder(this, id);

                    if (r == null) {
                        sendBroadcast(new Intent(Costants.ACTION_UPDATE_LIST));
                        Toast.makeText(this, getString(R.string.error), Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    pasw = r.getPasw();
                    controlPasw();

                    title.setText(r.getTitle());
                    setContent(r.getContent());
                    img = r.getImg();
                    checkImg();
                    setPriority(r.getPriority());

                    updateAddress(r.getAddress());

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
                            addImg(imageUri.toString());
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
                    updateAddress(savedInstanceState.getString(Costants.KEY_DIALOG_ADDRESS));
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
                        if (s.length() > 0) {
                            save_button.setAlpha(1f);
                        } else {
                            save_button.setAlpha(0.5f);
                        }
                        updateUrl();
                        setSuggestionsSnackbar();
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
                        setSuggestionsSnackbar();
                    }
                });


                setPriority(priority);
                updateUrl();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.dialog_menu, menu);

        // ATTACH
        menu.getItem(0).setVisible(r == null || r.getDate_archived() == 0);

        // REMINDER
        menu.getItem(1).setVisible(r == null || r.getDate_archived() == 0);
        menu.getItem(1).setIcon(alarm == 0 ? R.drawable.ic_action_social_notifications_dialog : R.drawable.ic_action_social_notifications_on_dialog);

        // LIST
        menu.getItem(2).setVisible(r == null || r.getDate_archived() == 0);
        menu.getItem(2).setIcon(Utils.checkList(getContent()) ? R.drawable.ic_action_ic_playlist_remove_dialog : R.drawable.ic_action_playlist_add_check_dialog);

        // PRIORITY
        menu.getItem(3).setVisible(r == null || r.getDate_archived() == 0);
        menu.getItem(3).setIcon(priority == 0 ? R.drawable.ic_action_toggle_star_outline_dialog : R.drawable.ic_action_toggle_star_dialog);

        // UNARCHIVE
        menu.getItem(4).setVisible(r != null && r.getDate_archived() != 0);

        // DELETE
        menu.getItem(5).setVisible(r != null && r.getDate_archived() != 0);

        // NOTE DETAILS
        menu.getItem(6).setVisible(r!= null);
        menu.getItem(6).setShowAsAction(r != null && r.getDate_archived() != 0 ? MenuItem.SHOW_AS_ACTION_ALWAYS : MenuItem.SHOW_AS_ACTION_IF_ROOM);

        // LOCK
        menu.getItem(7).setIcon(pasw.equals("") ? R.drawable.ic_action_action_lock_dialog : R.drawable.ic_action_ic_lock_open_dialog);
        menu.getItem(7).setTitle(pasw.equals("") ? R.string.action_lock : R.string.action_unlock);

        // SHARE
        menu.getItem(8).setVisible(r!= null);

        // ARCHIVE
        menu.getItem(9).setVisible(r == null || r.getDate_archived() == 0);
        menu.getItem(9).setVisible(r!= null && r.getDate_archived() == 0);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && SP.getString(Costants.PREFERENCE_STYLE_POPUP, Costants.PREFERENCE_STYLE_POPUP_DEFAULT).equals(Costants.PREFERENCE_STYLE_POPUP_MD)) {
            menu.getItem(0).getIcon().setTint(ContextCompat.getColor(this, android.R.color.white));
            menu.getItem(1).getIcon().setTint(ContextCompat.getColor(this, android.R.color.white));
            menu.getItem(2).getIcon().setTint(ContextCompat.getColor(this, android.R.color.white));
            menu.getItem(3).getIcon().setTint(ContextCompat.getColor(this, android.R.color.white));
            menu.getItem(4).getIcon().setTint(ContextCompat.getColor(this, android.R.color.white));
            menu.getItem(5).getIcon().setTint(ContextCompat.getColor(this, android.R.color.white));
            menu.getItem(6).getIcon().setTint(ContextCompat.getColor(this, android.R.color.white));
            menu.getItem(7).getIcon().setTint(ContextCompat.getColor(this, android.R.color.white));
            menu.getItem(8).getIcon().setTint(ContextCompat.getColor(this, android.R.color.white));
            menu.getItem(9).getIcon().setTint(ContextCompat.getColor(this, android.R.color.white));
        }

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_attach:
                setAttachSnackbar();
                break;
            case R.id.action_reminder:
                setRemindersSnackbar();
                break;
            case R.id.action_list:
                switchContent();
                break;
            case R.id.action_priority:
                togglePriority();
                break;
            case R.id.action_unarchive:
                r.setDate_archived(0);
                invalidateOptionsMenu();
                break;
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
                break;
            case R.id.action_lock:
                if (pasw.equals("")) {
                    setPasw();
                } else {
                    pasw = "";
                    invalidateOptionsMenu();
                }
                break;
            case R.id.action_share:
                Intent share_intent = new Intent(Intent.ACTION_SEND);
                share_intent.putExtra(Intent.EXTRA_TEXT, r.getTitle() + "\n" + Utils.getBigContentList(MyDialog.this, r.getContent()));
                share_intent.setType("text/plain");
                startActivity(share_intent);
                break;
            case R.id.action_show_info:
                showInfo();
                break;
            case R.id.action_archive:
                ReminderService.startAction(this, Costants.ACTION_ARCHIVE, r);
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
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
            if (data != null && !tmpImgNameFile.equals("")) {
                addImg(tmpImgNameFile);
                tmpImgNameFile = "";
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (snackbar_attach != null && snackbar_attach.isShown()) {
            snackbar_attach.dismiss();
        } else if (snackbar_reminders != null && snackbar_reminders.isShown()) {
            snackbar_reminders.dismiss();
        } else if (snackbar_suggestions != null && snackbar_suggestions.isShown()) {
            snackbar_suggestions.dismiss();
        } else {
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
            outState.putString(Costants.KEY_DIALOG_ADDRESS, address);
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
        if (action.equals("")) {
            String[] action_result = Utils.checkAction(titleN + " " + Utils.getBigContentList(this, contentT));
            if (!action_result[0].equals("")) {
                action = action_result[0];
                action_info = action_result[1];
            }
        }
        if (!edit) {
            Calendar c = Calendar.getInstance();
            long dateC = c.getTimeInMillis();
            r = new Reminder(titleN, contentT, action, action_info, img, pasw, dateC, 0, 0, 0, alarm, alarm_repeat, address, priority, voice_note, "", "", "");
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
            r.setAddress(address);
            r.setPriority(priority);
            r.setVoice_note(voice_note);
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
            r_snooze.setDate_reminded(0);
            r_snooze.setAlarm(alarm);
            r_snooze.setAlarm_repeat(alarm_repeat);
            ReminderService.startAction(this, Costants.ACTION_UPDATE, r_snooze);
            finish();
        } else {
            this.alarm = alarm;
            this.alarm_repeat = alarm_repeat;
            if (alarm != 0 && r != null) {
                    r.setDate_reminded(0);
            }
            invalidateOptionsMenu();
        }
    }

    @Override
     public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
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
                        invalidateOptionsMenu();
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

    public void setAttachSnackbar() {
        final View attachView = LayoutInflater.from(this).inflate(R.layout.attach_dialog, null);
        LinearLayout action_camera = (LinearLayout) attachView.findViewById(R.id.action_camera);
        LinearLayout action_gallery = (LinearLayout) attachView.findViewById(R.id.action_gallery);
        LinearLayout action_contact = (LinearLayout) attachView.findViewById(R.id.action_contact);
        LinearLayout action_address = (LinearLayout) attachView.findViewById(R.id.action_place);

        snackbar_attach = Snackbar.make(findViewById(R.id.back_to_dismiss), "", Snackbar.LENGTH_INDEFINITE);
        Snackbar.SnackbarLayout layout = (Snackbar.SnackbarLayout) snackbar_attach.getView();
        layout.setBackgroundColor(ContextCompat.getColor(this, R.color.background_material_light));
        TextView textView = (TextView) layout.findViewById(android.support.design.R.id.snackbar_text);
        textView.setVisibility(View.INVISIBLE);

        attachView.findViewById(R.id.action_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar_attach.dismiss();
            }
        });

        if (!action.equals(""))
            attachView.findViewById(R.id.replace_contact).setVisibility(View.VISIBLE);
        else
            attachView.findViewById(R.id.replace_contact).setVisibility(View.GONE);

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
                                        snackbar_attach.dismiss();
                                    }
                                })
                                .setNegativeButton(android.R.string.no, null).show();
                    } else {
                        startActivityForResult(contact_intent, Costants.CODE_REQUEST_CONTACT);
                        snackbar_attach.dismiss();
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
                            tmpImgNameFile = Uri.fromFile(new File(image.getAbsolutePath())).toString();
                            snackbar_attach.dismiss();
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
                    snackbar_attach.dismiss();
                } else {
                    requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
                }
            }
        });

        action_address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editAddress();
                snackbar_attach.dismiss();
            }
        });

        layout.addView(attachView, 0);
        snackbar_attach.show();
    }

    public void setRemindersSnackbar() {
        final View remindersView = LayoutInflater.from(this).inflate(R.layout.reminder_types_dialog, null);
        LinearLayout action_date = (LinearLayout) remindersView.findViewById(R.id.action_date);
        LinearLayout action_wifi = (LinearLayout) remindersView.findViewById(R.id.action_wifi);
        LinearLayout action_bluetooth = (LinearLayout) remindersView.findViewById(R.id.action_bluetooth);
        TextView actual_reminder = (TextView) remindersView.findViewById(R.id.actual_reminder);
        ImageView action_remove = (ImageView) remindersView.findViewById(R.id.action_remove);
        LinearLayout container_actual_reminder = (LinearLayout) remindersView.findViewById(R.id.container_actual_reminder);

        if (alarm != 0) {
            actual_reminder.setText(Utils.getAlarm(this, alarm, alarm_repeat, 0));
            action_remove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(MyDialog.this)
                            .setTitle(getResources().getString(R.string.attention))
                            .setMessage(getResources().getString(R.string.ask_delete_alarm) + "?")
                            .setPositiveButton(R.string.action_remove, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    setAlarm(0, "");
                                    snackbar_reminders.dismiss();
                                }
                            })
                            .setNegativeButton(android.R.string.cancel, null).show();
                }
            });
            container_actual_reminder.setVisibility(View.VISIBLE);
        } else {
            container_actual_reminder.setVisibility(View.GONE);
        }

        snackbar_reminders = Snackbar.make(findViewById(R.id.back_to_dismiss), "", Snackbar.LENGTH_INDEFINITE);
        Snackbar.SnackbarLayout layout = (Snackbar.SnackbarLayout) snackbar_reminders.getView();
        layout.setBackgroundColor(ContextCompat.getColor(this, R.color.background_material_light));
        TextView textView = (TextView) layout.findViewById(android.support.design.R.id.snackbar_text);
        textView.setVisibility(View.INVISIBLE);

        remindersView.findViewById(R.id.action_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar_reminders.dismiss();
            }
        });

        action_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar_reminders.dismiss();
                ReminderDialog r_dialog = new ReminderDialog(MyDialog.this, alarm, alarm_repeat);
                r_dialog.show();
            }
        });

        action_bluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar_reminders.dismiss();
                bluetoothReminder();
            }
        });

        action_wifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar_reminders.dismiss();
                wifiReminder();
            }
        });


        layout.addView(remindersView, 0);
        snackbar_reminders.show();
    }

    public void setSuggestionsSnackbar() {
        final String t = title.getText().toString();
        if (t.contains("@")) {
            if (ContextCompat.checkSelfPermission(MyDialog.this,
                    Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED && action.equals("")) {
                final View attachView = LayoutInflater.from(this).inflate(R.layout.item_suggestion, null);

                if (snackbar_suggestions != null && snackbar_suggestions.isShown()) {
                    snackbar_suggestions.dismiss();
                }

                snackbar_suggestions = Snackbar.make(findViewById(R.id.back_to_dismiss), "", Snackbar.LENGTH_INDEFINITE);
                Snackbar.SnackbarLayout layout = (Snackbar.SnackbarLayout) snackbar_suggestions.getView();
                layout.setBackgroundColor(ContextCompat.getColor(this, R.color.background_material_light));
                TextView textView = (TextView) layout.findViewById(android.support.design.R.id.snackbar_text);
                textView.setVisibility(View.INVISIBLE);

                TextView name_one = (TextView) attachView.findViewById(R.id.name_one);
                TextView name_two = (TextView) attachView.findViewById(R.id.name_two);
                TextView name_three = (TextView) attachView.findViewById(R.id.name_three);

                String toCheck = t.substring(t.lastIndexOf("@"), t.length());
                final ArrayList<String[]> contactList = Utils.getContactsList(this, toCheck);
                if (contactList.size() > 0) {
                    name_one.setText(contactList.get(0)[1]);
                    name_one.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            action = Costants.ACTION_CONTACT;
                            action_info = contactList.get(0)[0];
                            setContact();
                            title.setText(new StringBuilder(t).replace(t.lastIndexOf("@"), t.length(), contactList.get(0)[1]).toString());
                            snackbar_suggestions.dismiss();
                        }
                    });

                    if (contactList.size() > 1) {
                        name_two.setText(contactList.get(1)[1]);
                        name_two.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                action = Costants.ACTION_CONTACT;
                                action_info = contactList.get(1)[0];
                                setContact();
                                title.setText(new StringBuilder(t).replace(t.lastIndexOf("@"), t.length(), contactList.get(1)[1]).toString());
                                snackbar_suggestions.dismiss();
                            }
                        });
                        name_two.setVisibility(View.VISIBLE);
                    } else {
                        name_two.setVisibility(View.GONE);
                    }

                    if (contactList.size() > 2) {
                        name_three.setText(contactList.get(2)[1]);
                        name_three.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                action = Costants.ACTION_CONTACT;
                                action_info = contactList.get(2)[0];
                                setContact();
                                title.setText(new StringBuilder(t).replace(t.lastIndexOf("@"), t.length(), contactList.get(2)[1]).toString());
                                snackbar_suggestions.dismiss();
                            }
                        });
                        name_three.setVisibility(View.VISIBLE);
                    } else {
                        name_three.setVisibility(View.GONE);
                    }


                    layout.addView(attachView, 0);
                    snackbar_suggestions.show();
                } else {
                    if (snackbar_suggestions.isShown())
                        snackbar_suggestions.dismiss();
                }
            } else {
                if (snackbar_suggestions != null && snackbar_suggestions.isShown()) {
                    snackbar_suggestions.dismiss();
                }
            }
        }
        /* TODO Date with / and hour with :
        if (t.contains("")) {
            if (ContextCompat.checkSelfPermission(MyDialog.this,
                    Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED && action.equals("")) {
                final View attachView = LayoutInflater.from(this).inflate(R.layout.item_suggestion, null);

                if (snackbar_suggestions != null && snackbar_suggestions.isShown()) {
                    snackbar_suggestions.dismiss();
                }

                snackbar_suggestions = Snackbar.make(findViewById(R.id.back_to_dismiss), "", Snackbar.LENGTH_INDEFINITE);
                Snackbar.SnackbarLayout layout = (Snackbar.SnackbarLayout) snackbar_suggestions.getView();
                layout.setBackgroundColor(ContextCompat.getColor(this, R.color.background_material_light));
                TextView textView = (TextView) layout.findViewById(android.support.design.R.id.snackbar_text);
                textView.setVisibility(View.INVISIBLE);

                TextView name_one = (TextView) attachView.findViewById(R.id.name_one);
                TextView name_two = (TextView) attachView.findViewById(R.id.name_two);
                TextView name_three = (TextView) attachView.findViewById(R.id.name_three);

                String toCheck = t.substring(t.lastIndexOf("@"), t.length());
                final ArrayList<String[]> contactList = Utils.getContactsList(this, toCheck);
                if (contactList.size() > 0) {
                    name_one.setText(contactList.get(0)[1]);
                    name_one.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            action = Costants.ACTION_CONTACT;
                            action_info = contactList.get(0)[0];
                            setContact();
                            title.setText(new StringBuilder(t).replace(t.lastIndexOf("@"), t.length(), contactList.get(0)[1]).toString());
                            snackbar_suggestions.dismiss();
                        }
                    });

                    if (contactList.size() > 1) {
                        name_two.setText(contactList.get(1)[1]);
                        name_two.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                action = Costants.ACTION_CONTACT;
                                action_info = contactList.get(1)[0];
                                setContact();
                                title.setText(new StringBuilder(t).replace(t.lastIndexOf("@"), t.length(), contactList.get(1)[1]).toString());
                                snackbar_suggestions.dismiss();
                            }
                        });
                        name_two.setVisibility(View.VISIBLE);
                    } else {
                        name_two.setVisibility(View.GONE);
                    }

                    if (contactList.size() > 2) {
                        name_three.setText(contactList.get(2)[1]);
                        name_three.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                action = Costants.ACTION_CONTACT;
                                action_info = contactList.get(2)[0];
                                setContact();
                                title.setText(new StringBuilder(t).replace(t.lastIndexOf("@"), t.length(), contactList.get(2)[1]).toString());
                                snackbar_suggestions.dismiss();
                            }
                        });
                        name_three.setVisibility(View.VISIBLE);
                    } else {
                        name_three.setVisibility(View.GONE);
                    }


                    layout.addView(attachView, 0);
                    snackbar_suggestions.show();
                } else {
                    if (snackbar_suggestions.isShown())
                        snackbar_suggestions.dismiss();
                }
            } else {
                if (snackbar_suggestions != null && snackbar_suggestions.isShown()) {
                    snackbar_suggestions.dismiss();
                }
            }
        }*/
    }

    public void showInfo() {
        final View infoView = LayoutInflater.from(this).inflate(R.layout.info_dialog, null);
        ((TextView) infoView.findViewById(R.id.creation_date)).setText(Utils.getDay(this, r.getDate_create()));
        ((TextView) infoView.findViewById(R.id.reminded_date)).setText(Utils.getDay(this, r.getDate_reminded()));
        ((TextView) infoView.findViewById(R.id.last_changed)).setText(Utils.getDay(this, r.getLast_changed()));
        ((TextView) infoView.findViewById(R.id.archived)).setText(Utils.getDay(this, r.getDate_archived()));
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
            Utils.collapse(contact_card);
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
                                if (SP.getBoolean(Costants.PREFERENCE_CLOSE_NOTE_AFTER_ACTIONS, true))
                                    finish();
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
                                if (SP.getBoolean(Costants.PREFERENCE_CLOSE_NOTE_AFTER_ACTIONS, true))
                                    finish();
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
                                if (SP.getBoolean(Costants.PREFERENCE_CLOSE_NOTE_AFTER_ACTIONS, true))
                                    finish();
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
                                    if (SP.getBoolean(Costants.PREFERENCE_CLOSE_NOTE_AFTER_ACTIONS, true))
                                        finish();
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
                Utils.expand(contact_card);
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
                            if (mAdapter != null) {
                                ((EditText) findViewById(R.id.text_add_to_list)).setOnEditorActionListener(new TextView.OnEditorActionListener() {
                                    @Override
                                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                                        if (actionId == EditorInfo.IME_ACTION_GO) {
                                            mAdapter.addElement(v);
                                            v.requestFocus();
                                        }
                                        return false;
                                    }
                                });
                            }
                            updateHeight();
                        }
                    });
                }
            }).start();
        }

        content.setVisibility(!list ? View.VISIBLE : View.GONE);
        content_list.setVisibility(list ? View.VISIBLE : View.GONE);
        action_add_to_list.setVisibility(list ? View.VISIBLE : View.GONE);
        invalidateOptionsMenu();
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
        invalidateOptionsMenu();
    }

    public void togglePriority() {
        setPriority(priority == 1 ? 0 : 1);
    }

    public void updateUrl() {
        String url_from_title = Utils.checkURL(title.getText().toString());
        String url_from_content = Utils.checkURL(Utils.getBigContentList(this, getContent()));

        String toGo = url_from_title;
        if (toGo.equals(""))
            toGo = url_from_content;

        final String url_toGo = toGo;
        if (!url_toGo.equals("")) {
            Utils.expand(url_card);
            ((TextView) findViewById(R.id.title_url_to_open)).setText(url_toGo);
            url_card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent url_intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url_toGo));
                    startActivity(url_intent);
                    if (SP.getBoolean(Costants.PREFERENCE_CLOSE_NOTE_AFTER_ACTIONS, true))
                        finish();
                }
            });
        } else {
            Utils.collapse(url_card);
        }
    }

    public void updateAddress(String new_add) {
        address = new_add;
        if (!address.equals("")) {
            ((TextView) findViewById(R.id.title_address)).setText(address);
            address_card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent address_intent = new Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=" + address));
                    startActivity(address_intent);
                    if (SP.getBoolean(Costants.PREFERENCE_CLOSE_NOTE_AFTER_ACTIONS, true))
                        finish();
                }
            });
            findViewById(R.id.edit_address).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editAddress();
                }
            });
            Utils.expand(address_card);
        } else {
            Utils.collapse(address_card);
        }
    }

    public void editAddress() {
        final Dialog addressDialog = new Dialog(this, R.style.mDialog);
        final View addressView = LayoutInflater.from(this).inflate(R.layout.address_dialog, null);
        final EditText address_text = (EditText) addressView.findViewById(R.id.address);
        final TextView action_remove = (TextView) addressView.findViewById(R.id.action_remove);
        final TextView action_save = (TextView) addressView.findViewById(R.id.action_save);
        address_text.setText(address);

        action_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Handler mHandler = new Handler();

                new Thread(new Runnable() {
                    public void run() {
                        final Address postal_address = Utils.getLocationFromAddress(MyDialog.this, address_text.getText().toString());
                        mHandler.post(new Runnable() {
                            public void run() {
                                try {
                                    if (postal_address == null) {
                                        Utils.SnackbarC(MyDialog.this, getString(R.string.error_invalid_address), address_text);
                                    } else {
                                        updateAddress(postal_address.getAddressLine(0) + ", " + postal_address.getAddressLine(1) + ", " + postal_address.getAddressLine(2));
                                        addressDialog.dismiss();
                                    }
                                } catch (Exception e) {
                                    Utils.SnackbarC(MyDialog.this, getString(R.string.error), address_text);
                                }
                            }
                        });
                    }
                }).start();
            }
        });

        if (!address.equals("")) {
            action_remove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(MyDialog.this)
                            .setTitle(getResources().getString(R.string.attention))
                            .setMessage(getResources().getString(R.string.ask_remove_address) + "?")
                            .setPositiveButton(R.string.action_remove, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    updateAddress("");
                                    addressDialog.dismiss();
                                }
                            })
                            .setNegativeButton(android.R.string.cancel, null)
                            .show();
                }
            });
            action_remove.setVisibility(View.VISIBLE);
        } else {
            action_remove.setVisibility(View.GONE);
        }

        addressDialog.setContentView(addressView);
        addressDialog.show();
    }

    public void bluetoothReminder() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
            AlertDialog.Builder choose_B = new AlertDialog.Builder(this, R.style.mDialog);
            choose_B.setTitle(getResources().getString(R.string.action_choose_device));
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

            if (pairedDevices.size() > 0) {
                if (!SP.contains(Costants.PREFERENCES_DEVICE_ACTIVE_BLUETOOTH)) {
                    SharedPreferences.Editor editor = SP.edit();
                    String toPut = "";
                    for (BluetoothDevice device : pairedDevices) {
                        if (toPut.equals(""))
                            toPut = device.getAddress();
                        else
                            toPut = toPut + Costants.LIST_ITEM_SEPARATOR + device.getAddress();
                    }
                    editor.putString(Costants.PREFERENCES_DEVICE_ACTIVE_BLUETOOTH, toPut);
                    editor.apply();
                }

                try {
                    String[] deviceAct = SP.getString(Costants.PREFERENCES_DEVICE_ACTIVE_BLUETOOTH, "").split(Costants.LIST_ITEM_SEPARATOR);
                    if (deviceAct.length > 0) {

                        final String[] blDv = new String[deviceAct.length];
                        final String[] blDvMAC = new String[deviceAct.length];
                        int f = 0;
                        for (BluetoothDevice device : pairedDevices) {

                            for (String s : deviceAct) {
                                if (device.getAddress().equals(s)) {
                                    blDv[f] = device.getName();
                                    blDvMAC[f] = device.getAddress();
                                    f++;
                                }
                            }
                        }

                        if (blDv.length == 1) {
                            setAlarm(Costants.ALARM_TYPE_BLUETOOTH, alarm_repeat = blDvMAC[0] + Costants.LIST_ITEM_SEPARATOR + blDv[0]);
                        } else {

                            choose_B.setSingleChoiceItems(blDv, 0, null)
                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            dialog.dismiss();
                                            int selectedPosition = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                                            setAlarm(Costants.ALARM_TYPE_BLUETOOTH, alarm_repeat = blDvMAC[selectedPosition] + Costants.LIST_ITEM_SEPARATOR + blDv[selectedPosition]);
                                        }
                                    })
                                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });
                            choose_B.show();
                        }
                    } else {
                        Toast.makeText(this, getResources().getString(R.string.error_no_activated_device), Toast.LENGTH_SHORT).show();
                    }
                } catch (NullPointerException ex) {
                    Toast.makeText(this, getResources().getString(R.string.error), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, getResources().getString(R.string.error_no_paired_devices), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, getResources().getString(R.string.error_bluetooth_off), Toast.LENGTH_SHORT).show();
        }
    }

    public void wifiReminder() {
        WifiManager wifiM = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        if (wifiM != null && wifiM.isWifiEnabled()) {
            AlertDialog.Builder choose_W = new AlertDialog.Builder(this, R.style.mDialog);
            choose_W.setTitle(getResources().getString(R.string.action_choose_wifi));
            List<WifiConfiguration> wifiList = wifiM.getConfiguredNetworks();

            if (wifiList.size() > 0) {
                SharedPreferences SP = getSharedPreferences(Costants.PREFERENCES_COSTANT, Context.MODE_PRIVATE);
                if (!SP.contains(Costants.PREFERENCES_DEVICE_ACTIVE_WIFI)) {
                    SharedPreferences.Editor editor = SP.edit();
                    String toPut = "";
                    for (WifiConfiguration connection : wifiList) {
                        if (toPut.equals(""))
                            toPut = "" + connection.networkId;
                        else
                            toPut = toPut + Costants.LIST_ITEM_SEPARATOR + connection.networkId;
                    }
                    editor.putString(Costants.PREFERENCES_DEVICE_ACTIVE_WIFI, toPut);
                    editor.apply();
                }

                try {
                    String[] wifiAct = SP.getString(Costants.PREFERENCES_DEVICE_ACTIVE_WIFI, "").split(Costants.LIST_ITEM_SEPARATOR);
                    if (wifiAct.length > 0) {
                        final String[] wifiDv = new String[wifiAct.length];
                        final String[] wifiMAC = new String[wifiAct.length];
                        int f = 0;
                        for (WifiConfiguration connection : wifiList) {

                            for (String s : wifiAct) {
                                if (("" + connection.networkId).equals(s)) {
                                    wifiDv[f] = connection.SSID.replace("\"", "");
                                    wifiMAC[f] = "" + connection.networkId;
                                    f++;
                                }
                            }
                        }

                        if (wifiDv.length == 1) {
                            setAlarm(Costants.ALARM_TYPE_WIFI, wifiMAC[0] + Costants.LIST_ITEM_SEPARATOR + wifiDv[0]);
                        } else {
                            choose_W.setSingleChoiceItems(wifiDv, 0, null)
                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            dialog.dismiss();
                                            int selectedPosition = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                                            setAlarm(Costants.ALARM_TYPE_WIFI, alarm_repeat = wifiMAC[selectedPosition] + Costants.LIST_ITEM_SEPARATOR + wifiDv[selectedPosition]);
                                        }
                                    })
                                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });
                            choose_W.show();
                        }
                    } else  {
                        Toast.makeText(this, getResources().getString(R.string.error_no_activated_wifi), Toast.LENGTH_SHORT).show();
                    }
                } catch (NullPointerException ex) {
                    Toast.makeText(this, getResources().getString(R.string.error), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, getResources().getString(R.string.error_no_wifi_saved), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, getResources().getString(R.string.error_wifi_off), Toast.LENGTH_SHORT).show();
        }
    }
}
