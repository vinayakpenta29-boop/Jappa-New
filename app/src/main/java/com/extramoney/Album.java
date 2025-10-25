package com.extramoney;

import android.net.Uri;
import java.util.List;

public class Album {
    public String name;
    public List<PhotoItem> photoItems;
    public Album(String name, List<PhotoItem> list) {
        this.name = name;
        this.photoItems = list;
    }
    public Uri coverUri() {
        return photoItems.get(0).uri; // Use first as cover
    }
}
