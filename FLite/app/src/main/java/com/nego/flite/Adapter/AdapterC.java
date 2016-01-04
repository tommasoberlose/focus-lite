package com.nego.flite.Adapter;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nego.flite.ActiveDevice;
import com.nego.flite.Costants;
import com.nego.flite.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AdapterC extends RecyclerView.Adapter<AdapterC.ViewHolder> {
    private List<BluetoothDevice> mDatasetB = new ArrayList<>();
    private List<WifiConfiguration> mDatasetW = new ArrayList<>();
    private List<Boolean> mDatasetC = new ArrayList<>();
    private Context mContext;

    private int what = Costants.ALARM_TYPE_BLUETOOTH;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout v;
        public TextView title;
        public CheckBox checkBox;

        public ViewHolder(LinearLayout v, TextView title, CheckBox checkBox) {
            super(v);
            this.v = v;
            this.title = title;
            this.checkBox = checkBox;
        }

    }

    public AdapterC(int what, Context mContext) {
        this.what = what;
        this.mContext = mContext;
        generate_list();
    }

    @Override
    public AdapterC.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                  int viewType) {

        ViewHolder vh;
        View v;

        v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_choice, parent, false);

        vh = new ViewHolder((LinearLayout) v, (TextView) v.findViewById(R.id.title), (CheckBox) v.findViewById(R.id.check));
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {


        // TITLE
        TextView title = holder.title;
        title.setText(getItemName(position));

        final CheckBox check = holder.checkBox;
        check.setChecked(mDatasetC.get(position));

        holder.v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                check.setChecked(!check.isChecked());
                mDatasetC.set(position, check.isChecked());
                save_preference();
            }
        });

        check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDatasetC.set(position, check.isChecked());
                save_preference();
            }
        });

    }

    @Override
    public int getItemCount() {
        if (what == Costants.ALARM_TYPE_BLUETOOTH)
            return mDatasetB.size();
        else
            return mDatasetW.size();
    }

    public String getItemName(int pos) {
        if (what == Costants.ALARM_TYPE_BLUETOOTH)
            return mDatasetB.get(pos).getName();
        else
            return mDatasetW.get(pos).SSID.replace("\"", "");
    }

    // GENERATE LIST
    public void generate_list() {
        if (what == Costants.ALARM_TYPE_BLUETOOTH) {
            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
                Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
                if (pairedDevices.size() > 0) {
                    SharedPreferences SP = mContext.getSharedPreferences(Costants.PREFERENCES_COSTANT, Context.MODE_PRIVATE);
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

                    String[] deviceAct = SP.getString(Costants.PREFERENCES_DEVICE_ACTIVE_BLUETOOTH, "").split(Costants.LIST_ITEM_SEPARATOR);
                    for (BluetoothDevice device : pairedDevices) {
                        mDatasetB.add(device);
                        int k;
                        for (k = 0; k < deviceAct.length; k++) {
                            if (device.getAddress().equals(deviceAct[k])) {
                                mDatasetC.add(true);
                                break;
                            }
                        }
                        if (k == deviceAct.length)
                            mDatasetC.add(false);
                    }
                } else {
                    Toast.makeText(mContext, mContext.getString(R.string.error_no_paired_devices), Toast.LENGTH_SHORT).show();
                    ((ActiveDevice) mContext).finish();
                }
            } else {
                Toast.makeText(mContext, mContext.getString(R.string.error_bluetooth_off), Toast.LENGTH_SHORT).show();
                ((ActiveDevice) mContext).finish();
            }

        } else {
            WifiManager wifiM = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
                    if (wifiM != null && wifiM.isWifiEnabled()) {
                List<WifiConfiguration> wifiList = wifiM.getConfiguredNetworks();
                if (wifiList.size() > 0) {
                    SharedPreferences SP = mContext.getSharedPreferences(Costants.PREFERENCES_COSTANT, Context.MODE_PRIVATE);
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

                    String[] wifiAct = SP.getString(Costants.PREFERENCES_DEVICE_ACTIVE_WIFI, "").split(Costants.LIST_ITEM_SEPARATOR);
                    for (WifiConfiguration connection : wifiList) {
                        mDatasetW.add(connection);
                        int k;
                        for (k = 0; k < wifiAct.length; k++) {
                            if (("" + connection.networkId).equals(wifiAct[k])) {
                                mDatasetC.add(true);
                                break;
                            }
                        }
                        if (k == wifiAct.length)
                            mDatasetC.add(false);
                    }
                } else {
                    Toast.makeText(mContext, mContext.getString(R.string.error_no_wifi_saved), Toast.LENGTH_SHORT).show();
                    ((ActiveDevice) mContext).finish();
                }
            } else {
                Toast.makeText(mContext, mContext.getString(R.string.error_wifi_off), Toast.LENGTH_SHORT).show();
                ((ActiveDevice) mContext).finish();
            }
        }
    }

    public void save_preference() {
        SharedPreferences sharedPref = mContext.getSharedPreferences(Costants.PREFERENCES_COSTANT, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        if (what == Costants.ALARM_TYPE_BLUETOOTH) {
            String toPut = "";
            for (int k = 0; k< mDatasetB.size(); k++) {
                if (mDatasetC.get(k)) {
                    if (toPut.equals(""))
                        toPut = "" + mDatasetB.get(k).getAddress();
                    else
                        toPut = toPut + Costants.LIST_ITEM_SEPARATOR + mDatasetB.get(k).getAddress();
                }
            }
            editor.putString(Costants.PREFERENCES_DEVICE_ACTIVE_BLUETOOTH, toPut);
        } else {
            String toPut = "";
            for (int k = 0; k < mDatasetW.size(); k++) {
                if (mDatasetC.get(k)) {
                    if (toPut.equals(""))
                        toPut = "" + mDatasetW.get(k).networkId;
                    else
                        toPut = toPut + Costants.LIST_ITEM_SEPARATOR + mDatasetW.get(k).networkId;
                }
            }
            editor.putString(Costants.PREFERENCES_DEVICE_ACTIVE_WIFI, toPut);
        }
        editor.apply();
    }
}

