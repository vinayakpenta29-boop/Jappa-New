package com.extramoney;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.Calendar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ExtraFragment extends Fragment {

    private Calendar selectedDate = Calendar.getInstance();

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        // Use fragment_extra.xml as the layout for this fragment
        return inflater.inflate(R.layout.fragment_extra, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView dateText = view.findViewById(R.id.dateText);

        dateText.setOnClickListener(v -> {
            int year = selectedDate.get(Calendar.YEAR);
            int month = selectedDate.get(Calendar.MONTH);
            int day = selectedDate.get(Calendar.DAY_OF_MONTH);
            new DatePickerDialog(requireContext(), (picker, y, m, d) -> {
                selectedDate.set(y, m, d);
                dateText.setText(String.format("%02d/%02d/%04d", d, m + 1, y));
            }, year, month, day).show();
        });
    }
}
