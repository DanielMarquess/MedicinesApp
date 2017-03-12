package com.example.android.medicines;

import android.Manifest;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.medicines.data.MedicineContract.MedicineEntry;
import com.example.android.medicines.databinding.ActivityCatalogBinding;

public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int MEDICINE_LOADER = 0;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_MEDIA = 10;
    private MedicineCursorAdapter mCursorAdapter;

    private TextView mMedicineNameCatalog;
    private TextView mMedicineQuantityCatalog;
    private TextView mMedicinePriceCatalog;

    ActivityCatalogBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_catalog);
        getWindow().getDecorView().setBackgroundColor(Color.WHITE);

        FloatingActionButton fab = binding.fab;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        mMedicineNameCatalog = binding.medicineNameCatalog;
        mMedicineQuantityCatalog = binding.medicineQuantityCatalog;
        mMedicinePriceCatalog = binding.medicinePriceCatalog;

        mMedicineNameCatalog.setVisibility(View.INVISIBLE);
        mMedicineQuantityCatalog.setVisibility(View.INVISIBLE);
        mMedicinePriceCatalog.setVisibility(View.INVISIBLE);

        ListView medicineListView = binding.list;
        View emptyView = binding.emptyView;
        medicineListView.setEmptyView(emptyView);

        mCursorAdapter = new MedicineCursorAdapter(this, null);
        medicineListView.setAdapter(mCursorAdapter);

        medicineListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                int permissionCheck = ContextCompat.checkSelfPermission(CatalogActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(CatalogActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_MEDIA);
                } else {
                    Uri currentMedicineUri = ContentUris.withAppendedId(MedicineEntry.CONTENT_URI, id);
                    Intent clickedMedicineIntent = new Intent(CatalogActivity.this, EditorActivity.class);
                    clickedMedicineIntent.setData(currentMedicineUri);
                    startActivity(clickedMedicineIntent);
                }
            }
        });
        getLoaderManager().initLoader(MEDICINE_LOADER, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete_all_entries:
                showDeleteConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {MedicineEntry._ID, MedicineEntry.COLUMN_MEDICINE_NAME,
                MedicineEntry.COLUMN_MEDICINE_QUANTITY, MedicineEntry.COLUMN_MEDICINE_PRICE};
        return new CursorLoader(this, MedicineEntry.CONTENT_URI, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mCursorAdapter.swapCursor(cursor);
        if (cursor.moveToFirst()) {
            int nameColumnIndex = cursor.getColumnIndex(MedicineEntry.COLUMN_MEDICINE_NAME);
            String currentMedicineName = cursor.getString(nameColumnIndex);
            if (!currentMedicineName.isEmpty()) {
                mMedicineNameCatalog.setVisibility(View.VISIBLE);
                mMedicineQuantityCatalog.setVisibility(View.VISIBLE);
                mMedicinePriceCatalog.setVisibility(View.VISIBLE);
            }
        } else {
            mMedicineNameCatalog.setVisibility(View.INVISIBLE);
            mMedicineQuantityCatalog.setVisibility(View.INVISIBLE);
            mMedicinePriceCatalog.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_all_dialog_msg);
        builder.setNegativeButton(R.string.delete_all_confirmation, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int i) {
                deleteAllPets();
            }
        });
        builder.setPositiveButton(R.string.delete_all_cancellation, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int i) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteAllPets() {
        getContentResolver().delete(MedicineEntry.CONTENT_URI, null, null);
        mMedicineNameCatalog.setVisibility(View.INVISIBLE);
        mMedicineQuantityCatalog.setVisibility(View.INVISIBLE);
        mMedicinePriceCatalog.setVisibility(View.INVISIBLE);
    }
}
