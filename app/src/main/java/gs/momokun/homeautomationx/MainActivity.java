package gs.momokun.homeautomationx;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.icu.text.SimpleDateFormat;
import android.icu.util.TimeZone;
import android.os.AsyncTask;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import gs.momokun.homeautomationx.tools.ArduinoStateOnReceived;
import gs.momokun.homeautomationx.tools.BluetoothReceiver;
import gs.momokun.homeautomationx.tools.CustomElectronicListAdapter;
import gs.momokun.homeautomationx.tools.DataLogging;
import gs.momokun.homeautomationx.tools.DataStateLog;
import gs.momokun.homeautomationx.tools.DatabaseHandler;
import gs.momokun.homeautomationx.tools.DatabaseLogStatHandler;

public class MainActivity extends AppCompatActivity {

    BroadcastReceiver broadcastReceiver;
    TextView  hardware_status;
    AlertDialog.Builder adb;
    AlertDialog ad;

    static InputStream mmInStream;
    static OutputStream mmOutStream;
    static Handler btConnectionHandler = null;
    BluetoothAdapter btAdapter = null;
    BluetoothSocket btSocket = null;
    BluetoothDevice btDevice = null;
    StringBuilder receivedDataFromArduino = new StringBuilder();
    ConnectedThread mConnectedThread;
    SharedPreferences sp;
    SharedPreferences.Editor spEdit;

    //progress dialog for connecting and receiving data
    ProgressDialog pd;

    //handler state for receiving data
    static final int HANDLER_STATE = 0;

    //Device UUID
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    //arduino mac address
    private static String address;

    //arduino state
    int stateArduino = 0;
    boolean isConnected = false;

    LinearLayout ll;

    boolean alreadyPairState;
    boolean isCallingtoConnect = false; //test

    private LayoutInflater inflater;

    final String KEY_IS_CONNECT = "isConnected";
    final String KEY_IS_PAIRED = "pairState";
    final String KEY_BT_ADDRESS = "btAddr";

    final String NAME_SP_KEY[] = {"electronicName1","electronicName2","electronicName3","electronicName4","electronicName5"};

    private void initialize() {
        //((BaseApplication) this.getApplication()).setArduinoConnected(true);
        ll = (LinearLayout) findViewById(R.id.linearlayoutMain);

        hardware_status = (TextView) findViewById(R.id.arduino_status);
        marqueeHomeTitle = (TextView) findViewById(R.id.scroller);
        marqueeHomeTitle.setEllipsize(TextUtils.TruncateAt.MARQUEE);

        sp = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        sp.edit().putBoolean(KEY_IS_CONNECT, false).apply();
        address = sp.getString(KEY_BT_ADDRESS, null); //get address from SharedPref
       // address = null;


        alreadyPairState = address != null && sp.getBoolean(KEY_IS_PAIRED, false);
        disconnectedState();

        CustomElectronicListInitialize();

        if (alreadyPairState) {
            new connectBluetooth().execute("");
        } else {
            snackBarCustom("Please Pair first...","Ok");
        }

        if(!alreadyPairState && address==null){
            sp.edit().putString(NAME_SP_KEY[0],"Lamp").apply();
            sp.edit().putString(NAME_SP_KEY[1],"Lamp").apply();
            sp.edit().putString(NAME_SP_KEY[2],"Lamp").apply();
            sp.edit().putString(NAME_SP_KEY[3],"Fan").apply();
            sp.edit().putString(NAME_SP_KEY[4],"Electronic").apply();
        }

    }

    //custom list view init
    private void CustomElectronicListInitialize(){
        final List<String> LIST_DATA_HEADER = new ArrayList<>();
        final HashMap<String, List<String>> LIST_DATA_CHILD = new HashMap<>();
        final ExpandableListView ELECTRONIC_LIST_VIEW = (ExpandableListView) findViewById(R.id.lvExp);
        final View rootView = findViewById(android.R.id.content);

        prepareListData(LIST_DATA_HEADER,LIST_DATA_CHILD);
        ExpandableListAdapter listAdapter = new CustomElectronicListAdapter(rootView, this,this, LIST_DATA_HEADER, LIST_DATA_CHILD);
        ELECTRONIC_LIST_VIEW.setAdapter(listAdapter);

        final int LIST_COUNT = listAdapter.getGroupCount();
        for(int pos=1; pos <= LIST_COUNT; pos++){
            ELECTRONIC_LIST_VIEW.expandGroup(pos-1);
        }
    }

    private void prepareListData(List<String> listDataHeader, HashMap<String, List<String>> listDataChild){
        listDataHeader.add(getString(R.string.item_header_1));
        listDataHeader.add(getString(R.string.item_header_2));
        listDataHeader.add(getString(R.string.item_header_3));
        listDataHeader.add(getString(R.string.item_header_4));

        List<String> terrace = new ArrayList<>();
        terrace.add(sp.getString(NAME_SP_KEY[0],"Lamp"));

        List<String> bedRoom = new ArrayList<>();
        bedRoom.add(sp.getString(NAME_SP_KEY[1],"Lamp"));

        List<String> livingRoom = new ArrayList<>();
        livingRoom.add(sp.getString(NAME_SP_KEY[2],"Lamp"));
        livingRoom.add(sp.getString(NAME_SP_KEY[3],"Fan"));

        List<String> kitchenRoom = new ArrayList<>();
        kitchenRoom.add(sp.getString(NAME_SP_KEY[4],"Electronic"));

        listDataChild.put(listDataHeader.get(0), terrace);
        listDataChild.put(listDataHeader.get(1), bedRoom);
        listDataChild.put(listDataHeader.get(2), livingRoom);
        listDataChild.put(listDataHeader.get(3), kitchenRoom);


    }

    private void registerBluetoothListener(){
        broadcastReceiver = new BluetoothReceiver();
        IntentFilter f = new IntentFilter();
        f.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        f.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        f.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(broadcastReceiver, f);
        EventBus.getDefault().register(this);
    }

    String marqueeTitleText;
    TextView marqueeHomeTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        initialize(); //first init
        registerBluetoothListener(); //
        DatabaseHandler DB_HANDLER_SENSOR = new DatabaseHandler(this); //
        marqueeTitleText = DB_HANDLER_SENSOR.getLatestTemp();
        marqueeHomeTitle.setText("Welcome Mrs. Sonya, your current temperature is "+ marqueeTitleText+"°C");

    }

    @Override
    protected void onResume(){
        super.onResume();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        resetConnection();
        EventBus.getDefault().unregister(this);
        unregisterReceiver(broadcastReceiver);
    }

    //configure action bar menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Take appropriate action for each action item click
        inflater = getLayoutInflater();
        switch (item.getItemId()) {
            case R.id.action_pair:
                customDeviceList(getString(R.string.close_alert_dialog)); //for pairing
                return true;
            case R.id.view_graph:
                graphViewDialogBuilder(getString(R.string.close_alert_dialog)); //for graph view
                return true;
            case R.id.action_history:
                stateLogDialogBuilder(getString(R.string.close_alert_dialog)); //for log view
                return true;
            case R.id.action_dc:
                if(sp.getBoolean(KEY_IS_CONNECT,false)){
                    resetConnection();
                    sp.edit().putBoolean(KEY_IS_CONNECT,false).apply();
                }else{
                    resetConnection();
                    new connectBluetooth().execute("");
                }
                return true;
            case R.id.action_help:
                adb = new AlertDialog.Builder(MainActivity.this);
                View helpView = inflater.inflate(R.layout.help_dialog_custom,ll,false);
                adb.setView(helpView);
                adb.setCancelable(false).setNegativeButton(getString(R.string.close_alert_dialog), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ad.dismiss();
                    }
                });
                ad = adb.create();
                ad.show();
                return true;
            case R.id.action_about:
                adb = new AlertDialog.Builder(MainActivity.this);
                View aboutView = inflater.inflate(R.layout.about_dialog_custom,ll,false);
                adb.setView(aboutView);
                adb.setCancelable(false).setNegativeButton(getString(R.string.close_alert_dialog), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ad.dismiss();
                    }
                });
                ad = adb.create();
                ad.show();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        if(sp.getBoolean(KEY_IS_CONNECT,false)){
            menu.getItem(3).setTitle("Disconnect");
        }else{
            menu.getItem(3).setTitle("Connect");
        }
        return super.onPrepareOptionsMenu(menu);
    }
    //end of configure action bar menu

    //check if bluetooth module is on
    private void BluetoothStateChecker(){
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if(btAdapter!=null){
            if(!btAdapter.isEnabled()){
                Intent btRequestEnable = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(btRequestEnable,1);
            }
        }else{
            Toast.makeText(this, "No Bluetooth on this device, please check your device.", Toast.LENGTH_SHORT).show();
        }
    }

    //view log history - c
    private void stateLogDialogBuilder(String negativeButtonMsg){
        DatabaseLogStatHandler dblsh = new DatabaseLogStatHandler(this);
        inflater = getLayoutInflater();
        View historyView = inflater.inflate(R.layout.activity_state_log,ll,false);
        String[] from={"type","date"};//string array
        int[] to={R.id.type_id,R.id.date_millis};//int array of views id's

        TextView emptyText = (TextView) historyView.findViewById(R.id.emptyState);
        ListView listState = (ListView) historyView.findViewById(R.id.stateLogList);

        setTitle("Log History");
        emptyText.setText("Oops.. it's empty!");
        listState.setEmptyView(emptyText);

        ArrayList<HashMap<String,String>> arrayStateList=new ArrayList<>();
        List<DataStateLog> dsl = dblsh.getAllStateLog();

        String convertedDate;
        for (DataStateLog ds : dsl){
            long convertedRawTime = Long.valueOf(ds.get_date())*1000;
            Date dateConverted = new java.util.Date(convertedRawTime);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                convertedDate = new SimpleDateFormat("dd / MM / yyyy - HH:mm:ss").format(dateConverted);
            }else{
                convertedDate = new java.text.SimpleDateFormat("dd / MM / yyyy - HH:mm:ss").format(dateConverted);

            }
            HashMap<String,String> hashMap=new HashMap<>();
            hashMap.put("type",typeChecker(ds.get_type()));
            hashMap.put("date",convertedDate);
            arrayStateList.add(hashMap);
        }


        SimpleAdapter simpleAdapter=new SimpleAdapter(this,arrayStateList,R.layout.state_list_title_custom,from,to);//Create object and set the parameters for simpleAdapter
        listState.setAdapter(simpleAdapter);//sets the adapter for listView

        adb = new AlertDialog.Builder(MainActivity.this);
        adb.setView(historyView);
        adb.setCancelable(false).setNegativeButton(negativeButtonMsg, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ad.dismiss();
            }
        });
        ad = adb.create();
        ad.show();
    }

    public String typeChecker(String getType){

        String stateTypeFinal = "";
        Log.v("GetType",getType);

        for(int i = 1; i<6; i++){
            if(getType.equals("T"+i+i+"1")){
                stateTypeFinal=sp.getString(NAME_SP_KEY[i-1],"Electronic "+i)+" is On";
            }else if(getType.equals("T"+i+i+"0")){
                stateTypeFinal=sp.getString(NAME_SP_KEY[i-1],"Electronic "+i)+" is Off";
            }else if(getType.equals("T55"+i)){
                stateTypeFinal=sp.getString(NAME_SP_KEY[i-1],"Electronic "+i)+" is on";
            }
        }
        //code format: T[pos][pos][1/0]@[date]@~ <<Light/Temp
        //code format: T[5][5][pos]@[date]@~ << Timer
        return stateTypeFinal;
    }



    //graph set up
    private void graphViewDialogBuilder(String negativeButtonMsg){

        inflater = getLayoutInflater();
        View graphBaseView = inflater.inflate(R.layout.activity_view_graph_alt,ll,false);
        spinnerRangeDate(graphBaseView);
        DatabaseHandler DB_HANDLER_SENSOR = new DatabaseHandler(this);
        GraphAdapter gaa = new GraphAdapter(this,graphBaseView,DB_HANDLER_SENSOR);
        adb = new AlertDialog.Builder(this);
        adb.setView(graphBaseView);

        gaa.viewGraph();


        adb.setCancelable(true).setNegativeButton(negativeButtonMsg, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ad.dismiss();
            }
        });

        ad = adb.create();
        ad.show();
    }

    private void spinnerRangeDate(View parentView){
        Spinner spinnerDate = (Spinner) parentView.findViewById(R.id.spinner_date_range);
        Spinner spinnerData = (Spinner) parentView.findViewById(R.id.spinner_date_array);

        ArrayAdapter<CharSequence> adapterDate = ArrayAdapter.createFromResource(this,
                R.array.range_array_date, android.R.layout.simple_spinner_item);
        ArrayAdapter<CharSequence> adapterData = ArrayAdapter.createFromResource(this,
                R.array.type_data_array, android.R.layout.simple_spinner_item);

        adapterDate.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapterData.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerDate.setAdapter(adapterDate);
        spinnerData.setAdapter(adapterData);
    }
    //end of graph set up



    //create bluetooth socket using UUID
    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }

    private class connectBluetooth extends AsyncTask<String, String, String> {

        int stat = 0;

        protected void onPreExecute(){
            //creating progress dialog, call handler to run in background
            isCallingtoConnect = true;
            sysHandler();
            pd = ProgressDialog.show(MainActivity.this, "Please wait", "Connecting...", true, false);
            pd.setCancelable(false);

        }

        protected String doInBackground(String... params) {
            if(address==null){
                address=params[0];
            }


            if(address!=null) {
                //create device and set the MAC address
                btAdapter = BluetoothAdapter.getDefaultAdapter(); //OS
                btDevice = btAdapter.getRemoteDevice(address); //address

                try {
                    btSocket = createBluetoothSocket(btDevice);
                } catch (IOException e) {
                    Toast.makeText(MainActivity.this, "Socket creation failed", Toast.LENGTH_LONG).show();
                }
                // Establish the Bluetooth socket connection.

                try {
                    btSocket.connect();

                    //thread for sending n received
                    mConnectedThread = new ConnectedThread(btSocket); //threading
                    mConnectedThread.start();



                    stat = 1;
                    stateArduino=1;

                } catch (IOException e) {
                    try {
                        if(btSocket!=null) {
                            btSocket.close();
                        }
                    } catch (IOException e2) {
                        stat = 0;

                    }
                }


            }else{
                stat = 0;
                return "Failed";

            }
            return null;
        }

        protected void onPostExecute(String result) {
            if(stateArduino==0){
                if(pd.isShowing()){
                    pd.dismiss();
                }
            }

            if(stat==1 && stateArduino==1){
                connectedState();
                sp.edit().putBoolean(KEY_IS_CONNECT,true).apply();
                isConnected = true;
                mConnectedThread.write("777#77#77777"); //for get value from Arduino
                pd.setMessage(getString(R.string.updating_state_progressDialog));
                snackBarCustom("Updating data, please wait","Dismiss");
            }else if(stat==0){
                disconnectedState();
                if(pd.isShowing()){
                    pd.dismiss();
                }
                snackBarCustom("Can't connect to Home Automation Kit","Dismiss");
            }
            // reconnect.setRefreshing(false);
            isCallingtoConnect=false;
        }
    }

    private static class ConnectedThread extends Thread {

        // You declare your interface in the Class body
        interface CallBackListener
        {
            void onReceived(String msg);
        }

        private CallBackListener listener = null;

        ConnectedThread(BluetoothSocket socket, CallBackListener aListener) {
            this.listener = aListener;
        }

        //creation of the connect thread
        ConnectedThread(BluetoothSocket socket) {

            InputStream tmpIn = null;
            OutputStream tmpOut = null;


            try {
                //Create I/O streams for connection
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                getStackTrace();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {

            byte[] buffer = new byte[256];
            int bytes;
            btConnectionHandler.sendEmptyMessage(0); //return true if message successfully sent
            // Keep looping to listen for received messages
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);        	       //read bytes from input buffer
                    String readMessage = new String(buffer, 0, bytes); //array of byte, offset (location), total byte

                    // Send to UI via Handler
                    btConnectionHandler.obtainMessage(HANDLER_STATE, bytes, -1, readMessage).sendToTarget(); //state, arg1, arg2, object

                    if (listener != null)
                    {
                        listener.onReceived(readMessage); //for listener
                    }

                } catch (IOException e) {

                    break;
                }
            }
        }

        //write method
        void write(String input) {
            byte[] msgBuffer = input.getBytes(); //converts input to bytes
            try {
                mmOutStream.write(msgBuffer); //write bytes to arduino
            } catch (IOException e) {
                Log.d("Stat","Nothing Send");
            }
        }
    }


    protected int flushStat = 0;
    private void sysHandler(){
        final String[] TYPE = {""};
        final String[] DATA_IN_MILLIS = {""};
        final String[] TEMP = {""};
        final DatabaseHandler DB_HANDLER_SENSOR = new DatabaseHandler(this);
        final DatabaseLogStatHandler DB_HANDLER_HISTORY = new DatabaseLogStatHandler(this);

        btConnectionHandler = new Handler(){

            public void handleMessage(android.os.Message messageFromArduino){
                if(messageFromArduino.what == HANDLER_STATE){ //param0

                    String receiveMsg = (String) messageFromArduino.obj; //param4 (obj)
                    receivedDataFromArduino.append(receiveMsg); //appending data until end of line

                    int endOfLineIndex = receivedDataFromArduino.indexOf("~"); //eol
                        if (endOfLineIndex > 0) {

                            String extractedData = receivedDataFromArduino.substring(0, endOfLineIndex).trim(); //remove new empty line
                            int dataLength = extractedData.length();

                            //T[pos][pos][1/0]@[date]@~ <<Light/Temp
                            //T[5][5][pos]@[date]@~ << Timer
                            //if message start with 'T' after append
                            if(extractedData.charAt(0) == 'T'){
                                //split message before @
                                String[] ext = receivedDataFromArduino.toString().split("@");
                                TYPE[0] = ext[0].trim();
                                DATA_IN_MILLIS[0] = ext[1].trim();
                                if(dataLength==16) {
                                    //store to db
                                    DB_HANDLER_HISTORY.addData(new DataStateLog(DATA_IN_MILLIS[0], TYPE[0]));
                                }
                            }


                            //!@FLUSH~
                            //Dismiss Progress Dialog
                            //if message start with '!' after append
                            if(extractedData.charAt(0) == '!'){
                                //split message before @
                                String[] ext = receivedDataFromArduino.toString().split("@");
                                if(ext[1].contains("FLUSH")){
                                    pd.dismiss();
                                    flushStat=1;
                                }
                            }

                            //#@[TIME]@[AMPERE]@[SUHU]@~
                            if(extractedData.charAt(0) == '#'){
                                //split message before @
                                String[] ext = receivedDataFromArduino.toString().split("@");
                                String date = ext[1];
                                String amps = ext[2];
                                TEMP[0] = ext[3];


                                DecimalFormat df = new DecimalFormat("##.##");
                                Float energyKwHtemp = 220 * Float.parseFloat(amps);

                                String watt = df.format(energyKwHtemp);
                                String energykWh = df.format(energyKwHtemp/1000);
                                Long dateinMillis = Long.valueOf(date)*1000;



                               if(dataLength==23) {
                                   //store to db
                                    DB_HANDLER_SENSOR.addData(new DataLogging(dateinMillis, TEMP[0], "220", amps, watt, energykWh));
                                }

                            }
                            receivedDataFromArduino.delete(0, receivedDataFromArduino.length()); //remove previous data, start with new msg
                        }

                }
                //set latest temp after sync
                if(messageFromArduino.what==HANDLER_STATE) {
                    marqueeTitleText = DB_HANDLER_SENSOR.getLatestTemp();
                    marqueeHomeTitle.setText("Welcome Mrs. Sonya, your current temperature is " + marqueeTitleText + "°C");
                }

            }


        };


    }


    //close all stream and socket
    public void resetConnection() {
        if(mmInStream != null){
            try{mmInStream.close();} catch (Exception ignored) {}
            mmInStream = null;
        }

        if(mmOutStream!=null){
            try{mmOutStream.close();} catch (Exception ignored) {}
            mmOutStream = null;
        }

        if (btSocket != null) {
            try {btSocket.close();} catch (Exception ignored) {}
            btSocket = null;
        }
    }

    //pair device
    private void customDeviceList(String negativeButtonMsg){
        inflater = getLayoutInflater();
        View textEntryView = inflater.inflate(R.layout.list_view_dialog,ll,false);
        ListView pairedDeviceList=(ListView) textEntryView.findViewById(R.id.list_view);
        Button btn_setting = (Button) textEntryView.findViewById(R.id.btn_setting);
        View textTitle = inflater.inflate(R.layout.device_title,ll,false);

        ArrayAdapter<String> btListAdapter = new ArrayAdapter<>(this, R.layout.device_title);

        btAdapter = BluetoothAdapter.getDefaultAdapter();

        //easy access to system bluetooth setting
        btn_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS), 0);
            }
        });

        //show paired device (system)
        Set<BluetoothDevice> pairedDevice = btAdapter.getBondedDevices();

        if (pairedDevice.size()>0){
            //show paired device list
            for(BluetoothDevice devices : pairedDevice){
                btListAdapter.add(devices.getName() + "\n" + devices.getAddress());
            }
        }else{
            //if no devices
            String noDevices = "Make sure you turn on bluetooth/already pair with Arduino";
            btListAdapter.add(noDevices);
        }

        pairedDeviceList.setAdapter(btListAdapter);
        pairedDeviceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //saving and open connection after click
                String info = ((TextView) view).getText().toString();
                String address = info.substring(info.length() - 17);
                SharedPreferences.Editor edit = sp.edit();
                alreadyPairState=true;
                edit.putString("btAddr", address);
                edit.putBoolean("pairState",true);
                edit.apply();
                new connectBluetooth().execute(address);
                ad.dismiss();
            }
        });


        adb = new AlertDialog.Builder(MainActivity.this);
        adb.setView(textEntryView);
        adb.setCancelable(true).setNegativeButton(negativeButtonMsg, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ad.dismiss();
            }
        });
        ad = adb.create();
        ad.show();
    }


    //snackbar 1
    public void snackBarCustom(String msg, String cancelMsg){
        final Snackbar SNACKBAR = Snackbar.make(ll, msg, Snackbar.LENGTH_LONG);

        SNACKBAR.setAction(cancelMsg, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SNACKBAR.dismiss();
            }
        });
        ViewGroup group = (ViewGroup) SNACKBAR.getView(); //modify UI
        TextView tv = (TextView) group.findViewById(android.support.design.R.id.snackbar_action);
        tv.setTextColor(Color.WHITE);
        group.setBackgroundColor(0xFF1A237E);
        SNACKBAR.show();
    }

    //snackbar 2
    public void snackBarCustom(Activity a, String msg, String cancelMsg){

        final Snackbar SNACKBAR = Snackbar.make(a.findViewById(android.R.id.content), msg, Snackbar.LENGTH_LONG);
        SNACKBAR.setAction(cancelMsg, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SNACKBAR.dismiss();
            }
        });
        ViewGroup group = (ViewGroup) SNACKBAR.getView(); //modify UI
        group.setBackgroundColor(0xFF1A237E);
        TextView tv = (TextView) group.findViewById(android.support.design.R.id.snackbar_action);
        tv.setTextColor(Color.WHITE);
        SNACKBAR.show();
    }





    //Bluetooth receiver for check device
    @Subscribe
    public void onStateReceived(ArduinoStateOnReceived event){
        stateArduino = event.getStateArduino();
        if(stateArduino == 0) {
            BluetoothStateChecker();
            disconnectedState();
            resetConnection();
            isConnected=true;
        }else if(stateArduino == 1){
            connectedState();
        }
    }

    private void disconnectedState(){
        hardware_status.setText("SHARC Disconnected");
        hardware_status.setTextColor(Color.RED);
    }

    private void connectedState(){
        hardware_status.setText("SHARC Connected");
        hardware_status.setTextColor(Color.GREEN);
    }


    //switch on/off
    public void sendingToggleState(Context c,final String E_POS){
        SharedPreferences spToggle = PreferenceManager.getDefaultSharedPreferences(c);

        //create new instance of connected thread to use previous btSocket
        final ConnectedThread CONNECTED_THREAD = new ConnectedThread(btSocket, new ConnectedThread.CallBackListener() {
            @Override
            public void onReceived(String msg) {
                Log.v("String", msg);
            }
        });

        if(spToggle.getBoolean("isConnected",false)) {
            //[position+1][mode][on/off]#[value]#[valTimer]
            /*example

            110 # 00 # 00000 << Pos 1, Manual, Off
            211 # 00 # 00000 << Pos 2, Manual, On

             */
            CONNECTED_THREAD.write(E_POS + "#00#00000");
        }else{
            Toast.makeText(c, "Please connect to Arduino", Toast.LENGTH_SHORT).show();
        }
    }

    public void sendingConfiguration(Activity v,Context c, final int E_POS){
        String timerText = null;
        String tempSPKey[] = {"tempKey1","tempKey2","tempKey3","tempKey4","tempKey5"};
        String tempValueSPKey[] = {"tempVal1","tempVal2","tempVal3","tempVal4","tempVal5"};

        String lightSPKey[] = {"lightKey1","lightKey2","lightKey3","lightKey4","lightKey5"};
        String lightValueSPKey[] = {"lightVal1","lightVal2","lightVal3","lightVal4","lightVal5"};

        String timerSPKey[] = {"timerKey1","timerKey2","timerKey3","timerKey4","timerKey5"};
        String timerResult[] = {"timerRes1","timerRes2","timerRes3","timerRes4","timerRes5"};

        String nameSPKey[] = {"electronicName1","electronicName2","electronicName3","electronicName4","electronicName5"};

        final String IS_ENABLED_LIGHT;
        final String IS_ENABLED_TEMP;
        final String IS_ENABLED_TIMER;

        //get value from sharedpreference
        SharedPreferences spx = PreferenceManager.getDefaultSharedPreferences(c);
        final String LIGHT_VALUE = spx.getString(lightValueSPKey[E_POS],"999999999");
        final String TEMP_VALUE = spx.getString(tempValueSPKey[E_POS],"999999999");
        final Long TIMER_VALUE = spx.getLong(timerResult[E_POS],99999);
        Boolean lightStateIsEnabled = spx.getBoolean(lightSPKey[E_POS],false);
        Boolean tempStateIsEnabled = spx.getBoolean(tempSPKey[E_POS],false);
        Boolean timerStateIsEnabled = spx.getBoolean(timerSPKey[E_POS],false);

        String finalTimer = String.format("%05d", TIMER_VALUE); //setvalue in 5 digits format

        //create new instance of connected thread to use previous btSocket
        final ConnectedThread conThread = new ConnectedThread(btSocket, new ConnectedThread.CallBackListener()
        {
            @Override
            public void onReceived(String msg)
            {
                Log.v("String",msg);
            }
        });

        //send value when connected
        /*example
            321 # 20 # 00000 << Pos 3, Light, On, Value 20
            431 # 25 # 00000 << Pos 4, Temp, On, Value 25
            541 # 00 # 00025 << Pos 5, Timer, On, 25 Second

            only pos 1,2,3 for light snesor
            pos 4 for temp
            all pos for timer
         */
        if(spx.getBoolean("isConnected",false)) {
            if (E_POS != 3 && E_POS != 4) {
                if (lightStateIsEnabled) {
                    IS_ENABLED_LIGHT = "1";
                    conThread.write((E_POS + 1) + "2" + IS_ENABLED_LIGHT + "#" + LIGHT_VALUE + "#" + "00000");
                    snackBarCustom(v, spx.getString(nameSPKey[E_POS], "Lamp") + " configuration is saved.", "Ok");
                } else {
                    IS_ENABLED_LIGHT = "0";
                    conThread.write((E_POS + 1) + "2" + IS_ENABLED_LIGHT + "#00#" + "00000");
                    snackBarCustom(v, spx.getString(nameSPKey[E_POS], "Lamp") + " configuration is disabled.", "Ok");
                }
            }

            if (E_POS == 3) {
                if (tempStateIsEnabled) {
                    IS_ENABLED_TEMP = "1";
                    conThread.write((E_POS + 1) + "3" + IS_ENABLED_TEMP + "#" + TEMP_VALUE + "#" + "00000");
                    snackBarCustom(v, spx.getString(nameSPKey[E_POS], "Fan") + " will be on at " + TEMP_VALUE + " C", "Ok");
                } else {
                    IS_ENABLED_TEMP = "0";
                    conThread.write((E_POS + 1) + "3" + IS_ENABLED_TEMP + "#00#" + "00000");
                    snackBarCustom(v, spx.getString(nameSPKey[E_POS], "Fan") + " configuration is disabled.", "Ok");
                }
            }

            if (E_POS != 6 && (!lightStateIsEnabled && !tempStateIsEnabled)) {
                if (timerStateIsEnabled) {
                    IS_ENABLED_TIMER = "1";
                    conThread.write((E_POS + 1) + "4" + IS_ENABLED_TIMER + "#00#" + finalTimer);
                    //set value range for timer
                    if(TIMER_VALUE<=60){
                        timerText = ((TIMER_VALUE % 86400 ) % 3600 ) % 60 + " seconds";
                    }else if(TIMER_VALUE>60 && TIMER_VALUE<3600){
                        timerText = ((TIMER_VALUE % 86400 ) % 3600 ) / 60 + " minutes " + ((TIMER_VALUE % 86400 ) % 3600 ) % 60 + " seconds";
                    }else if (TIMER_VALUE >= 3600 && TIMER_VALUE < 86400) {
                        timerText = (TIMER_VALUE % 86400 ) / 3600 + " hours " + ((TIMER_VALUE % 86400 ) % 3600 ) / 60 + " minutes";
                    }else if (TIMER_VALUE >= 86400){
                        timerText = TIMER_VALUE / 86400 + " days " + (TIMER_VALUE % 86400 ) / 3600 + " hours";
                    }
                    snackBarCustom(v, spx.getString(nameSPKey[E_POS], "Electronic") + " will be on in " + timerText, "Ok");
                } else {
                    IS_ENABLED_TIMER = "0";
                    finalTimer = "00000";
                    conThread.write((E_POS + 1) + "4" + IS_ENABLED_TIMER + "#00#" + finalTimer);
                    snackBarCustom(v, spx.getString(nameSPKey[E_POS], "Electronic") + " configuration is disabled.", "Ok");
                }
            }
        }else{
            Toast.makeText(c, "Please connect to kit first", Toast.LENGTH_SHORT).show();
        }
    }

    //prevent back to exit
    boolean backToExitOnce = false;

    @Override
    public void onBackPressed() {
        if (backToExitOnce) {
            super.onBackPressed();
            return;
        }

        this.backToExitOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                backToExitOnce=false;
            }
        }, 2000);
    }




}
