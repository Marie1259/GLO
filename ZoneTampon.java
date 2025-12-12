package domaine.zone;

import java.awt.Point;

/**
 * Zone tampon dans la pièce
 */
public class ZoneTampon extends Zone {
    private double distance;

    public ZoneTampon(Point position, int largeur, int longueur, double distance, String nom) {
        super(position, largeur, longueur, nom);
        this.distance = distance;
    }

    public ZoneTampon(int x, int y, int largeur, int longueur, double distance, String nom) {
        super(x, y, largeur, longueur, nom);
        this.distance = distance;
    }

    // === Getters et Setters ===
    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    @Override
    public String getTypeZone() {
        return "Zone Tampon";
    }
}
