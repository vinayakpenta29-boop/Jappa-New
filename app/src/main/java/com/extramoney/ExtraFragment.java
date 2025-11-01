package com.extramoney;

import android.app.DatePickerDialog;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import java.text.DateFormatSymbols;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.fragment.app.Fragment;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.util.*;

public class ExtraFragment extends Fragment {

    private Calendar selectedDate = Calendar.getInstance();
    private double totalJappa = 0;
    private List<Double> jappaValues = new ArrayList<>();
    private List<String[]> rowDataList = new ArrayList<>();
    private String lastDate = "";

    private EditText salesmanNo, barcode, millRate, billNo, jappa;
    private TextView dateText, totalJappaText, cutoffJappaText, balanceText, lastUpdated;
    private Button addBtn;
    private Switch qrSwitch, cutoffSwitch;
    private TableLayout tableLayout;
    private TableRow cutoffRow, balanceRow;
    private ImageView qrCodeImage;

    private Set<String> selectedMonthYears = new HashSet<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

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

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);

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
            filterTableData();
        });

        qrSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> updateQRCodeAndLastUpdated());
        cutoffSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateSummaryRows();
            updateQRCodeAndLastUpdated();
            saveTableData();
        });

        MotionLayout swipeLayout = view.findViewById(R.id.swipeMotionLayout);
        swipeLayout.setTransitionListener(new MotionLayout.TransitionListener() {
            @Override public void onTransitionStarted(MotionLayout m, int s, int e) {}
            @Override public void onTransitionChange(MotionLayout m, int s, int e, float p) {}
            @Override public void onTransitionCompleted(MotionLayout m, int c) {
                if (c == R.id.end) {
                    showResetPopup();
                    swipeLayout.transitionToStart();
                }
            }
            @Override public void onTransitionTrigger(MotionLayout m, int t, boolean pos, float p) {}
        });

        loadTableData();
        updateSummaryRows();
        updateQRCodeAndLastUpdated();
        filterTableData();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_extra, menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_filter) {
            showMonthFilterDialog();
            return true;
        } else if (item.getItemId() == R.id.menu_delete_entry) {
            showDeleteEntryDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showMonthFilterDialog() {
        Set<String> allMonthYears = new LinkedHashSet<>();
        for (String[] row : rowDataList) {
            try {
                String date = row[5];
                String[] parts = date.split("/");
                String monthYear = getMonthName(Integer.parseInt(parts[1])) + " " + parts[2];
                allMonthYears.add(monthYear);
            } catch (Exception ignore) { }
        }
        final String[] items = allMonthYears.toArray(new String[0]);
        boolean[] checked = new boolean[items.length];
        for (int i = 0; i < items.length; i++) checked[i] = selectedMonthYears.contains(items[i]);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Filter by Month");
        builder.setMultiChoiceItems(items, checked, (dialog, which, isChecked) -> {
            checked[which] = isChecked;
        });
        builder.setPositiveButton("OK", (dialog, which) -> {
            selectedMonthYears.clear();
            for (int i = 0; i < items.length; i++) {
                if (checked[i]) selectedMonthYears.add(items[i]);
            }
            filterTableData();
        });
        builder.setNeutralButton("Clear", (dialog, which) -> {
            selectedMonthYears.clear();
            filterTableData();
        });
        builder.show();
    }
    private String getMonthName(int monthNumber) {
        return new DateFormatSymbols().getMonths()[monthNumber - 1];
    }

    private void showDeleteEntryDialog() {
        if (rowDataList.isEmpty()) {
            Toast.makeText(getContext(), "No entries to delete", Toast.LENGTH_SHORT).show();
            return;
        }
        // Build serial numbers (1, 2, 3, ...)
        String[] rowNumbers = new String[rowDataList.size()];
        for (int i = 0; i < rowNumbers.length; i++) {
            rowNumbers[i] = String.valueOf(i + 1);
        }
        boolean[] checked = new boolean[rowNumbers.length];

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Delete Entries");
        builder.setMultiChoiceItems(rowNumbers, checked, (dialog, which, isChecked) -> checked[which] = isChecked);
        builder.setPositiveButton("Delete", (dialog, which) -> {
            List<Integer> toDelete = new ArrayList<>();
            for (int i = 0; i < checked.length; i++) {
                if (checked[i]) toDelete.add(i);
            }
            if (toDelete.isEmpty()) return;
            Collections.sort(toDelete, Collections.reverseOrder());
            for (int idx : toDelete) rowDataList.remove(idx);
            reindexRows();
            jappaValues.clear();
            for (String[] row : rowDataList) {
                try {
                    jappaValues.add(Double.parseDouble(row[row.length - 1]));
                } catch (Exception ignore) {}
            }
            saveTableData();
            filterTableData();
            Toast.makeText(getContext(), "Deleted selected entries", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void reindexRows() {
        for (int i = 0; i < rowDataList.size(); i++) {
            rowDataList.get(i)[0] = String.valueOf(i + 1);
        }
    }

    private void filterTableData() {
        while (tableLayout.getChildCount() > 4) tableLayout.removeViewAt(1);

        double filteredTotal = 0;
        for (String[] row : rowDataList) {
            try {
                String date = row[5];
                String[] parts = date.split("/");
                String monthYear = getMonthName(Integer.parseInt(parts[1])) + " " + parts[2];
                boolean included = selectedMonthYears.isEmpty() || selectedMonthYears.contains(monthYear);
                if (included) {
                    TableRow tr = new TableRow(getContext());
                    for (String cell : row) tr.addView(makeCell(getContext(), cell));
                    int insertIdx = Math.max(tableLayout.getChildCount() - 3, 1);
                    tableLayout.addView(tr, insertIdx);
                    filteredTotal += Double.parseDouble(row[row.length - 1]);
                }
            } catch (Exception ignore) {}
        }

        totalJappaText.setText(String.format(Locale.getDefault(), "%.2f", filteredTotal));
        if (selectedMonthYears.isEmpty()) {
            updateSummaryRows();
        } else {
            if (cutoffSwitch.isChecked()) {
                double cutoffAmt = filteredTotal * 0.28;
                cutoffJappaText.setText(String.format(Locale.getDefault(), "%.2f", cutoffAmt));
                balanceText.setText(String.format(Locale.getDefault(), "%.2f", filteredTotal - cutoffAmt));
                cutoffRow.setVisibility(View.VISIBLE);
                balanceRow.setVisibility(View.VISIBLE);
            } else {
                cutoffRow.setVisibility(View.GONE);
                balanceRow.setVisibility(View.GONE);
            }
        }
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
            sb.append("");
        }
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
        } catch (Exception e) {
            qrCodeImage.setVisibility(View.GONE);
        }

        lastUpdated.setText(rowDataList.isEmpty() ? "" : ("Last updated: " + lastDate));
    }

    private TextView makeCell(Context ctx, String text) {
        TextView tv = new TextView(ctx);
        tv.setText(text);
        tv.setPadding(8, 8, 8, 8);
        tv.setTextColor(0xFF000000);
        tv.setGravity(Gravity.CENTER);
        tv.setBackgroundResource(R.drawable.border_cell);
        return tv;
    }

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

    private void showResetPopup() {
        View popupView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_reset_confirm, null);
        EditText passwordInput = popupView.findViewById(R.id.password_input);

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(popupView)
                .setCancelable(false)
                .create();

        popupView.findViewById(R.id.reset_btn).setOnClickListener(v -> {
            String input = passwordInput.getText().toString().trim();
            if (input.equals("1234")) {
                resetAllData();
                dialog.dismiss();
                Toast.makeText(getContext(), "Data reset successful", Toast.LENGTH_SHORT).show();
            } else {
                passwordInput.setError("Wrong password");
            }
        });
        popupView.findViewById(R.id.cancel_btn).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void resetAllData() {
        Context ctx = requireContext();

        SharedPreferences prefs = ctx.getSharedPreferences("jappa_prefs", Context.MODE_PRIVATE);
        prefs.edit().clear().apply();

        rowDataList.clear();
        jappaValues.clear();
        while (tableLayout.getChildCount() > 4) tableLayout.removeViewAt(1);
        updateSummaryRows();
        updateQRCodeAndLastUpdated();

        try {
            File photosDir = new File(ctx.getFilesDir(), "photos");
            deleteRecursive(photosDir);
        } catch (Exception ignore) {}

        if (getActivity() != null) {
            List<Fragment> fragments = getActivity().getSupportFragmentManager().getFragments();
            for (Fragment f : fragments) {
                if (f instanceof PhotosFragment) {
                    ((PhotosFragment) f).reloadData();
                }
            }
        }

        Toast.makeText(ctx, "All data reset", Toast.LENGTH_SHORT).show();
    }

    private void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory != null && fileOrDirectory.exists()) {
            if (fileOrDirectory.isDirectory()) {
                File[] children = fileOrDirectory.listFiles();
                if (children != null) {
                    for (File child : children) {
                        deleteRecursive(child);
                    }
                }
            }
            fileOrDirectory.delete();
        }
    }
}
