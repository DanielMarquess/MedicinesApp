package com.example.android.medicines.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.example.android.medicines.data.MedicineContract.MedicineEntry;

public class MedicineProvider extends ContentProvider {

    private MedicineDbHelper mDbHelper;

    public static final String LOG_TAG = MedicineProvider.class.getSimpleName();

    private static final int MEDICINES = 100;
    private static final int MEDICINES_ID = 101;
    private static UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(MedicineEntry.CONTENT_AUTHORITY, MedicineEntry.PATH_MEDICINES, MEDICINES);
        sUriMatcher.addURI(MedicineEntry.CONTENT_AUTHORITY, MedicineEntry.PATH_MEDICINES + "/#", MEDICINES_ID);
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new MedicineDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case MEDICINES:
                cursor = database.query(MedicineEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case MEDICINES_ID:
                selection = MedicineEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(MedicineEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MEDICINES:
                return insertMedicine(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertMedicine(Uri uri, ContentValues values) {

        String name = values.getAsString(MedicineEntry.COLUMN_MEDICINE_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Medicine requires a name");
        }

        Integer quantity = values.getAsInteger(MedicineEntry.COLUMN_MEDICINE_QUANTITY);
        if (quantity != null && quantity < 0 || quantity == null) {
            throw new IllegalArgumentException("Medicine requires a valid quantity");
        }
        Integer price = values.getAsInteger(MedicineEntry.COLUMN_MEDICINE_PRICE);
        if (price != null && price < 0 || price == null) {
            throw new IllegalArgumentException("Medicine requires a valid price");
        }

        Integer manufacturingMonth = values.getAsInteger(MedicineEntry.COLUMN_MEDICINE_MANUFACTURING_MONTH);
        if (manufacturingMonth != null && manufacturingMonth < 1 || manufacturingMonth != null &&
                manufacturingMonth > 12 || manufacturingMonth == null) {
            throw new IllegalArgumentException("Medicine requires a valid manufacturing month");
        }

        Integer manufacturingYear = values.getAsInteger(MedicineEntry.COLUMN_MEDICINE_MANUFACTURING_YEAR);
        if (manufacturingYear != null && manufacturingYear < 0 || manufacturingYear != null &&
                manufacturingYear < 2000 || manufacturingYear != null && manufacturingYear > 9999
                || manufacturingYear == null) {
            throw new IllegalArgumentException("Medicine requires a valid manufacturing year");
        }

        Integer expiryMonth = values.getAsInteger(MedicineEntry.COLUMN_MEDICINE_EXPIRY_MONTH);
        if (expiryMonth != null && expiryMonth < 1 || expiryMonth != null && expiryMonth > 12 || expiryMonth == null) {
            throw new IllegalArgumentException("Medicine requires a valid expiry month");
        }

        Integer expiryYear = values.getAsInteger(MedicineEntry.COLUMN_MEDICINE_EXPIRY_YEAR);
        if (expiryYear != null && expiryYear < 0 || expiryYear != null && expiryYear < 2000 ||
                expiryYear != null && expiryYear > 9999 || expiryYear == null) {
            throw new IllegalArgumentException("Medicine requires a valid expiry year");
        }

        String emailFromSupplier = values.getAsString(MedicineEntry.COLUMN_MEDICINE_EMAIL_SUPPLIER);
        if (emailFromSupplier == null) {
            throw new IllegalArgumentException("Medicine requires a email from the supplier");
        }
        byte[] imageView = values.getAsByteArray(MedicineEntry.COLUMN_MEDICINE_IMAGE);
        if (imageView == null) {
            throw new IllegalArgumentException("Medicine requires an image");
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        long id = database.insert(MedicineEntry.TABLE_NAME, null, values);

        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MEDICINES:
                return updateMedicine(uri, contentValues, selection, selectionArgs);
            case MEDICINES_ID:
                selection = MedicineEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateMedicine(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateMedicine(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        if (values.containsKey(MedicineEntry.COLUMN_MEDICINE_NAME)) {
            String name = values.getAsString(MedicineEntry.COLUMN_MEDICINE_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Medicine requires a name");
            }
        }
        if (values.containsKey(MedicineEntry.COLUMN_MEDICINE_QUANTITY)) {
            Integer quantity = values.getAsInteger(MedicineEntry.COLUMN_MEDICINE_QUANTITY);
            if (quantity != null && quantity < 0 || quantity == null) {
                throw new IllegalArgumentException("Medicine requires a valid quantity");
            }
        }
        if (values.containsKey(MedicineEntry.COLUMN_MEDICINE_PRICE)) {
            Integer price = values.getAsInteger(MedicineEntry.COLUMN_MEDICINE_PRICE);
            if (price != null && price < 0) {
                throw new IllegalArgumentException("Medicine requires a valid price");
            }
        }

        if (values.containsKey(MedicineEntry.COLUMN_MEDICINE_MANUFACTURING_MONTH)) {
            Integer manufacturingMonth = values.getAsInteger(MedicineEntry.COLUMN_MEDICINE_MANUFACTURING_MONTH);
            if (manufacturingMonth != null && manufacturingMonth < 1 || manufacturingMonth != null && manufacturingMonth > 12
                    || manufacturingMonth == null) {
                throw new IllegalArgumentException("Medicine requires a valid manufacturing month");
            }
        }

        if (values.containsKey(MedicineEntry.COLUMN_MEDICINE_MANUFACTURING_YEAR)) {
            Integer manufacturingYear = values.getAsInteger(MedicineEntry.COLUMN_MEDICINE_MANUFACTURING_YEAR);
            if (manufacturingYear != null && manufacturingYear < 0 || manufacturingYear == null) {
                throw new IllegalArgumentException("Medicine requires a valid manufacturing year");
            }
        }

        if (values.containsKey(MedicineEntry.COLUMN_MEDICINE_EXPIRY_MONTH)) {
            Integer expiryMonth = values.getAsInteger(MedicineEntry.COLUMN_MEDICINE_EXPIRY_MONTH);
            if (expiryMonth != null && expiryMonth < 1 || expiryMonth != null && expiryMonth > 12 || expiryMonth == null) {
                throw new IllegalArgumentException("Medicine requires a valid expiry month");
            }
        }

        if (values.containsKey(MedicineEntry.COLUMN_MEDICINE_EXPIRY_YEAR)) {
            Integer expiryYear = values.getAsInteger(MedicineEntry.COLUMN_MEDICINE_EXPIRY_YEAR);
            if (expiryYear != null && expiryYear < 0 || expiryYear == null) {
                throw new IllegalArgumentException("Medicine requires a valid expiry year");
            }
        }

        if (values.containsKey(MedicineEntry.COLUMN_MEDICINE_EMAIL_SUPPLIER)) {
            String emailFromSupplier = values.getAsString(MedicineEntry.COLUMN_MEDICINE_EMAIL_SUPPLIER);
            if (emailFromSupplier == null) {
                throw new IllegalArgumentException("Medicine requires an email from the supplier");
            }
        }

        if (values.containsKey(MedicineEntry.COLUMN_MEDICINE_IMAGE)) {
            byte[] imageView = values.getAsByteArray(MedicineEntry.COLUMN_MEDICINE_IMAGE);
            if (imageView == null) {
                throw new IllegalArgumentException("Medicine requires an image");
            }
        }

        if (values.size() == 0) {
            return 0;
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int rowsUpdated = database.update(MedicineEntry.TABLE_NAME, values, selection, selectionArgs);
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MEDICINES:
                rowsDeleted = database.delete(MedicineEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case MEDICINES_ID:
                selection = MedicineEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(MedicineEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MEDICINES:
                return MedicineEntry.CONTENT_LIST_TYPE;
            case MEDICINES_ID:
                return MedicineEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}
