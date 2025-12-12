package domaine.meuble;

/**
 * Représente une baignoire dans la salle de bain
 * Classe mise à jour pour forcer la recompilation
 */
public class Bain extends MeubleAvecDrain {

    public Bain(String nom, int x, int y, int largeur, int longueur) {
        super(nom, x, y, largeur, longueur);
    }

    @Override
    public String getType() {
        return "Bain";
    }
}
