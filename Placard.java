package domaine.meuble;

/**
 * Représente un placard dans la salle de bain
 * Classe mise à jour pour forcer la recompilation
 */
public class Placard extends MeubleSansDrain {

    public Placard(String nom, int x, int y, int largeur, int longueur) {
        super(nom, x, y, largeur, longueur);
    }

    @Override
    public String getType() {
        return "Placard";
    }
}
