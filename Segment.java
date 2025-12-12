package domaine.piece.util;

import java.awt.Point;

// Classe utilitaire pour la gestion des segments de ligne
// Dans le paquetage domaine.util ou similaire
/**
 * Représente un segment de ligne défini par deux points.
 * Utilisé pour représenter les murs de la pièce et les côtés des éléments chauffants.
 */
public class Segment {
    public final Point start;
    public final Point end;

    /**
     * Constructeur d'un segment.
     * @param start Le point de départ du segment.
     * @param end Le point de fin du segment.
     */
    public Segment(Point start, Point end) {
        // Crée des copies pour s'assurer que le segment est immuable,
        // même si les objets Point originaux sont modifiés ailleurs.
        this.start = new Point(start);
        this.end = new Point(end);
    }

    // Vous pouvez ajouter ici des méthodes utilitaires géométriques si nécessaire (comme getLongueur(), etc.)

    /**
     * Retourne une représentation textuelle du segment.
     */
    @Override
    public String toString() {
        return String.format("Segment[(%d, %d) à (%d, %d)]", start.x, start.y, end.x, end.y);
    }

//    private static final double EPSILON = 1e-9;
//    private final Point debut;
//    private final Point fin;
//
//    public Segment(int x1, int y1, int x2, int y2) {
//        // Assurez-vous que les points sont triés ou normalisés pour une comparaison facile
//        // Pour les segments horizontaux/verticaux, le tri n'est pas strictement nécessaire
//        this.debut = new Point(x1, y1);
//        this.fin = new Point(x2, y2);
//    }
//
//    // NOTE : La vraie implémentation de 'coincide' pour les segments non-colinéaires
//    // et non-parallèles est complexe. Pour une pièce rectangulaire ou une pièce
//    // irrégulière avec des murs droits (horizontaux ou verticaux), une vérification
//    // simple peut suffire.
//
//    /**
//     * Vérifie si le segment courant est co-linéaire et chevauche ou touche
//     * le segment 'autre' (pour les murs droits).
//     */
//    public boolean coincide(Segment autre) {
//        // 1. Les segments doivent être parallèles (horizontaux ou verticaux)
//        boolean estHorizontal = (this.debut.y == this.fin.y) && (autre.debut.y == autre.fin.y);
//        boolean estVertical = (this.debut.x == this.fin.x) && (autre.debut.x == autre.fin.x);
//
//        if (estHorizontal && this.debut.y == autre.debut.y) {
//            // Segments horizontaux : vérifie le chevauchement sur X
//            int minX1 = Math.min(this.debut.x, this.fin.x);
//            int maxX1 = Math.max(this.debut.x, this.fin.x);
//            int minX2 = Math.min(autre.debut.x, autre.fin.x);
//            int maxX2 = Math.max(autre.debut.x, autre.fin.x);
//
//            // Les deux segments doivent être entièrement égaux (pas seulement un chevauchement)
//            return (minX1 == minX2) && (maxX1 == maxX2);
//
//        } else if (estVertical && this.debut.x == autre.debut.x) {
//            // Segments verticaux : vérifie le chevauchement sur Y
//            int minY1 = Math.min(this.debut.y, this.fin.y);
//            int maxY1 = Math.max(this.debut.y, this.fin.y);
//            int minY2 = Math.min(autre.debut.y, autre.fin.y);
//            int maxY2 = Math.max(autre.debut.y, autre.fin.y);
//
//            // Les deux segments doivent être entièrement égaux
//            return (minY1 == minY2) && (maxY1 == maxY2);
//        }
//
//        return false;
//    }
//
//
///**
// * Vérifie si trois points sont colinéaires.
// * Utilise le produit vectoriel (cross product) qui doit être proche de zéro.
// * @param p1 Point 1
// * @param p2 Point 2
// * @param p3 Point 3
// */
//private static boolean sontColineaires(Point p1, Point p2, Point p3) {
//    // (y2 - y1) * (x3 - x2) - (y3 - y2) * (x2 - x1) = 0
//    // Nous utilisons une petite tolérance (EPSILON) pour les calculs en virgule flottante
//    final double EPSILON = 0.01;
//    double val = (double)(p2.y - p1.y) * (p3.x - p2.x) - (double)(p3.y - p2.y) * (p2.x - p1.x);
//    return Math.abs(val) < EPSILON;
//}
//
///**
// * Vérifie si le segment actuel est un sous-segment du segment 'mur'.
// * Le segment courant est un côté de l'élément; le 'mur' est un mur de la pièce.
// * @param mur Le segment de mur à vérifier.
// * @return true si le segment est collé au mur.
// */
//public boolean estSousSegmentDuMur(Segment mur) {
//
//    // 1. Vérifier la colinéarité des extrémités
//    if (!sontColineaires(mur.debut, mur.fin, this.debut) ||
//            !sontColineaires(mur.debut, mur.fin, this.fin)) {
//        return false; // Les deux segments ne sont pas sur la même ligne
//    }
//
//    // 2. Vérifier si le segment est dans les limites du mur
//    // Pour gérer les obliques, on vérifie si les coordonnées de l'élément
//    // sont comprises entre les coordonnées du mur.
//
//    double minMurX = Math.min(mur.debut.x, mur.fin.x);
//    double maxMurX = Math.max(mur.debut.x, mur.fin.x);
//    double minMurY = Math.min(mur.debut.y, mur.fin.y);
//    double maxMurY = Math.max(mur.debut.y, mur.fin.y);
//
//    double debutElementX = this.debut.x;
//    double finElementX = this.fin.x;
//    double debutElementY = this.debut.y;
//    double finElementY = this.fin.y;
//
//    // Le côté de l'élément doit être contenu dans le mur.
//    // On vérifie que les coordonnées de l'élément sont dans la plage des coordonnées du mur
//
//    boolean xEstDansPlage = (Math.min(debutElementX, finElementX) >= (minMurX - EPSILON)) &&
//            (Math.max(debutElementX, finElementX) <= (maxMurX + EPSILON));
//
//    boolean yEstDansPlage = (Math.min(debutElementY, finElementY) >= minMurY - EPSILON) &&
//            (Math.max(debutElementY, finElementY) <= maxMurY + EPSILON);
//
//    return xEstDansPlage && yEstDansPlage;
//}
}