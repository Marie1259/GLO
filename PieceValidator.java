package domaine.validation;

import domaine.chauffage.Thermostat;
import domaine.meuble.Meuble;
import domaine.piece.Piece;
import java.util.Collection;

/**
 * Classe centralisée pour toutes les validations et contraintes du projet.
 * Séparation des responsabilités : toutes les règles métier de validation sont ici.
 */
public final class PieceValidator {
    private PieceValidator() {
    }

    /**
     * Valide la position d'un meuble avec toutes les contraintes
     */
    public static String validerPositionMeuble(Piece piece, int x, int y, int largeur, int longueur, Integer idExclu) {
        if (piece == null) {
            return "Erreur: aucune pièce disponible";
        }

        java.util.List<java.awt.Point> points = piece.getPoints();
        boolean estIrreguliere = points != null && points.size() >= 3;

        String erreurDimensions = validerDimensions(largeur, longueur);
        if (erreurDimensions != null) {
            return erreurDimensions;
        }

        if (estIrreguliere) {
            String erreurDansPiece = validerDansPiece(piece, x, y, largeur, longueur);
            if (erreurDansPiece != null) {
                return erreurDansPiece;
            }

            String erreurChevauchement = validerChevauchementMeubles(piece, x, y, largeur, longueur, idExclu);
            if (erreurChevauchement != null) {
                return erreurChevauchement;
            }

            return null;
        }

        String erreurDansPiece = validerDansPiece(piece, x, y, largeur, longueur);
        if (erreurDansPiece != null) {
            return erreurDansPiece;
        }

        String erreurChevauchement = validerChevauchementMeubles(piece, x, y, largeur, longueur, idExclu);
        if (erreurChevauchement != null) {
            return erreurChevauchement;
        }

        return null;
    }

    public static String validerDimensions(int largeur, int longueur) {
        if (largeur <= 0) {
            return "La largeur doit être positive";
        }
        if (longueur <= 0) {
            return "La longueur doit être positive";
        }
        return null;
    }

    public static String validerDansPiece(Piece piece, int x, int y, int largeur, int longueur) {
        if (x < 0 || y < 0) {
            return "Le meuble ne peut pas être en dehors de la pièce (position négative)";
        }

        java.util.List<java.awt.Point> points = piece.getPoints();
        boolean estIrreguliere = points != null && points.size() >= 3;

        if (estIrreguliere) {
            return null;
        } else {
            if (x + largeur > piece.getLargeur()) {
                return String.format("Le meuble dépasse la largeur de la pièce (position X: %d\", largeur meuble: %d\", total: %d\" > largeur pièce: %d\")",
                        x, largeur, x + largeur, piece.getLargeur());
            }
            if (y + longueur > piece.getLongueur()) {
                return String.format("Le meuble dépasse la longueur de la pièce (position Y: %d\", longueur meuble: %d\", total: %d\" > longueur pièce: %d\")",
                        y, longueur, y + longueur, piece.getLongueur());
            }
        }
        return null;
    }

    public static String validerChevauchementMeubles(Piece piece, int x, int y, int largeur, int longueur, Integer idExclu) {
        Collection<Meuble> meubles = piece.getMeubles();

        for (Meuble meuble : meubles) {
            if (idExclu != null && meuble.getId() == idExclu) {
                continue;
            }

            boolean chevaucheX = (x < meuble.getX() + meuble.getLargeur()) &&
                    (x + largeur > meuble.getX());
            boolean chevaucheY = (y < meuble.getY() + meuble.getLongueur()) &&
                    (y + longueur > meuble.getY());

            if (chevaucheX && chevaucheY) {
                return "Position occupée par: " + meuble.getNom();
            }
        }

        return null;
    }

    /**
     * Valide la position d'un élément chauffant
     * Fonctionne pour RÉGULIER et IRRÉGULIER
     * Valide que l'élément chauffant est BIEN COLLÉ à un mur
     */
    public static String validerPositionElementChauffant(Piece piece, int x, int y, int largeur, int longueur) {
        if (piece == null) {
            return "Erreur: aucune pièce disponible";
        }

        String erreurDimensions = validerDimensions(largeur, longueur);
        if (erreurDimensions != null) {
            return erreurDimensions;
        }

        java.util.List<java.awt.Point> points = piece.getPoints();
        boolean estIrreguliere = points != null && points.size() >= 3;

        if (estIrreguliere) {
            // ✅ Pour pièce irrégulière: vérifier que l'élément touche UN mur
            // TOLÉRANCE: 2 pouces (très strict)
            if (!elementToucheMurIrreguliere(x, y, largeur, longueur, points)) {
                return "L'élément chauffant DOIT être collé à un mur de la pièce";
            }
            return null;
        }

        // Code pour pièce rectangulaire
        if (x < 0 || y < 0) {
            return "L'élément chauffant doit être dans la pièce";
        }

        if (x + largeur > piece.getLargeur()) {
            return "L'élément chauffant dépasse la largeur de la pièce";
        }
        if (y + longueur > piece.getLongueur()) {
            return "L'élément chauffant dépasse la longueur de la pièce";
        }

        boolean colleAuMurGauche = (x == 0);
        boolean colleAuMurDroit = (x + largeur == piece.getLargeur());
        boolean colleAuMurHaut = (y == 0);
        boolean colleAuMurBas = (y + longueur == piece.getLongueur());

        if (!(colleAuMurGauche || colleAuMurDroit || colleAuMurHaut || colleAuMurBas)) {
            return "L'élément chauffant doit être collé à un mur";
        }

        return null;
    }

    /**
     * Vérifie que l'élément touche UN mur (tolérance stricte: 2 pouces)
     */
    private static boolean elementToucheMurIrreguliere(
            int x, int y, int largeur, int longueur,
            java.util.List<java.awt.Point> pointsPolygone) {

        if (pointsPolygone == null || pointsPolygone.size() < 3) {
            return false;
        }

        double tolerance = 2.0; // TOLÉRANCE STRICTE: 2 pouces

        // Les 4 côtés de l'élément
        java.awt.Point[][] cotes = {
                {new java.awt.Point(x, y), new java.awt.Point(x + largeur, y)},  // Bas
                {new java.awt.Point(x + largeur, y), new java.awt.Point(x + largeur, y + longueur)},  // Droit
                {new java.awt.Point(x + largeur, y + longueur), new java.awt.Point(x, y + longueur)},  // Haut
                {new java.awt.Point(x, y + longueur), new java.awt.Point(x, y)}   // Gauche
        };

        // Vérifier si AU MOINS UN CÔTÉ est parallèle ET proche d'un mur
        for (java.awt.Point[] cote : cotes) {
            java.awt.Point coteStart = cote[0];
            java.awt.Point coteEnd = cote[1];

            for (int j = 0; j < pointsPolygone.size(); j++) {
                java.awt.Point murStart = pointsPolygone.get(j);
                java.awt.Point murEnd = pointsPolygone.get((j + 1) % pointsPolygone.size());

                // Vérifier si parallèles
                if (sontParalleles(coteStart, coteEnd, murStart, murEnd)) {
                    // Vérifier si proches
                    double dist = distanceLignesParalleles(coteStart, coteEnd, murStart, murEnd);
                    if (dist < tolerance) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Vérifie si deux segments sont parallèles
     */
    private static boolean sontParalleles(
            java.awt.Point p1Start, java.awt.Point p1End,
            java.awt.Point p2Start, java.awt.Point p2End) {

        int dx1 = p1End.x - p1Start.x;
        int dy1 = p1End.y - p1Start.y;
        int dx2 = p2End.x - p2Start.x;
        int dy2 = p2End.y - p2Start.y;

        // Produit croisé (si ~0, alors parallèles)
        int produitCroise = dx1 * dy2 - dy1 * dx2;

        // Tolérance: 50 pour accepter un angle de ~5 degrés
        return Math.abs(produitCroise) < 50;
    }

    /**
     * Calcule la distance entre deux lignes parallèles
     */
    private static double distanceLignesParalleles(
            java.awt.Point p1Start, java.awt.Point p1End,
            java.awt.Point p2Start, java.awt.Point p2End) {

        // Distance du point p2Start au segment p1Start-p1End
        return distancePointSegment(p2Start, p1Start, p1End);
    }


//    public static String validerPositionElementChauffant(Piece piece, int x, int y, int largeur, int longueur) {
//        if (piece == null) {
//            return "Erreur: aucune pièce disponible";
//        }
//
//        String erreurDimensions = validerDimensions(largeur, longueur);
//        if (erreurDimensions != null) {
//            return erreurDimensions;
//        }
//
//        java.util.List<java.awt.Point> points = piece.getPoints();
//        boolean estIrreguliere = points != null && points.size() >= 3;
//
//        if (estIrreguliere) {
//            // Pour pièce irrégulière: vérifier que l'élément touche un mur
//            if (!estElementToucheMurPolygone(x, y, largeur, longueur, points)) {
//                System.err.println("DEBUG: Élément (" + x + "," + y + ") " + largeur + "x" + longueur + " ne touche aucun mur");
//                return "L'élément chauffant doit être collé à un mur de la pièce";
//            }
//            return null;
//        }
//
//        // Pour pièce rectangulaire
//        if (x < 0 || y < 0) {
//            return "L'élément chauffant doit être dans la pièce";
//        }
//
//        if (x + largeur > piece.getLargeur()) {
//            return "L'élément chauffant dépasse la largeur de la pièce";
//        }
//        if (y + longueur > piece.getLongueur()) {
//            return "L'élément chauffant dépasse la longueur de la pièce";
//        }
//
//        boolean colleAuMurGauche = (x == 0);
//        boolean colleAuMurDroit = (x + largeur == piece.getLargeur());
//        boolean colleAuMurHaut = (y == 0);
//        boolean colleAuMurBas = (y + longueur == piece.getLongueur());
//
//        if (!(colleAuMurGauche || colleAuMurDroit || colleAuMurHaut || colleAuMurBas)) {
//            return "L'élément chauffant doit être collé à un mur";
//        }
//
//        return null;
//    }
//
    public static boolean validerPositionElementChauffantBool(Piece piece, int x, int y) {
        return validerPositionElementChauffant(piece, x, y, 10, 10) == null;
    }

    /**
     * Vérifie si l'élément touche UN mur du polygone
     * TOLÉRANCE TRÈS AUGMENTÉE: 10 pouces
     */
//    private static boolean estElementToucheMurPolygone(
//            int x, int y, int largeur, int longueur,
//            java.util.List<java.awt.Point> pointsPolygone) {
//
//        if (pointsPolygone == null || pointsPolygone.size() < 3) {
//            System.err.println("DEBUG: Polygone invalide");
//            return false;
//        }
//
//        double tolerance = 10.0; // TRÈS AUGMENTÉE pour pièces irrégulières
//
//        // Les 4 coins de l'élément
//        java.awt.Point[] coinsElement = {
//                new java.awt.Point(x, y),
//                new java.awt.Point(x + largeur, y),
//                new java.awt.Point(x + largeur, y + longueur),
//                new java.awt.Point(x, y + longueur)
//        };
//
//        // Vérifier chaque coin
//        for (int coinIdx = 0; coinIdx < coinsElement.length; coinIdx++) {
//            java.awt.Point coin = coinsElement[coinIdx];
//
//            for (int j = 0; j < pointsPolygone.size(); j++) {
//                java.awt.Point polyP1 = pointsPolygone.get(j);
//                java.awt.Point polyP2 = pointsPolygone.get((j + 1) % pointsPolygone.size());
//
//                double distance = distancePointSegment(coin, polyP1, polyP2);
//
//                if (distance < tolerance) {
//                    System.err.println("DEBUG: Coin " + coinIdx + " à distance " + distance + " du mur " + j);
//                    return true;
//                }
//            }
//        }
//
//        System.err.println("DEBUG: Aucun coin ne touche (meilleure distance: " + trouverMeilleureDistance(coinsElement, pointsPolygone) + ")");
//        return false;
//    }

    /**
     * HELPER: Trouver la meilleure distance (pour debug)
     */
    private static double trouverMeilleureDistance(java.awt.Point[] coins, java.util.List<java.awt.Point> murs) {
        double minDist = Double.MAX_VALUE;
        for (java.awt.Point coin : coins) {
            for (int j = 0; j < murs.size(); j++) {
                double dist = distancePointSegment(coin, murs.get(j), murs.get((j + 1) % murs.size()));
                minDist = Math.min(minDist, dist);
            }
        }
        return minDist;
    }

    /**
     * Calcule la distance d'un point à un segment
     */
    private static double distancePointSegment(
            java.awt.Point point, java.awt.Point segmentStart, java.awt.Point segmentEnd) {

        int dx = segmentEnd.x - segmentStart.x;
        int dy = segmentEnd.y - segmentStart.y;

        if (dx == 0 && dy == 0) {
            return distancePoints(point, segmentStart);
        }

        int px = point.x - segmentStart.x;
        int py = point.y - segmentStart.y;

        double t = (double) (px * dx + py * dy) / (dx * dx + dy * dy);
        t = Math.max(0, Math.min(1, t));

        int projX = segmentStart.x + (int) (t * dx);
        int projY = segmentStart.y + (int) (t * dy);

        return distancePoints(point, new java.awt.Point(projX, projY));
    }

    /**
     * Calcule la distance entre deux points
     */
    private static double distancePoints(java.awt.Point p1, java.awt.Point p2) {
        int dx = p1.x - p2.x;
        int dy = p1.y - p2.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Valide l'ajout d'un thermostat (UN SEUL par pièce)
     */
    public static String validerAjoutThermostat(Piece piece) {
        if (piece == null) {
            return "Erreur: aucune pièce disponible";
        }

        boolean aDejaUnThermostat = piece.getElementsChauffants().stream()
                .anyMatch(e -> e instanceof domaine.chauffage.Thermostat);

        if (aDejaUnThermostat) {
            return "Il ne peut y avoir qu'un seul thermostat par pièce";
        }

        return null;
    }

    public static String validerDimensionsPiece(int largeur, int longueur) {
        if (largeur <= 0) {
            return "La largeur de la pièce doit être positive";
        }
        if (longueur <= 0) {
            return "La longueur de la pièce doit être positive";
        }
        if (largeur > 10000) {
            return "La largeur de la pièce est trop grande (maximum 10000 pouces)";
        }
        if (longueur > 10000) {
            return "La longueur de la pièce est trop grande (maximum 10000 pouces)";
        }
        return null;
    }


}