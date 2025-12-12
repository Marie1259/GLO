package domaine.meuble;

/**
 * Représente une toilette dans la salle de bain
 */
public class Toilette extends MeubleAvecDrain {

    public Toilette(String nom, int x, int y, int largeur, int longueur) {
        super(nom, x, y, largeur, longueur);
    }

    @Override
    public String getType() {
        return "Toilette";
    }
}
