package domaine.meuble;

/**
 * Représente une armoire dans la salle de bain
 * Classe mise à jour pour forcer la recompilation
 */
public class Armoire extends MeubleSansDrain {

    public Armoire(String nom, int x, int y, int largeur, int longueur) {
        super(nom, x, y, largeur, longueur);
    }

    @Override
    public String getType() {
        return "Armoire";
    }
}
