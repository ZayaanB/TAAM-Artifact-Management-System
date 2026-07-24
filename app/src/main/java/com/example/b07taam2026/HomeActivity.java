package com.example.b07taam2026;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private String username;
    private String uid;
    private boolean isAdmin;

    private final List<Artifact> artifactList = new ArrayList<>();
    private ArtifactAdapter adapter;
    private ArtifactManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        isAdmin = getIntent().getBooleanExtra(LoginPage.EXTRA_IS_ADMIN, false);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        FloatingActionButton adminMenu = findViewById(R.id.adminMenuBtn);
        adminMenu.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
        adminMenu.setOnClickListener(v -> {
            // TODO: open add/manage-artifact screen
        });

        RecyclerView recyclerView = findViewById(R.id.artifactRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ArtifactAdapter(artifactList);
        recyclerView.setAdapter(adapter);

        manager = new ArtifactManager();
        manager.startLive(new ArtifactManager.ArtifactCallback() {
            @Override
            public void onResult(List<Artifact> artifacts) {
                artifactList.clear();
                artifactList.addAll(artifacts);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(HomeActivity.this, "Load failed: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });

        SearchView search = findViewById(R.id.searchArtifacts);
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { return false; }
            @Override public boolean onQueryTextChange(String newText) { return false; }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (manager != null) manager.stopLive();
    }
}