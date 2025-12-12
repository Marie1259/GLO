package domaine.piece;

import domaine.validation.PieceValidator;
import java.awt.Point;
import java.util.List;

/**
 * Fabrique pour créer des pièces rectangulaires ou irrégulières
 * Rectangulaire = Irrégulière à 4 points
 */
public final class PieceFactory {
    private PieceFactory() {}

    /**
     * Crée une pièce rectangulaire (4 points formant un rectangle)
     */
    public static Piece creerRectangulaire(int largeur, int longueur) {
        // Valider les dimensions avant de créer
        String erreur = PieceValidator.validerDimensionsPiece(largeur, longueur);
        if (erreur != null) {
            throw new IllegalArgumentException(erreur);
        }

        // Polygone: origine (0,0) en haut-gauche
        List<Point> pts = List.of(
                new Point(0, 0),
                new Point(largeur, 0),
                new Point(largeur, longueur),
                new Point(0, longueur)
        );
        return new Piece(pts);
    }

    /**
     * Crée une pièce irrégulière à partir d'une liste de points
     */
    public static Piece creerIrreguliere(List<Point> points) {
        if (points == null || points.size() < 3) {
            throw new IllegalArgumentException("Une pièce doit avoir au minimum 3 points");
        }
        return new Piece(points);
    }
}

