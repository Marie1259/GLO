package domaine.chauffage;

import domaine.piece.Mur;

/**
 * Représente un thermostat dans la pièce
 */
public class Thermostat extends ElementChauffant {
    private boolean enMarche;

    public Thermostat(String nom, int x, int y) {
        super(nom, x, y, 8, 6);
        this.enMarche = false;
    }

    public Thermostat(String nom, int x, int y, int largeur, int longueur) {
        super(nom, x, y, largeur, longueur);
        this.enMarche = false;
    }

    // === Getters et Setters ===
    public boolean estEnMarche() {
        return enMarche;
    }

    public void setEnMarche(boolean enMarche) {
        this.enMarche = enMarche;
    }

    // === Méthodes liées au mur ===
    public boolean estSurMur() {
        return getMur() != null;
    }

    public void activer() {
        this.enMarche = true;
    }

    public void desactiver() {
        this.enMarche = false;
    }
}
