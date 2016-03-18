package com.smithahm.ratetheinstructor;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.sql.SQLData;
import java.sql.SQLDataException;
import java.sql.SQLException;

public class InstructorDatabaseAdapter {

    InstructorDatabase insdatabase;

    public InstructorDatabaseAdapter(Context context){
        insdatabase = new InstructorDatabase(context);
    }

    public Long insertData(String fname, String lname, String office, String phone,String email){
        SQLiteDatabase db= insdatabase.getWritableDatabase();
        ContentValues insValue = new ContentValues();
        insValue.put(InstructorDatabase.FIRSTNAME, fname);
        insValue.put(InstructorDatabase.LASTNAME, lname);
        insValue.put(InstructorDatabase.OFFICE, office);
        insValue.put(InstructorDatabase.PHONE, phone);
        insValue.put(InstructorDatabase.EMAIL, email);
        long id = db.insert(InstructorDatabase.DETAIL_TABLE, null, insValue);
        return id;
    }

    public String getAlldata(){
        SQLiteDatabase db = insdatabase.getWritableDatabase();
        String[] columns = {InstructorDatabase.FIRSTNAME,InstructorDatabase.LASTNAME,InstructorDatabase.OFFICE,InstructorDatabase.PHONE,InstructorDatabase.EMAIL};
        StringBuffer buffer = new StringBuffer();
        Cursor cursor = db.query(InstructorDatabase.DETAIL_TABLE,columns,null,null,null,null,null);

        while(cursor.moveToNext()){
            int cid = cursor.getInt(0);
            String fname = cursor.getString(1);
            buffer.append(cid + " " + fname);
        }
        return buffer.toString();
    }

    public void close(){
        SQLiteDatabase db = insdatabase.getWritableDatabase();
        db.close();
    }


    class InstructorDatabase extends SQLiteOpenHelper{
        private Context context;
        private static final String DATABASE_NAME = "instdatabase";
        private static final String DETAIL_TABLE = "INSDETTABLE";
        private static final String RATE_TABLE = "RATETABLE";
        private static final int DATABASE_VERSION = 12;
        private static final String UID = "_id";
        private static final String ID = "instructorId";
        private static final String FIRSTNAME = "Fname";
        private static final String LASTNAME = "Lname";
        private static final String OFFICE = "office";
        private static final String PHONE = "phone";
        private static final String EMAIL = "email";
        private static final String AVGRATE = "AvgRate";
        private static final String TOTRATE = "TotRate";

        private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS "+DETAIL_TABLE+" ( "+UID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+FIRSTNAME+" VARCHAR(255) NOT NULL, "+LASTNAME+" VARCHAR(255), "+OFFICE+" VARCHAR(255), "+PHONE+" VARCHAR(255), "+EMAIL+" VARCHAR(255));";
        private static final String DROP_TABLE ="DROP TABLE IF EXISTS "+DETAIL_TABLE;
        private static final String DROP_RATE_TABLE ="DROP TABLE IF EXISTS "+RATE_TABLE;
        private static final String CREATE_RATE_TABLE = "CREATE TABLE IF NOT EXISTS "+RATE_TABLE+" ( "+AVGRATE+" VARCHAR(255) NOT NULL, "+TOTRATE+" VARCHAR(255), "+ID+" REFERENCES "+DETAIL_TABLE+" ("+UID+"));";

        public InstructorDatabase(Context context){
            super(context, DATABASE_NAME,null,DATABASE_VERSION);
            this.context = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_TABLE);
            db.execSQL(CREATE_RATE_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(DROP_TABLE);
            db.execSQL(DROP_RATE_TABLE);
            onCreate(db);
        }
    }
}
