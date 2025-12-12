package domaine.dto;

import java.awt.Point;

/**
 * DTO pour la communication des données de zone d'interdiction
 */
public class ZoneInterdictionDTO {
    private int id;
    private Point position;
    private int largeur;
    private int longueur;
    private String nom;

    public ZoneInterdictionDTO() {
    }

    public ZoneInterdictionDTO(int id, Point position, int largeur, int longueur, String nom) {
        this.id = id;
        this.position = position;
        this.largeur = largeur;
        this.longueur = longueur;
        this.nom = nom;
    }

    // === Getters et Setters ===
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }
}

