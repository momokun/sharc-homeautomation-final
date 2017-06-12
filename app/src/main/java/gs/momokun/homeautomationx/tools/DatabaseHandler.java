package gs.momokun.homeautomationx.tools;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;


public class DatabaseHandler extends SQLiteOpenHelper {

    private static final int DB_VER = 2;
    private static final String DB_NAME = "sensorLoggingManager";
    private static final String TABLE_SENSOR = "sensor";

    private static final String KEY_ID = "id";
    private static final String KEY_DATE = "datex";
    private static final String KEY_TEMP = "temp";

    private static final String KEY_VOLTAGE = "voltage";
    private static final String KEY_AMPS = "amps";
    private static final String KEY_WATT = "watt";
    private static final String KEY_ENERGY = "energy";

    private List<DataLogging> dataLogList;
    private SQLiteDatabase db;
    private String selectQuery;
    private Cursor cursor;

    public DatabaseHandler(Context context) {
        super(context, DB_NAME, null, DB_VER);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_SENSOR + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_DATE + " INTEGER,"
                + KEY_TEMP + " TEXT,"
                + KEY_VOLTAGE + " TEXT,"
                + KEY_AMPS + " TEXT,"
                + KEY_WATT + " TEXT,"
                + KEY_ENERGY + " TEXT" + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SENSOR);

        // Create tables again
        onCreate(db);
    }

    public boolean checkData() {
        db = this.getWritableDatabase();
        selectQuery = "SELECT * FROM "+TABLE_SENSOR+" ORDER BY "+KEY_ID+" DESC LIMIT 1";
        cursor = db.rawQuery(selectQuery,null);
        if(cursor.getCount() <= 0){
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

    public void addData(DataLogging dl) {
        db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_DATE, dl.get_date());
        values.put(KEY_TEMP, dl.get_temp());
        values.put(KEY_VOLTAGE, dl.get_voltage());
        values.put(KEY_AMPS, dl.get_amps());
        values.put(KEY_WATT, dl.get_watt());
        values.put(KEY_ENERGY, dl.get_energy());

        // Inserting Row
        db.insert(TABLE_SENSOR, null, values);
        db.close(); // Closing database connection
    }

    public String getLatestTemp(){
        selectQuery = "SELECT * FROM "+TABLE_SENSOR+" ORDER BY "+KEY_ID+" DESC LIMIT 1";
        db = this.getReadableDatabase();
        cursor = db.rawQuery(selectQuery,null);
        String x;


        cursor.moveToFirst();
        if(cursor.getCount()!=0) {
            x = cursor.getString(2);
        }else x = "25";

        cursor.close();
        db.close();

        return x;
    }

    public List<DataLogging>getAllData(){
        dataLogList = new ArrayList<>();
        db = this.getWritableDatabase();
        //selectQuery = "SELECT * FROM " + TABLE_SENSOR + " LIMIT 200";
        selectQuery =
                "SELECT * FROM (SELECT "+KEY_ID+"," +
                        "strftime(datetime(datex/1000, 'unixepoch', 'localtime')) as fl, " +
                        KEY_DATE+"/1000," +
                        "ROUND(AVG(CAST("+KEY_TEMP+" AS FLOAT)),2) as avg_temp, " +
                        "AVG("+KEY_VOLTAGE+") as avg_voltage, " +
                        "AVG("+KEY_AMPS+") as avg_amps, " +
                        "AVG("+KEY_WATT+") as avg_watt, " +
                        "SUM("+KEY_ENERGY+") as avg_energy FROM " +
                        TABLE_SENSOR;

        cursor = db.rawQuery(selectQuery,null);
        if (cursor.moveToFirst()) {
            do {
                DataLogging dl = new DataLogging();
                dl.set_id(Integer.parseInt(cursor.getString(0)));
                dl.set_date(cursor.getLong(1));
                dl.set_temp(cursor.getString(2));
                dl.set_voltage(cursor.getString(3));
                dl.set_amps(cursor.getString(4));
                dl.set_watt(cursor.getString(5));
                dl.set_energy(cursor.getString(6));
                dataLogList.add(dl);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return dataLogList;
    }

    //per jam
    public List<DataLogging>getByHourly(){
        dataLogList = new ArrayList<>();
        db = this.getWritableDatabase();
        /*selectQuery =
                "SELECT "+KEY_ID+"," +
                        "strftime(datetime(datex/1000, 'unixepoch', 'localtime')) as fl, " +
                        KEY_DATE+"/1000," +
                        "ROUND(AVG(CAST("+KEY_TEMP+" AS FLOAT)),2) as avg_temp, " +
                        "AVG("+KEY_VOLTAGE+") as avg_voltage, " +
                        "AVG("+KEY_AMPS+") as avg_amps, " +
                        "AVG("+KEY_WATT+") as avg_watt, " +
                        "SUM("+KEY_ENERGY+") as avg_energy FROM " +
                        TABLE_SENSOR + " WHERE fl >= DATE('now','localtime') GROUP BY strftime('%H',fl)";*/

        selectQuery =
                "SELECT * FROM (SELECT "+KEY_ID+"," +
                        "strftime(datetime(datex/1000, 'unixepoch', 'localtime')) as fl, " +
                        KEY_DATE+"/1000," +
                        "ROUND(AVG(CAST("+KEY_TEMP+" AS FLOAT)),2) as avg_temp, " +
                        "AVG("+KEY_VOLTAGE+") as avg_voltage, " +
                        "AVG("+KEY_AMPS+") as avg_amps, " +
                        "AVG("+KEY_WATT+") as avg_watt, " +
                        "SUM("+KEY_ENERGY+") as avg_energy FROM " +
                        TABLE_SENSOR + " WHERE fl >= DATE('now','start of year','localtime') GROUP BY strftime('%H',fl) ORDER BY "+KEY_DATE+" DESC LIMIT 0,24) ORDER BY id ASC";

        cursor = db.rawQuery(selectQuery,null);
        if (cursor.moveToFirst()) {
            do {
                DataLogging dl = new DataLogging();
                dl.set_id(Integer.parseInt(cursor.getString(0)));
                dl.setHr(cursor.getString(1));
                dl.set_date(cursor.getLong(2));
                dl.set_temp(cursor.getString(3));
                dl.set_voltage(cursor.getString(4));
                dl.set_amps(cursor.getString(5));
                dl.set_watt(cursor.getString(6));
                dl.set_energy(cursor.getString(7));

                dataLogList.add(dl);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return dataLogList;
    }

    public List<DataLogging>getByDummy(){
        dataLogList = new ArrayList<>();
        db = this.getWritableDatabase();
        selectQuery =
                "SELECT "+KEY_ID+"," +
                        "strftime(datetime(datex/1000, 'unixepoch', 'localtime')) as fl, " +
                        KEY_DATE+"/1000," +
                        "ROUND(AVG(CAST("+KEY_TEMP+" AS FLOAT)),2) as avg_temp, " +
                        "AVG("+KEY_VOLTAGE+") as avg_voltage, " +
                        "AVG("+KEY_AMPS+") as avg_amps, " +
                        "AVG("+KEY_WATT+") as avg_watt, " +
                        "SUM("+KEY_ENERGY+") as avg_energy FROM " +
                        TABLE_SENSOR;

        cursor = db.rawQuery(selectQuery,null);
        if (cursor.moveToFirst()) {
            do {
                DataLogging dl = new DataLogging();
                dl.set_id(Integer.parseInt(cursor.getString(0)));
                dl.setHr(cursor.getString(1));
                dl.set_date(cursor.getLong(2));
                dl.set_temp(cursor.getString(3));
                dl.set_voltage(cursor.getString(4));
                dl.set_amps(cursor.getString(5));
                dl.set_watt(cursor.getString(6));
                dl.set_energy(cursor.getString(7));

                dataLogList.add(dl);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return dataLogList;
    }

    //per hari
    //query update
    public List<DataLogging>getByDay(){
        dataLogList = new ArrayList<>();
        db = this.getWritableDatabase();
        selectQuery =
                "SELECT * FROM (SELECT "+KEY_ID+"," +
                        "strftime(datetime(datex/1000, 'unixepoch', 'localtime')) as fl, " +
                        KEY_DATE+"/1000," +
                        "ROUND(AVG(CAST("+KEY_TEMP+" AS FLOAT)),2) as avg_temp, " +
                        "AVG("+KEY_VOLTAGE+") as avg_voltage, " +
                        "AVG("+KEY_AMPS+") as avg_amps, " +
                        "AVG("+KEY_WATT+") as avg_watt, " +
                        "SUM("+KEY_ENERGY+") as avg_energy FROM " +
                        TABLE_SENSOR + " WHERE fl >= DATE('now','start of year','localtime') GROUP BY strftime('%j',fl) ORDER BY "+KEY_DATE+" DESC LIMIT 0,7) ORDER BY id ASC";

        cursor = db.rawQuery(selectQuery,null);
        if (cursor.moveToFirst()) {
            do {
                DataLogging dl = new DataLogging();
                dl.set_id(Integer.parseInt(cursor.getString(0)));
                dl.setHr(cursor.getString(1));
                dl.set_date(cursor.getLong(2));
                dl.set_temp(cursor.getString(3));
                dl.set_voltage(cursor.getString(4));
                dl.set_amps(cursor.getString(5));
                dl.set_watt(cursor.getString(6));
                dl.set_energy(cursor.getString(7));

                dataLogList.add(dl);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return dataLogList;

    }

    //per minggu
    public List<DataLogging>getByWeekly(){

        dataLogList = new ArrayList<>();
        db = this.getWritableDatabase();
        /*selectQuery =
                "SELECT "+KEY_ID+"," +
                        KEY_DATE+"," +
                        "ROUND(AVG(CAST("+KEY_TEMP+" AS FLOAT)),2) as avg_temp, " +
                        "AVG("+KEY_VOLTAGE+") as avg_voltage, " +
                        "AVG("+KEY_AMPS+") as avg_amps, " +
                        "AVG("+KEY_WATT+") as avg_watt, " +
                        "SUM("+KEY_ENERGY+") as avg_energy FROM " +
                        TABLE_SENSOR +
                        " WHERE "+KEY_DATE+" >= strftime('%s','now','start of year','localtime') GROUP BY strftime('%s','%W', "+KEY_DATE+")";*/

        selectQuery =
                "SELECT * FROM (SELECT "+KEY_ID+"," +
                        "strftime(datetime(datex/1000, 'unixepoch', 'localtime')) as fl, " +
                        KEY_DATE+"/1000," +
                        "ROUND(AVG(CAST("+KEY_TEMP+" AS FLOAT)),2) as avg_temp, " +
                        "AVG("+KEY_VOLTAGE+") as avg_voltage, " +
                        "AVG("+KEY_AMPS+") as avg_amps, " +
                        "AVG("+KEY_WATT+") as avg_watt, " +
                        "SUM("+KEY_ENERGY+") as avg_energy FROM " +
                        TABLE_SENSOR + " WHERE fl >= DATE('now','start of year','localtime') GROUP BY strftime('%W',fl) ORDER BY "+KEY_DATE+" DESC LIMIT 0,6) ORDER BY id ASC";

        cursor = db.rawQuery(selectQuery,null);
        if (cursor.moveToFirst()) {
            do {
                DataLogging dl = new DataLogging();
                dl.set_id(Integer.parseInt(cursor.getString(0)));
                dl.setHr(cursor.getString(1));
                dl.set_date(cursor.getLong(2));
                dl.set_temp(cursor.getString(3));
                dl.set_voltage(cursor.getString(4));
                dl.set_amps(cursor.getString(5));
                dl.set_watt(cursor.getString(6));
                dl.set_energy(cursor.getString(7));

                dataLogList.add(dl);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return dataLogList;

    }

    //per bulan
    public List<DataLogging>getByMonthly(){
        dataLogList = new ArrayList<>();
        db = this.getWritableDatabase();
        selectQuery =
                "SELECT * FROM (SELECT "+KEY_ID+"," +
                        "strftime(datetime(datex/1000, 'unixepoch', 'localtime')) as fl, " +
                        KEY_DATE+"/1000," +
                        "ROUND(AVG(CAST("+KEY_TEMP+" AS FLOAT)),2) as avg_temp, " +
                        "AVG("+KEY_VOLTAGE+") as avg_voltage, " +
                        "AVG("+KEY_AMPS+") as avg_amps, " +
                        "AVG("+KEY_WATT+") as avg_watt, " +
                        "SUM("+KEY_ENERGY+") as avg_energy FROM " +
                        TABLE_SENSOR + " WHERE fl >= DATE('now','start of year','localtime') GROUP BY strftime('%m',fl) ORDER BY "+KEY_DATE+" DESC LIMIT 0,12) ORDER BY id ASC";

        cursor = db.rawQuery(selectQuery,null);
        if (cursor.moveToFirst()) {
            do {
                DataLogging dl = new DataLogging();
                dl.set_id(Integer.parseInt(cursor.getString(0)));
                dl.setHr(cursor.getString(1));
                dl.set_date(cursor.getLong(2));
                dl.set_temp(cursor.getString(3));
                dl.set_voltage(cursor.getString(4));
                dl.set_amps(cursor.getString(5));
                dl.set_watt(cursor.getString(6));
                dl.set_energy(cursor.getString(7));

                dataLogList.add(dl);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return dataLogList;

    }

}
