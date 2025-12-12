package domaine.dto;

import java.awt.Point;

public class MeubleDTO {

    private int id;
    private String nom;

    private int x;
    private int y;

    private int largeur;
    private int longueur;

    private String type;

    private boolean selectionne;
    
    private double angle; // Angle de rotation en degrés

    // ----- Drain -----
    private boolean aDrain;
    private int drainX;
    private int drainY;
    private int diametreDrain = 4;
    private Point positionDrain; // pour le mapper

    // ----- GETTERS / SETTERS de base -----

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean estSelectionne() {
        return selectionne;
    }

    public void setSelectionne(boolean selectionne) {
        this.selectionne = selectionne;
    }

    public double getAngle() {
        return angle;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }

    // ----- Drain -----

    public boolean isaDrain() {      // utilisé dans PanelDessin
        return aDrain;
    }

    public void setaDrain(boolean aDrain) {
        this.aDrain = aDrain;
    }

    public int getDrainX() {
        return drainX;
    }

    public void setDrainX(int drainX) {
        this.drainX = drainX;
        this.positionDrain = new Point(drainX, this.drainY);
    }

    public int getDrainY() {
        return drainY;
    }

    public void setDrainY(int drainY) {
        this.drainY = drainY;
        this.positionDrain = new Point(this.drainX, drainY);
    }

    public int getDiametreDrain() {
        return diametreDrain;
    }

    public void setDiametreDrain(int diametreDrain) {
        this.diametreDrain = diametreDrain;
    }

    public Point getPositionDrain() {
        if (positionDrain == null) {
            positionDrain = new Point(drainX, drainY);
        }
        return positionDrain;
    }

    public void setPositionDrain(Point p) {
        if (p != null) {
            this.positionDrain = p;
            this.drainX = p.x;
            this.drainY = p.y;
        }
    }
}
