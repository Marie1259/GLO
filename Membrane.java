package domaine.graphe;

/**
 * Représente une membrane dans le système de chauffage
 */
public class Membrane {
    private String id;
    private double distanceIntersection;

    public Membrane(String id, double distanceIntersection) {
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
