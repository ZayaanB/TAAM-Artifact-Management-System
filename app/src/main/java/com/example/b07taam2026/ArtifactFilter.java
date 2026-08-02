package com.example.b07taam2026;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ArtifactFilter {
    private final List<Artifact> allArtifacts = new ArrayList<>();
    private final List<Artifact> visibleArtifacts = new ArrayList<>();
    private final Runnable onChanged; // adapter passes notifyDataSetChanged
    private String query = "";

    public static final int PAGE_SIZE_ALL = Integer.MAX_VALUE;
    private static final int DEFAULT_PAGE_SIZE = 12;
    private int pageSize = DEFAULT_PAGE_SIZE;
    private int page = 0; // index of the currently displayed page

    public ArtifactFilter(Runnable onChanged) {
        this.onChanged = onChanged;
    }

    public int size() {
        // number of artifacts in the current screen
        return Math.max(0, Math.min(pageSize, getTotalCount() - pageStart()));
    }

    public Artifact get(int pos) { return visibleArtifacts.get(pageStart() + pos); }

    public void setPageSize(int size) {
        pageSize = (size <= 0) ? DEFAULT_PAGE_SIZE : size;
        page = 0;
        notifyChanged();
    }
    public void setPage(int p) {
        page = clampPage(p);
        notifyChanged();
    }
    public int getPage() { return page; }
    public int getPageSize() { return pageSize; }
    public int getTotalCount() { return visibleArtifacts.size(); }

    public int getPageCount() {
        if (pageSize == PAGE_SIZE_ALL) return 1;
        return Math.max(1, (visibleArtifacts.size() + pageSize - 1) / pageSize);
    }

    private int pageStart() {
        // the index into the visibleArtifacts list representing first item on the current page
        return (pageSize == PAGE_SIZE_ALL) ? 0 : page * pageSize;
    }

    private int clampPage(int p) {
        return Math.max(0, Math.min(p, getPageCount() - 1));
    }

    public void submit(List<Artifact> artifacts) {
        allArtifacts.clear();
        if (artifacts != null) allArtifacts.addAll(artifacts);
        applyFilter();
    }

    public void setQuery(String q) {
        query = (q == null) ? "" : q.trim().toLowerCase(Locale.ROOT);
        page = 0; // a new search always starts from the first page
        applyFilter();
    }

    private void applyFilter() {
        visibleArtifacts.clear();
        for (Artifact a : allArtifacts) {
            // add the artifact to the visible list if it contains a match
            // or the query is empty when we don't want to filter anything
            if (query.isEmpty() || matches(a)) visibleArtifacts.add(a);
        }
        page = clampPage(page);
        notifyChanged();
    }

    private void notifyChanged() {
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
