package com.example.b07taam2026;

import com.google.firebase.database.Exclude;

public class Artifact implements java.io.Serializable {
    private String name;
    private String lotNumber;
    private String category;
    private String material;
    private String dynasty;
    private String description;
    private String culturalOrigin;
    private String dimensions;
    private String conditionReport;
    private String currentLocation;
    private String acquisitionMethod;
    private String provenance;
    private String accessionNumber;
    private String notes;
    private String imageUrl;

    // constructors, empty one needed by firebase
    public Artifact() {}

    public Artifact(String name, String lotNumber, String category, String material,
                    String dynasty, String description) {
        this.name = name;
        this.lotNumber = lotNumber;
        this.category = category;
        this.material = material;
        this.dynasty = dynasty;
        this.description = description;
    }

    public String[] searchableValues() {
        return new String[] {
                name, lotNumber, category, material, dynasty, description, culturalOrigin,
                dimensions, conditionReport, currentLocation, acquisitionMethod, provenance,
                accessionNumber, notes
        };
    }

    // standard getters and setters
    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    @Exclude
    public String getLotNumber() { return lotNumber; }

    @Exclude
    public void setLotNumber(String lotNumber) { this.lotNumber = lotNumber; }

    public String getCategory() { return category; }

    public void setCategory(String category) { this.category = category; }

    public String getMaterial() { return material; }

    public void setMaterial(String material) { this.material = material; }

    public String getDynasty() { return dynasty; }

    public void setDynasty(String dynasty) { this.dynasty = dynasty; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public String getCulturalOrigin() { return culturalOrigin; }

    public void setCulturalOrigin(String culturalOrigin) {
        this.culturalOrigin = culturalOrigin;
    }

    public String getDimensions() {
        return dimensions;
    }

    public void setDimensions(String dimensions) {
        this.dimensions = dimensions;
    }

    public String getConditionReport() {
        return conditionReport;
    }

    public void setConditionReport(String conditionReport) {
        this.conditionReport = conditionReport;
    }

    public String getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(String currentLocation) {
        this.currentLocation = currentLocation;
    }

    public String getAcquisitionMethod() {
        return acquisitionMethod;
    }

    public void setAcquisitionMethod(String acquisitionMethod) {
        this.acquisitionMethod = acquisitionMethod;
    }

    public String getProvenance() {
        return provenance;
    }

    public void setProvenance(String provenance) {
        this.provenance = provenance;
    }

    public String getAccessionNumber() {
        return accessionNumber;
    }

    public void setAccessionNumber(String accessionNumber) {
        this.accessionNumber = accessionNumber;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}