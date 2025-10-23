package com.extramoney;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * Fragment to host your main Extra (sales table) UI.
 * 
 * IMPORTANT:
 * Inflates fragment_extra.xml, NOT activity_main.xml!
 * (fragment_extra.xml should contain your ScrollView+LinearLayout+table etc.)
 */
public class ExtraFragment extends Fragment {

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
}
