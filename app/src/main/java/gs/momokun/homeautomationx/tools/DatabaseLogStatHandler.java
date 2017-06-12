package gs.momokun.homeautomationx.tools;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;


public class DatabaseLogStatHandler extends SQLiteOpenHelper {

    private static final int DB_VER = 4;
    private static final String DB_NAME = "statusLoggingManager";
    private static final String TABLE_LOGGING = "Logging";

    private static final String KEY_ID = "id";
    private static final String KEY_DATE = "datex";
    private static final String KEY_TYPE = "type";

    public DatabaseLogStatHandler(Context context) {
        super(context, DB_NAME, null, DB_VER);
    }

    private List<DataStateLog> dataStateList = new ArrayList<>();
    private SQLiteDatabase db;


    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_STATELOG_TABLE = "CREATE TABLE " + TABLE_LOGGING + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_DATE + " TEXT,"
                + KEY_TYPE + " TEXT" + ")";
        db.execSQL(CREATE_STATELOG_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOGGING);
        onCreate(db);
    }

    public void addData(DataStateLog dsl){
        db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_DATE, dsl.get_date());
        values.put(KEY_TYPE, dsl.get_type());

        db.insert(TABLE_LOGGING, null, values);
        db.close();

    }

    public List<DataStateLog> getAllStateLog(){
        String selectQuery = "SELECT * FROM " + TABLE_LOGGING + " ORDER BY " + KEY_DATE + " DESC LIMIT 50";
        db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if(cursor.moveToFirst()){
            do{
                DataStateLog dsl = new DataStateLog();
                dsl.set_id(Integer.parseInt(cursor.getString(0)));
                dsl.set_date(cursor.getString(1));
                dsl.set_type(cursor.getString(2));
                dataStateList.add(dsl);
            }while(cursor.moveToNext());
        }
        cursor.close();
        return dataStateList;
    }
}
