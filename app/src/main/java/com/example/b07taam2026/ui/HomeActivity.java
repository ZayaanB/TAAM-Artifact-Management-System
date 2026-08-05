package com.example.b07taam2026.ui;

import static com.example.b07taam2026.R.layout.home_dropdown_menu;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.b07taam2026.R;
import com.example.b07taam2026.adapter.ArtifactAdapter;
import com.example.b07taam2026.auth.AuthManager;
import com.example.b07taam2026.auth.LoginPage;
import com.example.b07taam2026.data.ArtifactManager;
import com.example.b07taam2026.data.PaginationPrefs;
import com.example.b07taam2026.model.Artifact;
import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private String username;
    private String uid;
    private boolean isAdmin;
    private boolean isManager;
    private String userRole;
    private ArtifactAdapter adapter;
    private ArtifactManager manager;
    private TextView textNoMatches;
    private PaginationPrefs paginationPrefs;
    private TextView textPageIndicator;
    private Button buttonPrevPage, buttonNextPage;
    private RecyclerView recyclerView;
    private static final int[] PAGE_SIZE_MENU_IDS = {
            R.id.menuPageSize12, R.id.menuPageSize24, R.id.menuPageSizeAll
    };

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // read the role passed from login
        userRole = getIntent().getStringExtra("USER_ROLE");
        if(userRole == null){
            userRole = "user";
        }

        isAdmin = userRole.equals("admin") || userRole.equals("admin_m");
        isManager = userRole.equals("admin_m");

        username = getIntent().getStringExtra("USER_NAME");
        uid = getIntent().getStringExtra("UID");
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        textNoMatches = findViewById(R.id.textNoMatches);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        // set up the artifact list
        recyclerView = findViewById(R.id.artifactRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ArtifactAdapter(new ArrayList<>());
        adapter.setOnReadMoreClickListener(this::showArtifactDetail);
        recyclerView.setAdapter(adapter);

        // restore pagination prefs and page buttons
        paginationPrefs = new PaginationPrefs(this);
        adapter.setPageSize(paginationPrefs.getPageSize()); // restore user pref for page size

        textPageIndicator = findViewById(R.id.textPageIndicator);
        buttonNextPage = findViewById(R.id.buttonNextPage);
        buttonPrevPage = findViewById(R.id.buttonPrevPage);

        buttonNextPage.setOnClickListener(v -> goToPage(adapter.getPage() + 1));
        buttonPrevPage.setOnClickListener(v -> goToPage(adapter.getPage() - 1));

        // live artifact updates from firebase
        manager = new ArtifactManager();
        manager.startLive(new ArtifactManager.ArtifactCallback() {
            @Override
            public void onResult(List<Artifact> artifacts) {
                adapter.submitList(artifacts);
                updatePaginationUI();
                updateEmptyText();
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(HomeActivity.this, "Load failed: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });

        // filter the list as the user types
        SearchView search = findViewById(R.id.searchArtifacts);
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.setQuery(query);
                updatePaginationUI();
                updateEmptyText();
                search.clearFocus();
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.setQuery(newText);
                updatePaginationUI();
                updateEmptyText();
                return true;
            }
        });
        ImageButton buttonMenu = findViewById(R.id.buttonMenu);
        buttonMenu.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(HomeActivity.this, v);
            popup.getMenuInflater().inflate(home_dropdown_menu, popup.getMenu());

            popup.getMenu().findItem(R.id.menuManageArtifacts).setVisible(isAdmin);
            popup.getMenu().findItem(R.id.menuManageAdmins).setVisible(isManager);


            // tick the saved option
            popup.getMenu().findItem(PAGE_SIZE_MENU_IDS[paginationPrefs.getSelectedIndex()]).setChecked(true);

            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();

                // check if the clicked id is one of the 3 page size radio buttons
                int pageSizeIndex = -1;
                for (int i = 0; i < PAGE_SIZE_MENU_IDS.length; i++) if (PAGE_SIZE_MENU_IDS[i] == id) pageSizeIndex = i;
                if (pageSizeIndex != -1) {
                    item.setChecked(true);
                    applyPageSize(pageSizeIndex);
                    return true;
                }

                if (id == R.id.menuProfile) {
                    Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
                    intent.putExtra("USER_NAME", username);
                    intent.putExtra("UID", uid);
                    intent.putExtra("IS_ADMIN", isAdmin);
                    startActivity(intent);
                    return true;
                } else if (id == R.id.menuLogout) {
                    logout();
                    return true;
                } else if(id == R.id.menuManageArtifacts){
                    Intent intent = new Intent(HomeActivity.this, ManageArtifactsActivity.class);
                    intent.putExtra(LoginPage.EXTRA_IS_ADMIN, isAdmin);
                    startActivity(intent);
                    return true;
                } else if(id == R.id.menuManageAdmins){
                    Intent intent = new Intent(HomeActivity.this, ManageAdminsActivity.class);
                    intent.putExtra(LoginPage.EXTRA_IS_ADMIN, isAdmin);
                    intent.putExtra("UID", uid);
                    intent.putExtra("USER_NAME", username);
                    startActivity(intent);
                    return true;
                }
                return false;
            });
            popup.show();
        });
    }


    private void applyPageSize(int index) {
        int size = PaginationPrefs.OPTIONS[index];
        paginationPrefs.setPageSize(size);
        adapter.setPageSize(size);
        recyclerView.scrollToPosition(0);
        updatePaginationUI();
    }

    // switch page and scroll back to top
    private void goToPage(int page) {
        adapter.setPage(page);
        recyclerView.scrollToPosition(0);
        updatePaginationUI();
    }

    private void updatePaginationUI() {
        int page = adapter.getPage();
        int pageCount = adapter.getPageCount();

        textPageIndicator.setText(getString(R.string.page_indicator, page + 1, pageCount, adapter.getTotalCount()));

        buttonNextPage.setEnabled(page < pageCount - 1); // next button disabled if last page
        buttonPrevPage.setEnabled(page > 0); // prev button disabled if first page

        // if only one single page the buttons are not displayed
        boolean showBar = pageCount > 1;
        buttonNextPage.setVisibility(showBar ? View.VISIBLE : View.INVISIBLE);
        buttonPrevPage.setVisibility(showBar ? View.VISIBLE : View.INVISIBLE);
    }

    private void logout() {
        if (manager != null) {
            manager.stopLive();
        }
        new AuthManager().logout();
        getSharedPreferences("LoginPrefs", MODE_PRIVATE)
                .edit().putBoolean("keepLoggedIn", false).apply();

        Intent intent = new Intent(HomeActivity.this, LoginPage.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // open the detail fragment for an artifact
    private void showArtifactDetail(Artifact artifact) {
        String username = getIntent().getStringExtra("USER_NAME");
        String uid = getIntent().getStringExtra("UID");
        ArtifactDetailFragment fragment = ArtifactDetailFragment.newInstance(artifact, username, uid, isAdmin);
        getSupportFragmentManager().beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .add(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (manager != null) manager.stopLive();
    }

    private void updateEmptyText() {
        textNoMatches.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }
}
