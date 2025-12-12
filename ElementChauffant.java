package domaine.chauffage;

import domaine.Identifiable;
import domaine.Ids;
import domaine.piece.Mur;
import java.awt.Point;

/**
 * Représente un élément chauffant rectangulaire dans la pièce
 * Peut être un radiateur, un thermostat, etc.
 * Note: Le plancher chauffant est généré à la fin de la modélisation
 */
public class ElementChauffant implements Identifiable {
    protected int id;
    protected String nom;
    protected int x, y;
    protected int largeur, longueur;
    protected boolean selectionne;
    protected boolean actif; // État actif/inactif de l'élément
    protected Mur mur;
    protected double angle; // Angle de rotation en degrés (0 = horizontal, 90 = vertical)

    public ElementChauffant(String nom, int x, int y) {
        this.id = Ids.next();
        this.nom = nom;
        this.x = x;
        this.y = y;
        this.largeur = 2; // Taille par défaut rectangulaire : 2" x 10"
        this.longueur = 10;
        this.selectionne = false;
        this.actif = false; // Par défaut, inactif
        this.angle = 0.0; // Par défaut, pas de rotation
    }

    public ElementChauffant(String nom, int x, int y, int largeur, int longueur) {
        this.id = Ids.next();
        this.nom = nom;
        this.x = x;
        this.y = y;
        this.largeur = largeur;
        this.longueur = longueur;
        this.selectionne = false;
        this.actif = false; // Par défaut, inactif
        this.angle = 0.0; // Par défaut, pas de rotation
    }

    @Override
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Point getPosition() {
        return new Point(x, y);
    }

    public void setPosition(Point position) {
        if (position != null) {
            this.x = position.x;
            this.y = position.y;
        }
    }

    // === Getters et Setters ===
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public int getX() { return x; }
    public int getY() { return y; }
    public void setPosition(int x, int y) { this.x = x; this.y = y; }

    public int getLargeur() { return largeur; }
    public void setLargeur(int largeur) { this.largeur = largeur; }

    public int getLongueur() { return longueur; }
    public void setLongueur(int longueur) { this.longueur = longueur; }

    public boolean estSelectionne() { return selectionne; }
    public void setSelectionne(boolean selectionne) { this.selectionne = selectionne; }

    public Mur getMur() { return mur; }
    public void setMur(Mur mur) { this.mur = mur; }
    
    public double getAngle() { return angle; }
    public void setAngle(double angle) { this.angle = angle; }
    
    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }

    // === Méthodes de gestion du mur ===
    public void associeAuMur(Mur mur) {
        this.mur = mur;
        if (mur != null) {
            mur.addElementChauffant(this);
        }
    }

    public void dissocieDuMur() {
        if (this.mur != null) {
            this.mur.removeElementChauffant(this);
            this.mur = null;
        }
    }

    public boolean estAssocieAuMur() {
        return this.mur != null;
    }

    @Override
    public String toString() {
        return String.format("Élément chauffant '%s' (%d\" x %d\")", nom, largeur, longueur);
    }
}
