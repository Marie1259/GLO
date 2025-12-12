package domaine.meuble;

/**
 * Énumération des types de meubles disponibles pour une salle de bain
 */
public enum TypeMeuble {
    PLACARD("Placard"),
    ARMOIRE("Armoire"),
    TOILETTE("Toilette"),
    DOUCHE("Douche"),
    BAIN("Bain"),
    VANITE("Vanité");

    private final String nom;

    TypeMeuble(String nom) {
        this.nom = nom;
    }

    public String getNom() {
        return nom;
    }

    @Override
    public String toString() {
        return nom;
    }
}
