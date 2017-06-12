package gs.momokun.homeautomationx;

import android.app.Activity;
import android.app.Dialog;

import android.content.Context;
import android.content.DialogInterface;

import android.content.SharedPreferences;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.icu.util.TimeZone;

import android.preference.PreferenceManager;

import android.support.v7.app.AlertDialog;

import android.support.v7.view.ContextThemeWrapper;
import android.util.Log;

import android.view.View;

import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;

import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Date;

import gs.momokun.homeautomationx.tools.BluetoothDataService;


public class ElectronicConfigureAdapter {

    private Activity activity;
    private View view;
    private Context context;
    private int pos;

    private CheckBox cbTemp,cbLight,cbTimer;
    private TimePicker tpTimer;
    private DatePicker dpTimer;
    private SeekBar sbTemp, sbLight;
    private TextView showTempVal,showLightVal;

    private SharedPreferences sp;
    private boolean tempRealState, lightRealState, timerRealState;

    private String tempSPKey[] = {"tempKey1","tempKey2","tempKey3","tempKey4","tempKey5"};
    private String tempValueSPKey[] = {"tempVal1","tempVal2","tempVal3","tempVal4","tempVal5"};

    private String lightSPKey[] = {"lightKey1","lightKey2","lightKey3","lightKey4","lightKey5"};
    private String lightValueSPKey[] = {"lightVal1","lightVal2","lightVal3","lightVal4","lightVal5"};

    private String timerSPKey[] = {"timerKey1","timerKey2","timerKey3","timerKey4","timerKey5"};
    private String timerHValueSPKey[] = {"timerHVal1","timerHVal2","timerHVal3","timerHVal4","timerHVal5"};
    private String timerMValueSPKey[] = {"timerMVal1","timerVal2","timerVal3","timerMVal4","timerMVal5"};
    private String timerResult[] = {"timerRes1","timerRes2","timerRes3","timerRes4","timerRes5"};

    private String temporaryLight, temporaryTemp;

    private AlertDialog ad;

    private int valTemp, valLight;
    private int currentHour = 0;
    private int currentMinute = 0;



    public ElectronicConfigureAdapter(Activity activity,Context c, View view, int ePos) {
        this.activity = activity;
        this.view = view;
        this.pos = ePos;
        this.context = c;
    }

    public static Context getContext() {
        return BaseApplication.getContext();
    }

    private void declaration() {

        sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        cbTemp = (CheckBox) view.findViewById(R.id.enableTempDetect);
        sbTemp = (SeekBar) view.findViewById(R.id.sbTemp);
        showTempVal = (TextView) view.findViewById(R.id.showTempVal);

        cbLight = (CheckBox) view.findViewById(R.id.enableLightDetect);
        sbLight = (SeekBar) view.findViewById(R.id.sbLight);
        showLightVal = (TextView) view.findViewById(R.id.showLightVal);

        cbTimer = (CheckBox) view.findViewById(R.id.enableTimer);
        tpTimer = (TimePicker) view.findViewById(R.id.timerValue);
        dpTimer = (DatePicker) view.findViewById(R.id.dateValue);

        temporaryTemp = sp.getString(tempValueSPKey[pos], "20");

        cbTemp.setChecked(sp.getBoolean(tempSPKey[pos], false));
        sbTemp.setEnabled(sp.getBoolean(tempSPKey[pos], false));
        sbTemp.setMax(15);


        if (temporaryTemp.equals("")) {
            sbTemp.setProgress(0);
            showTempVal.setText("15");
        }else {
            sbTemp.setProgress(Integer.parseInt(temporaryTemp) - 15);
            showTempVal.setText(temporaryTemp);
        }

        temporaryLight = sp.getString(lightValueSPKey[pos], "0");

        cbLight.setChecked(sp.getBoolean(lightSPKey[pos], false));
        sbLight.setEnabled(sp.getBoolean(lightSPKey[pos], false));
        sbLight.setMax(99);

        if(temporaryLight.equals("")){
            sbLight.setProgress(0);
            showLightVal.setText("0");
        }else{
            sbLight.setProgress(Integer.parseInt(temporaryLight));
            showLightVal.setText(temporaryLight);
        }

        cbTimer.setChecked(sp.getBoolean(timerSPKey[pos],false));
        tpTimer.setEnabled(sp.getBoolean(timerSPKey[pos],false));
        dpTimer.setEnabled(sp.getBoolean(timerSPKey[pos],false));

        currentHour = sp.getInt(timerHValueSPKey[pos],18);
        currentMinute = sp.getInt(timerMValueSPKey[pos],18);

        tpTimer.setIs24HourView(true);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            Calendar cal = Calendar.getInstance();
            tpTimer.setHour(currentHour);
            tpTimer.setMinute(currentMinute);
            dpTimer.updateDate(cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(Calendar.DAY_OF_MONTH));
        }else{
            java.util.Calendar calOld = java.util.Calendar.getInstance();
            tpTimer.setCurrentHour(currentHour);
            tpTimer.setCurrentMinute(currentMinute);
            dpTimer.updateDate(calOld.get(java.util.Calendar.YEAR),calOld.get(java.util.Calendar.MONTH),calOld.get(java.util.Calendar.DAY_OF_MONTH));
        }
    }

    private void setState(boolean temp, boolean light, boolean timer){
        tempRealState = temp;
        lightRealState = light;
        timerRealState = timer;
    }

    private void setConfigurationCondition(){
        if(pos==0 || pos ==1 || pos ==2){
            cbTemp.setEnabled(false);
            sbTemp.setEnabled(false);
            setState(sp.getBoolean(tempSPKey[pos],false),sp.getBoolean(lightSPKey[pos],false),sp.getBoolean(timerSPKey[pos],false));
        }else if(pos==3){
            cbLight.setEnabled(false);
            sbLight.setEnabled(false);
            setState(sp.getBoolean(tempSPKey[pos],false),sp.getBoolean(lightSPKey[pos],false),sp.getBoolean(timerSPKey[pos],false));
        }else{
            cbLight.setEnabled(false);
            sbLight.setEnabled(false);
            cbTemp.setEnabled(false);
            sbTemp.setEnabled(false);
            setState(sp.getBoolean(tempSPKey[pos],false),sp.getBoolean(lightSPKey[pos],false),sp.getBoolean(timerSPKey[pos],false));
        }
    }

    public void dialogBuilder() {
        declaration();
        setConfigurationCondition();



        cbTemp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cbTemp.isChecked()) {
                    setState(true,false,false);
                    sbTemp.setEnabled(tempRealState);
                    valTemp = Integer.parseInt(temporaryTemp);

                    cbTimer.setChecked(timerRealState);
                    tpTimer.setEnabled(timerRealState);
                    dpTimer.setEnabled(timerRealState);
                }else{
                    setState(false,false,false);
                    sbTemp.setEnabled(tempRealState);
                    valTemp = 0;
                }
            }
        });

        cbLight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cbLight.isChecked()) {
                    setState(false,true,false);
                    sbLight.setEnabled(lightRealState);
                    valLight = Integer.parseInt(temporaryLight);

                    cbTimer.setChecked(timerRealState);
                    tpTimer.setEnabled(timerRealState);
                    dpTimer.setEnabled(timerRealState);
                } else {
                    setState(false,false,false);
                    sbLight.setEnabled(false);
                    valLight = 0;
                }
            }
        });

        cbTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cbTimer.isChecked()) {
                    setState(false,false,true);

                    cbTemp.setChecked(false);
                    cbLight.setChecked(false);

                    sbTemp.setEnabled(false);
                    sbLight.setEnabled(false);
                    sbTemp.setProgress(0);
                    sbLight.setProgress(0);

                    dpTimer.setEnabled(timerRealState);
                    tpTimer.setEnabled(timerRealState);
                } else {
                    setState(false,false,false);
                    dpTimer.setEnabled(timerRealState);
                    tpTimer.setEnabled(timerRealState);
                }
            }
        });



        sbTemp.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                int value = 15 + (i);
                showTempVal.setText(Integer.toString(value));

                if(value==0){
                    valTemp = Integer.parseInt(temporaryTemp);
                }else valTemp = value;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        sbLight.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                valLight = i;
                showLightVal.setText(Integer.toString(valLight));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        AlertDialog.Builder adb = new AlertDialog.Builder(new ContextThemeWrapper(activity, android.R.style.Theme_Light_NoTitleBar_Fullscreen));

                adb.setView(view)
                .setCancelable(true)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ad.dismiss();
                    }
                });

        ad = adb.create();
        ad.show();

        Button ok = ad.getButton(DialogInterface.BUTTON_POSITIVE);
        ok.setOnClickListener(new CustomValidationListener(ad));


    }


    private MainActivity ma = new MainActivity();

    private class CustomValidationListener implements View.OnClickListener {
        private final Dialog DIALOG;

        private CustomValidationListener(Dialog dialog) {
            this.DIALOG = dialog;
        }

        @Override
        public void onClick(View v) {



            int stateTemp = -1;
            int stateLight;
            int stateTimer;
            Long timeValueinMillis;
            Long currentSystemTime = System.currentTimeMillis();

            //SDK Date Check
            //SDK 24
            Calendar cal;
            //SDK <24
            java.util.Calendar calOld;


            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                cal = Calendar.getInstance();
                cal.set(dpTimer.getYear(), dpTimer.getMonth(), dpTimer.getDayOfMonth(),
                        tpTimer.getHour(), tpTimer.getMinute(), 0);
                timeValueinMillis = cal.getTimeInMillis()+cal.getTimeZone().getOffset(cal.getTimeInMillis())/1000;
                currentHour = tpTimer.getHour();
                currentMinute = tpTimer.getMinute();
            }else{
                calOld = java.util.Calendar.getInstance();
                calOld.set(dpTimer.getYear(), dpTimer.getMonth(), dpTimer.getDayOfMonth(),
                        tpTimer.getCurrentHour(), tpTimer.getCurrentMinute(), 0);
                timeValueinMillis = calOld.getTimeInMillis()+calOld.getTimeZone().getOffset(calOld.getTimeInMillis())/1000;
                currentHour = tpTimer.getCurrentHour();
                currentMinute = tpTimer.getCurrentMinute();
            }

            Date df = new java.util.Date(timeValueinMillis);
            SimpleDateFormat sdf;
            java.text.SimpleDateFormat sdfOld;
            String convertedDate;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                sdf = new SimpleDateFormat("dd MM yyyy - HH:mm");
               // sdf.setTimeZone(TimeZone.getTimeZone("GMT+7"));
                convertedDate = sdf.format(df);
            }else{
                sdfOld = new java.text.SimpleDateFormat("yyyy-MM-dd - HH:mm:ss");
               // sdfOld.setTimeZone(java.util.TimeZone.getTimeZone("GMT+7"));
                convertedDate = sdfOld.format(df);
            }

            Log.v("TIMER",convertedDate);

            Long result = (timeValueinMillis-currentSystemTime)/1000;
            result = Math.abs(result-25);


            if (cbTemp.isChecked()) {
                lightRealState=false;
                timerRealState=false;
                if(valTemp==0){
                    valTemp = Integer.parseInt(temporaryTemp);
                }
                saveTemp(pos,tempRealState,valTemp);
                stateTemp = 1;
            }else if(!cbTemp.isChecked()){
                saveTemp(pos,tempRealState,0);
                stateTemp = 0;
                sbTemp.setProgress(0);
            }

            if(cbLight.isChecked()) {
                if(valLight==0){
                    valLight = Integer.parseInt(temporaryLight);
                }
                tempRealState=false;
                timerRealState=false;
                saveLight(pos,lightRealState,valLight);
                stateLight = 1;
            }else{
                saveLight(pos,lightRealState,0);
                stateLight = 0;
                sbLight.setProgress(0);
            }

            if(cbTimer.isChecked()) {
                tempRealState=false;
                lightRealState=false;
                if(timeValueinMillis>currentSystemTime){
                    saveTimer(timerRealState,currentHour,currentMinute,result);
                        stateTimer = 1;
                }else{
                    stateTimer=-1;
                    Toast.makeText(activity, "Time must be greater than now", Toast.LENGTH_SHORT).show();
                }
            }else{
                saveTimer(timerRealState,currentHour,currentMinute,-1);
                stateTimer = 0;
            }


            if((stateTemp==1 && stateLight==1 && stateTimer==1) || (stateTemp==0 && stateLight==0 && stateTimer==0)){
                ma.sendingConfiguration(activity,getContext(),pos);
                ad.dismiss();
            }else if(stateTemp==1 && stateLight==0 && stateTimer==0){ //temp only
                ma.sendingConfiguration(activity,getContext(),pos);
                ad.dismiss();
            }else if(stateTemp==1 && stateLight==1 && stateTimer==0){ //tmep & light
                ma.sendingConfiguration(activity,getContext(),pos);
                ad.dismiss();
            }else if(stateTemp==0 && stateLight==1 && stateTimer==0){ // light only
                ma.sendingConfiguration(activity,getContext(),pos);
                ad.dismiss();
            }else if(stateTemp==0 && stateLight==1 && stateTimer==1){ //timer & light
                ma.sendingConfiguration(activity,getContext(),pos);
                ad.dismiss();
            }else if(stateTemp==0 && stateLight==0 && stateTimer==1){ //timer only
                ma.sendingConfiguration(activity,getContext(),pos);
                ad.dismiss();
            }else if(stateTemp==1 && stateLight==0 && stateTimer==1){ //temp & timer
                ma.sendingConfiguration(activity,getContext(),pos);
                ad.dismiss();
            }

        }


    }

    private String posSPkey = "btnPos";
    private void saveTemp(int pos, boolean state, int value){
        sp.edit().putInt(posSPkey, pos)
                .putBoolean(tempSPKey[pos], state)
                .putString(tempValueSPKey[pos], String.valueOf(value))
                .apply();
    }

    private void saveLight(int pos, boolean state, int value){
        sp.edit().putInt(posSPkey, pos)
                .putBoolean(lightSPKey[pos], state)
                .putString(lightValueSPKey[pos], String.valueOf(value))
                .apply();
    }

    private void saveTimer(boolean state, int hour, int minute, long result){
        sp.edit().putBoolean(timerSPKey[pos],state)
                .putInt(timerHValueSPKey[pos],hour)
                .putInt(timerMValueSPKey[pos],minute)
                .putLong(timerResult[pos],result)
                .apply();
    }

}




