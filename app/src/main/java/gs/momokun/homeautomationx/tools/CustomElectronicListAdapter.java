package gs.momokun.homeautomationx.tools;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CompoundButton;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.HashMap;
import java.util.List;

import gs.momokun.homeautomationx.BaseApplication;
import gs.momokun.homeautomationx.ChangeElectronicNameAdapter;
import gs.momokun.homeautomationx.ElectronicConfigureAdapter;
import gs.momokun.homeautomationx.MainActivity;
import gs.momokun.homeautomationx.R;



public class CustomElectronicListAdapter extends BaseExpandableListAdapter {

    private Activity a;
    private Context c;
    private View homeView;
    private List<String> listHeader;
    private HashMap<String, List<String>> listComponent;
    private MainActivity ma = new MainActivity();
    private SharedPreferences sp;

    private boolean isConnected;

    //fordebug,disable
    private static final String TAG = "MOMODEBUG";

    private final ElectronicConfigureAdapter[] ECA = new ElectronicConfigureAdapter[1];
    private final ChangeElectronicNameAdapter[] CENA = new ChangeElectronicNameAdapter[1];

    public CustomElectronicListAdapter(View v, Context c,Activity a, List<String> listHeader, HashMap<String, List<String>> listComponent) {
        this.a = a;
        this.c = c;
        this.homeView = v;
        this.listHeader = listHeader;
        this.listComponent = listComponent;


    }

    public static Context getContext() {
        return BaseApplication.getContext();
    }


    @Override
    public int getGroupCount() {
        return this.listHeader.size();
    }

    @Override
    public int getChildrenCount(int childPos) {
        return this.listComponent.get(this.listHeader.get(childPos)).size();
    }

    @Override
    public Object getGroup(int parentPos) {
        return this.listHeader.get(parentPos);
    }

    @Override
    public Object getChild(int parentPos, int childPos) {
        return this.listComponent.get(this.listHeader.get(parentPos)).get(childPos);
    }

    @Override
    public long getGroupId(int parentPos) {
        return parentPos;
    }

    @Override
    public long getChildId(int parentPos, int childPos) {
        return childPos;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    private View viewConfigElec;

    @Override
    public View getGroupView(int parentPos, boolean b, View view, ViewGroup viewGroup) {
        String roomAreaTitle = (String) getGroup(parentPos);
        if(view == null){
            LayoutInflater li = (LayoutInflater) this.c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = li.inflate(R.layout.item_grouping,viewGroup,false);
            viewConfigElec = li.inflate(R.layout.configure_value,viewGroup,false);
        }

        final TextView LABEL_HEADER = (TextView) view.findViewById(R.id.lblListHeader);
        LinearLayout ll = (LinearLayout) view.findViewById(R.id.listHeaderLayout);
        LABEL_HEADER.setText(roomAreaTitle);
        ll.setBackgroundColor(0xFF1A237E);


        return view;
    }

    private String nameSPKey[] = {"electronicName1","electronicName2","electronicName3","electronicName4","electronicName5"};
    private String eStateSPKey[] = {"eState1","eState2","eState3","eState4","eState5"};

    //Bluetooth receiver for check device

    @Override
    public View getChildView(final int PARENT_POS, final int CHILD_POS, boolean b, View view, final ViewGroup viewGroup) {
        sp = PreferenceManager.getDefaultSharedPreferences(c);

        final String electronicTitle = (String) getChild(PARENT_POS,CHILD_POS);
        final String E_NAME_1 = sp.getString(nameSPKey[0],"Lamp");
        final String E_NAME_2 = sp.getString(nameSPKey[1],"Lamp");
        final String E_NAME_3 = sp.getString(nameSPKey[2],"Lamp");
        final String E_NAME_4 = sp.getString(nameSPKey[3],"Fan");
        final String E_NAME_5 = sp.getString(nameSPKey[4],"Electronic");

        final boolean E_STATE_1 = sp.getBoolean(eStateSPKey[0],false);
        final boolean E_STATE_2 = sp.getBoolean(eStateSPKey[1],false);
        final boolean E_STATE_3 = sp.getBoolean(eStateSPKey[2],false);
        final boolean E_STATE_4 = sp.getBoolean(eStateSPKey[3],false);
        final boolean E_STATE_5 = sp.getBoolean(eStateSPKey[4],false);

        LayoutInflater rootInfalter = null;
        if(view == null){
            rootInfalter = (LayoutInflater) this.c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = rootInfalter.inflate(R.layout.electronic_list_item,viewGroup,false);
        }


        final TextView titleElectronic = (TextView) view.findViewById(R.id.electronicItemCustom);
        ImageView icElectronic = (ImageView) view.findViewById(R.id.ic_electronic);
        ImageButton customizeElectronic = (ImageButton) view.findViewById(R.id.customizeElectronicCustom);
        ImageButton customizeName = (ImageButton) view.findViewById(R.id.changeNameCustom);
        final ToggleButton customizeToggleButton = (ToggleButton) view.findViewById(R.id.toggleButtonElectronicCustom);
        titleElectronic.setText(electronicTitle);
        customizeElectronic.setFocusable(false);

        isConnected = ((BaseApplication) getContext().getApplicationContext()).getArduinoStatus();
        isConnected = true;

            if (PARENT_POS == 0 && CHILD_POS == 0) {
                customizeToggleButton.setChecked(E_STATE_1);
                icElectronic.setImageResource(R.drawable.lamp);
                titleElectronic.setText(E_NAME_1);
            } else if (PARENT_POS == 1 && CHILD_POS == 0) {
                customizeToggleButton.setChecked(E_STATE_2);
                icElectronic.setImageResource(R.drawable.lamp);
                titleElectronic.setText(E_NAME_2);
            } else if (PARENT_POS == 2 && CHILD_POS == 0) {
                customizeToggleButton.setChecked(E_STATE_3);
                icElectronic.setImageResource(R.drawable.lamp);
                titleElectronic.setText(E_NAME_3);
            } else if (PARENT_POS == 2 && CHILD_POS == 1) {
                customizeToggleButton.setChecked(E_STATE_4);
                icElectronic.setImageResource(R.drawable.plug);
                titleElectronic.setText(E_NAME_4);
            } else if (PARENT_POS == 3 && CHILD_POS == 0) {
                customizeToggleButton.setChecked(E_STATE_5);
                icElectronic.setImageResource(R.drawable.plug);
                titleElectronic.setText(E_NAME_5);
            }




        final LayoutInflater ROOT_INFLATER = rootInfalter;
        customizeElectronic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater forceInflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                viewConfigElec = forceInflater.inflate(R.layout.configure_value, viewGroup,false);
                isConnected = ((BaseApplication) getContext().getApplicationContext()).getArduinoStatus();
                isConnected = true;
                if(isConnected) {
                    Log.v(TAG,"CALLED");
                    if (PARENT_POS == 0 && CHILD_POS == 0) {
                        ECA[0] = new ElectronicConfigureAdapter(a,c, viewConfigElec, 0);
                        ECA[0].dialogBuilder();
                    } else if (PARENT_POS == 1 && CHILD_POS == 0) {
                        ECA[0] = new ElectronicConfigureAdapter(a,c, viewConfigElec, 1);
                        ECA[0].dialogBuilder();
                    } else if (PARENT_POS == 2 && CHILD_POS == 0) {
                        ECA[0] = new ElectronicConfigureAdapter(a,c, viewConfigElec, 2);
                        ECA[0].dialogBuilder();
                    } else if (PARENT_POS == 2 && CHILD_POS == 1) {
                        ECA[0] = new ElectronicConfigureAdapter(a,c, viewConfigElec, 3);
                        ECA[0].dialogBuilder();
                    } else if (PARENT_POS == 3 && CHILD_POS == 0) {
                        ECA[0] = new ElectronicConfigureAdapter(a,c, viewConfigElec, 4);
                        ECA[0].dialogBuilder();
                    }
                }
            }
        });

        customizeName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater forceInflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View viewChangeName = forceInflater.inflate(R.layout.change_name_lamp_dialog_custom,null);
                    if (PARENT_POS == 0 && CHILD_POS == 0) {
                        CENA[0] = new ChangeElectronicNameAdapter(a,c, viewChangeName, 0);
                        CENA[0].dialogBuilder();
                        titleElectronic.setText(E_NAME_1);
                    } else if (PARENT_POS == 1 && CHILD_POS == 0) {
                        CENA[0] = new ChangeElectronicNameAdapter(a,c, viewChangeName, 1);
                        CENA[0].dialogBuilder();
                        titleElectronic.setText(E_NAME_2);
                    } else if (PARENT_POS == 2 && CHILD_POS == 0) {
                        CENA[0] = new ChangeElectronicNameAdapter(a,c, viewChangeName, 2);
                        CENA[0].dialogBuilder();
                        titleElectronic.setText(E_NAME_3);
                    } else if (PARENT_POS == 2 && CHILD_POS == 1) {
                        CENA[0] = new ChangeElectronicNameAdapter(a,c, viewChangeName, 3);
                        CENA[0].dialogBuilder();
                        titleElectronic.setText(E_NAME_4);
                    } else if (PARENT_POS == 3 && CHILD_POS == 0) {
                        CENA[0] = new ChangeElectronicNameAdapter(a,c, viewChangeName, 4);
                        CENA[0].dialogBuilder();
                        titleElectronic.setText(E_NAME_5);
                    }

            }
        });


        customizeToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                SharedPreferences.Editor edit = sp.edit();

                if(compoundButton.isChecked()) {
                    isConnected = ((BaseApplication) c.getApplicationContext()).getArduinoStatus();
                    String pos = "000";
                    if(PARENT_POS==0 && CHILD_POS == 0){
                        Log.v("MOMOKUN95LV", String.valueOf(isConnected));
                        pos = "111";
                        edit.putBoolean(eStateSPKey[0],true);
                        edit.apply();
                    }else if(PARENT_POS==1 && CHILD_POS ==0){
                        pos = "211";
                        edit.putBoolean(eStateSPKey[1],true);
                        edit.apply();
                    }else if(PARENT_POS==2 && CHILD_POS ==0){
                        pos = "311";
                        edit.putBoolean(eStateSPKey[2],true);
                        edit.apply();
                    }else if(PARENT_POS==2 && CHILD_POS ==1){
                        pos = "411";
                        edit.putBoolean(eStateSPKey[3],true);
                        edit.apply();
                    }else if(PARENT_POS==3 && CHILD_POS ==0){
                        pos = "511";
                        edit.putBoolean(eStateSPKey[4],true);
                        edit.apply();
                    }
                    ma.sendingToggleState(c,pos);
                }else{
                    String pos = "000";
                    if(PARENT_POS==0 && CHILD_POS == 0){
                        pos = "110";
                        edit.putBoolean(eStateSPKey[0],false);
                        edit.apply();
                    }else if(PARENT_POS==1 && CHILD_POS ==0){
                        pos = "210";
                        edit.putBoolean(eStateSPKey[1],false);
                        edit.apply();
                    }else if(PARENT_POS==2 && CHILD_POS ==0){
                        pos = "310";
                        edit.putBoolean(eStateSPKey[2],false);
                        edit.apply();
                    }else if(PARENT_POS==2 && CHILD_POS ==1){
                        pos = "410";
                        edit.putBoolean(eStateSPKey[3],false);
                        edit.apply();
                    }else if(PARENT_POS==3 && CHILD_POS ==0){
                        pos = "510";
                        edit.putBoolean(eStateSPKey[4],false);
                        edit.apply();
                    }
                    ma.sendingToggleState(c,pos);
                }
            }
        });



        return view;
    }

    @Override
    public boolean isChildSelectable(int parentPos, int childPos) {
        return true;
    }


}
