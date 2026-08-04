package com.example.b07taam2026;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButtonToggleGroup;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// shows the user liked and favourited artifacts
public class ProfileActivity extends AppCompatActivity {

    private ArtifactAdapter adapter;
    private TextView textEmpty;

    private final ArtifactManager artifactManager = new ArtifactManager();
    private final LikeManager likeManager = new LikeManager();
    private final SaveManager saveManager = new SaveManager();

    private final List<Artifact> allArtifacts = new ArrayList<>();
    private final Set<String> likedLots = new HashSet<>();
    private final Set<String> favouriteLots = new HashSet<>();

    private boolean showingLikes = true;
    private String username;
    private String uid;
    private boolean isAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // read user info passed through the intent
        username = getIntent().getStringExtra("USER_NAME");
        uid = getIntent().getStringExtra("UID");
        if (uid == null) {
            uid = new AuthManager().getCurrentUid();
        }
        isAdmin = getIntent().getBooleanExtra("IS_ADMIN", false);

        findViewById(R.id.buttonProfileBack).setOnClickListener(v -> finish());

        textEmpty = findViewById(R.id.textProfileEmpty);

        // set up the artifact list
        RecyclerView recyclerView = findViewById(R.id.profileRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ArtifactAdapter(new ArrayList<>());
        adapter.setOnReadMoreClickListener(this::showArtifactDetail);
        recyclerView.setAdapter(adapter);

        // switch between likes and favourites tabs
        MaterialButtonToggleGroup tabs = findViewById(R.id.toggleProfileTabs);
        tabs.check(R.id.buttonTabLikes);
        tabs.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            showingLikes = (checkedId == R.id.buttonTabLikes);
            refresh();
        });

        // listen for live artifact updates
        artifactManager.startLive(new ArtifactManager.ArtifactCallback() {
            @Override
            public void onResult(List<Artifact> artifacts) {
                allArtifacts.clear();
                allArtifacts.addAll(artifacts);
                refresh();
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(ProfileActivity.this,
                        "Load failed: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });

        // track which lots this user liked
        likeManager.startLive(uid, (counts, mine) -> {
            likedLots.clear();
            likedLots.addAll(mine);
            refresh();
        });

        // track which lots this user favourited
        saveManager.startLive(uid, saved -> {
            favouriteLots.clear();
            favouriteLots.addAll(saved);
            refresh();
        });
    }

    private void refresh() {
        Set<String> active = showingLikes ? likedLots : favouriteLots;

        List<Artifact> filtered = new ArrayList<>();
        for (Artifact artifact : allArtifacts) {
            if (active.contains(artifact.getLotNumber())) {
                filtered.add(artifact);
            }
        }
        adapter.submitList(filtered);

        textEmpty.setText(showingLikes
                ? R.string.empty_no_likes
                : R.string.empty_no_favourites);
        textEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void showArtifactDetail(Artifact artifact) {
        ArtifactDetailFragment fragment =
                ArtifactDetailFragment.newInstance(artifact, username, uid, isAdmin);
        getSupportFragmentManager().beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .add(R.id.profile_fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        artifactManager.stopLive();
        likeManager.stopLive();
        saveManager.stopLive();
    }
}