package domaine.meuble;

public class MeubleAvecDrain extends Meuble {

    private int drainXRelatif;          // coordonnées relatives au coin inférieur gauche du meuble
    private int drainYRelatif;
    private int diametreDrain = 4;   // ✅ diamètre par défaut
    private boolean drainModifieManuellement = false;

    public MeubleAvecDrain(String nom, int x, int y, int largeur, int longueur) {
        super(nom, x, y, largeur, longueur);
        recentrerDrain(); // ✅ par défaut au centre
    }

    // ---------- GETTERS / SETTERS DRAIN ----------

    @Override
    public boolean aDrain() {
        return true;
    }

    /**
     * Retourne la position X absolue du drain dans la pièce
     */
    public int getDrainX() {
        return x + drainXRelatif;
    }

    /**
     * Retourne la position Y absolue du drain dans la pièce
     */
    public int getDrainY() {
        return y + drainYRelatif;
    }

    /**
     * Retourne la position X relative du drain par rapport au coin inférieur gauche du meuble
     */
    public int getDrainXRelatif() {
        return drainXRelatif;
    }

    /**
     * Retourne la position Y relative du drain par rapport au coin inférieur gauche du meuble
     */
    public int getDrainYRelatif() {
        return drainYRelatif;
    }

    @Override
    public int getDiametreDrain() {
        return diametreDrain;
    }

    @Override
    public void setDiametreDrain(int diametre) {
        if (diametre <= 0) {
            diametre = 4; // fallback safe
        }
        this.diametreDrain = diametre;
        // on s'assure que le drain reste dans le meuble
        setDrainPositionRelative(drainXRelatif, drainYRelatif);
    }

    /**
     * Positionne le drain en coordonnées absolues dans la pièce.
     * Convertit automatiquement en coordonnées relatives.
     */
    public void setDrainPosition(int nouveauXAbsolu, int nouveauYAbsolu) {
        int nouveauXRelatif = nouveauXAbsolu - x;
        int nouveauYRelatif = nouveauYAbsolu - y;
        setDrainPositionRelative(nouveauXRelatif, nouveauYRelatif);
    }

    /**
     * Positionne le drain en coordonnées relatives au coin inférieur gauche du meuble.
     * Permet les valeurs négatives et supérieures aux dimensions pour X et Y (drain à l'extérieur du meuble).
     */
    public void setDrainPositionRelative(int nouveauXRelatif, int nouveauYRelatif) {
        // Permettre toutes les valeurs (négatives, positives, supérieures aux dimensions)
        // Le drain peut être positionné n'importe où, y compris à l'extérieur du meuble
        this.drainXRelatif = nouveauXRelatif;
        this.drainYRelatif = nouveauYRelatif;

        this.drainModifieManuellement = true;
    }

    // ---------- HOOKS DE Meuble ----------

    @Override
    protected void onPositionChanged() {
        // La position relative reste la même, donc la position absolue
        // est automatiquement mise à jour via getDrainX() et getDrainY()
        // Pas besoin de faire quoi que ce soit
    }

    @Override
    protected void onDimensionChanged() {
        if (!drainModifieManuellement) {
            recentrerDrain();
        } else {
            // Redimensionnement proportionnel : on ajuste la position relative
            // pour maintenir la proportion dans le meuble
            // On garde la position relative actuelle mais on s'assure qu'elle reste valide
            setDrainPositionRelative(drainXRelatif, drainYRelatif);
        }
    }

    // ---------- LOGIQUE INTERNE ----------

    private void recentrerDrain() {
        this.drainXRelatif = largeur / 2;
        this.drainYRelatif = longueur / 2;
        // on NE met PAS drainModifieManuellement à true ici :
        // tant que c'est auto, on laisse à false
    }

    @Override
    public String toString() {
        return getType() + " (" + largeur + "x" + longueur + ") "
                + "drain relatif=(" + drainXRelatif + "," + drainYRelatif + "), "
                + "drain absolu=(" + getDrainX() + "," + getDrainY() + "), Ø=" + diametreDrain;
    }
}
