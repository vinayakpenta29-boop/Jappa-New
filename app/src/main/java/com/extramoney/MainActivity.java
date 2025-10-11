package com.extramoney;

import android.app.DatePickerDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends AppCompatActivity {

    private TableLayout tableLayout;
    private EditText salesmanNo, barcode, millRate, billNo, jappa;
    private TextView dateText;
    private Button addBtn;
    private Switch qrSwitch, cutoffSwitch;
    private ImageView qrCodeImage;
    private TextView lastUpdated, totalJappaText, cutoffJappaText, balanceText;

    private double totalJappa = 0;
    private int rowCount = 0;
    private final List<Salesman> dataList = new ArrayList<>();
    private String lastDate = "";
    private Calendar selectedDate = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        salesmanNo = findViewById(R.id.salesmanNo);
        barcode = findViewById(R.id.barcode);
        millRate = findViewById(R.id.millRate);
        billNo = findViewById(R.id.billNo);
        jappa = findViewById(R.id.jappa);
        dateText = findViewById(R.id.dateText);
        addBtn = findViewById(R.id.addBtn);
        qrSwitch = findViewById(R.id.qrSwitch);
        cutoffSwitch = findViewById(R.id.cutoffSwitch);
        qrCodeImage = findViewById(R.id.qrCodeImage);
        lastUpdated = findViewById(R.id.lastUpdated);
        totalJappaText = findViewById(R.id.totalJappaText);
        cutoffJappaText = findViewById(R.id.cutoffJappaText);
        balanceText = findViewById(R.id.balanceText);
        tableLayout = findViewById(R.id.tableLayout);

        dateText.setOnClickListener(v -> {
            int year = selectedDate.get(Calendar.YEAR);
            int month = selectedDate.get(Calendar.MONTH);
            int day = selectedDate.get(Calendar.DAY_OF_MONTH);
            new DatePickerDialog(MainActivity.this, (view, y, m, d) -> {
                selectedDate.set(y, m, d);
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                dateText.setText(sdf.format(selectedDate.getTime()));
            }, year, month, day).show();
        });

        addBtn.setOnClickListener(v -> addRow());
        qrSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> updateQRCode());
        cutoffSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateTotals();
            updateQRCode();
        });
        updateTotals();
    }

    private void addRow() {
        String sNo = salesmanNo.getText().toString().trim();
        String bc = barcode.getText().toString().trim();
        String mr = millRate.getText().toString().trim();
        String bill = billNo.getText().toString().trim();
        String jp = jappa.getText().toString().trim();
        String dateStr = dateText.getText().toString();

        if (sNo.isEmpty() || bc.isEmpty() || mr.isEmpty() || bill.isEmpty() || jp.isEmpty() || dateStr.equals("Tap to select date")) {
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double jappaVal;
        try {
            jappaVal = Double.parseDouble(jp);
        } catch (Exception e) {
            Toast.makeText(this, "Jappa must be a number", Toast.LENGTH_SHORT).show();
            return;
        }

        rowCount++;
        lastDate = dateStr;
        totalJappa += jappaVal;

        Salesman s = new Salesman(rowCount, sNo, bc, mr, bill, dateStr, jappaVal);
        dataList.add(s);

        TableRow tr = new TableRow(this);
        for (String val : s.toArray()) {
            TextView tv = new TextView(this);
            tv.setText(val);
            tv.setPadding(8, 8, 8, 8);
            tr.addView(tv);
        }
        tableLayout.addView(tr, tableLayout.getChildCount() - 1);

        updateTotals();
        updateQRCode();

        barcode.setText("");
        millRate.setText("");
        billNo.setText("");
        jappa.setText("");
        // Don't clear the date/salesman number for convenient multiple entries
    }

    private void updateTotals() {
        totalJappaText.setText("Total Jappa: " + String.format(Locale.getDefault(), "%.2f", totalJappa));
        if (cutoffSwitch.isChecked()) {
            double cutoffAmt = totalJappa * 0.28;
            cutoffJappaText.setText("Cut Off Jappa (28%): " + String.format(Locale.getDefault(), "%.2f", cutoffAmt));
            balanceText.setText("Balance Amount: " + String.format(Locale.getDefault(), "%.2f", totalJappa - cutoffAmt));
            cutoffJappaText.setVisibility(View.VISIBLE);
            balanceText.setVisibility(View.VISIBLE);
        } else {
            cutoffJappaText.setVisibility(View.GONE);
            balanceText.setVisibility(View.GONE);
        }
    }

    private void updateQRCode() {
        if (!qrSwitch.isChecked() || dataList.isEmpty()) {
            qrCodeImage.setVisibility(View.GONE);
            lastUpdated.setText("");
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (Salesman s : dataList) sb.append(s.toDelimitedString()).append("");
        sb.append("Total Jappa: ").append(String.format(Locale.getDefault(), "%.2f", totalJappa)).append("");
        if (cutoffSwitch.isChecked()) {
            double cutoffAmt = totalJappa * 0.28;
            sb.append("Cut Off Jappa (28%): ").append(String.format(Locale.getDefault(), "%.2f", cutoffAmt)).append("");
            sb.append("Balance Amount: ").append(String.format(Locale.getDefault(), "%.2f", totalJappa - cutoffAmt)).append("");
        }

        try {
            BarcodeEncoder enc = new BarcodeEncoder();
            Bitmap bmp = enc.encodeBitmap(sb.toString(), BarcodeFormat.QR_CODE, 600, 600);
            qrCodeImage.setImageBitmap(bmp);
            qrCodeImage.setVisibility(View.VISIBLE);
            lastUpdated.setText("Last updated: " + lastDate);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
