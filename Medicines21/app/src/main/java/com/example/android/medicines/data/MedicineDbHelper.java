package com.example.android.medicines.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.medicines.data.MedicineContract.MedicineEntry;

public class MedicineDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "pharmacyyy.db";
    public static final int DATABASE_VERSION = 1;
    private static final String COMMA_SEP = ", ";
    private static final String TEXT_TYPE = " TEXT";
    private static final String INTEGER_TYPE = " INTEGER";
    private static final String BLOB_TYPE = " BLOB";
    private static final String NOT_NULL = " NOT NULL";
    private static final String PRIMARY_KEY = " PRIMARY KEY";
    private static final String AUTO_INCREMENT = " AUTOINCREMENT";

    public MedicineDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_MEDICINES_TABLE = "CREATE TABLE " + MedicineEntry.TABLE_NAME + " (" +
                MedicineEntry._ID + INTEGER_TYPE + PRIMARY_KEY + AUTO_INCREMENT + COMMA_SEP +
                MedicineEntry.COLUMN_MEDICINE_NAME + TEXT_TYPE + NOT_NULL + COMMA_SEP + MedicineEntry.COLUMN_MEDICINE_QUANTITY +
                INTEGER_TYPE + NOT_NULL + COMMA_SEP + MedicineEntry.COLUMN_MEDICINE_PRICE + INTEGER_TYPE + NOT_NULL + COMMA_SEP +
                MedicineEntry.COLUMN_MEDICINE_MANUFACTURING_MONTH + INTEGER_TYPE + NOT_NULL + COMMA_SEP +
                MedicineEntry.COLUMN_MEDICINE_MANUFACTURING_YEAR + INTEGER_TYPE + NOT_NULL + COMMA_SEP +
                MedicineEntry.COLUMN_MEDICINE_EXPIRY_MONTH + INTEGER_TYPE + NOT_NULL + COMMA_SEP +
                MedicineEntry.COLUMN_MEDICINE_EXPIRY_YEAR + INTEGER_TYPE + NOT_NULL + COMMA_SEP +
                MedicineEntry.COLUMN_MEDICINE_EMAIL_SUPPLIER +
                TEXT_TYPE + NOT_NULL + COMMA_SEP + MedicineEntry.COLUMN_MEDICINE_IMAGE + BLOB_TYPE + ")";
        db.execSQL(SQL_CREATE_MEDICINES_TABLE);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}