package domaine.piece;

import domaine.meuble.Meuble;
import domaine.chauffage.ElementChauffant;
import domaine.chauffage.FilChauffant;
import domaine.graphe.Graphe;
//import domaine.piece.util.Segment;
import domaine.piece.util.MurProcheInfo;
import domaine.validation.PieceValidator;
import domaine.zone.ZoneInterdiction;
import domaine.zone.ZoneTampon;

import java.awt.Point;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import domaine.SelectionService;
import domaine.meuble.MeubleAvecDrain;


/**
 * Représente une pièce dans l'application.
 * Une pièce a au minimum 3 côtés formés de 3 points non alignés.
 * Une pièce rectangulaire est formée de 4 points.
 * Contient des meubles, des éléments chauffants et un graphe.
 */
public class Piece {
    private int largeur; // en pouces
    private int longueur; // en pouces
    private List<Point> points; // Points définissant le contour de la pièce (minimum 3)
    private Path2D contour; // Contour de la pièce calculé à partir des points
    private final Map<Integer, Meuble> meubles; // Map pour accès par id
    private final Map<Integer, ElementChauffant> elementsChauffants; // Map pour accès par id
    private final Map<Integer, ZoneInterdiction> zonesInterdiction; // Map pour accès par id
    private final Map<Integer, ZoneTampon> zonesTampon; // Map pour accès par id
    private FilChauffant filChauffant; // Fil chauffant de la pièce (composition)
    private Graphe graphe; // Un graphe par pièce

    public Piece(int largeur, int longueur) {
        this.largeur = largeur;
        this.longueur = longueur;
        this.points = new ArrayList<>();
        this.meubles = new LinkedHashMap<>();
        this.elementsChauffants = new LinkedHashMap<>();
        this.zonesInterdiction = new LinkedHashMap<>();
        this.zonesTampon = new LinkedHashMap<>();
        this.graphe = new Graphe(largeur, longueur);
        // Initialiser avec un rectangle par défaut (4 points)
        initialiserContourRectangulaire();
        // Initialiser le fil chauffant
        this.filChauffant = new FilChauffant("Fil principal", largeur, longueur, 6);
    }

    /**
     * Constructeur à partir d'une liste de points (pour PieceFactory)
     */
    public Piece(List<Point> points) {
        if (points == null || points.size() < 3) {
            throw new IllegalArgumentException("Une pièce doit avoir au minimum 3 points");
        }
        this.points = new ArrayList<>(points);
        this.meubles = new LinkedHashMap<>();
        this.elementsChauffants = new LinkedHashMap<>();
        this.zonesInterdiction = new LinkedHashMap<>();
        this.zonesTampon = new LinkedHashMap<>();
        recalculerContour();
        // Calculer largeur et longueur depuis les points
        calculerDimensionsDepuisPoints();
        this.graphe = new Graphe(largeur, longueur);
        // Initialiser le fil chauffant
        this.filChauffant = new FilChauffant("Fil principal", largeur, longueur, 6);
    }

    /**
     * Initialise le fil chauffant (utilisé par PieceFactory)
     */
    public void initialiserFilChauffant(int largeur, int longueur) {
        this.filChauffant = new FilChauffant("Fil principal", largeur, longueur, 6);
    }

    public FilChauffant getFilChauffant() {
        return filChauffant;
    }

    /**
     * Calcule la largeur et longueur depuis les points du contour
     * Méthode publique pour permettre la mise à jour après modification des points
     */
    public void calculerDimensionsDepuisPoints() {
        if (points.isEmpty()) {
            this.largeur = 0;
            this.longueur = 0;
            return;
        }
        int minX = points.get(0).x;
        int maxX = points.get(0).x;
        int minY = points.get(0).y;
        int maxY = points.get(0).y;

        for (Point p : points) {
            if (p.x < minX) minX = p.x;
            if (p.x > maxX) maxX = p.x;
            if (p.y < minY) minY = p.y;
            if (p.y > maxY) maxY = p.y;
        }

        this.largeur = maxX - minX;
        this.longueur = maxY - minY;
    }

    /**
     * Initialise le contour avec un rectangle (4 points)
     */
    private void initialiserContourRectangulaire() {
        points.clear();
        points.add(new Point(0, 0));
        points.add(new Point(largeur, 0));
        points.add(new Point(largeur, longueur));
        points.add(new Point(0, longueur));
        recalculerContour();
    }

    /**
     * Recalcule le contour Path2D à partir des points
     */
    private void recalculerContour() {
        contour = new Path2D.Double();
        if (!points.isEmpty()) {
            Point premierPoint = points.get(0);
            contour.moveTo(premierPoint.x, premierPoint.y);

            for (int i = 1; i < points.size(); i++) {
                Point point = points.get(i);
                contour.lineTo(point.x, point.y);
            }
            contour.closePath();
        }
    }

    // === Getters ===
    public int getLargeur() {
        return largeur;
    }

    public int getLongueur() {
        return longueur;
    }

    public void setLargeur(int largeur) {
        modifierDimensions(largeur, this.longueur);
    }

    public void setLongueur(int longueur) {
        modifierDimensions(this.largeur, longueur);
    }

    /**
     * Modifie les dimensions de la pièce et met à jour tous les éléments associés
     * (fil chauffant, contour, etc.) - logique métier déplacée du contrôleur
     * Valide les dimensions avant modification
     */
    public void modifierDimensions(int largeur, int longueur) {
        // Valider les dimensions avant modification
        String erreur = PieceValidator.validerDimensionsPiece(largeur, longueur);
        if (erreur != null) {
            throw new IllegalArgumentException(erreur);
        }

        this.largeur = largeur;
        this.longueur = longueur;

        // Recalculer le contour rectangulaire avec les nouvelles dimensions
        initialiserContourRectangulaire();

        // Mettre à jour le fil chauffant
        if (filChauffant != null) {
            filChauffant.mettreAJourDimensions(largeur, longueur);
        }
        
        // Mettre à jour le graphe (membrane) avec les nouvelles dimensions
        if (graphe != null) {
            graphe.mettreAJourDimensions(largeur, longueur);
        }
        
        for (Meuble m : meubles.values()) {
            int newX = m.getX();
            int newY = m.getY();

            if (m.getX() + m.getLargeur() > largeur) {
                newX = Math.max(0, largeur - m.getLargeur());
            }
            if (m.getY() + m.getLongueur() > longueur) {
                newY = Math.max(0, longueur - m.getLongueur());
            }

            // utilise bien le hook onPositionChanged()
            m.setPosition(newX, newY);
        }


        // TODO: Mettre à jour les zones tampons si nécessaire (L4)
    }

    public List<Point> getPoints() {
        return new ArrayList<>(points);
    }

    public Path2D getContour() {
        return contour;
    }

    public Collection<Meuble> getMeubles() {
        return Collections.unmodifiableCollection(meubles.values());
    }

    // === Méthodes de gestion des meubles par ID ===
    public void modifierDrain(int id, int drainX, int drainY, int diametre) {
        Meuble m = requireMeuble(id);
        if (m instanceof MeubleAvecDrain d) {
            d.setDiametreDrain(diametre);
            d.setDrainPosition(drainX, drainY);  // le clamp + flag modifié
        } else {
            throw new IllegalArgumentException("Ce meuble n'a pas de drain");
        }
    }

    /**
     * Modifie le drain avec des coordonnées relatives au meuble
     */
    public void modifierDrainRelatif(int id, int drainXRelatif, int drainYRelatif, int diametre) {
        Meuble m = requireMeuble(id);
        if (m instanceof MeubleAvecDrain d) {
            d.setDiametreDrain(diametre);
            d.setDrainPositionRelative(drainXRelatif, drainYRelatif);
        } else {
            throw new IllegalArgumentException("Ce meuble n'a pas de drain");
        }
    }



    /**
     * Ajoute un meuble et retourne son ID
     * Valide la position avant d'ajouter
     * @throws IllegalArgumentException si la position est invalide
     */
    public int ajouterMeuble(Meuble m) {
        if (m == null) {
            throw new IllegalArgumentException("Meuble invalide");
        }

        // Valider la position via PieceValidator
        String erreur = PieceValidator.validerPositionMeuble(
                this, m.getX(), m.getY(), m.getLargeur(), m.getLongueur(), null);
        if (erreur != null) {
            throw new IllegalArgumentException(erreur);
        }

        meubles.put(m.getId(), m);
        return m.getId();
    }

    /**
     * Trouve un meuble par son ID
     */
    public Optional<Meuble> trouverMeuble(int id) {
        return Optional.ofNullable(meubles.get(id));
    }

    /**
     * Déplace un meuble par son ID
     * Valide la nouvelle position avant de déplacer
     * @throws IllegalArgumentException si la position est invalide
     */
    public void deplacerMeuble(int id, Point nouvellePos) {
        Meuble m = requireMeuble(id);

        // Valider la nouvelle position via PieceValidator
        String erreur = PieceValidator.validerPositionMeuble(
                this, nouvellePos.x, nouvellePos.y, m.getLargeur(), m.getLongueur(), id);
        if (erreur != null) {
            throw new IllegalArgumentException(erreur);
        }

        m.setPosition(nouvellePos.x, nouvellePos.y);

    }

    /**
     * Redimensionne un meuble par son ID
     * Valide les dimensions et la position avant de redimensionner
     * @throws IllegalArgumentException si les dimensions ou la position sont invalides
     */
    public void redimensionnerMeuble(int id, int largeur, int longueur) {
        Meuble m = requireMeuble(id);

        // Valider les dimensions via PieceValidator
        String erreurDimensions = PieceValidator.validerDimensions(largeur, longueur);
        if (erreurDimensions != null) {
            throw new IllegalArgumentException(erreurDimensions);
        }

        // Valider que la nouvelle taille respecte toujours les contraintes de position
        String erreurPosition = PieceValidator.validerPositionMeuble(
                this, m.getX(), m.getY(), largeur, longueur, id);
        if (erreurPosition != null) {
            throw new IllegalArgumentException(erreurPosition);
        }

        m.setLargeur(largeur);
        m.setLongueur(longueur);
    }

    /**
     * Supprime un meuble par son ID
     */
    public void supprimerMeuble(int id) {
        meubles.remove(id);
    }

    /**
     * Trouve l'ID d'un objet à un point donné (pour hit-test abstrait)
     * Cherche d'abord dans les meubles, puis dans les éléments chauffants, puis dans les zones
     */
    public Integer trouverObjetId(Point p) {
        // Chercher dans les meubles
        for (Meuble m : meubles.values()) {
            if (p.x >= m.getX() && p.x <= m.getX() + m.getLargeur() &&
                    p.y >= m.getY() && p.y <= m.getY() + m.getLongueur()) {
                return m.getId();
            }
        }
        // Chercher dans les éléments chauffants (zone de clic autour du point)
        int margeClic = 10; // Zone de clic de 10 pouces autour du point
        for (ElementChauffant e : elementsChauffants.values()) {
            if (p.x >= e.getX() - margeClic && p.x <= e.getX() + e.getLargeur() + margeClic &&
                    p.y >= e.getY() - margeClic && p.y <= e.getY() + e.getLongueur() + margeClic) {
                return e.getId();
            }
        }
        // Chercher dans les zones d'interdiction
        for (ZoneInterdiction z : zonesInterdiction.values()) {
            Point pos = z.getPosition();
            if (p.x >= pos.x && p.x <= pos.x + z.getLargeur() &&
                    p.y >= pos.y && p.y <= pos.y + z.getLongueur()) {
                return z.getId();
            }
        }
        // Chercher dans les zones tampon
        for (ZoneTampon z : zonesTampon.values()) {
            Point pos = z.getPosition();
            if (p.x >= pos.x && p.x <= pos.x + z.getLargeur() &&
                    p.y >= pos.y && p.y <= pos.y + z.getLongueur()) {
                return z.getId();
            }
        }
        return null;
    }

    /**
     * Trouve le type d'élément pour un ID donné
     */
    public SelectionService.TypeElement trouverTypeElement(int id) {
        if (meubles.containsKey(id)) {
            return SelectionService.TypeElement.MEUBLE;
        }
        if (elementsChauffants.containsKey(id)) {
            ElementChauffant e = elementsChauffants.get(id);
            if (e instanceof domaine.chauffage.Thermostat) {
                return SelectionService.TypeElement.THERMOSTAT;
            }
            return SelectionService.TypeElement.ELEMENT_CHAUFFANT;
        }
        if (zonesInterdiction.containsKey(id)) {
            return SelectionService.TypeElement.ZONE_INTERDICTION;
        }
        if (zonesTampon.containsKey(id)) {
            return SelectionService.TypeElement.ZONE_TAMPON;
        }
        return null;
    }

    /**
     * Helper pour obtenir un meuble ou lever une exception
     */
    private Meuble requireMeuble(int id) {
        Meuble m = meubles.get(id);
        if (m == null) throw new NoSuchElementException("Meuble absent: " + id);
        return m;
    }

    /**
     * Valide la position d'un meuble (délègue à PieceValidator)
     * Retourne null si OK, ou un message d'erreur
     */
    public String validerPositionMeubleAvecMessage(int x, int y, int largeur, int longueur, Integer idExclu) {
        return PieceValidator.validerPositionMeuble(this, x, y, largeur, longueur, idExclu);
    }

    /**
     * Valide la position d'un élément chauffant (délègue à PieceValidator)
     */
    public boolean validerPositionElementChauffant(int x, int y) {
        return PieceValidator.validerPositionElementChauffantBool(this, x, y);
    }


    /**
     * Valide la position d'un élément chauffant avec message en tenant compte de sa taille
     */
    public String validerPositionElementChauffantAvecMessage(int x, int y, int largeur, int longueur) {
        return PieceValidator.validerPositionElementChauffant(this, x, y, largeur, longueur);
    }

    public Collection<ElementChauffant> getElementsChauffants() {
        return Collections.unmodifiableCollection(elementsChauffants.values());
    }
    
    private boolean estDansPolygone(Point pointTest) {
        if (points == null || points.size() < 3) {
            // Un polygone doit avoir au moins 3 sommets
            return false;
        }

        int n = points.size();
        int intersections = 0;

        // La position X du point testé
        int x = pointTest.x;
        int y = pointTest.y;

        // Parcourir tous les segments (murs) du polygone
        for (int i = 0; i < n; i++) {
            // Sommet actuel (p1) et sommet suivant (p2)
            Point p1 = points.get(i);
            Point p2 = points.get((i + 1) % n); // Pour revenir au premier sommet

            // Coordonnées pour simplifier la lecture
            int x1 = p1.x;
            int y1 = p1.y;
            int x2 = p2.x;
            int y2 = p2.y;

            // Étape 1 : Vérifier si le segment [p1, p2] croise l'horizontale
            // du point testé.
            // Le segment doit avoir un point au-dessus et un point au-dessous de Y
            if (((y1 <= y) && (y2 > y)) || ((y1 > y) && (y2 <= y))) {

                // Étape 2 : Calculer l'intersection en X
                // On calcule l'abscisse (X) du point d'intersection du segment [p1, p2] 
                // avec la droite horizontale y=y.

                // Équation de la ligne : x = x1 + (y - y1) / (y2 - y1) * (x2 - x1)
                double xIntersection = (double)(y - y1) * (x2 - x1) / (y2 - y1) + x1;

                // Étape 3 : Vérifier si l'intersection est à droite du point testé (x < xIntersection)
                if (x < xIntersection) {
                    intersections++;
                }
            }
        }

        // Si le nombre d'intersections est impair, le point est à l'intérieur.
        return (intersections % 2 != 0);
    }
    
    /**
     * Vérifie si l'élément rectangulaire est entièrement contenu dans la pièce.
     * Il suffit de vérifier si les quatre coins du rectangle sont dans le polygone de la pièce.
     */
    public boolean contientElement(int posX, int posY, int largeur, int longueur) {

        // 1. Définir les quatre coins de l'élément (rectangle)
        Point coinSudOuest = new Point(posX, posY);
        Point coinSudEst = new Point(posX + largeur, posY);
        Point coinNordOuest = new Point(posX, posY + longueur);
        Point coinNordEst = new Point(posX + largeur, posY + longueur);

        // 2. Vérifier si chaque coin est dans le polygone de la pièce.
        // NOTE : La méthode estDansPolygone doit être implémentée (voir section 2)
        return estDansPolygone(coinSudOuest) &&
                estDansPolygone(coinSudEst) &&
                estDansPolygone(coinNordOuest) &&
                estDansPolygone(coinNordEst);
    }

    // === Méthodes de gestion des éléments chauffants par ID ===

    /**
     * Vérifie s'il existe déjà un thermostat dans la pièce (délègue à PieceValidator)
     * @deprecated Utiliser PieceValidator.validerAjoutThermostat() directement
     */
    @Deprecated
    public boolean aDejaUnThermostat() {
        return PieceValidator.validerAjoutThermostat(this) != null;
    }

    /**
     * Ajoute un élément chauffant et retourne son ID
     * Valide la position et les contraintes avant d'ajouter
     * Contraintes :
     * - L'élément doit être collé à un mur
     * - L'élément a une forme rectangulaire (déjà géré par ElementChauffant)
     * - Un seul thermostat par pièce (pour Thermostat uniquement)
     * @throws IllegalArgumentException si la position ou les contraintes sont invalides
     */
    public int ajouterElementChauffant(ElementChauffant element) {
        if (element == null) {
            throw new IllegalArgumentException("Élément chauffant invalide");
        }

        // Vérifier la contrainte : un seul thermostat par pièce (délègue à PieceValidator)
        if (element instanceof domaine.chauffage.Thermostat) {
            String erreur = PieceValidator.validerAjoutThermostat(this);
            if (erreur != null) {
                throw new IllegalArgumentException(erreur);
            }
        }

        // Valider la position via PieceValidator (vérifie aussi que l'élément est collé au mur)
        // Utiliser la version avec taille pour vérifier que l'élément ne dépasse pas de la pièce
        String erreurPosition = PieceValidator.validerPositionElementChauffant(
                this, element.getX(), element.getY(), element.getLargeur(), element.getLongueur());
        if (erreurPosition != null) {
            throw new IllegalArgumentException(erreurPosition);
        }

        elementsChauffants.put(element.getId(), element);
        return element.getId();
    }

    /**
     * Trouve un élément chauffant par son ID
     */
    public Optional<ElementChauffant> trouverElementChauffant(int id) {
        return Optional.ofNullable(elementsChauffants.get(id));
    }

    /**
     * Déplace un élément chauffant par son ID
     * Valide la nouvelle position avant de déplacer
     * @throws IllegalArgumentException si la position est invalide
     */
    public void deplacerElementChauffant(int id, Point nouvellePos) {
        ElementChauffant element = requireElementChauffant(id);

        // Valider la nouvelle position via PieceValidator en tenant compte de la taille
        String erreur = PieceValidator.validerPositionElementChauffant(
                this, nouvellePos.x, nouvellePos.y, element.getLargeur(), element.getLongueur());
        if (erreur != null) {
            throw new IllegalArgumentException(erreur);
        }

        element.setPosition(nouvellePos);
    }

    /**
     * Redimensionne un élément chauffant par son ID
     * Valide les dimensions et la position avant de redimensionner
     * @throws IllegalArgumentException si les dimensions ou la position sont invalides
     */
    public void redimensionnerElementChauffant(int id, int largeur, int longueur) {
        ElementChauffant element = requireElementChauffant(id);

        // Valider les dimensions via PieceValidator
        String erreurDimensions = PieceValidator.validerDimensions(largeur, longueur);
        if (erreurDimensions != null) {
            throw new IllegalArgumentException(erreurDimensions);
        }

        // Valider que la nouvelle taille respecte toujours les contraintes de position
        // (doit être collé au mur et ne pas dépasser de la pièce)
        String erreurPosition = PieceValidator.validerPositionElementChauffant(
                this, element.getX(), element.getY(), largeur, longueur);
        if (erreurPosition != null) {
            throw new IllegalArgumentException(erreurPosition);
        }

        element.setLargeur(largeur);
        element.setLongueur(longueur);
    }

    /**
     * Supprime un élément chauffant par son ID
     */
    public void supprimerElementChauffant(int id) {
        elementsChauffants.remove(id);
    }

    /**
     * Helper pour obtenir un élément chauffant ou lever une exception
     */
    private ElementChauffant requireElementChauffant(int id) {
        ElementChauffant e = elementsChauffants.get(id);
        if (e == null) throw new NoSuchElementException("Élément chauffant absent: " + id);
        return e;
    }

    public Graphe getGraphe() {
        return graphe;
    }

    /**
     * Génère le graphe de la pièce (façade pour le contrôleur)
     */
    public void genererGraphe() {
        if (graphe != null) {
            graphe.genererGraphe();
        }
    }

    /**
     * Active une intersection du graphe (façade pour le contrôleur)
     */
    public void activerIntersection(int x, int y) {
        if (graphe != null) {
            graphe.activerIntersection(x, y);
        }
    }

    /**
     * Désactive toutes les intersections du graphe (façade pour le contrôleur)
     */
    public void desactiverToutesIntersections() {
        if (graphe != null) {
            graphe.desactiverToutesIntersections();
        }
    }

    // === Méthodes de gestion des points ===

    /**
     * Ajoute un point au contour de la pièce
     */
    public void ajouterPoint(int x, int y) {
        points.add(new Point(x, y));
        recalculerContour();
    }

    /**
     * Ajoute un point au contour de la pièce
     */
    public void ajouterPoint(Point point) {
        points.add(new Point(point));
        recalculerContour();
    }

    /**
     * Supprime un point du contour de la pièce
     * Vérifie qu'il reste au moins 3 points
     */
    public boolean supprimerPoint(int index) {
        if (points.size() > 3 && index >= 0 && index < points.size()) {
            points.remove(index);
            recalculerContour();
            return true;
        }
        return false; // Ne peut pas supprimer si moins de 4 points
    }

    /**
     * Modifie un point existant
     */
    public boolean modifierPoint(int index, int x, int y) {
        if (index >= 0 && index < points.size()) {
            points.set(index, new Point(x, y));
            recalculerContour();
            return true;
        }
        return false;
    }

    /**
     * Redimensionne la pièce irrégulière en mettant à l'échelle tous les points proportionnellement
     * @param nouvelleLargeur Nouvelle largeur du rectangle englobant
     * @param nouvelleLongueur Nouvelle longueur du rectangle englobant
     */
    public void redimensionnerPieceIrreguliere(int nouvelleLargeur, int nouvelleLongueur) {
        if (points.isEmpty()) {
            this.largeur = nouvelleLargeur;
            this.longueur = nouvelleLongueur;
            return;
        }

        // Calculer le rectangle englobant actuel
        int minX = points.get(0).x;
        int maxX = points.get(0).x;
        int minY = points.get(0).y;
        int maxY = points.get(0).y;
        
        for (Point p : points) {
            if (p.x < minX) minX = p.x;
            if (p.x > maxX) maxX = p.x;
            if (p.y < minY) minY = p.y;
            if (p.y > maxY) maxY = p.y;
        }
        
        int largeurActuelle = maxX - minX;
        int longueurActuelle = maxY - minY;
        
        if (largeurActuelle == 0 || longueurActuelle == 0) {
            // Si la largeur ou longueur actuelle est 0, on ne peut pas redimensionner
            this.largeur = nouvelleLargeur;
            this.longueur = nouvelleLongueur;
            return;
        }

        // Calculer les facteurs d'échelle
        double scaleX = (double) nouvelleLargeur / largeurActuelle;
        double scaleY = (double) nouvelleLongueur / longueurActuelle;

        // Mettre à l'échelle tous les points par rapport au coin inférieur gauche (minX, minY)
        for (int i = 0; i < points.size(); i++) {
            Point p = points.get(i);
            int nouveauX = minX + (int) Math.round((p.x - minX) * scaleX);
            int nouveauY = minY + (int) Math.round((p.y - minY) * scaleY);
            points.set(i, new Point(nouveauX, nouveauY));
        }

        // Mettre à jour les dimensions
        this.largeur = nouvelleLargeur;
        this.longueur = nouvelleLongueur;

        // Recalculer le contour
        recalculerContour();

        // Mettre à jour le graphe (membrane) avec les nouvelles dimensions
        if (graphe != null) {
            graphe.mettreAJourDimensions(nouvelleLargeur, nouvelleLongueur);
        }

        // Mettre à jour le fil chauffant
        if (filChauffant != null) {
            filChauffant.mettreAJourDimensions(nouvelleLargeur, nouvelleLongueur);
        }
    }

    /**
     * Crée une pièce en utilisant des intersections du graphe
     * Au minimum 3 intersections non voisines sont requises
     */
    public void creerPieceAvecIntersections(List<String> idsIntersections) {
        if (idsIntersections.size() < 3) {
            throw new IllegalArgumentException("Une pièce doit avoir au minimum 3 intersections");
        }

        // Générer le graphe s'il n'est pas encore généré
        if (!graphe.estGenere()) {
            graphe.genererGraphe();
        }

        // Créer la pièce avec les intersections
        graphe.creerPieceAvecIntersections(idsIntersections);

        // Mettre à jour les points du contour basés sur les intersections actives
        mettreAJourPointsDepuisIntersections();
    }

    /**
     * Met à jour les points du contour basés sur les intersections actives
     */
    private void mettreAJourPointsDepuisIntersections() {
        points.clear();
        for (Graphe.Intersection intersection : graphe.getIntersectionsActives()) {
            points.add(new Point(intersection.getX(), intersection.getY()));
        }
        recalculerContour();
    }

    /**
     * Retourne les IDs des intersections actives de la pièce
     */
    public List<String> getIdsIntersectionsActives() {
        List<String> ids = new ArrayList<>();
        for (Graphe.Intersection intersection : graphe.getIntersectionsActives()) {
            ids.add(intersection.getId());
        }
        return ids;
    }

    /**
     * Vérifie si la pièce est valide (au minimum 3 intersections non voisines)
     */
    public boolean estPieceValideAvecIntersections() {
        return graphe.getNombreIntersectionsActives() >= 3;
    }

    /**
     * Retourne le nombre de points du contour
     */
    public int getNombrePoints() {
        return points.size();
    }

    /**
     * Retourne un point spécifique par son index
     */
    public Point getPoint(int index) {
        if (index >= 0 && index < points.size()) {
            return points.get(index);
        }
        return null;
    }

    /**
     * Vérifie si un point est à l'intérieur de la pièce
     * Utilise Path2D.contains()
     */
    public boolean contientPoint(int x, int y) {
        return contour != null && contour.contains(x, y);
    }

    /**
     * Vérifie si un point (java.awt.Point) est à l'intérieur de la pièce
     */
    public boolean contientPoint(Point point) {
        return contientPoint(point.x, point.y);
    }

    // Les méthodes ajouterMeuble(Meuble) et supprimerMeuble(Meuble) sont maintenant gérées par les méthodes par ID ci-dessus

    // Les méthodes ajouterElementChauffant(ElementChauffant) et supprimerElementChauffant(ElementChauffant) 
    // sont maintenant gérées par les méthodes par ID ci-dessus

    /**
     * @deprecated Utiliser supprimerElementChauffant(int id)
     */
    @Deprecated
    public void supprimerElementChauffant(ElementChauffant chauffage) {
        if (chauffage != null) {
            elementsChauffants.remove(chauffage.getId());
        }
    }

    /**
     * Supprime un élément chauffant par nom et position (pour éviter les boucles dans le contrôleur)
     */
    public void supprimerElementChauffantParNomEtPosition(String nom, int x, int y) {
        elementsChauffants.values().removeIf(element ->
                element.getNom().equals(nom) &&
                        element.getX() == x &&
                        element.getY() == y
        );
    }

    // ==================== ZONES D'INTERDICTION ====================

    public Collection<ZoneInterdiction> getZonesInterdiction() {
        return Collections.unmodifiableCollection(zonesInterdiction.values());
    }

    public int ajouterZoneInterdiction(ZoneInterdiction zone) {
        if (zone == null) {
            throw new IllegalArgumentException("Zone d'interdiction invalide");
        }
        zonesInterdiction.put(zone.getId(), zone);
        return zone.getId();
    }

    public void supprimerZoneInterdiction(int id) {
        zonesInterdiction.remove(id);
    }

    public Optional<ZoneInterdiction> trouverZoneInterdiction(int id) {
        return Optional.ofNullable(zonesInterdiction.get(id));
    }

    /**
     * Déplace une zone d'interdiction par son ID
     */
    public void deplacerZoneInterdiction(int id, Point nouvellePos) {
        ZoneInterdiction zone = zonesInterdiction.get(id);
        if (zone == null) {
            throw new IllegalArgumentException("Zone d'interdiction introuvable: " + id);
        }
        zone.setPosition(nouvellePos);
    }

    /**
     * Redimensionne une zone d'interdiction par son ID
     */
    public void redimensionnerZoneInterdiction(int id, int largeur, int longueur) {
        ZoneInterdiction zone = zonesInterdiction.get(id);
        if (zone == null) {
            throw new IllegalArgumentException("Zone d'interdiction introuvable: " + id);
        }
        zone.setLargeur(largeur);
        zone.setLongueur(longueur);
    }

    /**
     * Vérifie si un point est dans une zone d'interdiction
     */
    public boolean estDansZoneInterdiction(int x, int y) {
        for (ZoneInterdiction zone : zonesInterdiction.values()) {
            if (x >= zone.getPosition().x && x <= zone.getPosition().x + zone.getLargeur() &&
                y >= zone.getPosition().y && y <= zone.getPosition().y + zone.getLongueur()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Vérifie si un segment de ligne intersecte une zone d'interdiction
     */
    public boolean intersecteZoneInterdiction(int x1, int y1, int x2, int y2) {
        for (ZoneInterdiction zone : zonesInterdiction.values()) {
            int zx = zone.getPosition().x;
            int zy = zone.getPosition().y;
            int zw = zone.getLargeur();
            int zh = zone.getLongueur();
            
            // Vérifier si le segment intersecte le rectangle de la zone
            if (segmentIntersecteRectangle(x1, y1, x2, y2, zx, zy, zw, zh)) {
                return true;
            }
        }
        return false;
    }

    private boolean segmentIntersecteRectangle(int x1, int y1, int x2, int y2, 
                                                int rx, int ry, int rw, int rh) {
        // Vérifier si une extrémité est dans le rectangle
        if ((x1 >= rx && x1 <= rx + rw && y1 >= ry && y1 <= ry + rh) ||
            (x2 >= rx && x2 <= rx + rw && y2 >= ry && y2 <= ry + rh)) {
            return true;
        }
        
        // Vérifier l'intersection avec les bords du rectangle
        // Intersection avec le bord gauche
        if (x1 < rx && x2 > rx) {
            double t = (rx - x1) / (double)(x2 - x1);
            double y = y1 + t * (y2 - y1);
            if (y >= ry && y <= ry + rh) return true;
        }
        // Intersection avec le bord droit
        if (x1 > rx + rw && x2 < rx + rw) {
            double t = (rx + rw - x1) / (double)(x2 - x1);
            double y = y1 + t * (y2 - y1);
            if (y >= ry && y <= ry + rh) return true;
        }
        // Intersection avec le bord bas
        if (y1 < ry && y2 > ry) {
            double t = (ry - y1) / (double)(y2 - y1);
            double x = x1 + t * (x2 - x1);
            if (x >= rx && x <= rx + rw) return true;
        }
        // Intersection avec le bord haut
        if (y1 > ry + rh && y2 < ry + rh) {
            double t = (ry + rh - y1) / (double)(y2 - y1);
            double x = x1 + t * (x2 - x1);
            if (x >= rx && x <= rx + rw) return true;
        }
        
        return false;
    }

    // ==================== ZONES TAMPON ====================

    public Collection<ZoneTampon> getZonesTampon() {
        return Collections.unmodifiableCollection(zonesTampon.values());
    }

    public int ajouterZoneTampon(ZoneTampon zone) {
        if (zone == null) {
            throw new IllegalArgumentException("Zone tampon invalide");
        }
        zonesTampon.put(zone.getId(), zone);
        return zone.getId();
    }

    public void supprimerZoneTampon(int id) {
        zonesTampon.remove(id);
    }

    public Optional<ZoneTampon> trouverZoneTampon(int id) {
        return Optional.ofNullable(zonesTampon.get(id));
    }

    /**
     * Déplace une zone tampon par son ID
     */
    public void deplacerZoneTampon(int id, Point nouvellePos) {
        ZoneTampon zone = zonesTampon.get(id);
        if (zone == null) {
            throw new IllegalArgumentException("Zone tampon introuvable: " + id);
        }
        zone.setPosition(nouvellePos);
    }

    /**
     * Redimensionne une zone tampon par son ID
     */
    public void redimensionnerZoneTampon(int id, int largeur, int longueur) {
        ZoneTampon zone = zonesTampon.get(id);
        if (zone == null) {
            throw new IllegalArgumentException("Zone tampon introuvable: " + id);
        }
        zone.setLargeur(largeur);
        zone.setLongueur(longueur);
    }

    /**
     * Retourne la liste des murs formant le périmètre de la pièce.
     * Le dernier segment relie le dernier sommet au premier.
     * @return List<Mur> La liste des murs de la pièce.
     */
    public List<Mur> getMurs() {
        List<Mur> murs = new ArrayList<>();
        int n = points.size();
        if (n < 3) return murs;

        for (int i = 0; i < n; i++) {
            Point p1 = points.get(i);
            Point p2 = points.get((i + 1) % n);
            murs.add(new Mur(p1.x, p1.y, p2.x, p2.y));
        }
        return murs;
    }

//    public boolean estColleAuMur(int posX, int posY, int largeur, int longueur) {
//
//        // Définir les segments de l'élément (côtés du rectangle)
//        Segment coteOuest = new Segment(posX, posY, posX, posY + longueur);
//        Segment coteEst = new Segment(posX + largeur, posY, posX + largeur, posY + longueur);
//        Segment coteSud = new Segment(posX, posY, posX + largeur, posY);
//        Segment coteNord = new Segment(posX, posY + longueur, posX + largeur, posY + longueur);
//
//        List<Segment> mursPiece = this.getSegmentsMurs();
//
//        // Vérifier si n'importe quel côté de l'élément est un sous-segment d'un mur de la pièce
//        for (Segment mur : mursPiece) {
//            if (coteOuest.estSousSegmentDuMur(mur) ||
//                    coteEst.estSousSegmentDuMur(mur) ||
//                    coteSud.estSousSegmentDuMur(mur) ||
//                    coteNord.estSousSegmentDuMur(mur)) {
//                return true;
//            }
//        }
//
//        return false;
//    }


    /**
     * Trouve le mur le plus proche de la position souhaitée par l'utilisateur (xs, ys).
     *
     * @param xs Position X souhaitée de l'élément.
     * @param ys Position Y souhaitée de l'élément.
     * @return MurProcheInfo contenant le Mur le plus proche et le point d'accrochage optimal, ou null si trop loin.
     */
    public MurProcheInfo trouverMurLePlusProche(int xs, int ys) {
        Point pointSouhaite = new Point(xs, ys);
        List<Mur> murs = this.getMurs(); // Doit retourner la liste des murs de la pièce

        double distanceMin = Double.MAX_VALUE;
        Mur murLePlusProche = null;
        Point pointAccrochageFinal = null;

        // Tolérance maximale de magnétisation : si le point est plus loin que cette distance, on ne magnétise pas.
        final int DISTANCE_MAX_MAGNETISATION = 50; // À ajuster selon vos unités (pouces)

        for (Mur mur : murs) {
            // 1. Projeter le point souhaité sur le mur pour trouver le point de contact le plus proche
            // La méthode projeterPointSurMur est dans la classe Mur (implémentée précédemment).
            Point pointProjete = mur.projeterPointSurMur(pointSouhaite);

            // 2. Calculer la distance euclidienne entre la position souhaitée et sa projection sur le mur
            // distance(Point p) est une méthode de la classe java.awt.Point
            double distance = pointSouhaite.distance(pointProjete);

            // 3. Mettre à jour le mur le plus proche
            if (distance < distanceMin) {
                distanceMin = distance;
                murLePlusProche = mur;
                pointAccrochageFinal = pointProjete;
            }
        }

        // 4. Vérification finale de la tolérance
        if (distanceMin > DISTANCE_MAX_MAGNETISATION) {
            return null; // L'élément est trop loin d'un mur pour être magnétisé
        }

        // Le mur le plus proche est trouvé et le point d'accrochage est déterminé
        return new MurProcheInfo(murLePlusProche, pointAccrochageFinal);
    }

    /**
     * Calcule la position (X, Y) du coin inférieur gauche de l'élément chauffant/thermostat
     * après qu'il a été magnétisé (tourné et décalé) sur un mur.
     * * @param mur Mur sur lequel l'élément est ancré.
     * @param pointAccrochage Point sur le mur où l'élément est centré.
     * @param largeur Largeur de l'élément.
     * @param longueur Longueur de l'élément.
     * @param angleDegres Angle de rotation de l'élément (identique à l'angle du mur).
     * @return La position (X, Y) finale du coin inférieur gauche de l'élément.
     */
    public Point calculerPointAncrage(Mur mur, Point pointAccrochage,
                                      int largeur, int longueur, double angleDegres) {

        // --- 1. Décalage pour le centrage ---
        // Le pointAccrochage est le point central de l'élément sur le mur.
        // L'élément a une largeur/longueur, il faut le décaler de son centre.
        // L'élément est considéré comme orienté "normalement" (angle 0) pour ce calcul.
        double cx = pointAccrochage.getX();
        double cy = pointAccrochage.getY();

        // Décalage pour trouver le coin inférieur gauche théorique (si angle = 0)
        // Nous travaillons en coordonnées monde (pouces), pas écran.
        double xDecale = cx - (largeur / 2.0);
        double yDecale = cy - (longueur / 2.0);

        // Si l'angle n'est pas 0 (rotation), le coin inférieur gauche n'est plus (xDecale, yDecale).
        // Nous devons inverser la rotation pour trouver le (X, Y) du coin d'origine.

        // --- 2. Trouver la Distance Maximale au Mur (Profondeur) ---
        // La profondeur est le plus grand rayon de l'élément dans la direction normale au mur.
        // C'est la distance entre le centre de l'élément et le point de l'élément le plus éloigné dans la
        // direction normale au mur, après rotation.

        // Le rayon maximal de l'élément est calculé par la distance du centre aux coins :
        double halfWidth = largeur / 2.0;
        double halfLength = longueur / 2.0;

        // On calcule la moitié du rayon maximal de l'élément après rotation (demi-profondeur)
        // C'est la plus grande projection de la moitié de la largeur ou de la longueur sur l'axe perpendiculaire.
        // L'angle du mur (angleDegres) est l'angle de rotation (alpha).
        double alphaRad = Math.toRadians(angleDegres);

        // Calcul de la demi-largeur effective (projectée sur l'axe Y)
        double effectiveHalfWidth = Math.abs(halfWidth * Math.sin(alphaRad)) +
                Math.abs(halfLength * Math.cos(alphaRad));

        // Calcul de la demi-longueur effective (projectée sur l'axe X)
        double effectiveHalfLength = Math.abs(halfWidth * Math.cos(alphaRad)) +
                Math.abs(halfLength * Math.sin(alphaRad));

        // ATTENTION: Puisque l'élément est pivoté, il n'est plus simplement la moitié de la largeur/longueur.
        // Nous devons trouver le point le plus éloigné du centre dans la direction perpendiculaire au mur.

        // Simplification : Dans la plupart des cas simples (rectangles non très minces),
        // la distance maximale est (largeur/2) ou (longueur/2).
        // Cependant, pour la magnétisation, la distance est simplement la moitié de la dimension la plus petite
        // si l'élément est collé par son côté, ou la moitié de la dimension la plus grande.

        // Pour un élément rectangulaire collé par son côté "largeur" au mur :
        // La distance du centre au mur est (longueur / 2).
        // Pour la suite, nous allons simplement utiliser la dimension qui donne la profondeur de l'élément.
        // Si l'angle est l'angle du mur (longueur perpendiculaire, largeur parallèle),
        // alors la profondeur est `longueur / 2.0`.
        double profondeurElement = longueur / 2.0;

        // --- 3. Vecteur Normal au Mur ---
        // Le mur a un angle alpha. Le vecteur normal est (alpha + 90 degrés).
        double angleNormalRad = Math.toRadians(angleDegres + 90.0);

        // Détermination du vecteur unitaire normal (pointant vers l'intérieur de la pièce)
        // x_normal = cos(alpha + 90)
        // y_normal = sin(alpha + 90)
        double normalX = Math.cos(angleNormalRad);
        double normalY = Math.sin(angleNormalRad);

        // --- 4. Décalage final du Centre (Déplacement vers l'intérieur) ---

        // Décalage pour déplacer le CENTRE de l'élément à l'intérieur de la pièce
        // Le décalage est égal à la moitié de la dimension perpendiculaire (profondeurElement)
        double cxFinal = cx + normalX * profondeurElement;
        double cyFinal = cy + normalY * profondeurElement;

        // --- 5. Trouver le Coin (X, Y) initial de l'élément non tourné ---

        // Nous devons effectuer la translation inverse due à la rotation:
        // Le centre de l'élément est maintenant (cxFinal, cyFinal).
        // Le pointAccrochage est au centre. L'élément est tourné.

        // La position non tourné (X, Y) qui, une fois tournée, aura son centre à (cxFinal, cyFinal) est:
        // (X, Y) = (cxFinal - halfWidth, cyFinal - halfLength)

        // Cependant, si nous voulons que le point (X, Y) soit le coin de la bounding box NON-ROTATIVE,
        // nous devons faire la rotation inverse du point (halfWidth, halfLength) à partir de (cxFinal, cyFinal).

        // Simplification : En Java2D, si on définit l'objet par (X, Y) et on le tourne autour de son centre,
        // l'objet *lui-même* reste défini par (X, Y) dans l'espace non tourné.
        // La position (X, Y) est donc simplement le centre moins la moitié des dimensions.

        // Position du coin inférieur gauche (X, Y) non tourné :
        int xFinal = (int) Math.round(cxFinal - halfWidth);
        int yFinal = (int) Math.round(cyFinal - halfLength);

        // Retourner la position finale (X, Y) du coin de l'élément.
        return new Point(xFinal, yFinal);
    }


    @Override
    public String toString() {
        return String.format("Pièce %d\" x %d\" (%d points) - %s",
                largeur, longueur, points.size(),
                points.size() >= 3 ? "valide" : "invalide");
    }


}
