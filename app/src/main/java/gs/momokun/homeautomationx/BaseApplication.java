package gs.momokun.homeautomationx;

import android.app.Application;
import android.bluetooth.BluetoothSocket;
import android.content.Context;

/**
 * Created by ElmoTan on 1/18/2017.
 */

public class BaseApplication extends Application {

    public boolean isArduinoConnected;
    private static Context appContext;

    public boolean getArduinoStatus() {
        return isArduinoConnected;
    }

    public void setArduinoConnected(boolean arduinoStatus) {
        this.isArduinoConnected = isArduinoConnected;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = getApplicationContext();
    }

    public static Context getContext() {
        return appContext;
    }

}
