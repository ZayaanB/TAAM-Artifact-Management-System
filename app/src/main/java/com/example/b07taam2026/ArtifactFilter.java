package com.example.b07taam2026;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ArtifactFilter {
    private final List<Artifact> allArtifacts = new ArrayList<>();
    private final List<Artifact> visibleArtifacts = new ArrayList<>();
    private final Runnable onChanged; // adapter passes notifyDataSetChanged
    private String query = "";

    public ArtifactFilter(Runnable onChanged) {
        this.onChanged = onChanged;
    }

    public int size() { return visibleArtifacts.size(); }

    public Artifact get(int pos) { return visibleArtifacts.get(pos); }

    public void submit(List<Artifact> artifacts) {
        allArtifacts.clear();
        if (artifacts != null) allArtifacts.addAll(artifacts);
        applyFilter();
    }

    public void setQuery(String q) {
        query = (q == null) ? "" : q.trim().toLowerCase(Locale.ROOT);
        applyFilter();
    }

    private void applyFilter() {
        visibleArtifacts.clear();
        for (Artifact a : allArtifacts) {
            // add the artifact to the visible list if it contains a match
            // or the query is empty when we don't want to filter anything
            if (query.isEmpty() || matches(a)) visibleArtifacts.add(a);
        }
        if (onChanged != null) onChanged.run();
    }

    private boolean matches(Artifact a) {
        // check if search text is a substring of these 6 mandatory fields
        return contains(a.getName())
                || contains(a.getLotNumber())
                || contains(a.getCategory())
                || contains(a.getMaterial())
                || contains(a.getDynasty())
                || contains(a.getDescription());
    }

    private boolean contains(String field) {
        // simple substring matching
        return field != null && field.toLowerCase(Locale.ROOT).contains(query);
    }
}
