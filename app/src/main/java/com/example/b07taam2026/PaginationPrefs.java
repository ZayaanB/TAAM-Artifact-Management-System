package com.example.b07taam2026;

import android.content.Context;
import android.content.SharedPreferences;

public class PaginationPrefs {
    private static final String PREFS = "taam_prefs";
    private static final String KEY_PAGE_SIZE = "home_page_size";

    // Page-size options in order
    public static final int[] OPTIONS = { 12, 24, ArtifactFilter.PAGE_SIZE_ALL };
    private static final int DEFAULT_PAGE_SIZE = 12;

    private final SharedPreferences prefs;

    public PaginationPrefs(Context context) {
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public int getPageSize() {
        // if the key doesn't exist return the default page size
        return prefs.getInt(KEY_PAGE_SIZE, DEFAULT_PAGE_SIZE);
    }

    public void setPageSize(int size) { prefs.edit().putInt(KEY_PAGE_SIZE, size).apply(); }

    public int getSelectedIndex() {
        int saved = getPageSize();
        for (int i = 0; i < OPTIONS.length; i++) {
            if (OPTIONS[i] == saved) return i;
        }
        return 0;
    }
}
