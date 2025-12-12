package domaine.dto;

import java.awt.Point;
import java.util.List;

/**
 * DTO pour la communication des données de fil chauffant
 */
public class FliChauffantDTO {
    private String nom;
    private double longueur;
    private double distanceEnroulement;
    private boolean actif;
    private List<MembraneDTO> membranes;
    private List<IntersectionDTO> intersections;
    private List<Point> chemin; // Chemin du fil (liste de points)

    public FliChauffantDTO() {
    }

    public FliChauffantDTO(String nom, double longueur, double distanceEnroulement) {
        this.nom = nom;
        this.longueur = longueur;
        this.distanceEnroulement = distanceEnroulement;
        this.actif = false;
    }

    // === Getters et Setters ===
    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public double getLongueur() {
        return longueur;
    }

    public void setLongueur(double longueur) {
        this.longueur = longueur;
    }

    public double getDistanceEnroulement() {
        return distanceEnroulement;
    }

    public void setDistanceEnroulement(double distanceEnroulement) {
        this.distanceEnroulement = distanceEnroulement;
    }

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }

    public List<MembraneDTO> getMembranes() {
        return membranes;
    }

    public void setMembranes(List<MembraneDTO> membranes) {
        this.membranes = membranes;
    }

    public List<IntersectionDTO> getIntersections() {
        return intersections;
    }

    public void setIntersections(List<IntersectionDTO> intersections) {
        this.intersections = intersections;
    }

    public List<Point> getChemin() {
        return chemin;
    }

    public void setChemin(List<Point> chemin) {
        this.chemin = chemin;
    }
}
