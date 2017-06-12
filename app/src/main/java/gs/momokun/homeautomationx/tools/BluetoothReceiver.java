package gs.momokun.homeautomationx.tools;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.greenrobot.eventbus.EventBus;

import gs.momokun.homeautomationx.BaseApplication;
import gs.momokun.homeautomationx.tools.ArduinoStateOnReceived;

public class BluetoothReceiver extends BroadcastReceiver {

    String action = "null";

    public static Context getContext() {
        return BaseApplication.getContext();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        action = intent.getAction();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            switch (action) {
                case BluetoothDevice.ACTION_ACL_CONNECTED:
                    EventBus.getDefault().post(new ArduinoStateOnReceived(1));
                    sp.edit().putBoolean("isConnected",true).apply();
                  //  ((BaseApplication) getContext().getApplicationContext()).setArduinoConnected(true);

                    break;
                case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                    EventBus.getDefault().post(new ArduinoStateOnReceived(0));
                    //((BaseApplication) getContext().getApplicationContext()).setArduinoConnected(false);
                    sp.edit().putBoolean("isConnected",false).apply();
                    break;
            }
        }
}
