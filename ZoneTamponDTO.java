package domaine.dto;

import java.awt.Point;

/**
 * DTO pour la communication des données de zone tampon
 */
public class ZoneTamponDTO {
    private int id;
    private Point position;
    private int largeur;
    private int longueur;
    private double distance;
    private String nom;

    public ZoneTamponDTO() {
    }

    public ZoneTamponDTO(Point position, int largeur, int longueur, double distance, String nom) {
        this.position = position;
        this.largeur = largeur;
        this.longueur = longueur;
        this.distance = distance;
        this.nom = nom;
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

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
