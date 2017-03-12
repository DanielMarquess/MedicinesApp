package com.example.android.medicines;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.medicines.data.MedicineContract.MedicineEntry;

public class MedicineCursorAdapter extends CursorAdapter {

    public MedicineCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    private static class ViewHolderItem {
        TextView nameTextView;
        TextView quantityTextView;
        TextView priceTextView;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
        ViewHolderItem viewHolder = new ViewHolderItem();
        viewHolder.nameTextView = (TextView) view.findViewById(R.id.name);
        viewHolder.quantityTextView = (TextView) view.findViewById(R.id.quantity);
        viewHolder.priceTextView = (TextView) view.findViewById(R.id.price);

        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolderItem viewHolder = (ViewHolderItem) view.getTag();

        String medicineName = cursor.getString(cursor.getColumnIndexOrThrow(MedicineEntry.COLUMN_MEDICINE_NAME));
        String medicineQuantity = cursor.getString(cursor.getColumnIndexOrThrow(MedicineEntry.COLUMN_MEDICINE_QUANTITY));

        float floatMedicinePrice;

        int priceColumnIndex = cursor.getColumnIndexOrThrow(MedicineEntry.COLUMN_MEDICINE_PRICE);
        int intMedicinePrice = cursor.getInt(priceColumnIndex);
        floatMedicinePrice = (float) intMedicinePrice;
        floatMedicinePrice = floatMedicinePrice / 100;
        String stringMedicinePrice = String.valueOf(floatMedicinePrice);

        viewHolder.nameTextView.setText(medicineName);
        viewHolder.quantityTextView.setText(medicineQuantity);
        viewHolder.priceTextView.setText(stringMedicinePrice);
    }
}
