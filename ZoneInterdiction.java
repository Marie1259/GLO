package domaine.zone;

import java.awt.Point;

/**
 * Zone d'interdiction dans la pièce
 * Le fil chauffant ne peut pas passer dans cette zone
 */
public class ZoneInterdiction extends Zone {

    public ZoneInterdiction(Point position, int largeur, int longueur, String nom) {
        super(position, largeur, longueur, nom);
    }

    public ZoneInterdiction(int x, int y, int largeur, int longueur, String nom) {
        super(x, y, largeur, longueur, nom);
    }

    @Override
    public String getTypeZone() {
        return "Zone Interdiction";
    }
}

