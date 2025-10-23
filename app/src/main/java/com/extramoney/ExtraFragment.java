package com.extramoney;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.*;

public class ExtraFragment extends Fragment {

    private Calendar selectedDate = Calendar.getInstance();
    private double totalJappa = 0;
    private List<Double> jappaValues = new ArrayList<>();
    private List<String[]> rowDataList = new ArrayList<>(); // Use string array for table
    private String lastDate = "";

    private EditText salesmanNo, barcode, millRate, billNo, jappa;
    private TextView dateText, totalJappaText, cutoffJappaText, balanceText, lastUpdated;
    private Button addBtn;
    private Switch qrSwitch, cutoffSwitch;
    private TableLayout tableLayout;
    private TableRow cutoffRow, balanceRow;
    private ImageView qrCodeImage;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_extra, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bind views
        salesmanNo = view.findViewById(R.id.salesmanNo);
        barcode = view.findViewById(R.id.barcode);
        millRate = view.findViewById(R.id.millRate);
        billNo = view.findViewById(R.id.billNo);
        jappa = view.findViewById(R.id.jappa);
        dateText = view.findViewById(R.id.dateText);
        addBtn = view.findViewById(R.id.addBtn);
        qrSwitch = view.findViewById(R.id.qrSwitch);
        cutoffSwitch = view.findViewById(R.id.cutoffSwitch);
        tableLayout = view.findViewById(R.id.tableLayout);
        totalJappaText = view.findViewById(R.id.totalJappaText);
        cutoffJappaText = view.findViewById(R.id.cutoffJappaText);
        balanceText = view.findViewById(R.id.balanceText);
        cutoffRow = view.findViewById(R.id.cutoffRow);
        balanceRow = view.findViewById(R.id.balanceRow);
        qrCodeImage = view.findViewById(R.id.qrCodeImage);
        lastUpdated = view.findViewById(R.id.lastUpdated);

        // Date picker dialog
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

            double jappaVal;
            try { jappaVal = Double.parseDouble(jp); }
            catch (Exception e) { Toast.makeText(getContext(), "Jappa must be a number", Toast.LENGTH_SHORT).show(); return; }

            lastDate = dateStr;
            jappaValues.add(jappaVal);
            rowDataList.add(new String[]{
                    String.valueOf(rowDataList.size() + 1), sNo, bc, mr, bill, dateStr, String.format(Locale.getDefault(), "%.2f", jappaVal)
            });

            // Add row visually before summary/footer
            TableRow tr = new TableRow(getContext());
            for (String cell : rowDataList.get(rowDataList.size() - 1))
                tr.addView(makeCell(getContext(), cell));
            int insertIdx = Math.max(tableLayout.getChildCount() - 3, 1);
            tableLayout.addView(tr, insertIdx);

            barcode.setText("");
            millRate.setText("");
            billNo.setText("");
            jappa.setText("");

            updateSummaryRows();
            updateQRCodeAndLastUpdated();
            saveTableData();
        });

        qrSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> updateQRCodeAndLastUpdated());
        cutoffSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateSummaryRows();
            updateQRCodeAndLastUpdated();
            saveTableData();
        });

        loadTableData(); // Load and visually re-create all rows and summary
        updateSummaryRows();
        updateQRCodeAndLastUpdated();
    }

    private void updateSummaryRows() {
        totalJappa = 0;
        for (double val : jappaValues) totalJappa += val;
        totalJappaText.setText(String.format(Locale.getDefault(), "%.2f", totalJappa));

        if (cutoffSwitch.isChecked()) {
            double cutoffAmt = totalJappa * 0.28;
            cutoffJappaText.setText(String.format(Locale.getDefault(), "%.2f", cutoffAmt));
            balanceText.setText(String.format(Locale.getDefault(), "%.2f", totalJappa - cutoffAmt));
            cutoffRow.setVisibility(View.VISIBLE);
            balanceRow.setVisibility(View.VISIBLE);
        } else {
            cutoffRow.setVisibility(View.GONE);
            balanceRow.setVisibility(View.GONE);
        }
    }

    private void updateQRCodeAndLastUpdated() {
        if (!qrSwitch.isChecked() || rowDataList.isEmpty()) {
            qrCodeImage.setVisibility(View.GONE);
            lastUpdated.setText("");
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (String[] row : rowDataList) {
            for (int i = 0; i < row.length; i++) {
                sb.append(row[i]);
                if (i < row.length - 1) sb.append(" | ");
            }
            sb.append("
");
        }
        sb.append("Total Jappa: ").append(String.format(Locale.getDefault(), "%.2f", totalJappa)).append("
");
        if (cutoffSwitch.isChecked()) {
            double cutoffAmt = totalJappa * 0.28;
            sb.append("Cut Off Jappa (28%): ").append(String.format(Locale.getDefault(), "%.2f", cutoffAmt)).append("
");
            sb.append("Balance Amount: ").append(String.format(Locale.getDefault(), "%.2f", totalJappa - cutoffAmt)).append("
");
        }

        try {
            BarcodeEncoder enc = new BarcodeEncoder();
            Bitmap bmp = enc.encodeBitmap(sb.toString(), BarcodeFormat.QR_CODE, 600, 600);
            qrCodeImage.setImageBitmap(bmp);
            qrCodeImage.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            qrCodeImage.setVisibility(View.GONE);
        }

        lastUpdated.setText(rowDataList.isEmpty() ? "" : ("Last updated: " + lastDate));
    }

    // Returns a boxed (bordered) TextView for a table cell
    private TextView makeCell(Context ctx, String text) {
        TextView tv = new TextView(ctx);
        tv.setText(text);
        tv.setPadding(8, 8, 8, 8);
        tv.setBackgroundResource(R.drawable.border_cell);
        return tv;
    }

    // --- Persistence ---
    private void saveTableData() {
        JSONArray json = new JSONArray();
        for (String[] row : rowDataList) {
            JSONArray arr = new JSONArray();
            for (String cell : row) arr.put(cell);
            json.put(arr);
        }
        requireContext().getSharedPreferences("jappa_prefs", Context.MODE_PRIVATE)
            .edit().putString("table", json.toString()).apply();
    }

    private void loadTableData() {
        SharedPreferences prefs = requireContext().getSharedPreferences("jappa_prefs", Context.MODE_PRIVATE);
        String data = prefs.getString("table", null);
        if (data == null) return;
        try {
            JSONArray json = new JSONArray(data);
            for (int i = 0; i < json.length(); i++) {
                JSONArray arr = json.getJSONArray(i);
                String[] row = new String[arr.length()];
                for (int j = 0; j < arr.length(); j++) row[j] = arr.getString(j);
                rowDataList.add(row);
                double val = Double.parseDouble(row[row.length - 1]);
                jappaValues.add(val);
                TableRow tr = new TableRow(getContext());
                for (String cell : row) tr.addView(makeCell(getContext(), cell));
                int insertIdx = Math.max(tableLayout.getChildCount() - 3, 1);
                tableLayout.addView(tr, insertIdx);
            }
            if (!rowDataList.isEmpty()) {
                lastDate = rowDataList.get(rowDataList.size()-1)[5];
            }
        } catch (Exception ignore) {}
    }
}
