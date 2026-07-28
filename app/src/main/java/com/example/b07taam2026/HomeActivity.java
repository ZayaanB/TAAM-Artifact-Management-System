package com.example.b07taam2026;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
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
    private boolean isAdminMenuOpen = false;

    private final List<Artifact> artifactList = new ArrayList<>();
    private ArtifactAdapter adapter;
    private ArtifactManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        isAdmin = getIntent().getBooleanExtra(LoginPage.EXTRA_IS_ADMIN, false);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        FloatingActionButton adminMenuBtn = findViewById(R.id.adminMenuBtn);
        ExtendedFloatingActionButton manageAdminsBtn = findViewById(R.id.adminManageAdminsBtn);
        ExtendedFloatingActionButton manageArtifactsBtn = findViewById(R.id.adminManageArtifactsBtn);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        adminMenuBtn.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
        adminMenuBtn.setOnClickListener(v -> {
            // TODO: open add/manage-artifact screen
            if (!isAdminMenuOpen){// if the menu is closed, open it
                manageAdminsBtn.setVisibility(View.VISIBLE);
                manageArtifactsBtn.setVisibility(View.VISIBLE);

                adminMenuBtn.setImageResource(R.drawable.ic_minus);
                isAdminMenuOpen = true;
            }
            else{ // if menu is open close it
                manageAdminsBtn.setVisibility(View.GONE);
                manageArtifactsBtn.setVisibility(View.GONE);

                adminMenuBtn.setImageResource(R.drawable.ic_add);
                isAdminMenuOpen = false;
            }
        });
        manageArtifactsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ManageArtifactsActivity.class);
            intent.putExtra(LoginPage.EXTRA_IS_ADMIN, isAdmin);
            startActivity(intent);
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