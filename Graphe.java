package domaine.graphe;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Représente le graphe des intersections de la membrane
 * Utilise la librairie standard java.util.graph
 * Une pièce est créée en réunissant au minimum 3 intersections (sommets) non voisins
 */
public class Graphe {
    private int largeur; // largeur de la pièce
    private int longueur; // longueur de la pièce
    private Map<String, Intersection> intersections; // Map pour stocker les intersections par clé
    private Set<Intersection> intersectionsActives; // Set des intersections actives
    private boolean genere;

    /**
     * Représente une intersection dans le graphe
     */
    public static class Intersection {
        private String id; // Identifiant unique de l'intersection
        private int x, y;
        private boolean active;
        private Set<Intersection> connexions; // Connexions vers d'autres intersections

        public Intersection(String id, int x, int y) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.active = false;
            this.connexions = new HashSet<>();
        }

        // Getters et Setters
        public String getId() { return id; }
        public int getX() { return x; }
        public int getY() { return y; }
        public boolean estActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
        public Set<Intersection> getConnexions() { return connexions; }

        public void ajouterConnexion(Intersection intersection) {
            if (intersection != null && !intersection.equals(this)) {
                this.connexions.add(intersection);
                intersection.connexions.add(this); // Connexion bidirectionnelle
            }
        }

        public void supprimerConnexion(Intersection intersection) {
            this.connexions.remove(intersection);
            intersection.connexions.remove(this);
        }

        public boolean estVoisin(Intersection autre) {
            return connexions.contains(autre);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Intersection that = (Intersection) obj;
            return Objects.equals(id, that.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }

        @Override
        public String toString() {
            return String.format("Intersection(%s: %d, %d) - %s", id, x, y, active ? "active" : "inactive");
        }
    }

    public Graphe(int largeur, int longueur) {
        this.largeur = largeur;
        this.longueur = longueur;
        this.intersections = new HashMap<>();
        this.intersectionsActives = new HashSet<>();
        this.genere = false;
    }

    // === Getters et Setters ===
    public int getLargeur() {
        return largeur;
    }

    public void setLargeur(int largeur) {
        this.largeur = largeur;
    }

    public int getLongueur() {
        return longueur;
    }

    public void setLongueur(int longueur) {
        this.longueur = longueur;
    }

    public Collection<Intersection> getIntersections() {
        return intersections.values();
    }

    public Set<Intersection> getIntersectionsActives() {
        return new HashSet<>(intersectionsActives);
    }

    public boolean estGenere() {
        return genere;
    }

    private int espacement = 3 * 32; // Espacement entre les intersections (en 32èmes de pouce)
    private int translationX = 0; // Translation X de la membrane (en 32èmes de pouce)
    private int translationY = 0; // Translation Y de la membrane (en 32èmes de pouce)
    private boolean confirmee = false; // Si true, on ne garde que les intersections dans la pièce

    public int getEspacement() {
        return espacement;
    }

    public void setEspacement(int espacement) {
        this.espacement = espacement;
        this.genere = false; // Régénérer le graphe avec le nouvel espacement
    }

    public int getTranslationX() {
        return translationX;
    }

    public void setTranslationX(int translationX) {
        this.translationX = translationX;
        this.genere = false;
    }

    public int getTranslationY() {
        return translationY;
    }

    public void setTranslationY(int translationY) {
        this.translationY = translationY;
        this.genere = false;
    }
    
    // Méthodes pour obtenir les valeurs en pouces (double)
    public double getEspacementEnPouces() {
        return espacement / 32.0;
    }
    
    public double getTranslationXEnPouces() {
        return translationX / 32.0;
    }
    
    public double getTranslationYEnPouces() {
        return translationY / 32.0;
    }

    public boolean estConfirmee() {
        return confirmee;
    }

    public void setConfirmee(boolean confirmee) {
        this.confirmee = confirmee;
        this.genere = false; // Régénérer le graphe avec le nouvel état
    }

    /**
     * Génère le graphe des intersections selon les dimensions de la pièce
     * Crée une grille d'intersections avec des connexions non voisines
     * Permet des intersections en dehors de la pièce pour l'affichage visuel
     */
    public void genererGraphe() {
        genererGrapheAvecValidation(null);
    }
    
    /**
     * Génère le graphe avec validation des intersections (pour la confirmation)
     * @param piece La pièce pour valider les intersections (null si pas de validation)
     */
    public void genererGrapheAvecValidation(domaine.piece.Piece piece) {
        intersections.clear();
        intersectionsActives.clear();

        if (confirmee && piece != null) {
            // Après confirmation : garder la position actuelle (translation) et compléter
            // toutes les intersections manquantes dans la pièce, en validant chaque intersection
            
            // La grille de base commence à (0, 0) et s'étend jusqu'à (largeur, longueur)
            // Avec la translation, les intersections sont à (xBase + translationX, yBase + translationY)
            // On crée toutes les intersections possibles dans la pièce
            
            // Convertir les valeurs en 32èmes en pouces pour la génération
            double espacementPouces = espacement / 32.0;
            double translationXPouces = translationX / 32.0;
            double translationYPouces = translationY / 32.0;
            
            // Trouver le point de départ de la grille de base pour couvrir toute la pièce
            // On commence avant 0 si nécessaire pour que les intersections avec translation couvrent toute la pièce
            double debutXBase = 0;
            // Ajuster pour que la première intersection avec translation soit >= 0
            while (debutXBase + translationXPouces < 0) {
                debutXBase += espacementPouces;
            }
            // Normaliser pour commencer au plus tôt possible
            double resteX = (debutXBase + translationXPouces) % espacementPouces;
            if (resteX < 0) resteX += espacementPouces;
            debutXBase = resteX - translationXPouces;
            while (debutXBase < 0) debutXBase += espacementPouces;
            while (debutXBase >= espacementPouces) debutXBase -= espacementPouces;
            
            double debutYBase = 0;
            while (debutYBase + translationYPouces < 0) {
                debutYBase += espacementPouces;
            }
            double resteY = (debutYBase + translationYPouces) % espacementPouces;
            if (resteY < 0) resteY += espacementPouces;
            debutYBase = resteY - translationYPouces;
            while (debutYBase < 0) debutYBase += espacementPouces;
            while (debutYBase >= espacementPouces) debutYBase -= espacementPouces;
            
            // Créer toutes les intersections dans la pièce
            for (double xBase = debutXBase; xBase <= largeur + espacementPouces; xBase += espacementPouces) {
                for (double yBase = debutYBase; yBase <= longueur + espacementPouces; yBase += espacementPouces) {
                    int x = (int) Math.round(xBase + translationXPouces);
                    int y = (int) Math.round(yBase + translationYPouces);
                    
                    // Vérifier que l'intersection est dans la pièce
                    if (x >= 0 && x <= largeur && y >= 0 && y <= longueur) {
                        // Valider l'intersection (pas dans un meuble, pas trop près, etc.)
                        if (estIntersectionValide(x, y, piece)) {
                            String id = String.format("I_%d_%d", x, y);
                            Intersection intersection = new Intersection(id, x, y);
                            intersections.put(id, intersection);
                        }
                    }
                }
            }
        } else {
            // Avant confirmation : créer une grille étendue pour l'affichage
            
            // Convertir les valeurs en 32èmes en pouces pour la génération
            double espacementPouces = espacement / 32.0;
            double translationXPouces = translationX / 32.0;
            double translationYPouces = translationY / 32.0;
            
            // Calculer la plage étendue pour inclure les intersections qui dépassent
            double minX = Math.min(0, translationXPouces);
            double maxX = Math.max(largeur, largeur + translationXPouces);
            double minY = Math.min(0, translationYPouces);
            double maxY = Math.max(longueur, longueur + translationYPouces);
            
            // Ajouter une marge pour s'assurer qu'on couvre tout
            double marge = espacementPouces * 2;
            minX -= marge;
            maxX += marge;
            minY -= marge;
            maxY += marge;
            
            // Créer la grille de base (sans translation)
            for (double xBase = 0; xBase <= largeur; xBase += espacementPouces) {
                for (double yBase = 0; yBase <= longueur; yBase += espacementPouces) {
                    int xTranslated = (int) Math.round(xBase + translationXPouces);
                    int yTranslated = (int) Math.round(yBase + translationYPouces);
                    
                    // Avant confirmation : toutes les intersections (même celles qui dépassent)
                    if (xTranslated >= minX && xTranslated <= maxX && 
                        yTranslated >= minY && yTranslated <= maxY) {
                        String id = String.format("I_%d_%d", xTranslated, yTranslated);
                        Intersection intersection = new Intersection(id, xTranslated, yTranslated);
                        intersections.put(id, intersection);
                    }
                }
            }
        }

        // Créer des connexions uniquement horizontales et verticales (90° uniquement)
        // Le fil doit toujours passer par les intersections et prendre uniquement des directions de 90°
        // On crée des connexions pour toutes les intersections, même celles en dehors de la pièce
        for (Intersection intersection : intersections.values()) {
            int x = intersection.getX();
            int y = intersection.getY();

            // Connexions horizontales (90° - droite)
            int xVoisin = x + espacement;
            String idVoisin = String.format("I_%d_%d", xVoisin, y);
            Intersection voisin = intersections.get(idVoisin);
            if (voisin != null) {
                intersection.ajouterConnexion(voisin);
            }

            // Connexions horizontales (90° - gauche)
            int xVoisinGauche = x - espacement;
            String idVoisinGauche = String.format("I_%d_%d", xVoisinGauche, y);
            Intersection voisinGauche = intersections.get(idVoisinGauche);
            if (voisinGauche != null) {
                intersection.ajouterConnexion(voisinGauche);
            }

            // Connexions verticales (90° - haut)
            int yVoisin = y + espacement;
            String idVoisinHaut = String.format("I_%d_%d", x, yVoisin);
            Intersection voisinHaut = intersections.get(idVoisinHaut);
            if (voisinHaut != null) {
                intersection.ajouterConnexion(voisinHaut);
            }

            // Connexions verticales (90° - bas)
            int yVoisinBas = y - espacement;
            String idVoisinBas = String.format("I_%d_%d", x, yVoisinBas);
            Intersection voisinBas = intersections.get(idVoisinBas);
            if (voisinBas != null) {
                intersection.ajouterConnexion(voisinBas);
            }
        }

        this.genere = true;
    }
    
    /**
     * Vérifie si une intersection est valide (dans la pièce, pas dans un meuble, etc.)
     * @param x Coordonnée X de l'intersection
     * @param y Coordonnée Y de l'intersection
     * @param piece La pièce pour valider
     * @return true si l'intersection est valide
     */
    private boolean estIntersectionValide(int x, int y, domaine.piece.Piece piece) {
        // Constantes de contraintes (en pouces)
        final int DISTANCE_MIN_MEUBLE = 3;
        final int DISTANCE_MIN_DRAIN = 6;
        final int DISTANCE_MIN_DRAIN_TOILETTE = 10;
        
        // Vérifier si la pièce est irrégulière
        java.util.List<java.awt.Point> points = piece.getPoints();
        boolean estIrreguliere = points != null && points.size() >= 3;
        
        if (estIrreguliere) {
            // Pour une pièce irrégulière, vérifier que l'intersection est dans le polygone
            if (!piece.contientPoint(x, y)) {
                return false;
            }
        } else {
            // Pour une pièce rectangulaire, vérifier que l'intersection est dans les limites
            if (x < 0 || y < 0 || x > largeur || y > longueur) {
                return false;
            }
        }
        
        // Vérifier distance aux meubles
        for (domaine.meuble.Meuble m : piece.getMeubles()) {
            // Vérifier si l'intersection est dans le meuble
            if (x >= m.getX() && x <= m.getX() + m.getLargeur() &&
                y >= m.getY() && y <= m.getY() + m.getLongueur()) {
                return false; // Dans le meuble
            }
            
            // Vérifier distance minimale au meuble
            int distX = Math.max(0, Math.max(m.getX() - x, x - (m.getX() + m.getLargeur())));
            int distY = Math.max(0, Math.max(m.getY() - y, y - (m.getY() + m.getLongueur())));
            double distance = Math.sqrt(distX * distX + distY * distY);
            if (distance < DISTANCE_MIN_MEUBLE) {
                return false; // Trop près du meuble
            }
            
            // Vérifier distance aux drains
            if (m instanceof domaine.meuble.MeubleAvecDrain d) {
                int drainX = d.getDrainX();
                int drainY = d.getDrainY();
                double distanceDrain = Math.sqrt((x - drainX) * (x - drainX) + (y - drainY) * (y - drainY));
                int distanceMinDrain = (m instanceof domaine.meuble.Toilette) ? 
                    DISTANCE_MIN_DRAIN_TOILETTE : DISTANCE_MIN_DRAIN;
                if (distanceDrain < distanceMinDrain) {
                    return false; // Trop près du drain
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
     * Met à jour les dimensions du graphe selon la pièce
     */
    public void mettreAJourDimensions(int largeur, int longueur) {
        this.largeur = largeur;
        this.longueur = longueur;
        this.genere = false;
        genererGraphe(); // Régénérer le graphe avec les nouvelles dimensions
    }

    /**
     * Active/désactive une intersection à une position donnée
     */
    public void activerIntersection(int x, int y) {
        String id = String.format("I_%d_%d", x, y);
        Intersection intersection = intersections.get(id);
        if (intersection != null) {
            intersection.setActive(true);
            intersectionsActives.add(intersection);
        }
    }

    /**
     * Désactive toutes les intersections
     */
    public void desactiverToutesIntersections() {
        for (Intersection intersection : intersectionsActives) {
            intersection.setActive(false);
        }
        intersectionsActives.clear();
    }

    /**
     * Retourne le nombre d'intersections actives
     */
    public int getNombreIntersectionsActives() {
        return intersectionsActives.size();
    }

    /**
     * Crée une pièce en réunissant au minimum 3 intersections non voisines
     */
    public void creerPieceAvecIntersections(List<String> idsIntersections) {
        if (idsIntersections.size() < 3) {
            throw new IllegalArgumentException("Une pièce doit avoir au minimum 3 intersections");
        }

        // Vérifier que les intersections existent
        List<Intersection> intersectionsPiece = new ArrayList<>();
        for (String id : idsIntersections) {
            Intersection intersection = intersections.get(id);
            if (intersection == null) {
                throw new IllegalArgumentException("Intersection " + id + " n'existe pas");
            }
            intersectionsPiece.add(intersection);
        }

        // Vérifier qu'elles ne sont pas toutes voisines
        boolean toutesVoisines = true;
        for (int i = 0; i < intersectionsPiece.size(); i++) {
            for (int j = i + 1; j < intersectionsPiece.size(); j++) {
                if (!intersectionsPiece.get(i).estVoisin(intersectionsPiece.get(j))) {
                    toutesVoisines = false;
                    break;
                }
            }
            if (!toutesVoisines) break;
        }

        if (toutesVoisines) {
            throw new IllegalArgumentException("Les intersections doivent être non voisines pour former une pièce");
        }

        // Activer les intersections de la pièce
        for (Intersection intersection : intersectionsPiece) {
            intersection.setActive(true);
            intersectionsActives.add(intersection);
        }
    }

    /**
     * Retourne une intersection par son ID
     */
    public Intersection getIntersection(String id) {
        return intersections.get(id);
    }

    @Override
    public String toString() {
        return String.format("Graphe (%d\" x %d\") - %d intersections, %d actives",
                largeur, longueur, intersections.size(), getNombreIntersectionsActives());
    }
}
