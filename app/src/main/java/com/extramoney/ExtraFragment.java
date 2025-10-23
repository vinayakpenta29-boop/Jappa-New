package com.extramoney;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.util.Calendar;
import java.util.Locale;

public class ExtraFragment extends Fragment {

    private Calendar selectedDate = Calendar.getInstance();

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        // Inflate your sales table UI (should be res/layout/fragment_extra.xml)
        return inflater.inflate(R.layout.fragment_extra, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        EditText salesmanNo = view.findViewById(R.id.salesmanNo);
        EditText barcode = view.findViewById(R.id.barcode);
        EditText millRate = view.findViewById(R.id.millRate);
        EditText billNo = view.findViewById(R.id.billNo);
        EditText jappa = view.findViewById(R.id.jappa);
        TextView dateText = view.findViewById(R.id.dateText);
        Button addBtn = view.findViewById(R.id.addBtn);
        TableLayout tableLayout = view.findViewById(R.id.tableLayout);

        // Date picker dialog logic
        dateText.setOnClickListener(v -> {
            int year = selectedDate.get(Calendar.YEAR);
            int month = selectedDate.get(Calendar.MONTH);
            int day = selectedDate.get(Calendar.DAY_OF_MONTH);
            new DatePickerDialog(requireContext(), (picker, y, m, d) -> {
                selectedDate.set(y, m, d);
                dateText.setText(String.format(Locale.getDefault(), "%02d/%02d/%04d", d, m + 1, y));
            }, year, month, day).show();
        });

        addBtn.setOnClickListener(v -> {
            String sNo = salesmanNo.getText().toString().trim();
            String bc = barcode.getText().toString().trim();
            String mr = millRate.getText().toString().trim();
            String bill = billNo.getText().toString().trim();
            String jp = jappa.getText().toString().trim();
            String dateStr = dateText.getText().toString();

            if (sNo.isEmpty() || bc.isEmpty() || mr.isEmpty() || bill.isEmpty() || jp.isEmpty() || dateStr.equals("Tap to select date")) {
                Toast.makeText(getContext(), "Fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Add table row (before summary/footer)
            TableRow tr = new TableRow(getContext());
            tr.addView(makeCell(getContext(), String.valueOf(getCurrentRowNumber(tableLayout))));
            tr.addView(makeCell(getContext(), sNo));
            tr.addView(makeCell(getContext(), bc));
            tr.addView(makeCell(getContext(), mr));
            tr.addView(makeCell(getContext(), bill));
            tr.addView(makeCell(getContext(), dateStr));
            tr.addView(makeCell(getContext(), jp));

            // Insert before the last 3 summary/footer rows
            int footerIdx = Math.max(tableLayout.getChildCount() - 3, 1);
            tableLayout.addView(tr, footerIdx);

            // Optionally, clear inputs for new entry
            barcode.setText("");
            millRate.setText("");
            billNo.setText("");
            jappa.setText("");
        });
    }

    // Returns a boxed (bordered) TextView for a table cell
    private TextView makeCell(Context ctx, String text) {
        TextView tv = new TextView(ctx);
        tv.setText(text);
        tv.setPadding(8, 8, 8, 8);
        tv.setBackgroundResource(R.drawable.border_cell);
        return tv;
    }

    // Figure out the serial number for the new row (excluding header and footer rows)
    private int getCurrentRowNumber(TableLayout tableLayout) {
        // 1 header, 3 footer rows
        return Math.max(tableLayout.getChildCount() - 3, 1);
    }
}
