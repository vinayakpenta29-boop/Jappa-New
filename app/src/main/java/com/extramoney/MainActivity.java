package com.extramoney;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.*;
import com.journeyapps.barcodescanner.BarcodeEncoder;
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
    private final List<Salesman> dataList = new ArrayList<>();
    private String lastDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            // Bind Views (make sure all IDs exist in activity_main.xml)
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

            // Prevent null crashes if layout IDs mismatch
            if (tableLayout == null || salesmanNo == null) {
                Toast.makeText(this, "Layout binding error: check activity_main.xml IDs.", Toast.LENGTH_LONG).show();
                return;
            }

            // Set Listeners
            addBtn.setOnClickListener(v -> addRow());
            qrSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> toggleQRCode());
            cutoffSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                updateTotals();
                updateQRCode();
            });

            updateTable();
            updateTotals();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Initialization error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void addRow() {
        try {
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
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
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

            // Clear fields except salesman number
            barcode.setText("");
            millRate.setText("");
            billNo.setText("");
            jappa.setText("");
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error adding row: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateTable() {
        try {
            // Remove dynamic rows: keep header (index 0) and footer (last)
            int childCount = tableLayout.getChildCount();
            if (childCount > 2) {
                tableLayout.removeViews(1, childCount - 2);
            }

            for (Salesman s : dataList) {
                TableRow tr = new TableRow(this);
                for (String val : s.toArray()) {
                    TextView tv = new TextView(this);
                    tv.setText(val);
                    tv.setPadding(6, 6, 6, 6);
                    tr.addView(tv);
                }
                tableLayout.addView(tr, tableLayout.getChildCount() - 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error updating table", Toast.LENGTH_SHORT).show();
        }
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

        try {
            StringBuilder sb = new StringBuilder();
            for (Salesman s : dataList) {
                sb.append(s.toDelimitedString()).append("\n");
            }

            sb.append("Total Jappa: ")
              .append(String.format(Locale.getDefault(), "%.2f", totalJappa))
              .append("\n");

            if (cutoffSwitch.isChecked()) {
                double cutoffAmt = totalJappa * 0.28;
                sb.append("Cut Off Jappa (28%): ")
                  .append(String.format(Locale.getDefault(), "%.2f", cutoffAmt))
                  .append("\n");
                sb.append("Balance Amount: ")
                  .append(String.format(Locale.getDefault(), "%.2f", totalJappa - cutoffAmt))
                  .append("\n");
            }

            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.encodeBitmap(sb.toString(), BarcodeFormat.QR_CODE, 600, 600);
            qrCodeImage.setImageBitmap(bitmap);
            qrCodeImage.setVisibility(View.VISIBLE);
            lastUpdated.setText("Last updated: " + lastDate);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error creating QR Code", Toast.LENGTH_SHORT).show();
        }
    }

    private void toggleQRCode() {
        updateQRCode();
    }

    // Salesman class
    public static class Salesman {
        private final int no;
        private final String sNo, bc, mr, bill, date;
        private final double jappaVal;

        public Salesman(int no, String sNo, String bc, String mr, String bill, String date, double jappaVal) {
            this.no = no;
            this.sNo = sNo;
            this.bc = bc;
            this.mr = mr;
            this.bill = bill;
            this.date = date;
            this.jappaVal = jappaVal;
        }

        public String[] toArray() {
            return new String[]{
                String.valueOf(no),
                sNo,
                bc,
                mr,
                bill,
                date,
                String.format(Locale.getDefault(), "%.2f", jappaVal)
            };
        }

        public String toDelimitedString() {
            return no + ", " + sNo + ", " + bc + ", " + mr + ", " + bill + ", " + date + ", " +
                    String.format(Locale.getDefault(), "%.2f", jappaVal);
        }
    }
}
