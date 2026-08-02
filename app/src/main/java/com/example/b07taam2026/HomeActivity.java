package com.example.b07taam2026;

import static com.example.b07taam2026.R.layout.home_dropdown_menu;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageButton;
import androidx.appcompat.widget.PopupMenu;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private String username;
    private String uid;
    private boolean isAdmin;
    private boolean isAdminMenuOpen = false;

    private ArtifactAdapter adapter;
    private ArtifactManager manager;
    private TextView textNoMatches;
    private FloatingActionButton adminMenuBtn;
    private ExtendedFloatingActionButton manageAdminsBtn, manageArtifactsBtn;
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
        isAdmin = getIntent().getBooleanExtra(LoginPage.EXTRA_IS_ADMIN, false);
        username = getIntent().getStringExtra("USER_NAME");
        uid = getIntent().getStringExtra("UID");
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        textNoMatches = findViewById(R.id.textNoMatches);

        adminMenuBtn = findViewById(R.id.adminMenuBtn);
        manageAdminsBtn = findViewById(R.id.adminManageAdminsBtn);
        manageArtifactsBtn = findViewById(R.id.adminManageArtifactsBtn);

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
                collapseAdminMenu();
            }
        });
        manageArtifactsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ManageArtifactsActivity.class);
            intent.putExtra(LoginPage.EXTRA_IS_ADMIN, isAdmin);
            startActivity(intent);
        });
        manageAdminsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, UserDebugActivity.class);
            startActivity(intent);
        });

        manageAdminsBtn.setOnClickListener(v ->{
            Intent intent = new Intent(HomeActivity.this, ManageAdminsActivity.class /* This class does not exist yet*/);
            intent.putExtra(LoginPage.EXTRA_IS_ADMIN, isAdmin);
            intent.putExtra("USER_NAME", username);
            intent.putExtra("UID", uid);
            startActivity(intent);
        });

        recyclerView = findViewById(R.id.artifactRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ArtifactAdapter(new ArrayList<>());
        adapter.setOnReadMoreClickListener(this::showArtifactDetail);
        recyclerView.setAdapter(adapter);

        paginationPrefs = new PaginationPrefs(this);
        adapter.setPageSize(paginationPrefs.getPageSize()); // restore user pref for page size

        textPageIndicator = findViewById(R.id.textPageIndicator);
        buttonNextPage = findViewById(R.id.buttonNextPage);
        buttonPrevPage = findViewById(R.id.buttonPrevPage);

        buttonNextPage.setOnClickListener(v -> goToPage(adapter.getPage() + 1));
        buttonPrevPage.setOnClickListener(v -> goToPage(adapter.getPage() - 1));

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
                }
                return false;
            });
            popup.show();
        });

        getSupportFragmentManager().addOnBackStackChangedListener(this::syncAdminFAB);
        syncAdminFAB();
    }

    private void syncAdminFAB() {
        boolean artifactDetailOpen = getSupportFragmentManager().getBackStackEntryCount() > 0;

        if (artifactDetailOpen) {
            collapseAdminMenu();
            adminMenuBtn.setVisibility(View.GONE);
        } else {
            adminMenuBtn.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
        }
    }

    private void collapseAdminMenu() {
        manageAdminsBtn.setVisibility(View.GONE);
        manageArtifactsBtn.setVisibility(View.GONE);
        adminMenuBtn.setImageResource(R.drawable.ic_add);
        isAdminMenuOpen = false;
    }

    private void applyPageSize(int index) {
        int size = PaginationPrefs.OPTIONS[index];
        paginationPrefs.setPageSize(size);
        adapter.setPageSize(size);
        recyclerView.scrollToPosition(0);
        updatePaginationUI();
    }

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
