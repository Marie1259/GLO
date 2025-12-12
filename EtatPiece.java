package domaine.sauvegarde;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.awt.Point;

/**
 * Classe pour sauvegarder l'état complet d'une pièce
 * Utilise Serializable pour la sauvegarde/chargement
 */
public class EtatPiece implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int largeur;
    private int longueur;
    private List<Point> points;
    
    // Meubles
    private List<EtatMeuble> meubles;
    
    // Éléments chauffants
    private List<EtatElementChauffant> elementsChauffants;
    
    // Zones
    private List<EtatZone> zonesInterdiction;
    private List<EtatZone> zonesTampon;
    
    // Fil chauffant
    private EtatFilChauffant filChauffant;
    
    public EtatPiece() {
        this.meubles = new ArrayList<>();
        this.elementsChauffants = new ArrayList<>();
        this.zonesInterdiction = new ArrayList<>();
        this.zonesTampon = new ArrayList<>();
        this.points = new ArrayList<>();
    }
    
    // Getters et Setters
    public int getLargeur() { return largeur; }
    public void setLargeur(int largeur) { this.largeur = largeur; }
    
    public int getLongueur() { return longueur; }
    public void setLongueur(int longueur) { this.longueur = longueur; }
    
    public List<Point> getPoints() { return points; }
    public void setPoints(List<Point> points) { this.points = points; }
    
    public List<EtatMeuble> getMeubles() { return meubles; }
    public void setMeubles(List<EtatMeuble> meubles) { this.meubles = meubles; }
    
    public List<EtatElementChauffant> getElementsChauffants() { return elementsChauffants; }
    public void setElementsChauffants(List<EtatElementChauffant> elementsChauffants) { this.elementsChauffants = elementsChauffants; }
    
    public List<EtatZone> getZonesInterdiction() { return zonesInterdiction; }
    public void setZonesInterdiction(List<EtatZone> zonesInterdiction) { this.zonesInterdiction = zonesInterdiction; }
    
    public List<EtatZone> getZonesTampon() { return zonesTampon; }
    public void setZonesTampon(List<EtatZone> zonesTampon) { this.zonesTampon = zonesTampon; }
    
    public EtatFilChauffant getFilChauffant() { return filChauffant; }
    public void setFilChauffant(EtatFilChauffant filChauffant) { this.filChauffant = filChauffant; }
    
    /**
     * Classe interne pour sauvegarder un meuble
     */
    public static class EtatMeuble implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private int id;
        private String type;
        private String nom;
        private int x, y;
        private int largeur, longueur;
        private double angle;
        private Integer drainX, drainY;
        private Integer diametreDrain;
        
        // Getters et Setters
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public String getNom() { return nom; }
        public void setNom(String nom) { this.nom = nom; }
        
        public int getX() { return x; }
        public void setX(int x) { this.x = x; }
        
        public int getY() { return y; }
        public void setY(int y) { this.y = y; }
        
        public int getLargeur() { return largeur; }
        public void setLargeur(int largeur) { this.largeur = largeur; }
        
        public int getLongueur() { return longueur; }
        public void setLongueur(int longueur) { this.longueur = longueur; }
        
        public double getAngle() { return angle; }
        public void setAngle(double angle) { this.angle = angle; }
        
        public Integer getDrainX() { return drainX; }
        public void setDrainX(Integer drainX) { this.drainX = drainX; }
        
        public Integer getDrainY() { return drainY; }
        public void setDrainY(Integer drainY) { this.drainY = drainY; }
        
        public Integer getDiametreDrain() { return diametreDrain; }
        public void setDiametreDrain(Integer diametreDrain) { this.diametreDrain = diametreDrain; }
    }
    
    /**
     * Classe interne pour sauvegarder un élément chauffant
     */
    public static class EtatElementChauffant implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private int id;
        private String type;
        private String nom;
        private int x, y;
        private int largeur, longueur;
        private double angle;
        private boolean actif;
        
        // Getters et Setters
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public String getNom() { return nom; }
        public void setNom(String nom) { this.nom = nom; }
        
        public int getX() { return x; }
        public void setX(int x) { this.x = x; }
        
        public int getY() { return y; }
        public void setY(int y) { this.y = y; }
        
        public int getLargeur() { return largeur; }
        public void setLargeur(int largeur) { this.largeur = largeur; }
        
        public int getLongueur() { return longueur; }
        public void setLongueur(int longueur) { this.longueur = longueur; }
        
        public double getAngle() { return angle; }
        public void setAngle(double angle) { this.angle = angle; }
        
        public boolean isActif() { return actif; }
        public void setActif(boolean actif) { this.actif = actif; }
    }
    
    /**
     * Classe interne pour sauvegarder une zone
     */
    public static class EtatZone implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private int id;
        private String type;
        private String nom;
        private int x, y;
        private int largeur, longueur;
        private Double distance; // Pour ZoneTampon
        
        // Getters et Setters
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public String getNom() { return nom; }
        public void setNom(String nom) { this.nom = nom; }
        
        public int getX() { return x; }
        public void setX(int x) { this.x = x; }
        
        public int getY() { return y; }
        public void setY(int y) { this.y = y; }
        
        public int getLargeur() { return largeur; }
        public void setLargeur(int largeur) { this.largeur = largeur; }
        
        public int getLongueur() { return longueur; }
        public void setLongueur(int longueur) { this.longueur = longueur; }
        
        public Double getDistance() { return distance; }
        public void setDistance(Double distance) { this.distance = distance; }
    }
    
    /**
     * Classe interne pour sauvegarder le fil chauffant
     */
    public static class EtatFilChauffant implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private String nom;
        private int largeur, longueur;
        private int distanceFil;
        private boolean actif;
        private List<Point> chemin; // Chemin du fil (liste de points)
        
        // Getters et Setters
        public String getNom() { return nom; }
        public void setNom(String nom) { this.nom = nom; }
        
        public int getLargeur() { return largeur; }
        public void setLargeur(int largeur) { this.largeur = largeur; }
        
        public int getLongueur() { return longueur; }
        public void setLongueur(int longueur) { this.longueur = longueur; }
        
        public int getDistanceFil() { return distanceFil; }
        public void setDistanceFil(int distanceFil) { this.distanceFil = distanceFil; }
        
        public boolean isActif() { return actif; }
        public void setActif(boolean actif) { this.actif = actif; }
        
        public List<Point> getChemin() { return chemin; }
        public void setChemin(List<Point> chemin) { this.chemin = chemin; }
    }
}

