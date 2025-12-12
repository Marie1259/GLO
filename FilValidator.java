package domaine.validation;

import domaine.piece.Piece;
import domaine.meuble.Meuble;
import domaine.meuble.MeubleAvecDrain;
import domaine.meuble.Toilette;
import domaine.chauffage.FilChauffant;

import java.awt.Point;
import java.util.List;

/**
 * Valide les contraintes du fil chauffant
 */
public final class FilValidator {
    private FilValidator() {}
    
    // Constantes de contraintes (en pouces)
    private static final int DISTANCE_MIN_DRAIN = 6;
    private static final int DISTANCE_MIN_DRAIN_TOILETTE = 10;
    private static final int LONGUEUR_MAX_SEGMENT = 120; // 10 pieds = 120 pouces
    private static final int DISTANCE_MIN_MUR = 3;
    private static final int DISTANCE_MIN_MEUBLE = 3;
    private static final int DISTANCE_MIN_FIL = 3;
    
    /**
     * Valide toutes les contraintes du fil chauffant
     * @param piece La pièce
     * @param fil Le fil chauffant
     * @return Liste des messages d'erreur (vide si valide)
     */
    public static List<String> validerFil(Piece piece, FilChauffant fil) {
        List<String> erreurs = new java.util.ArrayList<>();
        
        if (piece == null || fil == null) {
            erreurs.add("Pièce ou fil invalide");
            return erreurs;
        }
        
        List<Point> chemin = fil.getChemin();
        if (chemin == null || chemin.size() < 2) {
            // Pas de chemin, pas d'erreur (le fil n'est peut-être pas encore calculé)
            return erreurs;
        }
        
        // Valider chaque segment
        for (int i = 0; i < chemin.size() - 1; i++) {
            Point p1 = chemin.get(i);
            Point p2 = chemin.get(i + 1);
            
            // Vérifier longueur du segment
            double longueurSegment = distance(p1, p2);
            if (longueurSegment > LONGUEUR_MAX_SEGMENT) {
                erreurs.add(String.format("Segment trop long (%.1f\") entre (%d, %d) et (%d, %d). Maximum: %d\"",
                    longueurSegment, p1.x, p1.y, p2.x, p2.y, LONGUEUR_MAX_SEGMENT));
            }
            
            // Vérifier distance aux murs
            String erreurMur = validerDistanceMur(piece, p1, p2);
            if (erreurMur != null) {
                erreurs.add(erreurMur);
            }
            
            // Vérifier distance aux meubles
            String erreurMeuble = validerDistanceMeuble(piece, p1, p2);
            if (erreurMeuble != null) {
                erreurs.add(erreurMeuble);
            }
            
            // Vérifier distance aux drains
            String erreurDrain = validerDistanceDrain(piece, p1, p2);
            if (erreurDrain != null) {
                erreurs.add(erreurDrain);
            }
            
            // Vérifier zones d'interdiction
            if (piece.intersecteZoneInterdiction(p1.x, p1.y, p2.x, p2.y)) {
                erreurs.add(String.format("Le fil passe dans une zone d'interdiction entre (%d, %d) et (%d, %d)",
                    p1.x, p1.y, p2.x, p2.y));
            }
        }
        
        // Vérifier auto-croisement
        String erreurCroisement = validerAutoCroisement(chemin);
        if (erreurCroisement != null) {
            erreurs.add(erreurCroisement);
        }
        
        // Vérifier distance entre segments
        String erreurDistanceFil = validerDistanceEntreSegments(chemin);
        if (erreurDistanceFil != null) {
            erreurs.add(erreurDistanceFil);
        }
        
        return erreurs;
    }
    
    private static String validerDistanceMur(Piece piece, Point p1, Point p2) {
        // Vérifier que les points sont à au moins 3 pouces des murs
        if (p1.x < DISTANCE_MIN_MUR || p1.y < DISTANCE_MIN_MUR ||
            p1.x > piece.getLargeur() - DISTANCE_MIN_MUR ||
            p1.y > piece.getLongueur() - DISTANCE_MIN_MUR) {
            return String.format("Point (%d, %d) trop proche d'un mur (minimum: %d\")",
                p1.x, p1.y, DISTANCE_MIN_MUR);
        }
        if (p2.x < DISTANCE_MIN_MUR || p2.y < DISTANCE_MIN_MUR ||
            p2.x > piece.getLargeur() - DISTANCE_MIN_MUR ||
            p2.y > piece.getLongueur() - DISTANCE_MIN_MUR) {
            return String.format("Point (%d, %d) trop proche d'un mur (minimum: %d\")",
                p2.x, p2.y, DISTANCE_MIN_MUR);
        }
        return null;
    }
    
    private static String validerDistanceMeuble(Piece piece, Point p1, Point p2) {
        for (Meuble m : piece.getMeubles()) {
            double dist1 = distancePointRectangle(p1, m.getX(), m.getY(), m.getLargeur(), m.getLongueur());
            double dist2 = distancePointRectangle(p2, m.getX(), m.getY(), m.getLargeur(), m.getLongueur());
            
            if (dist1 < DISTANCE_MIN_MEUBLE) {
                return String.format("Point (%d, %d) trop proche du meuble '%s' (minimum: %d\")",
                    p1.x, p1.y, m.getNom(), DISTANCE_MIN_MEUBLE);
            }
            if (dist2 < DISTANCE_MIN_MEUBLE) {
                return String.format("Point (%d, %d) trop proche du meuble '%s' (minimum: %d\")",
                    p2.x, p2.y, m.getNom(), DISTANCE_MIN_MEUBLE);
            }
        }
        return null;
    }
    
    private static String validerDistanceDrain(Piece piece, Point p1, Point p2) {
        for (Meuble m : piece.getMeubles()) {
            if (m instanceof MeubleAvecDrain d) {
                int drainX = d.getDrainX();
                int drainY = d.getDrainY();
                int distanceMin = (m instanceof Toilette) ? DISTANCE_MIN_DRAIN_TOILETTE : DISTANCE_MIN_DRAIN;
                
                double dist1 = distance(p1, new Point(drainX, drainY));
                double dist2 = distance(p2, new Point(drainX, drainY));
                
                if (dist1 < distanceMin) {
                    return String.format("Point (%d, %d) trop proche du drain de '%s' (minimum: %d\")",
                        p1.x, p1.y, m.getNom(), distanceMin);
                }
                if (dist2 < distanceMin) {
                    return String.format("Point (%d, %d) trop proche du drain de '%s' (minimum: %d\")",
                        p2.x, p2.y, m.getNom(), distanceMin);
                }
            }
        }
        return null;
    }
    
    private static String validerAutoCroisement(List<Point> chemin) {
        // Vérifier si le fil se croise lui-même
        for (int i = 0; i < chemin.size() - 1; i++) {
            Point p1 = chemin.get(i);
            Point p2 = chemin.get(i + 1);
            
            for (int j = i + 2; j < chemin.size() - 1; j++) {
                Point p3 = chemin.get(j);
                Point p4 = chemin.get(j + 1);
                
                if (segmentsSeCroisent(p1, p2, p3, p4)) {
                    return String.format("Le fil se croise entre (%d, %d)-(%d, %d) et (%d, %d)-(%d, %d)",
                        p1.x, p1.y, p2.x, p2.y, p3.x, p3.y, p4.x, p4.y);
                }
            }
        }
        return null;
    }
    
    private static String validerDistanceEntreSegments(List<Point> chemin) {
        // Vérifier que les segments sont à au moins 3 pouces les uns des autres
        for (int i = 0; i < chemin.size() - 1; i++) {
            Point p1 = chemin.get(i);
            Point p2 = chemin.get(i + 1);
            
            for (int j = i + 2; j < chemin.size() - 1; j++) {
                Point p3 = chemin.get(j);
                Point p4 = chemin.get(j + 1);
                
                double distMin = distanceMinEntreSegments(p1, p2, p3, p4);
                if (distMin < DISTANCE_MIN_FIL) {
                    return String.format("Segments trop proches (%.1f\") entre (%d, %d)-(%d, %d) et (%d, %d)-(%d, %d). Minimum: %d\"",
                        distMin, p1.x, p1.y, p2.x, p2.y, p3.x, p3.y, p4.x, p4.y, DISTANCE_MIN_FIL);
                }
            }
        }
        return null;
    }
    
    private static double distance(Point p1, Point p2) {
        double dx = p1.x - p2.x;
        double dy = p1.y - p2.y;
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    private static double distancePointRectangle(Point p, int rx, int ry, int rw, int rh) {
        int closestX = Math.max(rx, Math.min(p.x, rx + rw));
        int closestY = Math.max(ry, Math.min(p.y, ry + rh));
        return distance(p, new Point(closestX, closestY));
    }
    
    private static boolean segmentsSeCroisent(Point p1, Point p2, Point p3, Point p4) {
        // Algorithme pour vérifier si deux segments se croisent
        int o1 = orientation(p1, p2, p3);
        int o2 = orientation(p1, p2, p4);
        int o3 = orientation(p3, p4, p1);
        int o4 = orientation(p3, p4, p2);
        
        if (o1 != o2 && o3 != o4) {
            return true;
        }
        
        return false;
    }
    
    private static int orientation(Point p, Point q, Point r) {
        int val = (q.y - p.y) * (r.x - q.x) - (q.x - p.x) * (r.y - q.y);
        if (val == 0) return 0; // Colinéaire
        return (val > 0) ? 1 : 2; // Sens horaire ou anti-horaire
    }
    
    private static double distanceMinEntreSegments(Point p1, Point p2, Point p3, Point p4) {
        // Distance minimale entre deux segments
        double minDist = Double.MAX_VALUE;
        
        // Distance entre extrémités
        minDist = Math.min(minDist, distance(p1, p3));
        minDist = Math.min(minDist, distance(p1, p4));
        minDist = Math.min(minDist, distance(p2, p3));
        minDist = Math.min(minDist, distance(p2, p4));
        
        // Distance entre points et segments (approximation)
        minDist = Math.min(minDist, distancePointSegment(p1, p3, p4));
        minDist = Math.min(minDist, distancePointSegment(p2, p3, p4));
        minDist = Math.min(minDist, distancePointSegment(p3, p1, p2));
        minDist = Math.min(minDist, distancePointSegment(p4, p1, p2));
        
        return minDist;
    }
    
    private static double distancePointSegment(Point p, Point s1, Point s2) {
        double A = p.x - s1.x;
        double B = p.y - s1.y;
        double C = s2.x - s1.x;
        double D = s2.y - s1.y;
        
        double dot = A * C + B * D;
        double lenSq = C * C + D * D;
        double param = (lenSq != 0) ? dot / lenSq : -1;
        
        double xx, yy;
        if (param < 0) {
            xx = s1.x;
            yy = s1.y;
        } else if (param > 1) {
            xx = s2.x;
            yy = s2.y;
        } else {
            xx = s1.x + param * C;
            yy = s1.y + param * D;
        }
        
        double dx = p.x - xx;
        double dy = p.y - yy;
        return Math.sqrt(dx * dx + dy * dy);
    }
}

