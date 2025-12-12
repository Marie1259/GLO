package domaine.meuble;

import java.awt.Point;

/**
 * Représente un drain dans la pièce
 */
public class Drain {
    private Point position;
    private double diametre;
    private String nom;

    public Drain(Point position, double diametre, String nom) {
        this.position = position;
        this.diametre = diametre;
        this.nom = nom;
    }

    public Drain(int x, int y, double diametre, String nom) {
        this.position = new Point(x, y);
        this.diametre = diametre;
        this.nom = nom;
    }

    // === Getters et Setters ===
    public Point getPosition() {
        return position;
    }

    public void setPosition(Point position) {
        this.position = position;
    }

    public double getDiametre() {
        return diametre;
    }

    public void setDiametre(double diametre) {
        this.diametre = diametre;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    // === Méthodes utilitaires ===
    public boolean estDansMeuble(Meuble meuble) {
        return position.x >= meuble.getX() &&
                position.x <= meuble.getX() + meuble.getLargeur() &&
                position.y >= meuble.getY() &&
                position.y <= meuble.getY() + meuble.getLongueur();
    }
}
