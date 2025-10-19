package com.extramoney;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.*;
import android.widget.Button;
import android.widget.EditText;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;

import java.util.*;

public class PhotosFragment extends Fragment {

    private Button uploadImageBtn;
    private RecyclerView photosRecycler;

    // Basic example for images: List of Triplets (Uri, month, year)
    private final List<PhotoItem> images = new ArrayList<>();
    private final Map<String, List<PhotoItem>> monthGroupMap = new LinkedHashMap<>();
    private PhotosAdapter adapter;

    // Modern recommended activity result launcher
    private ActivityResultLauncher<Intent> pickImageLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.photos_fragment, container, false);
        uploadImageBtn = v.findViewById(R.id.uploadImageBtn);
        photosRecycler = v.findViewById(R.id.photosRecycler);

        adapter = new PhotosAdapter(requireContext(), monthGroupMap);
        photosRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        photosRecycler.setAdapter(adapter);

        // Register result launcher
        pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    selectMonthAndYear(imageUri);
                }
            });

        uploadImageBtn.setOnClickListener(vw -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            pickImageLauncher.launch(intent);
        });

        return v;
    }

    private void selectMonthAndYear(Uri imageUri) {
        // Month picker
        final String[] months = new String[]{
            "January", "February", "March", "April", "May", "June", "July",
            "August", "September", "October", "November", "December"};
        new AlertDialog.Builder(getContext())
                .setTitle("Select Month")
                .setItems(months, (dialog, which) -> {
                    String monthText = months[which];

                    // Year input dialog
                    final EditText yearBox = new EditText(getContext());
                    yearBox.setHint("Enter Year");
                    yearBox.setInputType(InputType.TYPE_CLASS_NUMBER);
                    new AlertDialog.Builder(getContext())
                            .setTitle("Year")
                            .setView(yearBox)
                            .setPositiveButton("Done", (dialog2, which2) -> {
                                String year = yearBox.getText().toString();
                                if (!year.isEmpty()) {
                                    String key = monthText + ", " + year;
                                    PhotoItem item = new PhotoItem(imageUri, monthText, year);
                                    if (!monthGroupMap.containsKey(key)) {
                                        monthGroupMap.put(key, new ArrayList<>());
                                    }
                                    monthGroupMap.get(key).add(item);
                                    adapter.notifyDataSetChanged();
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                })
                .show();
    }
}

// Helper class to hold photo and tags
class PhotoItem {
    Uri uri;
    String month;
    String year;
    PhotoItem(Uri u, String m, String y) {
        uri = u; month = m; year = y;
    }
}
