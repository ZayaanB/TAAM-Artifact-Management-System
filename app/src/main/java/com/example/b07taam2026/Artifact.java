package com.example.b07taam2026;

public class Artifact {
    private String name;
    private String lotNumber;
    private String category;
    private String material;
    private String dynastyPeriod;
    private String description;

    public Artifact() {}

    public Artifact(String name, String lotNumber, String category, String material,
                    String dynastyPeriod, String description) {
        this.name = name;
        this.lotNumber = lotNumber;
        this.category = category;
        this.material = material;
        this.dynastyPeriod = dynastyPeriod;
        this.description = description;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLotNumber() { return lotNumber; }
    public void setLotNumber(String lotNumber) { this.lotNumber = lotNumber; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getMaterial() { return material; }
    public void setMaterial(String material) { this.material = material; }

    public String getDynastyPeriod() { return dynastyPeriod; }
    public void setDynastyPeriod(String dynastyPeriod) { this.dynastyPeriod = dynastyPeriod; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}