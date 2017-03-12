package com.example.android.medicines.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class MedicineContract {

    public static final class MedicineEntry implements BaseColumns {

        public static final String TABLE_NAME = "medicines";

        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_MEDICINE_NAME = "name";
        public static final String COLUMN_MEDICINE_QUANTITY = "quantity";
        public static final String COLUMN_MEDICINE_PRICE = "price";
        public static final String COLUMN_MEDICINE_MANUFACTURING_MONTH = "manufacturing_month";
        public static final String COLUMN_MEDICINE_MANUFACTURING_YEAR = "manufacturing_year";
        public static final String COLUMN_MEDICINE_EXPIRY_MONTH = "expiry_month";
        public static final String COLUMN_MEDICINE_EXPIRY_YEAR = "expiry_year";
        public static final String COLUMN_MEDICINE_EMAIL_SUPPLIER = "email_supplier";
        public static final String COLUMN_MEDICINE_IMAGE = "medicine_image";

        public static final String CONTENT_AUTHORITY = "com.example.android.medicines";
        public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
        public static final String PATH_MEDICINES = "medicines";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_MEDICINES);

        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MEDICINES;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MEDICINES;
    }
}
