package domaine.dto;

/**
 * DTO pour la communication des données de membrane
 */
public class MembraneDTO {
    private String id;
    private double distanceIntersection;

    public MembraneDTO() {
    }

    public MembraneDTO(String id, double distanceIntersection) {
        this.id = id;
        this.distanceIntersection = distanceIntersection;
    }

    // === Getters et Setters ===
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getDistanceIntersection() {
        return distanceIntersection;
    }

    public void setDistanceIntersection(double distanceIntersection) {
        this.distanceIntersection = distanceIntersection;
    }
}
