package com.example.b07taam2026;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;


public class ManageArtifactsActivity extends AppCompatActivity implements ManageArtifactAdapter.Listener {

    private NestedScrollView manageScroll;
    private View cardAddForm;
    private TextView textFormTitle, textEditingBanner, textNoMatches;
    private EditText editLot, editName, editCategory, editDynasty, editMaterial,
            editDimensions, editCulturalOrigin, editCurrentLocation, editAccessionNumber,
            editAcquisitionMethod, editProvenance, editConditionReport, editDescription,
            editNotes, editImageUrl;
    private Button buttonSubmit, buttonCancelEdit;

    private ManageArtifactAdapter adapter;
    private ArtifactManager manager;

    // prevent editing lot number
    private String editingLot = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_manage_artifacts);
        bindViews();

        // embedded scroller through artifacts
        RecyclerView recycler = findViewById(R.id.recyclerManage);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ManageArtifactAdapter(new ArrayList<>(), this);
        recycler.setAdapter(adapter);

        // same live artifact feed as home page
        manager = new ArtifactManager();
        manager.startLive(new ArtifactManager.ArtifactCallback() {
            @Override
            public void onResult(List<Artifact> artifacts) {
                adapter.submitList(artifacts);
                updateEmptyText();
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(ManageArtifactsActivity.this,
                        "Load failed: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });

        // same search stub as home
        SearchView search = findViewById(R.id.searchManage);
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) {
                adapter.setQuery(query);
                updateEmptyText();
                search.clearFocus();
                return true;
            }
            @Override public boolean onQueryTextChange(String newText) {
                adapter.setQuery(newText);
                updateEmptyText();
                return true;
            }
        });

        buttonSubmit.setOnClickListener(v -> handleSubmit());
        buttonCancelEdit.setOnClickListener(v -> exitEditMode());
    }

    private void bindViews() {
        manageScroll = findViewById(R.id.manageScroll);
        cardAddForm = findViewById(R.id.cardAddForm);
        textFormTitle = findViewById(R.id.textFormTitle);
        textEditingBanner = findViewById(R.id.textEditingBanner);
        textNoMatches = findViewById(R.id.textNoMatches);
        editLot = findViewById(R.id.editLot);
        editName = findViewById(R.id.editName);
        editCategory = findViewById(R.id.editCategory);
        editDynasty = findViewById(R.id.editDynasty);
        editMaterial = findViewById(R.id.editMaterial);
        editDimensions = findViewById(R.id.editDimensions);
        editCulturalOrigin = findViewById(R.id.editCulturalOrigin);
        editCurrentLocation = findViewById(R.id.editCurrentLocation);
        editAccessionNumber = findViewById(R.id.editAccessionNumber);
        editAcquisitionMethod = findViewById(R.id.editAcquisitionMethod);
        editProvenance = findViewById(R.id.editProvenance);
        editConditionReport = findViewById(R.id.editConditionReport);
        editDescription = findViewById(R.id.editDescription);
        editNotes = findViewById(R.id.editNotes);
        editImageUrl = findViewById(R.id.editImageUrl);
        buttonSubmit = findViewById(R.id.buttonSubmitArtifact);
        buttonCancelEdit = findViewById(R.id.buttonCancelEdit);
    }

    // add or save, no real writes yet
    private void handleSubmit() {
        String lot = editLot.getText().toString().trim();
        String name = editName.getText().toString().trim();
        String category = editCategory.getText().toString().trim();
        String dynasty = editDynasty.getText().toString().trim();

        if (lot.isEmpty() || name.isEmpty() || category.isEmpty() || dynasty.isEmpty()) {
            Toast.makeText(this, R.string.toast_fill_required, Toast.LENGTH_SHORT).show();
            return;
        }

        if (editingLot == null) {
            // TODO: write new artifact to the database
            Toast.makeText(this, R.string.toast_artifact_added, Toast.LENGTH_SHORT).show();
            clearForm();
        } else {
            // TODO: update artifact in the database
            Toast.makeText(this, R.string.toast_artifact_updated, Toast.LENGTH_SHORT).show();
            exitEditMode();
        }
    }

    // prefill form, lock the lot number
    @Override
    public void onEditClicked(Artifact artifact) {
        editingLot = artifact.getLotNumber();

        editLot.setText(artifact.getLotNumber());
        editName.setText(artifact.getName());
        editCategory.setText(artifact.getCategory());
        editDynasty.setText(artifact.getDynasty());
        editMaterial.setText(artifact.getMaterial());
        editDimensions.setText(artifact.getDimensions());
        editCulturalOrigin.setText(artifact.getCulturalOrigin());
        editCurrentLocation.setText(artifact.getCurrentLocation());
        editAccessionNumber.setText(artifact.getAccessionNumber());
        editAcquisitionMethod.setText(artifact.getAcquisitionMethod());
        editProvenance.setText(artifact.getProvenance());
        editConditionReport.setText(artifact.getConditionReport());
        editDescription.setText(artifact.getDescription());
        editNotes.setText(artifact.getNotes());
        editImageUrl.setText(artifact.getImageUrl());

        editLot.setEnabled(false);
        textFormTitle.setText(R.string.section_edit_artifact);
        textEditingBanner.setVisibility(View.VISIBLE);
        buttonSubmit.setText(R.string.action_save_changes);
        buttonCancelEdit.setVisibility(View.VISIBLE);

        // scrolling to form
        manageScroll.post(() -> manageScroll.smoothScrollTo(0, cardAddForm.getTop()));
    }

    @Override
    public void onDeleteClicked(Artifact artifact) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_dialog_title)
                .setMessage(R.string.delete_dialog_message)
                .setPositiveButton(R.string.delete_dialog_confirm, (dialog, which) -> {
                    // TODO: delete artifact from the database
                    Toast.makeText(this, R.string.toast_artifact_deleted, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.delete_dialog_cancel, null)
                .show();
    }

    // clear add form
    private void exitEditMode() {
        editingLot = null;
        clearForm();
        editLot.setEnabled(true);
        textFormTitle.setText(R.string.section_add_artifact);
        textEditingBanner.setVisibility(View.GONE);
        buttonSubmit.setText(R.string.action_add_artifact);
        buttonCancelEdit.setVisibility(View.GONE);
    }

    private void clearForm() {
        EditText[] fields = {editLot, editName, editCategory, editDynasty, editMaterial,
                editDimensions, editCulturalOrigin, editCurrentLocation, editAccessionNumber,
                editAcquisitionMethod, editProvenance, editConditionReport, editDescription,
                editNotes, editImageUrl};
        for (EditText field : fields) {
            field.setText("");
        }
        editLot.requestFocus();
    }

    private void updateEmptyText() {
        textNoMatches.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (manager != null) manager.stopLive();
    }
}
