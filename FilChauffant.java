package domaine.chauffage;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * Représente un fil chauffant dans la pièce
 * Le fil chauffant est unique et parcourt toute la pièce
 */
public class FilChauffant {
    private String nom;
    private int largeur; // largeur de la pièce
    private int longueur; // longueur de la pièce
    private int distanceFil; // distance entre les spires du fil (en pouces)
    private int longueurSouhaitee; // longueur souhaitée du fil (en pouces)
    private boolean actif;
    private List<Point> chemin; // Chemin du fil (liste de points/intersections)

    public FilChauffant(String nom, int largeur, int longueur, int distanceFil) {
        this.nom = nom;
        this.largeur = largeur;
        this.longueur = longueur;
        this.distanceFil = distanceFil;
        this.actif = false; // Inactif par défaut
        this.chemin = new ArrayList<>();
    }

    // === Getters et Setters ===
    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
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

    public int getLongueurSouhaitee() {
        return longueurSouhaitee;
    }

    public void setLongueurSouhaitee(int longueurSouhaitee) {
        this.longueurSouhaitee = longueurSouhaitee;
    }

    public int getDistanceFil() {
        return distanceFil;
    }

    public void setDistanceFil(int distanceFil) {
        this.distanceFil = distanceFil;
    }

    public boolean estActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }

    public List<Point> getChemin() {
        return new ArrayList<>(chemin);
    }

    public void setChemin(List<Point> chemin) {
        this.chemin = chemin != null ? new ArrayList<>(chemin) : new ArrayList<>();
    }

    public void ajouterPointChemin(Point point) {
        if (point != null) {
            this.chemin.add(new Point(point));
        }
    }

    public void viderChemin() {
        this.chemin.clear();
    }

    /**
     * Calcule la longueur totale du fil chauffant nécessaire
     */
    public double calculerLongueurTotale() {
        if (chemin != null && chemin.size() > 1) {
            // Calculer la longueur réelle du chemin
            double longueurTotale = 0;
            for (int i = 0; i < chemin.size() - 1; i++) {
                Point p1 = chemin.get(i);
                Point p2 = chemin.get(i + 1);
                double dx = p2.x - p1.x;
                double dy = p2.y - p1.y;
                longueurTotale += Math.sqrt(dx * dx + dy * dy);
            }
            return longueurTotale;
        }
        // Fallback: calcul approximatif
        int nombreSpires = (int) Math.ceil((double) longueur / distanceFil);
        return nombreSpires * largeur;
    }

    /**
     * Met à jour les dimensions du fil selon la pièce
     */
    public void mettreAJourDimensions(int largeur, int longueur) {
        this.largeur = largeur;
        this.longueur = longueur;
    }

    @Override
    public String toString() {
        return String.format("Fil chauffant '%s' (%d\" x %d\", distance: %d\", longueur totale: %.1f\")",
                nom, largeur, longueur, distanceFil, calculerLongueurTotale());
    }
}
