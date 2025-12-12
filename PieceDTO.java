package domaine.dto;

import java.util.List;

/**
 * DTO pour la communication des données de pièce
 */
public class PieceDTO {
    private int largeur;
    private int longueur;
    private List<MeubleDTO> meubles;
    private List<ElementChauffantDTO> elementsChauffants;
    private List<ZoneTamponDTO> zonesTampon;
    private List<ZoneInterdictionDTO> zonesInterdiction;
    private FliChauffantDTO fliChauffant;
    private List<java.awt.Point> points; // Points définissant le contour de la pièce

    public PieceDTO() {
    }

    public PieceDTO(int largeur, int longueur) {
        this.largeur = largeur;
        this.longueur = longueur;
    }

    // === Getters et Setters ===
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

    public List<MeubleDTO> getMeubles() {
        return meubles;
    }

    public void setMeubles(List<MeubleDTO> meubles) {
        this.meubles = meubles;
    }

    public List<ElementChauffantDTO> getElementsChauffants() {
        return elementsChauffants;
    }

    public void setElementsChauffants(List<ElementChauffantDTO> elementsChauffants) {
        this.elementsChauffants = elementsChauffants;
    }

    public List<ZoneTamponDTO> getZonesTampon() {
        return zonesTampon;
    }

    public void setZonesTampon(List<ZoneTamponDTO> zonesTampon) {
        this.zonesTampon = zonesTampon;
    }

    public List<ZoneInterdictionDTO> getZonesInterdiction() {
        return zonesInterdiction;
    }

    public void setZonesInterdiction(List<ZoneInterdictionDTO> zonesInterdiction) {
        this.zonesInterdiction = zonesInterdiction;
    }

    public FliChauffantDTO getFliChauffant() {
        return fliChauffant;
    }

    public void setFliChauffant(FliChauffantDTO fliChauffant) {
        this.fliChauffant = fliChauffant;
    }

    public List<java.awt.Point> getPoints() {
        return points;
    }

    public void setPoints(List<java.awt.Point> points) {
        this.points = points;
    }
}
