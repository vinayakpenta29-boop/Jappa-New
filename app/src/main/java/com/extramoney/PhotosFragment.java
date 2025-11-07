package com.extramoney;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.*;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.*;

public class PhotosFragment extends Fragment {

    private Button uploadImageBtn;
    private RecyclerView albumsRecycler;
    private final Map<String, List<PhotoItem>> monthGroupMap = new LinkedHashMap<>();
    private List<Album> albumList = new ArrayList<>();
    private AlbumAdapter albumAdapter;
    private ActivityResultLauncher<Intent> pickImageLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.photos_fragment, container, false);
        uploadImageBtn = v.findViewById(R.id.uploadImageBtn);
        albumsRecycler = v.findViewById(R.id.photosRecycler);

        albumsRecycler.setLayoutManager(new GridLayoutManager(getContext(), 3));

        albumAdapter = new AlbumAdapter(requireContext(), albumList, album -> {
            AlbumPhotosFragment fragment = AlbumPhotosFragment.newInstance(
                    album.name,
                    toUris(album.photoItems)
            );
            requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.photos_sub_fragment_container, fragment)
                .addToBackStack(null)
                .commit();
        });
        albumsRecycler.setAdapter(albumAdapter);

        pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();

                    // ✅ Take persistable permission
                    try {
                        requireContext().getContentResolver().takePersistableUriPermission(
                                imageUri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        );
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Could not save image permission", Toast.LENGTH_SHORT).show();
                    }

                    selectMonthAndYear(imageUri);
                }
            });

        uploadImageBtn.setOnClickListener(vw -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT); // ✅ Changed from ACTION_PICK
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION 
                          | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION); // ✅ Both flags
            pickImageLauncher.launch(intent);
        });

        loadPhotosData();
        return v;
    }

    private ArrayList<Uri> toUris(List<PhotoItem> items) {
        ArrayList<Uri> uris = new ArrayList<>();
        for (PhotoItem p : items) uris.add(p.uri);
        return uris;
    }

    private void selectMonthAndYear(Uri imageUri) {
        final String[] months = new String[]{
            "January", "February", "March", "April", "May", "June", "July",
            "August", "September", "October", "November", "December"};
        new AlertDialog.Builder(getContext())
                .setTitle("Select Month")
                .setItems(months, (dialog, which) -> {
                    String monthText = months[which];
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
                                    savePhotosData();
                                    updateAlbums();
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                })
                .show();
    }

    private void savePhotosData() {
        JSONArray albumJson = new JSONArray();
        for (Map.Entry<String, List<PhotoItem>> group : monthGroupMap.entrySet()) {
            for (PhotoItem item : group.getValue()) {
                JSONObject record = new JSONObject();
                try {
                    record.put("uri", item.uri.toString());
                    record.put("month", item.month);
                    record.put("year", item.year);
                    albumJson.put(record);
                } catch (JSONException ignore) {}
            }
        }
        requireContext().getSharedPreferences("jappa_prefs", Context.MODE_PRIVATE)
            .edit().putString("photos", albumJson.toString()).apply();
    }

    private void loadPhotosData() {
        SharedPreferences prefs = requireContext().getSharedPreferences("jappa_prefs", Context.MODE_PRIVATE);
        String data = prefs.getString("photos", null);
        if (data == null) return;
        try {
            monthGroupMap.clear();
            JSONArray arr = new JSONArray(data);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                String month = obj.getString("month");
                String year = obj.getString("year");
                Uri uri = Uri.parse(obj.getString("uri"));

                // ✅ Validate URI is still accessible
                if (isUriAccessible(uri)) {
                    String key = month + ", " + year;
                    PhotoItem item = new PhotoItem(uri, month, year);
                    if (!monthGroupMap.containsKey(key)) monthGroupMap.put(key, new ArrayList<>());
                    monthGroupMap.get(key).add(item);
                }
            }
            updateAlbums();
        } catch (Exception ignore) {}
    }

    private boolean isUriAccessible(Uri uri) {
        try {
            requireContext().getContentResolver().openInputStream(uri).close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void reloadData() {
        loadPhotosData();
    }

    private void updateAlbums() {
        albumList.clear();
        for (Map.Entry<String, List<PhotoItem>> entry : monthGroupMap.entrySet()) {
            albumList.add(new Album(entry.getKey(), entry.getValue()));
        }
        albumAdapter.notifyDataSetChanged();
    }
}
