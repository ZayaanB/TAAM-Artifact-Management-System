package com.example.b07taam2026;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
    private Button buttonSubmit, buttonCancelEdit, buttonUploadImage;

    private ManageArtifactAdapter adapter;
    private ArtifactManager manager;
    private SupabaseImageUploader imageUploader;

    // system photo picker
    private final ActivityResultLauncher<String> imagePicker = registerForActivityResult(new ActivityResultContracts.GetContent(), this::handleImagePicked);

    // prevent editing lot number
    private String editingLot = null;

    // image url when edit started
    private String editingOriginalImageUrl = null;
    // image uploaded but unsaved
    private String sessionUploadedUrl = null;

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
                Toast.makeText(ManageArtifactsActivity.this, "Load failed: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });

        // same as home for searching
        SearchView search = findViewById(R.id.searchManage);
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override 
            public boolean onQueryTextSubmit(String query) {
                adapter.setQuery(query);
                updateEmptyText();
                search.clearFocus();
                return true;
            }
            @Override 
            public boolean onQueryTextChange(String newText) {
                adapter.setQuery(newText);
                updateEmptyText();
                return true;
            }
        });

        imageUploader = new SupabaseImageUploader(this);

        buttonSubmit.setOnClickListener(v -> handleSubmit());
        buttonCancelEdit.setOnClickListener(v -> exitEditMode());
        buttonUploadImage.setOnClickListener(v -> imagePicker.launch("image/*"));
        findViewById(R.id.buttonBackManage).setOnClickListener(v -> finish());
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
        buttonUploadImage = findViewById(R.id.buttonUploadImage);
    }

    // image url field gets the public url
    private void handleImagePicked(Uri uri) {
        if (uri == null) {
            return;
        }

        String lot = editLot.getText().toString().trim();
        if (lot.isEmpty()) {
            Toast.makeText(this, R.string.toast_lot_before_upload, Toast.LENGTH_SHORT).show();
            return;
        }

        buttonUploadImage.setEnabled(false);
        buttonUploadImage.setText(R.string.state_uploading);

        imageUploader.uploadImage(uri, lot, new SupabaseImageUploader.UploadCallback() {
            
            // getting public url
            @Override
            public void onSuccess(String publicUrl) {
                buttonUploadImage.setEnabled(true);
                buttonUploadImage.setText(R.string.action_upload_image);
                discardUnsavedUpload();
                sessionUploadedUrl = publicUrl;
                editImageUrl.setText(publicUrl);
                Toast.makeText(ManageArtifactsActivity.this, R.string.toast_image_uploaded, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String message) {
                buttonUploadImage.setEnabled(true);
                buttonUploadImage.setText(R.string.action_upload_image);
                Toast.makeText(ManageArtifactsActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    // add or save
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
            buttonSubmit.setEnabled(false);
            Artifact artifact = buildArtifactFromForm();
            manager.addArtifact(lot, artifact, new ArtifactManager.WriteCallback() {
                @Override
                public void onSuccess() {
                    buttonSubmit.setEnabled(true);
                    finishUploadTracking(artifact.getImageUrl());
                    Toast.makeText(ManageArtifactsActivity.this, R.string.toast_artifact_added, Toast.LENGTH_SHORT).show();
                    clearForm();
                }

                @Override
                public void onError(String errorMessage) {
                    buttonSubmit.setEnabled(true);
                    Toast.makeText(ManageArtifactsActivity.this, "Add failed: " + errorMessage, Toast.LENGTH_LONG).show();
                }
            });
        }
        else {
            buttonSubmit.setEnabled(false);
            Artifact artifact = buildArtifactFromForm();
            manager.updateArtifact(editingLot, artifact, new ArtifactManager.WriteCallback() {
                @Override
                public void onSuccess() {
                    buttonSubmit.setEnabled(true);
                    finishUploadTracking(artifact.getImageUrl());
                    // old image was replaced
                    if (editingOriginalImageUrl != null && !editingOriginalImageUrl.equals(artifact.getImageUrl())) {
                        imageUploader.deleteImage(editingOriginalImageUrl);
                    }
                    Toast.makeText(ManageArtifactsActivity.this, R.string.toast_artifact_updated, Toast.LENGTH_SHORT).show();
                    exitEditMode();
                }

                @Override
                public void onError(String errorMessage) {
                    buttonSubmit.setEnabled(true);
                    Toast.makeText(ManageArtifactsActivity.this, "Update failed: " + errorMessage, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    // snapshot of every form field
    private Artifact buildArtifactFromForm() {
        Artifact artifact = new Artifact();
        artifact.setName(editName.getText().toString().trim());
        artifact.setCategory(editCategory.getText().toString().trim());
        artifact.setDynasty(editDynasty.getText().toString().trim());
        artifact.setMaterial(nullIfEmpty(editMaterial));
        artifact.setDimensions(nullIfEmpty(editDimensions));
        artifact.setCulturalOrigin(nullIfEmpty(editCulturalOrigin));
        artifact.setCurrentLocation(nullIfEmpty(editCurrentLocation));
        artifact.setAccessionNumber(nullIfEmpty(editAccessionNumber));
        artifact.setAcquisitionMethod(nullIfEmpty(editAcquisitionMethod));
        artifact.setProvenance(nullIfEmpty(editProvenance));
        artifact.setConditionReport(nullIfEmpty(editConditionReport));
        artifact.setDescription(nullIfEmpty(editDescription));
        artifact.setNotes(nullIfEmpty(editNotes));
        artifact.setImageUrl(nullIfEmpty(editImageUrl));
        return artifact;
    }

    private String nullIfEmpty(EditText field) {
        String value = field.getText().toString().trim();
        return value.isEmpty() ? null : value;
    }

    // prefill form, lock the lot number
    @Override
    public void onEditClicked(Artifact artifact) {
        discardUnsavedUpload();
        editingLot = artifact.getLotNumber();
        editingOriginalImageUrl = artifact.getImageUrl();

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
                .setPositiveButton(R.string.delete_dialog_confirm, (dialog, which) ->
                        manager.deleteArtifact(artifact.getLotNumber(), new ArtifactManager.WriteCallback() {
                            @Override
                            public void onSuccess() {
                                imageUploader.deleteImage(artifact.getImageUrl());
                                
                                if (artifact.getLotNumber() != null && artifact.getLotNumber().equals(editingLot)) {
                                    exitEditMode();
                                }
                                Toast.makeText(ManageArtifactsActivity.this, R.string.toast_artifact_deleted, Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onError(String errorMessage) {
                                Toast.makeText(ManageArtifactsActivity.this, "Delete failed: " + errorMessage, Toast.LENGTH_LONG).show();
                            }
                        }))
                .setNegativeButton(R.string.delete_dialog_cancel, null)
                .show();
    }

    // clear add form
    private void exitEditMode() {
        discardUnsavedUpload();
        editingLot = null;
        editingOriginalImageUrl = null;
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

    // delete unsaved upload
    private void discardUnsavedUpload() {
        if (sessionUploadedUrl != null) {
            imageUploader.deleteImage(sessionUploadedUrl);
            sessionUploadedUrl = null;
        }
    }

    // delete url unsaved
    private void finishUploadTracking(String savedUrl) {
        if (sessionUploadedUrl != null && !sessionUploadedUrl.equals(savedUrl)) {
            imageUploader.deleteImage(sessionUploadedUrl);
        }
        sessionUploadedUrl = null;
    }

    private void updateEmptyText() {
        textNoMatches.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isFinishing()) {
            discardUnsavedUpload();
        }
        if (manager != null) {
            manager.stopLive();
        }
    }
}
