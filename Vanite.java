package domaine.meuble;

/**
 * Représente une vanité dans la salle de bain
 */
public class Vanite extends MeubleAvecDrain {

    public Vanite(String nom, int x, int y, int largeur, int longueur) {
        super(nom, x, y, largeur, longueur);
    }

    @Override
    public String getType() {
        return "Vanité";
    }
}
