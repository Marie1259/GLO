package domaine.piece.util;

import java.awt.Point;
import domaine.piece.Mur;

public class MurProcheInfo {
    private final Mur mur;
    private final Point pointAccrochage;

    /**
     * @param mur Le mur de la pièce le plus proche.
     * @param pointAccrochage Le point sur ce mur le plus proche de la position donnée.
     */
    public MurProcheInfo(Mur mur, Point pointAccrochage) {
        this.mur = mur;
        this.pointAccrochage = pointAccrochage;
    }

    public Mur getMur() { return mur; }
    public Point getPointAccrochage() { return pointAccrochage; }
}