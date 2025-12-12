package domaine.piece;

import domaine.chauffage.ElementChauffant;
import domaine.chauffage.Thermostat;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * Représente un mur dans la pièce.
 * Un mur peut contenir des éléments chauffants et des thermostats.
 */
public class Mur {
    private Point start;
    private Point end;
    private List<ElementChauffant> elementsChauffants;
    private List<Thermostat> thermostats;

    public Mur(Point start, Point end) {
        this.start = start;
        this.end = end;
        this.elementsChauffants = new ArrayList<>();
        this.thermostats = new ArrayList<>();
    }

    public Mur(int x1, int y1, int x2, int y2) {
        this.start = new Point(x1, y1);
        this.end = new Point(x2, y2);
        this.elementsChauffants = new ArrayList<>();
        this.thermostats = new ArrayList<>();
    }

    // === Getters et Setters ===
    public Point getStart() {
        return start;
    }

    public void setStart(Point start) {
        this.start = start;
    }

    public Point getEnd() {
        return end;
    }

    public void setEnd(Point end) {
        this.end = end;
    }

    public List<ElementChauffant> getElementsChauffants() {
        return elementsChauffants;
    }

    public List<Thermostat> getThermostats() {
        return thermostats;
    }

    // === Méthodes de gestion des éléments chauffants ===
    public void addElementChauffant(ElementChauffant element) {
        if (!elementsChauffants.contains(element)) {
            elementsChauffants.add(element);
        }
    }

    public void removeElementChauffant(ElementChauffant element) {
        elementsChauffants.remove(element);
    }

    // === Méthodes de gestion des thermostats ===
    public void addThermostat(Thermostat thermostat) {
        if (!thermostats.contains(thermostat)) {
            thermostats.add(thermostat);
        }
    }

    public void removeThermostat(Thermostat thermostat) {
        thermostats.remove(thermostat);
    }


// ===============================================
//           MÉTHODES GÉOMÉTRIQUES AJOUTÉES
// ===============================================

    /**
     * Calcule l'angle d'inclinaison du mur par rapport à l'axe X positif (horizontale).
     * L'angle est retourné en degrés, dans l'intervalle [0, 360).
     *
     * @return L'angle du mur en degrés.
     */
    public double calculerAngleDegres() {
        // L'ordre est important : fin - début
        double deltaX = end.x - start.x;
        double deltaY = end.y - start.y;

        // Utiliser Math.atan2(y, x) qui gère tous les quadrants
        double angleMurRad = Math.atan2(deltaY, deltaX);
        double angleMurDeg = Math.toDegrees(angleMurRad);

        // Normaliser l'angle dans l'intervalle [0, 360)
        if (angleMurDeg < 0) {
            angleMurDeg += 360.0;
        }

        return angleMurDeg;
    }

    /**
     * Projette un point donné (position souhaitée) sur le mur.
     * Retourne le point sur le mur le plus proche de la position donnée.
     * @param p Le point à projeter.
     * @return Le point sur le mur le plus proche de p.
     */
    public Point projeterPointSurMur(Point p) {
        double x1 = start.x;
        double y1 = start.y;
        double x2 = end.x;
        double y2 = end.y;

        // 1. Calculer le carré de la longueur du mur (l2)
        double l2 = (x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1);
        if (l2 == 0.0) { // Mur de longueur zéro (cas dégénéré)
            return start;
        }

        // 2. Calculer 't', la position relative de la projection sur la ligne infinie
        // t = (Produit scalaire du vecteur P1P et P1P2) / l2
        double t = ((p.x - x1) * (x2 - x1) + (p.y - y1) * (y2 - y1)) / l2;

        // 3. Limiter la projection au SEGMENT du mur [0, 1]
        // Si la projection est en dehors du mur, on retourne l'extrémité la plus proche.
        if (t < 0.0) return start;
        if (t > 1.0) return end;

        // 4. La projection tombe sur le mur (t est entre 0 et 1)
        int projX = (int) Math.round(x1 + t * (x2 - x1));
        int projY = (int) Math.round(y1 + t * (y2 - y1));

        return new Point(projX, projY);
    }
}