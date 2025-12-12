package domaine.chauffage;

import domaine.graphe.Graphe;
import domaine.piece.Piece;
import domaine.meuble.Meuble;
import domaine.meuble.MeubleAvecDrain;
import domaine.chauffage.Thermostat;
import domaine.zone.ZoneInterdiction;

import java.awt.Point;
import java.util.*;

/**
 * Calcule le chemin du fil chauffant en utilisant un algorithme de recherche de chemin
 * Implémente un algorithme serpentine pour simplifier le chemin
 */
public class CalculateurCheminFil {
    
    // Constantes de contraintes (en pouces)
    private static final int DISTANCE_MIN_MUR = 3;
    private static final int DISTANCE_MIN_MEUBLE = 3;
    private static final int DISTANCE_MIN_DRAIN = 6;
    private static final int DISTANCE_MIN_DRAIN_TOILETTE = 10;
    private static final int DISTANCE_MIN_FIL = 3;
    private static final int LONGUEUR_MAX_SEGMENT = 120; // 10 pieds = 120 pouces
    
    /**
     * Calcule le chemin du fil chauffant en utilisant le graphe de la pièce
     * @param piece La pièce contenant le graphe
     * @param fil Le fil chauffant à configurer
     * @param distanceEntreFils La distance souhaitée entre les fils (en pouces)
     * @param longueurSouhaitee La longueur souhaitée du fil (en pouces)
     * @return Le chemin calculé (liste de points), ou null si aucun chemin valide n'est trouvé
     */
    public static List<Point> calculerChemin(Piece piece, FilChauffant fil, 
                                             int distanceEntreFils, int longueurSouhaitee) {
        if (piece == null || fil == null) {
            return null;
        }
        
        Graphe graphe = piece.getGraphe();
        if (graphe == null) {
            return null;
        }
        if (!graphe.estGenere()) {
            graphe.genererGraphe();
        }
        
        // La distance minimale aux murs doit être au moins égale à la distance entre les fils
        int distanceMinMur = Math.max(DISTANCE_MIN_MUR, distanceEntreFils);
        
        // Trouver le point de départ (thermostat)
        Point pointDepart = trouverPointDepart(piece);
        if (pointDepart == null) {
            // Pas de thermostat, utiliser le coin inférieur gauche
            pointDepart = new Point(distanceMinMur, distanceMinMur);
        }
        
        // Filtrer les intersections valides (respectant les contraintes)
        List<Graphe.Intersection> intersectionsValides = filtrerIntersectionsValides(
            graphe, piece, distanceMinMur);
        
        if (intersectionsValides.isEmpty()) {
            return null;
        }
        
        // Trouver l'intersection la plus proche du point de départ
        Graphe.Intersection intersectionDepart = trouverIntersectionPlusProche(
            pointDepart, intersectionsValides);
        
        if (intersectionDepart == null) {
            return null;
        }
        
        // Calculer le chemin en serpentine
        List<Point> chemin = calculerCheminSerpentine(
            graphe, piece, intersectionDepart, distanceEntreFils, longueurSouhaitee, distanceMinMur);
        
        return chemin;
    }
    
    /**
     * Trouve le point de départ (thermostat)
     */
    private static Point trouverPointDepart(Piece piece) {
        for (domaine.chauffage.ElementChauffant element : piece.getElementsChauffants()) {
            if (element instanceof Thermostat) {
                return new Point(element.getX(), element.getY());
            }
        }
        return null;
    }
    
    /**
     * Filtre les intersections valides (respectant les contraintes)
     */
    private static List<Graphe.Intersection> filtrerIntersectionsValides(
            Graphe graphe, Piece piece, int distanceMin) {
        List<Graphe.Intersection> valides = new ArrayList<>();
        
        for (Graphe.Intersection inter : graphe.getIntersections()) {
            if (estIntersectionValide(inter, piece, distanceMin)) {
                valides.add(inter);
            }
        }
        
        return valides;
    }
    
    /**
     * Vérifie si une intersection est valide (respecte les contraintes)
     */
    private static boolean estIntersectionValide(Graphe.Intersection inter, 
                                                 Piece piece, int distanceMin) {
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
            // Vérifier aussi la distance minimale aux murs (bords du polygone)
            // On vérifie la distance au contour
            if (!estPointAssezLoinDuContour(piece, x, y, distanceMin)) {
                return false;
            }
        } else {
            // Pour une pièce rectangulaire, vérifier distance aux murs
            if (x < distanceMin || y < distanceMin || 
                x > piece.getLargeur() - distanceMin || 
                y > piece.getLongueur() - distanceMin) {
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
     * Vérifie si un point est assez loin du contour d'une pièce irrégulière
     */
    private static boolean estPointAssezLoinDuContour(Piece piece, int x, int y, int distanceMin) {
        List<Point> points = piece.getPoints();
        if (points == null || points.size() < 3) {
            return true;
        }
        
        // Vérifier la distance minimale à chaque segment du contour
        for (int i = 0; i < points.size(); i++) {
            Point p1 = points.get(i);
            Point p2 = points.get((i + 1) % points.size());
            
            double distance = distancePointSegment(new Point(x, y), p1, p2);
            if (distance < distanceMin) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Calcule la distance d'un point à un segment
     */
    private static double distancePointSegment(Point point, Point segmentStart, Point segmentEnd) {
        int dx = segmentEnd.x - segmentStart.x;
        int dy = segmentEnd.y - segmentStart.y;
        
        if (dx == 0 && dy == 0) {
            return distance(point, segmentStart);
        }
        
        int px = point.x - segmentStart.x;
        int py = point.y - segmentStart.y;
        
        double t = (double)(px * dx + py * dy) / (dx * dx + dy * dy);
        t = Math.max(0, Math.min(1, t));
        
        int projX = segmentStart.x + (int)(t * dx);
        int projY = segmentStart.y + (int)(t * dy);
        
        return distance(point, new Point(projX, projY));
    }
    
    /**
     * Calcule la distance entre deux points
     */
    private static double distance(Point p1, Point p2) {
        double dx = p1.x - p2.x;
        double dy = p1.y - p2.y;
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    /**
     * Trouve l'intersection la plus proche d'un point
     */
    private static Graphe.Intersection trouverIntersectionPlusProche(
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
     * Calcule le chemin en serpentine (zigzag)
     * Le fil passe toujours par les intersections et utilise uniquement les connexions du graphe
     * Crée un motif serpentine où les lignes parallèles sont espacées de distanceEntreFils
     */
    private static List<Point> calculerCheminSerpentine(
            Graphe graphe, Piece piece, Graphe.Intersection depart,
            int distanceEntreFils, int longueurSouhaitee, int distanceMinMur) {
        
        List<Point> chemin = new ArrayList<>();
        Set<Graphe.Intersection> visites = new HashSet<>();
        Graphe.Intersection courant = depart;
        
        chemin.add(new Point(courant.getX(), courant.getY()));
        visites.add(courant);
        
        double longueurActuelle = 0;
        int iterationsMax = 5000; // Limite de sécurité
        int iterations = 0;
        
        // Direction actuelle : true = horizontal (aller-retour), false = vertical
        boolean directionHorizontale = true;
        boolean aller = true; // true = vers la droite/haut, false = vers la gauche/bas
        
        // Calculer l'espacement en nombre d'intersections
        int espacementGraphe = graphe.getEspacement();
        int espacementIntersections = Math.max(1, distanceEntreFils / espacementGraphe);
        
        // Coordonnées de référence pour le motif serpentine
        int ligneY = depart.getY(); // Pour les lignes horizontales
        int ligneX = depart.getX(); // Pour les lignes verticales
        
        while (longueurActuelle < longueurSouhaitee * 0.95 && iterations < iterationsMax) {
            iterations++;
            
            // Utiliser uniquement les connexions du graphe avec un motif serpentine
            Graphe.Intersection suivant = trouverIntersectionSerpentine(
                graphe, piece, courant, visites, longueurSouhaitee - longueurActuelle,
                directionHorizontale, aller, espacementIntersections, ligneX, ligneY, distanceMinMur);
            
            if (suivant == null) {
                // On a atteint un bord, changer de direction perpendiculairement
                boolean auBord = detecterChangementDirection(
                    courant, piece, directionHorizontale, aller);
                
                if (auBord) {
                    // Se déplacer perpendiculairement de distanceEntreFils
                    Graphe.Intersection suivantPerpendiculaire = trouverIntersectionPerpendiculaire(
                        graphe, piece, courant, visites, directionHorizontale, 
                        espacementIntersections, ligneX, ligneY, distanceMinMur);
                    
                    if (suivantPerpendiculaire != null) {
                        suivant = suivantPerpendiculaire;
                        // Inverser la direction pour le retour
                        aller = !aller;
                        // Mettre à jour la ligne de référence
                        if (directionHorizontale) {
                            ligneY = suivant.getY();
                        } else {
                            ligneX = suivant.getX();
                        }
                    } else {
                        // Essayer toutes les directions disponibles
                        suivant = trouverIntersectionSuivanteDepuisConnexions(
                            graphe, piece, courant, visites, longueurSouhaitee - longueurActuelle, distanceMinMur);
                        
                        if (suivant == null) {
                            // Plus d'intersections disponibles
                            break;
                        }
                    }
                } else {
                    // Essayer toutes les directions disponibles
                    suivant = trouverIntersectionSuivanteDepuisConnexions(
                        graphe, piece, courant, visites, longueurSouhaitee - longueurActuelle, distanceMinMur);
                    
                    if (suivant == null) {
                        // Plus d'intersections disponibles
                        break;
                    }
                }
            }
            
            double distanceSegment = distance(courant, suivant);
            
            // Vérifier que l'ajout de ce segment ne dépasse pas trop la longueur souhaitée
            if (longueurActuelle + distanceSegment > longueurSouhaitee * 1.05) {
                // On dépasse trop, arrêter
                break;
            }
            
            // Vérifier que le segment ne dépasse pas la longueur maximale
            if (distanceSegment > LONGUEUR_MAX_SEGMENT) {
                // Segment trop long, essayer une autre intersection
                visites.add(suivant); // Marquer comme visitée pour ne pas la réessayer
                continue;
            }
            
            // Vérifier que le nouveau segment ne croise pas les segments existants
            Point nouveauPoint = new Point(suivant.getX(), suivant.getY());
            if (cheminSeCroise(chemin, nouveauPoint)) {
                // Le segment se croise, essayer une autre intersection
                visites.add(suivant);
                continue;
            }
            
            longueurActuelle += distanceSegment;
            chemin.add(nouveauPoint);
            visites.add(suivant);
            courant = suivant;
        }
        
        return chemin;
    }
    
    /**
     * Trouve l'intersection suivante en suivant un motif serpentine
     * Privilégie les mouvements qui créent des lignes parallèles espacées de distanceEntreFils
     */
    private static Graphe.Intersection trouverIntersectionSerpentine(
            Graphe graphe, Piece piece, Graphe.Intersection courant,
            Set<Graphe.Intersection> visites, double longueurRestante,
            boolean directionHorizontale, boolean aller, int espacementIntersections,
            int ligneX, int ligneY, int distanceMinMur) {
        
        List<Graphe.Intersection> candidats = new ArrayList<>();
        
        // Utiliser uniquement les connexions du graphe (90° uniquement)
        for (Graphe.Intersection connexion : courant.getConnexions()) {
            if (visites.contains(connexion)) {
                continue; // Ne pas revisiter
            }
            
            // Vérifier que l'intersection est valide
            if (!estIntersectionValide(connexion, piece, distanceMinMur)) {
                continue;
            }
            
            // Vérifier la direction et que l'intersection est sur la même ligne
            int dx = connexion.getX() - courant.getX();
            int dy = connexion.getY() - courant.getY();
            
            boolean correspondDirection = false;
            if (directionHorizontale) {
                // On cherche un mouvement horizontal sur la même ligne Y (exactement)
                if (dx != 0 && dy == 0 && connexion.getY() == ligneY) {
                    correspondDirection = (aller && dx > 0) || (!aller && dx < 0);
                }
            } else {
                // On cherche un mouvement vertical sur la même ligne X (exactement)
                if (dx == 0 && dy != 0 && connexion.getX() == ligneX) {
                    correspondDirection = (aller && dy > 0) || (!aller && dy < 0);
                }
            }
            
            if (correspondDirection) {
                double dist = distance(courant, connexion);
                if (dist <= longueurRestante * 1.1) {
                    candidats.add(connexion);
                }
            }
        }
        
        if (candidats.isEmpty()) {
            return null;
        }
        
        // Choisir la connexion la plus proche dans la bonne direction
        Graphe.Intersection meilleur = null;
        double distanceMin = Double.MAX_VALUE;
        
        for (Graphe.Intersection candidat : candidats) {
            double dist = distance(courant, candidat);
            if (dist < distanceMin) {
                distanceMin = dist;
                meilleur = candidat;
            }
        }
        
        return meilleur;
    }
    
    /**
     * Trouve l'intersection perpendiculaire pour changer de ligne (serpentine)
     * Se déplace perpendiculairement de espacementIntersections intersections
     */
    private static Graphe.Intersection trouverIntersectionPerpendiculaire(
            Graphe graphe, Piece piece, Graphe.Intersection courant,
            Set<Graphe.Intersection> visites, boolean directionHorizontale,
            int espacementIntersections, int ligneX, int ligneY, int distanceMinMur) {
        
        // Parcourir espacementIntersections intersections dans la direction perpendiculaire
        // en utilisant un BFS pour trouver l'intersection à la bonne distance
        // Si certaines intersections sont invalides, trouver la plus proche de la distance souhaitée
        Queue<Graphe.Intersection> queue = new LinkedList<>();
        Map<Graphe.Intersection, Integer> distances = new HashMap<>();
        queue.offer(courant);
        distances.put(courant, 0);
        
        Graphe.Intersection cible = null;
        Graphe.Intersection meilleureCible = null;
        int meilleureDistance = Integer.MAX_VALUE;
        
        while (!queue.isEmpty()) {
            Graphe.Intersection actuel = queue.poll();
            int dist = distances.get(actuel);
            
            // Vérifier si l'intersection est valide et perpendiculaire
            boolean estValide = !visites.contains(actuel) && estIntersectionValide(actuel, piece, distanceMinMur);
            boolean estPerpendiculaire = false;
            if (directionHorizontale) {
                // On cherche une ligne Y différente (mouvement vertical)
                if (actuel.getY() != ligneY) {
                    estPerpendiculaire = true;
                }
            } else {
                // On cherche une ligne X différente (mouvement horizontal)
                if (actuel.getX() != ligneX) {
                    estPerpendiculaire = true;
                }
            }
            
            if (estValide && estPerpendiculaire) {
                // Si on a atteint exactement le nombre d'intersections souhaité
                if (dist == espacementIntersections) {
                    cible = actuel;
                    break;
                }
                
                // Sinon, garder la meilleure approximation (la plus proche de la distance souhaitée)
                int ecart = Math.abs(dist - espacementIntersections);
                if (ecart < meilleureDistance) {
                    meilleureDistance = ecart;
                    meilleureCible = actuel;
                }
            }
            
            // Continuer à explorer si on n'a pas encore dépassé trop la distance souhaitée
            // (explorer jusqu'à espacementIntersections + 2 pour avoir une marge)
            if (dist < espacementIntersections + 2) {
                for (Graphe.Intersection connexion : actuel.getConnexions()) {
                    if (!distances.containsKey(connexion)) {
                        int dx = connexion.getX() - actuel.getX();
                        int dy = connexion.getY() - actuel.getY();
                        
                        // Vérifier que c'est un mouvement perpendiculaire
                        boolean estMouvementPerpendiculaire = false;
                        if (directionHorizontale) {
                            // Mouvement vertical uniquement (vers le haut ou le bas)
                            if (dx == 0 && dy != 0) {
                                estMouvementPerpendiculaire = true;
                            }
                        } else {
                            // Mouvement horizontal uniquement (vers la droite ou la gauche)
                            if (dx != 0 && dy == 0) {
                                estMouvementPerpendiculaire = true;
                            }
                        }
                        
                        if (estMouvementPerpendiculaire) {
                            distances.put(connexion, dist + 1);
                            queue.offer(connexion);
                        }
                    }
                }
            }
        }
        
        // Retourner l'intersection exacte si trouvée, sinon la meilleure approximation
        return cible != null ? cible : meilleureCible;
    }
    
    /**
     * Détecte si on doit changer de direction (atteint un bord)
     */
    private static boolean detecterChangementDirection(
            Graphe.Intersection courant, Piece piece,
            boolean directionHorizontale, boolean aller) {
        
        int marge = DISTANCE_MIN_MUR + 5; // Marge pour détecter le bord
        
        if (directionHorizontale) {
            // Si on va horizontalement, vérifier si on est près d'un bord vertical
            if (aller) {
                // Aller vers la droite
                return courant.getX() >= piece.getLargeur() - marge;
            } else {
                // Aller vers la gauche
                return courant.getX() <= marge;
            }
        } else {
            // Si on va verticalement, vérifier si on est près d'un bord horizontal
            if (aller) {
                // Aller vers le haut
                return courant.getY() >= piece.getLongueur() - marge;
            } else {
                // Aller vers le bas
                return courant.getY() <= marge;
            }
        }
    }
    
    /**
     * Trouve l'intersection suivante en utilisant uniquement les connexions du graphe
     * Cela assure que le fil passe toujours par les intersections et prend uniquement des directions de 90°
     */
    private static Graphe.Intersection trouverIntersectionSuivanteDepuisConnexions(
            Graphe graphe, Piece piece, Graphe.Intersection courant,
            Set<Graphe.Intersection> visites, double longueurRestante, int distanceMinMur) {
        
        List<Graphe.Intersection> candidats = new ArrayList<>();
        
        // Utiliser uniquement les connexions du graphe (assure les angles de 45°, 90°, 135°)
        for (Graphe.Intersection connexion : courant.getConnexions()) {
            if (visites.contains(connexion)) {
                continue; // Ne pas revisiter
            }
            
            // Vérifier que l'intersection est valide (respecte les contraintes)
            if (estIntersectionValide(connexion, piece, distanceMinMur)) {
                double dist = distance(courant, connexion);
                // Préférer les connexions qui utilisent bien la longueur restante
                if (dist <= longueurRestante * 1.1) {
                    candidats.add(connexion);
                }
            }
        }
        
        if (candidats.isEmpty()) {
            return null;
        }
        
        // Choisir la meilleure intersection basée sur plusieurs critères
        Graphe.Intersection meilleur = null;
        double meilleurScore = Double.NEGATIVE_INFINITY;
        
        for (Graphe.Intersection candidat : candidats) {
            double score = 0;
            double dist = distance(courant, candidat);
            
            // Préférer les intersections qui maximisent la couverture
            // (plus éloignées du centre de la pièce)
            int centreX = piece.getLargeur() / 2;
            int centreY = piece.getLongueur() / 2;
            double distanceAuCentre = Math.sqrt(
                Math.pow(candidat.getX() - centreX, 2) + Math.pow(candidat.getY() - centreY, 2));
            score += distanceAuCentre * 0.1;
            
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
        
        return meilleur != null ? meilleur : candidats.get(0);
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
     * Calcule la distance entre deux intersections
     */
    private static double distance(Graphe.Intersection i1, Graphe.Intersection i2) {
        double dx = i1.getX() - i2.getX();
        double dy = i1.getY() - i2.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }
}

