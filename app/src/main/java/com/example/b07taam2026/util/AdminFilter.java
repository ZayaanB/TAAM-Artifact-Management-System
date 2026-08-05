package com.example.b07taam2026.util;

import com.example.b07taam2026.model.User;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// filters the admin list by search query
public class AdminFilter {
    private final List<User> allAdmins = new ArrayList<>();
    private final List<User> visibleAdmins = new ArrayList<>();
    private final Runnable onChanged; // adapter passes notifyDataSetChanged
    private String query = "";

    public AdminFilter(Runnable onChanged) {
        this.onChanged = onChanged;
    }

    public int size() {
        return visibleAdmins.size();
    }

    public User get(int pos) {
        return visibleAdmins.get(pos);
    }

    // replace the full admin list
    public void submit(List<User> admins) {
        allAdmins.clear();
        if (admins != null) allAdmins.addAll(admins);
        applyFilter();
    }

    public void setQuery(String q){
        query = (q == null) ? "" : q.trim().toLowerCase(Locale.ROOT);
        applyFilter();
    }

    // rebuild visible list from the query
    private void applyFilter(){
        visibleAdmins.clear();
        for (User a : allAdmins) {
            // add the admin to the visible list if it contains a match
            // or the query is empty when we don't want to filter anything
            if (query.isEmpty() || matches(a)) visibleAdmins.add(a);
        }
        if (onChanged != null) onChanged.run();
    }


    private boolean matches(User a) {
        return contains(a.getUsername()) || contains(a.getEmail());
    }

    // case insensitive substring check
    private boolean contains(String field) {
        return field != null && field.toLowerCase(Locale.ROOT).contains(query);
    }
}
