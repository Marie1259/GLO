package domaine.meuble;

/**
 * Représente une douche dans la salle de bain
 */
public class Douche extends MeubleAvecDrain {

    public Douche(String nom, int x, int y, int largeur, int longueur) {
        super(nom, x, y, largeur, longueur);
    }

    @Override
    public String getType() {
        return "Douche";
    }
}
