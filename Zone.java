package domaine.zone;

import domaine.Identifiable;
import java.awt.Point;

/**
 * Classe abstraite de base pour toutes les zones
 */
public abstract class Zone implements Identifiable {
    protected int id;
    protected Point position;
    protected int largeur;
    protected int longueur;
    protected String nom;

    public Zone(Point position, int largeur, int longueur, String nom) {
        this.id = domaine.Ids.next();
        this.position = position;
        this.largeur = largeur;
        this.longueur = longueur;
        this.nom = nom;
    }

    public Zone(int x, int y, int largeur, int longueur, String nom) {
        this.id = domaine.Ids.next();
        this.position = new Point(x, y);
        this.largeur = largeur;
        this.longueur = longueur;
        this.nom = nom;
    }

    @Override
    public int getId() {
        return id;
    }

    // === Getters et Setters ===
    public Point getPosition() {
        return position;
    }

    public void setPosition(Point position) {
        this.position = position;
    }

    public int getLargeur() {
        return largeur;
    }

    public void setLargeur(int largeur) {
        this.largeur = largeur;
    }

    public int getLongueur() {
        return longueur;
    }

    public void setLongueur(int longueur) {
        this.longueur = longueur;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    // === Méthodes abstraites ===
    public abstract String getTypeZone();
}
