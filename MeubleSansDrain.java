package domaine.meuble;

/**
 * Classe abstraite pour les meubles sans drain (placard, armoire, toilette, vanité)
 */
public abstract class MeubleSansDrain extends Meuble {

    public MeubleSansDrain(String nom, int x, int y, int largeur, int longueur) {
        super(nom, x, y, largeur, longueur);
    }

    @Override
    public boolean aDrain() {
        return false;
    }
}
