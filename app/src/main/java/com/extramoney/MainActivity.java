package com.extramoney;

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
    private DatePicker datePicker;
    private Button addBtn;
    private Switch qrSwitch, cutoffSwitch;
    private ImageView qrCodeImage;
    private TextView lastUpdated, totalJappaText, cutoffJappaText, balanceText;

    private double totalJappa = 0;
    private int rowCount = 0;
    private final List<Salesman> dataList = new ArrayList<>();
    private String lastDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_main);

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
            tableLayout = findViewById(R.id.tableLayout);

            addBtn.setOnClickListener(v -> addRow());

        } catch (Exception e) {
            showError("onCreate error: " + e.getMessage());
        }
    }

    private void addRow() {
        try {
            String sNo = salesmanNo.getText().toString().trim();
            String bc = barcode.getText().toString().trim();
            String mr = millRate.getText().toString().trim();
            String bill = billNo.getText().toString().trim();
            String jp = jappa.getText().toString().trim();

            if (sNo.isEmpty() || bc.isEmpty() || mr.isEmpty() || bill.isEmpty() || jp.isEmpty()) {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            double jappaVal = Double.parseDouble(jp);

            Calendar cal = Calendar.getInstance();
            cal.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String dateStr = sdf.format(cal.getTime());
            lastDate = dateStr;

            rowCount++;
            totalJappa += jappaVal;

            Salesman s = new Salesman(rowCount, sNo, bc, mr, bill, dateStr, jappaVal);
            dataList.add(s);

            TableRow tr = new TableRow(this);
            for (String val : s.toArray()) {
                TextView tv = new TextView(this);
                tv.setText(val);
                tr.addView(tv);
            }
            tableLayout.addView(tr, tableLayout.getChildCount() - 1);

            totalJappaText.setText("Total Jappa: " + totalJappa);
            updateQRCode();

        } catch (Exception e) {
            showError("addRow error: " + e.getMessage());
        }
    }

    private void updateQRCode() {
        try {
            if (!qrSwitch.isChecked()) {
                qrCodeImage.setVisibility(View.GONE);
                lastUpdated.setText("");
                return;
            }

            StringBuilder sb = new StringBuilder();
            for (Salesman s : dataList) sb.append(s.toDelimitedString()).append("\n");
            sb.append("Total Jappa: ").append(totalJappa);

            BarcodeEncoder enc = new BarcodeEncoder();
            Bitmap bmp = enc.encodeBitmap(sb.toString(), BarcodeFormat.QR_CODE, 600, 600);
            qrCodeImage.setImageBitmap(bmp);
            qrCodeImage.setVisibility(View.VISIBLE);
            lastUpdated.setText("Last updated: " + lastDate);
        } catch (Exception e) {
            showError("QR error: " + e.getMessage());
        }
    }

    private void showError(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();

        // Also show on screen for devices that suppress Toasts
        TextView err = new TextView(this);
        err.setText(msg);
        err.setTextSize(16);
        setContentView(err);
    }
}
