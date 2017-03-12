package com.example.android.medicines;

import android.Manifest;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.medicines.data.MedicineContract.MedicineEntry;
import com.example.android.medicines.databinding.ActivityEditorBinding;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

import static com.example.android.medicines.DbBitmapUtil.getBytes;
import static com.example.android.medicines.DbBitmapUtil.getImage;


public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private EditText mNameEditText;
    private EditText mQuantityEditText;
    private EditText mPriceEditText;
    private EditText mManufMonthEditText;
    private EditText mManufYearEditText;
    private EditText mExpiryMonthEditText;
    private EditText mExpiryYearEditText;
    private EditText mEmailFromSupplierEditText;
    private ImageView mImageView;

    private String mStringImageUri;

    private Uri mCurrentMedicineUri;

    private static final int EXISTING_PET_LOADER = 0;

    private boolean mMedicineHasChanged = false;

    private int mIncrementableQuantity = 1;

    private Button mBuyButton;
    private Button mSellButton;

    private String mStringIncrementableQantity;

    private String quitDialogMessage;
    private String keepDialogpMessage;
    private String stopDialogMessage;

    private static final int IMAGE_GALLERY_REQUEST = 20;
    private static final int MY_PERMISSIONS_REQUEST_READ_MEDIA = 1;

    private Bitmap mImage;

    ActivityEditorBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_editor);

        mNameEditText = binding.editableFields.medicineName;
        mQuantityEditText = binding.editableFields.medicineQuantity;
        mPriceEditText = binding.editableFields.medicinePrice;
        mManufMonthEditText = binding.editableFields.manufMonth;
        mManufYearEditText = binding.editableFields.manufYear;
        mExpiryMonthEditText = binding.editableFields.expiryMonth;
        mExpiryYearEditText = binding.editableFields.expiryYear;
        mEmailFromSupplierEditText = binding.editableFields.emailSupplier;
        mImageView = binding.imageView;

        mBuyButton = binding.editableFields.buyButton;
        mBuyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                increment(mBuyButton);
            }
        });
        mSellButton = binding.editableFields.sellButton;
        mSellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                decrement(mSellButton);
            }
        });
        Button mToOrderButton = binding.editableFields.toOrderButton;
        Button mSelectImageBtn = binding.selectImageBtn;
        mSelectImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onImageGalleryClicked(view);
            }
        });
        mToOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toOrderEmail();
            }
        });

        Intent intent = getIntent();
        mCurrentMedicineUri = intent.getData();
        titleDefinition();

        mNameEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mManufMonthEditText.setOnTouchListener(mTouchListener);
        mManufYearEditText.setOnTouchListener(mTouchListener);
        mExpiryMonthEditText.setOnTouchListener(mTouchListener);
        mExpiryYearEditText.setOnTouchListener(mTouchListener);
        mEmailFromSupplierEditText.setOnTouchListener(mTouchListener);
        mSellButton.setOnTouchListener(mTouchListener);
        mBuyButton.setOnTouchListener(mTouchListener);
    }

    private void toOrderEmail() {
        String toOrderEmail = getString(R.string.to_order_email);
        toOrderEmail += " " + mQuantityEditText.getText().toString().trim() + " " +
                mNameEditText.getText().toString().trim();
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]
                {mEmailFromSupplierEditText.getText().toString().trim()});
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject));
        intent.putExtra(Intent.EXTRA_TEXT, toOrderEmail);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    private void titleDefinition() {
        if (mCurrentMedicineUri == null) {
            setTitle(R.string.editor_activity_title_new_medicine);
            mQuantityEditText.setText("1");
        } else {
            setTitle(R.string.editor_activity_title_edit_medicine);
            getLoaderManager().initLoader(EXISTING_PET_LOADER, null, this);
        }
    }

    private void showDataNotValidDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.data_not_valid_dialog_begin);

        builder.setPositiveButton(R.string.ok_dialog_message, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface d, int id) {
                if (d != null) {
                    d.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void saveMedicine() {
        String nameString;
        int medicineQuantity;
        int intMedicinePrice;
        int manufMonth;
        int manufYear;
        int expiryMonth;
        int expiryYear;
        String emailFromSupplier;

        if (!TextUtils.isEmpty(mNameEditText.getText().toString().trim()) &&
                !TextUtils.isEmpty(mQuantityEditText.getText().toString().trim()) &&
                !TextUtils.isEmpty(mPriceEditText.getText().toString().trim()) &&
                !TextUtils.isEmpty(mManufMonthEditText.getText().toString().trim()) &&
                !TextUtils.isEmpty(mManufYearEditText.getText().toString().trim()) &&
                !TextUtils.isEmpty(mExpiryMonthEditText.getText().toString().trim()) &&
                !TextUtils.isEmpty(mExpiryYearEditText.getText().toString().trim()) &&
                !TextUtils.isEmpty(mEmailFromSupplierEditText.getText().toString().trim())) {

            nameString = mNameEditText.getText().toString().trim();
            medicineQuantity = Integer.parseInt(mQuantityEditText.getText().toString().trim());

            float floatMedicinePrice = Float.parseFloat(mPriceEditText.getText().toString().trim());
            floatMedicinePrice = floatMedicinePrice * 100;
            intMedicinePrice = (int) floatMedicinePrice;

            manufMonth = Integer.parseInt(mManufMonthEditText.getText().toString().trim());
            manufYear = Integer.parseInt(mManufYearEditText.getText().toString().trim());
            expiryMonth = Integer.parseInt(mExpiryMonthEditText.getText().toString().trim());
            expiryYear = Integer.parseInt(mExpiryYearEditText.getText().toString().trim());
            emailFromSupplier = mEmailFromSupplierEditText.getText().toString().trim();

            if (medicineQuantity >= 1 &&
                    intMedicinePrice > 0 && manufMonth > 0 && manufMonth < 13 & expiryMonth > 0 &&
                    expiryMonth < 13 && manufYear > 2000 && manufYear < 10000 && expiryYear > 2000
                    && expiryYear < 10000) {

                ContentValues values = new ContentValues();
                values.put(MedicineEntry.COLUMN_MEDICINE_NAME, nameString);
                values.put(MedicineEntry.COLUMN_MEDICINE_QUANTITY, medicineQuantity);
                values.put(MedicineEntry.COLUMN_MEDICINE_PRICE, intMedicinePrice);
                values.put(MedicineEntry.COLUMN_MEDICINE_MANUFACTURING_MONTH, manufMonth);
                values.put(MedicineEntry.COLUMN_MEDICINE_MANUFACTURING_YEAR, manufYear);
                values.put(MedicineEntry.COLUMN_MEDICINE_EXPIRY_MONTH, expiryMonth);
                values.put(MedicineEntry.COLUMN_MEDICINE_EXPIRY_YEAR, expiryYear);
                values.put(MedicineEntry.COLUMN_MEDICINE_EMAIL_SUPPLIER, emailFromSupplier);

                if (mCurrentMedicineUri == null && !TextUtils.isEmpty(mStringImageUri)) {
                    byte[] imageByte = getBytes(mImage);
                    values.put(MedicineEntry.COLUMN_MEDICINE_IMAGE, imageByte);
                    insertMedicines(values);
                } else if (mCurrentMedicineUri == null && TextUtils.isEmpty(mStringImageUri)) {
                    showDataNotValidDialog();
                } else if (mCurrentMedicineUri != null && TextUtils.isEmpty(mStringImageUri)) {
                    updateMedicines(values);
                } else {
                    byte[] imageByte = getBytes(mImage);
                    values.put(MedicineEntry.COLUMN_MEDICINE_IMAGE, imageByte);
                    updateMedicines(values);
                }
            } else {
                showDataNotValidDialog();
            }
        } else {
            showDataNotValidDialog();
        }
    }

    private void insertMedicines(ContentValues values) {
        Uri newUri = getContentResolver().insert(MedicineEntry.CONTENT_URI, values);
        if (newUri == null) {
            Toast.makeText(this, getString(R.string.editor_insert_medicine_failed),
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.editor_insert_medicine_successful),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void updateMedicines(ContentValues values) {
        int rowsAffected = getContentResolver().update(mCurrentMedicineUri, values, null, null);
        if (rowsAffected == 0) {
            Toast.makeText(this, getString(R.string.editor_update_medicine_failed),
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.editor_update_medicine_successful),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveMedicine();
                return true;

            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;

            case android.R.id.home:
                if (!mMedicineHasChanged) {
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                }
                if (mCurrentMedicineUri == null) {
                    quitDialogMessage = getString(R.string.quit_adding_dialog_msg);
                    stopDialogMessage = getString(R.string.stop_adding_dialog_msg);
                    keepDialogpMessage = getString(R.string.keep_adding_dialog_msg);
                } else {
                    quitDialogMessage = getString(R.string.quit_editing_dialog_msg);
                    stopDialogMessage = getString(R.string.stop_editing_dialog_msg);
                    keepDialogpMessage = getString(R.string.keep_editing_dialog_msg);
                }
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };
                showExitWithChangesDialog(discardButtonClickListener, quitDialogMessage, stopDialogMessage,
                        keepDialogpMessage);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {MedicineEntry._ID, MedicineEntry.COLUMN_MEDICINE_NAME, MedicineEntry.COLUMN_MEDICINE_QUANTITY,
                MedicineEntry.COLUMN_MEDICINE_PRICE, MedicineEntry.COLUMN_MEDICINE_MANUFACTURING_MONTH,
                MedicineEntry.COLUMN_MEDICINE_MANUFACTURING_YEAR, MedicineEntry.COLUMN_MEDICINE_EXPIRY_MONTH,
                MedicineEntry.COLUMN_MEDICINE_EXPIRY_YEAR, MedicineEntry.COLUMN_MEDICINE_EMAIL_SUPPLIER,
                MedicineEntry.COLUMN_MEDICINE_IMAGE};

        return new CursorLoader(this, mCurrentMedicineUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor.moveToFirst()) {
            int nameColumnIndex = cursor.getColumnIndex(MedicineEntry.COLUMN_MEDICINE_NAME);
            String currentMedicineName = cursor.getString(nameColumnIndex);
            mNameEditText.setText(currentMedicineName);

            int quantityColumnIndex = cursor.getColumnIndex(MedicineEntry.COLUMN_MEDICINE_QUANTITY);
            String currentQuantity = String.valueOf(cursor.getInt(quantityColumnIndex));
            mQuantityEditText.setText(currentQuantity);

            int priceColumnIndex = cursor.getColumnIndex(MedicineEntry.COLUMN_MEDICINE_PRICE);
            int intCurrentPrice = cursor.getInt(priceColumnIndex);
            float floatCurrentPrice = (float) intCurrentPrice;
            floatCurrentPrice = floatCurrentPrice / 100;
            String StringCurrentPrice = String.valueOf(floatCurrentPrice);
            mPriceEditText.setText(StringCurrentPrice);

            int manufMonthColumnIndex = cursor.getColumnIndex(MedicineEntry.COLUMN_MEDICINE_MANUFACTURING_MONTH);
            String currentManufMonth = String.valueOf(cursor.getInt(manufMonthColumnIndex));
            mManufMonthEditText.setText(currentManufMonth);

            int manufYearColumnIndex = cursor.getColumnIndex(MedicineEntry.COLUMN_MEDICINE_MANUFACTURING_YEAR);
            String currentManufYear = String.valueOf(cursor.getInt(manufYearColumnIndex));
            mManufYearEditText.setText(currentManufYear);

            int expiryMonthColumnIndex = cursor.getColumnIndex(MedicineEntry.COLUMN_MEDICINE_EXPIRY_MONTH);
            String currentExpiryMonth = String.valueOf(cursor.getInt(expiryMonthColumnIndex));
            mExpiryMonthEditText.setText(currentExpiryMonth);

            int expiryYearColumnIndex = cursor.getColumnIndex(MedicineEntry.COLUMN_MEDICINE_EXPIRY_YEAR);
            String currentExpiryYear = String.valueOf(cursor.getInt(expiryYearColumnIndex));
            mExpiryYearEditText.setText(currentExpiryYear);

            int emailColumnIndex = cursor.getColumnIndex(MedicineEntry.COLUMN_MEDICINE_EMAIL_SUPPLIER);
            String currentEmailFromSupplier = cursor.getString(emailColumnIndex);
            mEmailFromSupplierEditText.setText(currentEmailFromSupplier);

            int imageColumnIndex = cursor.getColumnIndex(MedicineEntry.COLUMN_MEDICINE_IMAGE);
            byte[] currentImagebyte = cursor.getBlob(imageColumnIndex);
            Bitmap image = getImage(currentImagebyte);
            mImageView.setImageBitmap(image);
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {
        mNameEditText.getText().clear();
        mQuantityEditText.getText().clear();
        mPriceEditText.getText().clear();
        mManufMonthEditText.getText().clear();
        mManufYearEditText.getText().clear();
        mExpiryMonthEditText.getText().clear();
        mExpiryYearEditText.getText().clear();
        mEmailFromSupplierEditText.getText().clear();
    }

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mMedicineHasChanged = true;
            return false;
        }
    };

    @Override
    public void onBackPressed() {
        if (!mMedicineHasChanged) {
            super.onBackPressed();
            return;
        }
        if (mCurrentMedicineUri == null) {
            quitDialogMessage = getString(R.string.quit_adding_dialog_msg);
            stopDialogMessage = getString(R.string.stop_adding_dialog_msg);
            keepDialogpMessage = getString(R.string.keep_adding_dialog_msg);
        } else {
            quitDialogMessage = getString(R.string.quit_editing_dialog_msg);
            stopDialogMessage = getString(R.string.stop_editing_dialog_msg);
            keepDialogpMessage = getString(R.string.keep_editing_dialog_msg);
        }
        DialogInterface.OnClickListener stopEditingButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };
        showExitWithChangesDialog(stopEditingButtonClickListener, quitDialogMessage, stopDialogMessage,
                keepDialogpMessage);
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (mCurrentMedicineUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    private void showExitWithChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener, String quitDialogMessage,
            String stopMessage, String keepMessage) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(quitDialogMessage);
        builder.setPositiveButton(stopMessage, discardButtonClickListener);
        builder.setNegativeButton(keepMessage, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(EditorActivity.this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                deleteMedicine();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteMedicine() {
        if (mCurrentMedicineUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentMedicineUri, null, null);
            if (rowsDeleted == 0) {
                Toast.makeText(this, R.string.editor_delete_medicine_failed, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, R.string.editor_delete_medicine_successful, Toast.LENGTH_LONG).show();
            }
        }
        finish();
    }

    public void increment(View view) {
        mStringIncrementableQantity = mQuantityEditText.getText().toString().trim();

        if (TextUtils.isEmpty(mStringIncrementableQantity)) {
            mStringIncrementableQantity = "0";
        }

        mIncrementableQuantity = Integer.parseInt(mStringIncrementableQantity);
        mIncrementableQuantity = mIncrementableQuantity + 1;

        String quantityResult = String.valueOf(mIncrementableQuantity);

        mQuantityEditText.setText(quantityResult);
    }

    public void decrement(View view) {

        mStringIncrementableQantity = mQuantityEditText.getText().toString().trim();

        String quantityResult;

        if (TextUtils.isEmpty(mStringIncrementableQantity)) {
            mStringIncrementableQantity = "1";
        }

        mIncrementableQuantity = Integer.parseInt(mStringIncrementableQantity);

        if (mIncrementableQuantity > 1) {
            mIncrementableQuantity = mIncrementableQuantity - 1;
        } else {
            Toast.makeText(this, R.string.quantity_less_than_one_toast, Toast.LENGTH_LONG).show();
        }
        quantityResult = String.valueOf(mIncrementableQuantity);
        mQuantityEditText.setText(quantityResult);
    }

    public void onImageGalleryClicked(View view) {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_MEDIA);
        } else {
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);

            File pictureDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            String pictureDirectoryPath = pictureDirectory.getPath();

            Uri data = Uri.parse(pictureDirectoryPath);
            photoPickerIntent.setDataAndType(data, "image/*");

            startActivityForResult(photoPickerIntent, IMAGE_GALLERY_REQUEST);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_GALLERY_REQUEST) {
                Uri imageUri = data.getData();
                mStringImageUri = imageUri.toString();
                try {
                    InputStream inputStream = getContentResolver().openInputStream(imageUri);
                    mImage = BitmapFactory.decodeStream(inputStream);
                    mImageView.setImageBitmap(mImage);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(this, R.string.unable_open_image_toast, Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}

