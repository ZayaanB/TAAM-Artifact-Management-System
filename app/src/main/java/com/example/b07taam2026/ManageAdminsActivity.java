package com.example.b07taam2026;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.SearchView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SwitchCompat;

import java.util.ArrayList;
import java.util.List;

public class ManageAdminsActivity extends AppCompatActivity implements ManageAdminsAdapter.Listener {

    private ImageButton buttonBack;
    private EditText editEmail;
    private Button buttonSubmit;
    private TextView textNoMatches;
    private SwitchCompat switchIsManager;

    private SearchView searchView;
    private TextView textFormTitle;
    private String editingUid = null;
    private ImageButton buttonCancelEdit;
    private NestedScrollView manageScroll;
    private View cardAddForm;


    private ManageAdminsAdapter adapter;
    private AdminManager manager;
    private ArrayList<User> adminList = new ArrayList<User>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_manage_admins);

        bindViews();

        // RecyclerView Setup:
        RecyclerView recycler = findViewById(R.id.recyclerManage);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ManageAdminsAdapter(adminList, this, getIntent().getStringExtra("UID"));
        recycler.setAdapter(adapter);

        //manager setup and getting live data
        manager = new AdminManager();
        manager.startLive(new AdminManager.AdminCallback() {
            @Override
            public void onResult(List<User> admins){
                adapter.submitList(admins);
                updateEmptyText();
            }

            @Override
            public void onError(String errorMessage){
                Toast.makeText(ManageAdminsActivity.this, "Load failed: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText){
                adapter.setQuery(newText);
                updateEmptyText();
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query){
                adapter.setQuery(query);
                updateEmptyText();
                searchView.clearFocus();
                return true;
            }
        });

        // Click Listeners:
        buttonBack.setOnClickListener(v -> finish());

        buttonSubmit.setOnClickListener(v -> {
            String email = editEmail.getText().toString().trim();
            if(email.isEmpty()){
                Toast.makeText(this, R.string.toast_fill_required, Toast.LENGTH_SHORT).show();
                return;
            }

            String role;
            if(switchIsManager.isChecked()){
                role = "admin_m";
            } else{
                role = "admin";
            }

            if(editingUid == null){
                manager.addAdmin(email, new AdminManager.WriteCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(ManageAdminsActivity.this, R.string.toast_admin_added, Toast.LENGTH_SHORT).show();
                        editEmail.setText("");
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Toast.makeText(ManageAdminsActivity.this, R.string.toast_admin_add_fail + errorMessage, Toast.LENGTH_LONG).show();
                    }
                }, role);
            } else{
                manager.updateAdminRole(editingUid, role, new AdminManager.WriteCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(ManageAdminsActivity.this, R.string.toast_admin_added, Toast.LENGTH_SHORT).show();
                        editExitMode();
                    }
                    @Override
                    public void onError(String errorMessage){
                        Toast.makeText(ManageAdminsActivity.this, R.string.toast_admin_add_fail + errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
            }


        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.manageRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void bindViews(){
        buttonBack = findViewById(R.id.buttonBackManageAdmin);
        editEmail = findViewById(R.id.editEmail);
        buttonSubmit = findViewById(R.id.buttonSubmitArtifact);
        textNoMatches = findViewById(R.id.textNoMatches);
        searchView = findViewById(R.id.searchManage);
        switchIsManager = findViewById(R.id.switchIsManager);
        textFormTitle = findViewById(R.id.textFormTitle);
        manageScroll = findViewById(R.id.manageScroll);
        cardAddForm = findViewById(R.id.cardAddForm);
        buttonCancelEdit = findViewById(R.id.buttonCancelEdit);

        buttonCancelEdit.setOnClickListener(v -> editExitMode());
    }

    private void updateEmptyText(){
        if(adapter != null && textNoMatches != null){
            boolean isEmpty = adapter.getItemCount() == 0;
            textNoMatches.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onRemoveClicked(User user){
        manager.removeAdmin(user.getUid(), new AdminManager.WriteCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(ManageAdminsActivity.this, R.string.toast_admin_removed, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(ManageAdminsActivity.this, R.string.toast_admin_remove_fail + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onEditClicked(User user){
        editingUid = user.getUid();

        //fill out form with current data
        editEmail.setText(user.getEmail());
        editEmail.setEnabled(false);
        switchIsManager.setChecked(user.getRole().equals("admin_m"));

        textFormTitle.setText("Edit Admin Permissions");
        buttonSubmit.setText("Save Changes");

        buttonCancelEdit.setVisibility(View.VISIBLE);

        manageScroll.post(() -> manageScroll.smoothScrollTo(0, cardAddForm.getTop()));
    }

    private void editExitMode(){
        editingUid = null;

        editEmail.setText("");
        editEmail.setEnabled(true);
        switchIsManager.setChecked(false);

        textFormTitle.setText(R.string.section_add_admin);
        buttonSubmit.setText(R.string.action_add_admin);

        buttonCancelEdit.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if (manager != null){
            manager.stopLive();
        }
    }
}