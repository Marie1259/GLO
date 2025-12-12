package domaine.chauffage;

import domaine.graphe.Graphe;
import domaine.piece.Piece;
import domaine.meuble.Meuble;
import domaine.meuble.MeubleAvecDrain;

import java.awt.Point;
import java.util.*;

/**
 * Calcule le chemin du fil chauffant en utilisant l'algorithme de Dijkstra
 * pour maximiser la couverture de surface en partant du thermostat
 */
public class CalculateurCheminDijkstra {
    
    // Constantes de contraintes (en pouces)
    private static final int DISTANCE_MIN_MUR = 3;
    private static final int DISTANCE_MIN_MEUBLE = 3;
    private static final int DISTANCE_MIN_DRAIN = 6;
    private static final int DISTANCE_MIN_DRAIN_TOILETTE = 10;
    
    /**
     * Recalcule le chemin à partir d'une intersection en utilisant Dijkstra
     * @param piece La pièce
     * @param graphe Le graphe des intersections
     * @param intersectionDepart L'intersection de départ
     * @param cheminPartiel Le chemin déjà parcouru (jusqu'à l'intersection de départ)
     * @param longueurSouhaitee La longueur totale souhaitée
     * @param distanceEntreFils La distance entre les fils (utilisée comme distance minimale aux murs)
     * @return Le nouveau chemin complet
     */
    public static List<Point> recalculerCheminAvecDijkstra(
            Piece piece, Graphe graphe, Graphe.Intersection intersectionDepart,
            List<Point> cheminPartiel, int longueurSouhaitee, int distanceEntreFils) {
        
        if (piece == null || graphe == null || intersectionDepart == null) {
            return null;
        }
        
        if (!graphe.estGenere()) {
            graphe.genererGraphe();
        }
        
        // La distance minimale aux murs doit être au moins égale à la distance entre les fils
        int distanceMinMur = Math.max(DISTANCE_MIN_MUR, distanceEntreFils);
        
        // Filtrer les intersections valides
        List<Graphe.Intersection> intersectionsValides = filtrerIntersectionsValides(graphe, piece, distanceMinMur);
        
        if (intersectionsValides.isEmpty()) {
            return cheminPartiel;
        }
        
        // Créer une copie du chemin partiel
        List<Point> nouveauChemin = new ArrayList<>(cheminPartiel);
        Set<Graphe.Intersection> dejaVisites = new HashSet<>();
        
        // Marquer les intersections déjà visitées dans le chemin partiel
        for (Point p : cheminPartiel) {
            Graphe.Intersection inter = trouverIntersectionProche(p, intersectionsValides);
            if (inter != null) {
                dejaVisites.add(inter);
            }
        }
        
        // Calculer la longueur déjà parcourue
        double longueurParcourue = calculerLongueurChemin(cheminPartiel);
        double longueurRestante = longueurSouhaitee - longueurParcourue;
        
        // Utiliser un algorithme amélioré qui utilise uniquement les connexions du graphe
        // Cela assure que le fil passe toujours par les intersections et prend uniquement des directions de 90°
        Graphe.Intersection courant = intersectionDepart;
        double longueurActuelle = 0;
        int iterationsMax = 5000; // Limite de sécurité
        int iterations = 0;
        
        while (longueurActuelle < longueurRestante * 0.95 && courant != null && iterations < iterationsMax) {
            iterations++;
            
            // Trouver la prochaine intersection en utilisant uniquement les connexions du graphe
            Graphe.Intersection suivant = trouverMeilleureIntersectionDepuisConnexions(
                graphe, piece, courant, dejaVisites, longueurRestante - longueurActuelle, distanceMinMur);
            
            if (suivant == null) {
                // Plus d'intersections disponibles
                break;
            }
            
            double distance = distance(courant, suivant);
            
            // Vérifier que l'ajout de ce segment ne dépasse pas la longueur restante
            if (longueurActuelle + distance > longueurRestante * 1.05) {
                // On dépasse trop, arrêter
                break;
            }
            
            // Vérifier que le nouveau segment ne croise pas les segments existants
            Point nouveauPoint = new Point(suivant.getX(), suivant.getY());
            if (cheminSeCroise(nouveauChemin, nouveauPoint)) {
                // Le segment se croise, essayer une autre intersection
                dejaVisites.add(suivant);
                continue;
            }
            
            longueurActuelle += distance;
            nouveauChemin.add(nouveauPoint);
            dejaVisites.add(suivant);
            courant = suivant;
        }
        
        return nouveauChemin;
    }
    
    /**
     * Trouve la meilleure intersection en utilisant uniquement les connexions du graphe
     * Cela assure que le fil passe toujours par les intersections et prend des directions de 45°, 90° ou 135°
     */
    private static Graphe.Intersection trouverMeilleureIntersectionDepuisConnexions(
            Graphe graphe, Piece piece, Graphe.Intersection depart,
            Set<Graphe.Intersection> dejaVisites, double longueurRestante, int distanceMinMur) {
        
        Graphe.Intersection meilleur = null;
        double meilleurScore = Double.NEGATIVE_INFINITY;
        
        // Utiliser uniquement les connexions du graphe (assure uniquement les angles de 90°)
        for (Graphe.Intersection candidat : depart.getConnexions()) {
            if (dejaVisites.contains(candidat)) {
                continue; // Ne pas revisiter
            }
            
            // Vérifier que l'intersection est valide (respecte les contraintes)
            if (!estIntersectionValide(candidat, piece, distanceMinMur)) {
                continue;
            }
            
            double dist = distance(depart, candidat);
            
            // Ignorer les intersections trop éloignées
            if (dist > longueurRestante * 1.1) {
                continue;
            }
            
            // Calculer un score basé sur plusieurs critères
            double score = calculerScoreIntersection(candidat, piece, dist, longueurRestante);
            
            // Préférer les intersections qui utilisent bien la longueur restante
            if (dist < longueurRestante * 0.9) {
                score += 10;
            }
            
            // Bonus pour les intersections qui explorent de nouvelles zones
            score += 5;
            
            if (score > meilleurScore) {
                meilleurScore = score;
                meilleur = candidat;
            }
        }
        
        return meilleur;
    }
    
    /**
     * Vérifie si le nouveau point créerait un croisement avec le chemin existant
     */
    private static boolean cheminSeCroise(List<Point> chemin, Point nouveauPoint) {
        if (chemin.size() < 2) {
            return false;
        }
        
        Point dernierPoint = chemin.get(chemin.size() - 1);
        
        // Vérifier si le nouveau segment croise les segments existants
        for (int i = 0; i < chemin.size() - 1; i++) {
            Point p1 = chemin.get(i);
            Point p2 = chemin.get(i + 1);
            
            // Ignorer le segment précédent (celui qui se termine au dernier point)
            if (i == chemin.size() - 2 && p2.equals(dernierPoint)) {
                continue;
            }
            
            if (segmentsIntersect(dernierPoint, nouveauPoint, p1, p2)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Vérifie si deux segments se croisent
     */
    private static boolean segmentsIntersect(Point p1, Point q1, Point p2, Point q2) {
        // Vérifier si les segments se croisent (sauf aux extrémités)
        int o1 = orientation(p1, q1, p2);
        int o2 = orientation(p1, q1, q2);
        int o3 = orientation(p2, q2, p1);
        int o4 = orientation(p2, q2, q1);
        
        // Cas général : les segments se croisent
        if (o1 != 0 && o2 != 0 && o3 != 0 && o4 != 0 && o1 != o2 && o3 != o4) {
            return true;
        }
        
        // Cas colinéaires : vérifier si un point est sur le segment
        if (o1 == 0 && onSegment(p1, p2, q1)) return true;
        if (o2 == 0 && onSegment(p1, q2, q1)) return true;
        if (o3 == 0 && onSegment(p2, p1, q2)) return true;
        if (o4 == 0 && onSegment(p2, q1, q2)) return true;
        
        return false;
    }
    
    /**
     * Calcule l'orientation de trois points
     */
    private static int orientation(Point p, Point q, Point r) {
        long val = (long)(q.y - p.y) * (r.x - q.x) - (long)(q.x - p.x) * (r.y - q.y);
        if (val == 0) return 0;  // Colinéaire
        return (val > 0) ? 1 : 2; // Horaire ou anti-horaire
    }
    
    /**
     * Vérifie si un point est sur un segment
     */
    private static boolean onSegment(Point p, Point q, Point r) {
        return q.x <= Math.max(p.x, r.x) && q.x >= Math.min(p.x, r.x) &&
               q.y <= Math.max(p.y, r.y) && q.y >= Math.min(p.y, r.y);
    }
    
    /**
     * Calcule un score pour une intersection basé sur la couverture de surface
     */
    private static double calculerScoreIntersection(
            Graphe.Intersection inter, Piece piece, double distanceParcourue, double longueurRestante) {
        
        double score = 0;
        
        // Préférer les intersections qui maximisent la couverture
        // Plus l'intersection est éloignée du centre, meilleur est le score
        int centreX = piece.getLargeur() / 2;
        int centreY = piece.getLongueur() / 2;
        double distanceAuCentre = Math.sqrt(
            Math.pow(inter.getX() - centreX, 2) + Math.pow(inter.getY() - centreY, 2));
        
        score += distanceAuCentre * 0.1; // Préférer les intersections périphériques
        
        // Préférer les intersections qui utilisent bien la longueur restante
        if (distanceParcourue < longueurRestante * 0.9) {
            score += 10; // Bonne utilisation de la longueur
        }
        
        // Bonus pour les intersections qui explorent de nouvelles zones
        score += 5;
        
        // Pénaliser les intersections trop proches des murs
        int marge = 10;
        if (inter.getX() < marge || inter.getX() > piece.getLargeur() - marge ||
            inter.getY() < marge || inter.getY() > piece.getLongueur() - marge) {
            score -= 5;
        }
        
        return score;
    }
    
    /**
     * Filtre les intersections valides
     */
    private static List<Graphe.Intersection> filtrerIntersectionsValides(Graphe graphe, Piece piece, int distanceMinMur) {
        List<Graphe.Intersection> valides = new ArrayList<>();
        
        for (Graphe.Intersection inter : graphe.getIntersections()) {
            if (estIntersectionValide(inter, piece, distanceMinMur)) {
                valides.add(inter);
            }
        }
        
        return valides;
    }
    
    /**
     * Vérifie si une intersection est valide
     */
    private static boolean estIntersectionValide(Graphe.Intersection inter, Piece piece, int distanceMinMur) {
        int x = inter.getX();
        int y = inter.getY();
        
        // Vérifier si la pièce est irrégulière (>= 3 points, y compris 4 points pour rectangles)
        List<Point> points = piece.getPoints();
        boolean estIrreguliere = points != null && points.size() >= 3;
        
        if (estIrreguliere) {
            // Pour une pièce irrégulière, vérifier que l'intersection est dans le polygone
            if (!piece.contientPoint(x, y)) {
                return false;
            }
        } else {
            // Pour une pièce rectangulaire, vérifier distance aux murs
            if (x < distanceMinMur || y < distanceMinMur || 
                x > piece.getLargeur() - distanceMinMur || 
                y > piece.getLongueur() - distanceMinMur) {
                return false;
            }
        }
        
        // Vérifier distance aux meubles
        for (Meuble m : piece.getMeubles()) {
            int distX = Math.max(0, Math.max(m.getX() - x, x - (m.getX() + m.getLargeur())));
            int distY = Math.max(0, Math.max(m.getY() - y, y - (m.getY() + m.getLongueur())));
            double distance = Math.sqrt(distX * distX + distY * distY);
            if (distance < DISTANCE_MIN_MEUBLE) {
                return false;
            }
        }
        
        // Vérifier distance aux drains
        for (Meuble m : piece.getMeubles()) {
            if (m instanceof MeubleAvecDrain d) {
                int drainX = d.getDrainX();
                int drainY = d.getDrainY();
                double distance = Math.sqrt((x - drainX) * (x - drainX) + (y - drainY) * (y - drainY));
                int distanceMinDrain = (m instanceof domaine.meuble.Toilette) ? 
                    DISTANCE_MIN_DRAIN_TOILETTE : DISTANCE_MIN_DRAIN;
                if (distance < distanceMinDrain) {
                    return false;
                }
            }
        }
        
        // Vérifier zones d'interdiction
        if (piece.estDansZoneInterdiction(x, y)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Trouve l'intersection la plus proche d'un point
     */
    private static Graphe.Intersection trouverIntersectionProche(
            Point point, List<Graphe.Intersection> intersections) {
        if (intersections.isEmpty()) {
            return null;
        }
        
        Graphe.Intersection plusProche = intersections.get(0);
        double distanceMin = distance(point, plusProche);
        
        for (Graphe.Intersection inter : intersections) {
            double dist = distance(point, inter);
            if (dist < distanceMin) {
                distanceMin = dist;
                plusProche = inter;
            }
        }
        
        return plusProche;
    }
    
    /**
     * Calcule la distance entre un point et une intersection
     */
    private static double distance(Point p, Graphe.Intersection inter) {
        double dx = p.x - inter.getX();
        double dy = p.y - inter.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    /**
     * Calcule la distance entre deux intersections
     */
    private static double distance(Graphe.Intersection i1, Graphe.Intersection i2) {
        double dx = i1.getX() - i2.getX();
        double dy = i1.getY() - i2.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    /**
     * Calcule la longueur totale d'un chemin
     */
    private static double calculerLongueurChemin(List<Point> chemin) {
        if (chemin == null || chemin.size() < 2) {
            return 0;
        }
        
        double longueur = 0;
        for (int i = 0; i < chemin.size() - 1; i++) {
            Point p1 = chemin.get(i);
            Point p2 = chemin.get(i + 1);
            longueur += Math.sqrt(
                Math.pow(p2.x - p1.x, 2) + Math.pow(p2.y - p1.y, 2));
        }
        
        return longueur;
    }
}

