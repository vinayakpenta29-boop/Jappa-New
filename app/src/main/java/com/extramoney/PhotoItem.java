package com.extramoney;

import android.net.Uri;
import java.io.Serializable;

public class PhotoItem implements Serializable {
    public Uri uri;
    public String month;
    public String year;
    public PhotoItem(Uri u, String m, String y) {
        uri = u; month = m; year = y;
    }
}
