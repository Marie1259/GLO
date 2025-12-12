package domaine.dto;

import java.awt.Point;

/**
 * DTO pour la communication des données d'élément chauffant
 */
public class ElementChauffantDTO {
    private int id;
    private String nom;
    private Point position;
    private int largeur;
    private int longueur;
    private String type; // "ElementChauffant" ou "Thermostat"
    private boolean actif;
    private double angle; // Angle de rotation en degrés

    public ElementChauffantDTO() {
    }

    public ElementChauffantDTO(int id, String nom, Point position, int largeur, int longueur, String type, boolean actif) {
        this.id = id;
        this.nom = nom;
        this.position = position;
        this.largeur = largeur;
        this.longueur = longueur;
        this.type = type;
        this.actif = actif;
    }

    // === Getters et Setters ===
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }

    public double getAngle() {
        return angle;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }

    // Méthodes pour gérer les coordonnées x et y séparément
    public int getX() {
        return position != null ? position.x : 0;
    }

    public int getY() {
        return position != null ? position.y : 0;
    }

    public void setX(int x) {
        if (position == null) {
            position = new Point();
        }
        position.x = x;
    }

    public void setY(int y) {
        if (position == null) {
            position = new Point();
        }
        position.y = y;
    }

    // Méthode pour la sélection
    public boolean estSelectionne() {
        return false; // Par défaut, pas sélectionné
    }
}
