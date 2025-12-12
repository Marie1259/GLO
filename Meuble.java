package domaine.meuble;

import domaine.Identifiable;
import java.awt.Point;

public abstract class Meuble implements Identifiable {

    protected int id;
    protected String nom;

    protected int x;
    protected int y;

    protected int largeur;
    protected int longueur;

    // pour différencier douche/bain/toilette...
    protected String type;
    
    protected double angle; // Angle de rotation en degrés (0 = horizontal, 90 = vertical)

    public Meuble(String nom, int x, int y, int largeur, int longueur) {
        this.nom = nom;
        this.x = x;
        this.y = y;
        this.largeur = largeur;
        this.longueur = longueur;
    }

    // ----------- IDENTIFIABLE -------------
    @Override
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    // ----------- GETTERS / SETTERS ------------------
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public int getX() { return x; }
    public int getY() { return y; }

    public int getLargeur() { return largeur; }
    public int getLongueur() { return longueur; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public double getAngle() { return angle; }
    public void setAngle(double angle) { this.angle = angle; }

    public boolean aDrain() { return false; }

    // ======== DRAIN par défaut (override dans MeubleAvecDrain) =========
    public int getDiametreDrain() { return 0; }
    public void setDiametreDrain(int diametre) {}

    // =========================================
    //   HOOKS POUR LES SOUS-CLASSES
    // =========================================
    protected void onPositionChanged() {}
    protected void onDimensionChanged() {}

    // ----------- POSITION -----------------
    public void setPosition(int newX, int newY) {
        this.x = newX;
        this.y = newY;
        onPositionChanged(); // notifie MeubleAvecDrain
    }

    public void setPosition(Point p) {
        setPosition(p.x, p.y);
    }

    // ----------- DIMENSIONS ----------------
    public void setLargeur(int largeur) {
        this.largeur = largeur;
        onDimensionChanged();
    }

    public void setLongueur(int longueur) {
        this.longueur = longueur;
        onDimensionChanged();
    }

    public void setDimensions(int largeur, int longueur) {
        this.largeur = largeur;
        this.longueur = longueur;
        onDimensionChanged();
    }

    @Override
    public String toString() {
        return nom + " (" + largeur + "x" + longueur + ")";
    }
}
