package com.example.b07taam2026;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;

import java.util.Arrays;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        RecyclerView recyclerView = findViewById(R.id.artifactRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        ArtifactAdapter adapter = new ArtifactAdapter(sampleArtifacts());
        recyclerView.setAdapter(adapter);

        SearchView search = findViewById(R.id.searchArtifacts);
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { return false; }
            @Override public boolean onQueryTextChange(String newText) { return false; }
        });
    }

    private List<Artifact> sampleArtifacts() {
        return Arrays.asList(
                new Artifact("A Tang 'Sancai'", "LOT001", "Ceramics", "Ceramic",
                        "Tang Dynasty (618-907 CE)", "This tricolor (sancai) box has a round shape with shallow straight walls, a concave circular mouth, a flat base, and a slightly curved lid. The outer surface of the lid is decorated with intricate molded patterns, displaying exquisite and varied designs."),
                new Artifact("A Blue-Glazed Ceramic 'Tiger'", "LOT002", "Ceramics", "Ceramic",
                        "Three Kingdoms Period (220-280 CE)", "This blue-glazed ceramic tiger was crafted during the Three Kingdoms Period and originates from the Yue Kiln. It features a rounded and well-proportioned form with a slanting neck and circular mouth.")
        );
    }
}