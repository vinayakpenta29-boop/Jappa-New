package com.extramoney;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.*;
import com.journeyapps.barcodescanner.BarcodeEncoder; // ZXing dependency
import com.google.zxing.BarcodeFormat;
import android.graphics.Bitmap;

public class MainActivity extends AppCompatActivity {

    private TableLayout tableLayout;
    private EditText salesmanNo, barcode, millRate, billNo, jappa;
    private DatePicker datePicker;
    private Button addBtn;
    private Switch qrSwitch, cutoffSwitch;
    private ImageView qrCodeImage;
    private TextView lastUpdated, totalJappaText, cutoffJappaText, balanceText;

    private double totalJappa = 0;
    private int rowCount = 0;
    private List<Salesman> dataList = new ArrayList<>();
    private String lastDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Bind Views
        tableLayout = findViewById(R.id.tableLayout);
        salesmanNo = findViewById(R.id.salesmanNo);
        barcode = findViewById(R.id.barcode);
        millRate = findViewById(R.id.millRate);
        billNo = findViewById(R.id.billNo);
        jappa = findViewById(R.id.jappa);
        datePicker = findViewById(R.id.datePicker);
        addBtn = findViewById(R.id.addBtn);
        qrSwitch = findViewById(R.id.qrSwitch);
        cutoffSwitch = findViewById(R.id.cutoffSwitch);
        qrCodeImage = findViewById(R.id.qrCodeImage);
        lastUpdated = findViewById(R.id.lastUpdated);
        totalJappaText = findViewById(R.id.totalJappaText);
        cutoffJappaText = findViewById(R.id.cutoffJappaText);
        balanceText = findViewById(R.id.balanceText);

        // Button listener
        addBtn.setOnClickListener(v -> addRow());
        qrSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> toggleQRCode());
        cutoffSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateTotals();
            updateQRCode();
        });

        updateTable();
        updateTotals();
    }

    private void addRow() {
        String sNo = salesmanNo.getText().toString().trim();
        String bc = barcode.getText().toString().trim();
        String mr = millRate.getText().toString().trim();
        String bill = billNo.getText().toString().trim();
        String jp = jappa.getText().toString().trim();

        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth();
        int year = datePicker.getYear();
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String dateStr = sdf.format(cal.getTime());

        if (sNo.isEmpty() || bc.isEmpty() || mr.isEmpty() || bill.isEmpty() || jp.isEmpty()) {
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

        dataList.add(new Salesman(rowCount, sNo, bc, mr, bill, dateStr, jappaVal));
        updateTable();
        updateTotals();
        updateQRCode();

        // Clear fields
        barcode.setText("");
        millRate.setText("");
        billNo.setText("");
        jappa.setText("");
    }

    private void updateTable() {
        // Remove previous dynamic rows but keep first and last rows (header/footer)
        while (tableLayout.getChildCount() > 2) {
            tableLayout.removeViewAt(1);
        }
        for (Salesman s : dataList) {
            TableRow tr = new TableRow(this);
            for (String val : s.toArray()) {
                TextView tv = new TextView(this);
                tv.setText(val);
                tv.setPadding(6,6,6,6);
                tr.addView(tv);
            }
            tableLayout.addView(tr, tableLayout.getChildCount() - 1);
        }
    }

    private void updateTotals() {
        totalJappaText.setText("Total Jappa: " + String.format("%.2f", totalJappa));
        if (cutoffSwitch.isChecked()) {
            double cutoffAmt = totalJappa * 0.28;
            cutoffJappaText.setText("Cut Off Jappa (28%): " + String.format("%.2f", cutoffAmt));
            balanceText.setText("Balance Amount: " + String.format("%.2f", totalJappa - cutoffAmt));
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
        for (Salesman s : dataList) {
            sb.append(s.toDelimitedString()).append("");
        }
        sb.append("Total Jappa: ").append(String.format("%.2f", totalJappa)).append("");
        if (cutoffSwitch.isChecked()) {
            double cutoffAmt = totalJappa * 0.28;
            sb.append("Cut Off Jappa (28%): ").append(String.format("%.2f", cutoffAmt)).append("");
            sb.append("Balance Amount: ").append(String.format("%.2f", totalJappa - cutoffAmt)).append("");
        }
        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.encodeBitmap(
                sb.toString(), BarcodeFormat.QR_CODE, 600, 600);
            qrCodeImage.setImageBitmap(bitmap);
            qrCodeImage.setVisibility(View.VISIBLE);
            lastUpdated.setText("Last updated: " + lastDate);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void toggleQRCode() {
        updateQRCode();
    }
            }
