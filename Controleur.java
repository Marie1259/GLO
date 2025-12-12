package domaine;

import domaine.dto.*;
import domaine.meuble.Meuble;
import domaine.meuble.MeubleAvecDrain;
import domaine.meuble.MeubleFactory;
import domaine.piece.Mur;
import domaine.piece.Piece;
import domaine.piece.PieceFactory;
import domaine.chauffage.ElementChauffant;
import domaine.chauffage.Thermostat;
import domaine.chauffage.FilChauffant;
import domaine.dto.ElementChauffantMapper;
import domaine.piece.util.MurProcheInfo;
import domaine.sauvegarde.EtatPiece;
import domaine.zone.ZoneInterdiction;
import domaine.zone.ZoneTampon;
import domaine.chauffage.CalculateurCheminFil;
import domaine.chauffage.CalculateurCheminDijkstra;
import domaine.graphe.Graphe;

import java.awt.Point;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Contrôleur principal pour la gestion de la pièce et de ses éléments.
 * Architecture mince : toute la logique métier est dans le domaine.
 */
public class Controleur {

    private Piece piece;
    private final SelectionService selection;
    private UndoRedo undoRedo;

    public Controleur() {
        this.selection = new SelectionService();
        this.undoRedo = new UndoRedo();
    }
    public Integer trouverObjetId(int x, int y) {
        PieceDTO piece = getPieceCourante();
        if (piece == null) return null;

        // On appelle la vraie pièce dans le domaine
        return this.piece.trouverObjetId(new Point(x, y));
    }
    public Integer getIdElementSelectionne() {
        return selection.getIdSelectionne();
    }



    // ==================== PIECE ====================

    public void creerNouvellePiece(int largeur, int longueur) {
        creerPieceRectangulaire(largeur, longueur);
    }

    public void creerPieceRectangulaire(int largeur, int longueur) {
        this.piece = PieceFactory.creerRectangulaire(largeur, longueur);
        selection.clear();
        undoRedo.clear(); // Vider les piles undo/redo lors de la création d'une nouvelle pièce
        // FilChauffant est créé automatiquement dans Piece (si c'est ton design actuel)
    }

    public void creerPieceIrreguliere(int largeur, int longueur) {
        // Pour compatibilité : crée un rectangle pour l’instant
        creerPieceRectangulaire(largeur, longueur);
    }

    public void creerPieceIrreguliere(List<Point> points) {
        this.piece = PieceFactory.creerIrreguliere(points);
        selection.clear();
        undoRedo.clear(); // Vider les piles undo/redo lors de la création d'une nouvelle pièce
    }

    public void modifierPieceCourante(int nouvelleLargeur, int nouvelleLongueur) {
        requirePiece();
        enregistrerEtatPourUndo();
        piece.modifierDimensions(nouvelleLargeur, nouvelleLongueur);
    }

    /**
     * Modifie un point d'une pièce irrégulière
     * @param index Index du point à modifier
     * @param x Nouvelle coordonnée X
     * @param y Nouvelle coordonnée Y
     * @param enregistrerPourUndo Si true, enregistre l'état pour undo (par défaut false pour le drag)
     */
    public void modifierPointPieceIrreguliere(int index, int x, int y, boolean enregistrerPourUndo) {
        requirePiece();
        if (enregistrerPourUndo) {
            enregistrerEtatPourUndo();
        }
        piece.modifierPoint(index, x, y);
        // Recalculer les dimensions depuis les points
        piece.calculerDimensionsDepuisPoints();
    }

    /**
     * Redimensionne une pièce irrégulière en mettant à l'échelle tous les points proportionnellement
     * @param nouvelleLargeur Nouvelle largeur
     * @param nouvelleLongueur Nouvelle longueur
     * @param enregistrerPourUndo Si true, enregistre l'état pour undo
     */
    public void redimensionnerPieceIrreguliere(int nouvelleLargeur, int nouvelleLongueur, boolean enregistrerPourUndo) {
        requirePiece();
        if (enregistrerPourUndo) {
            enregistrerEtatPourUndo();
        }
        piece.redimensionnerPieceIrreguliere(nouvelleLargeur, nouvelleLongueur);
    }

    public PieceDTO getPieceCourante() {
        return GetPiece();
    }

    public PieceDTO GetPiece() {
        requirePiece();
        return PieceMapper.toDTO(piece);
    }
//
//    /**
//     * Valide la position d'un élément chauffant ou d'un thermostat dans la pièce irrégulière.
//     * @param posX La position X (en pouces) du coin inférieur gauche de l'élément.
//     * @param posY La position Y (en pouces) du coin inférieur gauche de l'élément.
//     * @param largeur La largeur de l'élément.
//     * @param longueur La longueur de l'élément.
//     * @throws IllegalArgumentException si la position est invalide.
//     */
//    public void validerPositionElementChauffant(int posX, int posY, int largeur, int longueur) throws IllegalArgumentException {
//        if (piece == null) {
//            throw new IllegalArgumentException("Veuillez d'abord créer la pièce.");
//        }
//
//        // 1. Vérifier si l'élément est dans les limites de la pièce
//        if (!piece.contientElement(posX, posY, largeur, longueur)) {
//            throw new IllegalArgumentException("L'élément est en dehors des limites de la pièce.");
//        }
//
//        // 2. Vérifier la contrainte de "collé au mur"
//        if (!piece.estColleAuMur(posX, posY, largeur, longueur)) {
//            throw new IllegalArgumentException("Le thermostat et l'élément chauffant doivent être collés à un mur de la pièce.");
//        }
//    }

    // ==================== MEUBLES ====================

    public void modifierDrain(MeubleDTO dto, int drainX, int drainY, int diametre) {
        requirePiece();
        if (dto == null) return;
        
        // Vérifier si le drain change vraiment avant d'enregistrer
        piece.trouverMeuble(dto.getId()).ifPresent(m -> {
            if (m instanceof MeubleAvecDrain d) {
                if (d.getDrainXRelatif() != drainX || d.getDrainYRelatif() != drainY || 
                    d.getDiametreDrain() != diametre) {
                    enregistrerEtatPourUndo();
                }
            }
        });
        
        // Les valeurs passées sont relatives au meuble
        piece.modifierDrainRelatif(dto.getId(), drainX, drainY, diametre);
    }


    /**
     * API générique (L3) : ajouter un meuble sans drain via type.
     */
    public int ajouterMeuble(Point position, int largeur, int longueur, String type) {
        requirePiece();
        Meuble m = MeubleFactory.creerSansDrain(type, position, largeur, longueur);
        int id = piece.ajouterMeuble(m);
        selection.setSelection(id, SelectionService.TypeElement.MEUBLE);
        return id;
    }



    // ---- AJOUT MEUBLES AVEC DRAIN (surcharges avec / sans drain explicite) ----

    public void ajouterDouche(String nom, int x, int y, int largeur, int longueur,int diametre) {
        // version utilisée par DialogConfigurationMeuble actuellement
        ajouterMeubleAvecDrain("douche", nom, x, y, largeur, longueur, null, null,diametre);
    }

    public void ajouterDouche(String nom, int x, int y, int largeur, int longueur, int drainX, int drainY,int diametre) {
        ajouterMeubleAvecDrain("douche", nom, x, y, largeur, longueur, drainX, drainY,diametre);
    }

    public void ajouterBain(String nom, int x, int y, int largeur, int longueur,int diametre) {
        ajouterMeubleAvecDrain("bain", nom, x, y, largeur, longueur, null, null,diametre);
    }

    public void ajouterBain(String nom, int x, int y, int largeur, int longueur, int drainX, int drainY,int diametre) {
        ajouterMeubleAvecDrain("bain", nom, x, y, largeur, longueur, drainX, drainY,diametre);
    }

    public void ajouterToilette(String nom, int x, int y, int largeur, int longueur, int diametre) {
        ajouterMeubleAvecDrain("toilette", nom, x, y, largeur, longueur, null, null, diametre);
    }

    public void ajouterToilette(String nom, int x, int y, int largeur, int longueur, int drainX, int drainY,int diametre) {
        ajouterMeubleAvecDrain("toilette", nom, x, y, largeur, longueur, drainX, drainY,diametre);
    }



    public void ajouterVanite(String nom, int x, int y, int largeur, int longueur, int drainX, int drainY,int diametre) {
        ajouterMeubleAvecDrain("vanite", nom, x, y, largeur, longueur, drainX, drainY,diametre);
    }

    // ---- AJOUT MEUBLES SANS DRAIN ----

    public void ajouterPlacard(String nom, int x, int y, int largeur, int longueur) {
        ajouterMeubleSansDrain("placard", nom, x, y, largeur, longueur);
    }

    public void ajouterArmoire(String nom, int x, int y, int largeur, int longueur) {
        ajouterMeubleSansDrain("armoire", nom, x, y, largeur, longueur);
    }

    // ---- LOGIQUE CENTRALISÉE DE CRÉATION ----

    private void ajouterMeubleAvecDrain(String type, String nom, int x, int y,
                                        int largeur, int longueur,
                                        Integer dx, Integer dy, Integer diametre) {
        requirePiece();
        enregistrerEtatPourUndo();

        Meuble m = MeubleFactory.creerAvecDrain(type, new Point(x, y), largeur, longueur);
        m.setNom(nom);

        if (diametre != null) {
            m.setDiametreDrain(diametre);
        }

        // Si on a reçu une position explicite pour le drain, l'interpréter comme relative
        if (m instanceof MeubleAvecDrain d && dx != null && dy != null) {
            d.setDrainPositionRelative(dx, dy);
        }

        int id = piece.ajouterMeuble(m);
        selection.setSelection(id, SelectionService.TypeElement.MEUBLE);
    }

    private void ajouterMeubleSansDrain(String type, String nom, int x, int y, int largeur, int longueur) {
        requirePiece();
        enregistrerEtatPourUndo();
        
        Meuble m = MeubleFactory.creerSansDrain(type, new Point(x, y), largeur, longueur);
        m.setNom(nom);
        int id = piece.ajouterMeuble(m);
        selection.setSelection(id, SelectionService.TypeElement.MEUBLE);
    }

    /**
     * Ajout à partir d’un DTO (utilisé par d’autres couches éventuellement).
     */
    public void ajouterMeuble(MeubleDTO meubleDTO) {
        requirePiece();
        if (meubleDTO == null) return;

        Meuble m = MeubleFactory.creerDepuisDTO(meubleDTO);
        int id = piece.ajouterMeuble(m);
        selection.setSelection(id, SelectionService.TypeElement.MEUBLE);
    }

    // ==================== DÉPLACEMENT / REDIMENSIONNEMENT ====================


    public void deplacerMeuble(int id, Point p) {

        // — sécurité ID inexistant —
        if (piece.trouverMeuble(id).isEmpty()) {
            selection.clear();
            return;
        }

        try {
            piece.deplacerMeuble(id, p);
        } catch (IllegalArgumentException ex) {
            // collision ou dépassement → on ignore le déplacement
            System.err.println("Déplacement refusé : " + ex.getMessage());
        }
    }


    public String deplacerMeuble(MeubleDTO dto, int x, int y) {
        return deplacerMeuble(dto, x, y, true);
    }

    /**
     * Déplace un meuble avec option d'enregistrer l'état pour undo
     * @param dto Le meuble à déplacer
     * @param x Nouvelle position X
     * @param y Nouvelle position Y
     * @param enregistrerPourUndo Si true, enregistre l'état pour undo (par défaut true)
     * @return Message d'erreur ou null si succès
     */
    public String deplacerMeuble(MeubleDTO dto, int x, int y, boolean enregistrerPourUndo) {
        requirePiece();
        if (dto == null) return "Erreur: meuble invalide";

        Point nouvellePos = new Point(x, y);

        try {
            // Vérifier si la position change vraiment avant d'enregistrer
            if (enregistrerPourUndo) {
                piece.trouverMeuble(dto.getId()).ifPresent(m -> {
                    if (m.getX() != x || m.getY() != y) {
                        enregistrerEtatPourUndo();
                    }
                });
            }
            
            piece.deplacerMeuble(dto.getId(), nouvellePos);
            return null;
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        } catch (java.util.NoSuchElementException e) {
            return "Erreur: meuble introuvable";
        }
    }

    public void deplacerMeubleSelectionne(int x, int y) {
        if (selection.isMeubleSelected()) {
            deplacerMeuble(selection.getSelectedId(), new Point(x, y));
        }
    }

    public void redimensionnerMeuble(int id, int largeur, int longueur) {
        requirePiece();
        piece.redimensionnerMeuble(id, largeur, longueur);
    }

    public void redimensionnerMeuble(MeubleDTO dto, int largeur, int longueur) {
        requirePiece();
        if (dto == null) {
            throw new IllegalArgumentException("Meuble invalide");
        }
        
        // Vérifier si les dimensions changent vraiment avant d'enregistrer
        piece.trouverMeuble(dto.getId()).ifPresent(m -> {
            if (m.getLargeur() != largeur || m.getLongueur() != longueur) {
                enregistrerEtatPourUndo();
            }
        });
        
        piece.redimensionnerMeuble(dto.getId(), largeur, longueur);
    }
    public void modifierDrain(int id, int drainX, int drainY) {
        requirePiece();
        // Ne pas enregistrer pour undo à chaque mouvement (seulement au début du drag)
        piece.trouverMeuble(id).ifPresent(m -> {
            if (m instanceof MeubleAvecDrain d) {
                d.setDrainPosition(drainX, drainY);
            }
        });
    }


    public void modifierMeubleSelectionne(int largeur, int longueur) {
        if (selection.isMeubleSelected()) {
            redimensionnerMeuble(selection.getSelectedId(), largeur, longueur);
        }
    }

    // ==================== SUPPRESSION ====================

    public void supprimerMeuble(int id) {
        requirePiece();
        enregistrerEtatPourUndo();
        
        piece.supprimerMeuble(id);
        selection.clearSiSelectionne(id);
    }

    public void supprimerMeuble(MeubleDTO dto) {
        requirePiece();
        if (dto != null) {
            supprimerMeuble(dto.getId());
        }
    }

    public void supprimerMeubleSelectionne() {
        if (selection.isMeubleSelected()) {
            supprimerMeuble(selection.getSelectedId());
        }
    }

    // ==================== GETTERS MEUBLES ====================

    public MeubleDTO getMeubleSelectionne() {
        return GetMeuble();
    }

    public MeubleDTO GetMeuble() {
        if (selection.isMeubleSelected()) {
            return GetMeubleDTO(selection.getSelectedId());
        }
        return new MeubleDTO();
    }

    public MeubleDTO GetMeubleDTO(int id) {
        requirePiece();
        return piece.trouverMeuble(id)
                .map(MeubleMapper::toDTO)
                .orElseThrow(() -> new IllegalArgumentException("Meuble inconnu: " + id));
    }

    public List<MeubleDTO> GetMeubleDTO() {
        requirePiece();
        return piece.getMeubles().stream()
                .map(MeubleMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<MeubleDTO> getMeubles() {
        return GetMeubleDTO();
    }

    // ==================== SÉLECTION ====================

    public void gererClicSouris(int x, int y) {
        requirePiece();
        selection.gererClicSouris(piece, new Point(x, y));
    }

    public Integer getSelectionId() {
        return selection.getSelectedId();
    }

    public void setSelectionId(Integer id) {
        requirePiece();
        selection.setSelectionAvecDetectionType(piece, id);
    }

    public String getTypeElementSelectionne() {
        if (!selection.hasSelection()) return null;
        requirePiece();

        int id = selection.getSelectedId();

        if (selection.isMeubleSelected()) {
            return piece.trouverMeuble(id)
                    .map(m -> m.getClass().getSimpleName())
                    .orElse(null);
        }

        if (selection.isElementChauffantOuThermostatSelected()) {
            return piece.trouverElementChauffant(id)
                    .map(e -> e.getClass().getSimpleName())
                    .orElse(null);
        }

        return null;
    }

    // ==================== ÉLÉMENTS CHAUFFANTS / THERMOSTAT ====================

    // --- Surcharges AVEC largeur/longueur (Les cibles de la magnétisation) ---

    // Remplacer les anciennes méthodes `ajouterElementChauffant(..., int largeur, int longueur)`
    public int ajouterElementChauffant(String nom, int x, int y, int largeur, int longueur) {
        // La position (x, y) est maintenant la position SOUHAITÉE (pour la magnétisation)
        return ajouterElementMagnetise("ELEMENTCHAUFFANT", nom, x, y, largeur, longueur);
    }

    // Remplacer les anciennes méthodes `ajouterThermostat(..., int largeur, int longueur)`
    public int ajouterThermostat(String nom, int x, int y, int largeur, int longueur) {
        // La position (x, y) est maintenant la position SOUHAITÉE (pour la magnétisation)
        return ajouterElementMagnetise("THERMOSTAT", nom, x, y, largeur, longueur);
    }

    public int ajouterElementChauffant(String nom, int x, int y) {
        // Supposons des dimensions par défaut pour l'élément chauffant standard
        return ajouterElementChauffant(nom, x, y, 2, 10);
    }

    public int ajouterThermostat(String nom, int x, int y) {
        // Supposons des dimensions par défaut pour le thermostat
        return ajouterThermostat(nom, x, y, 6, 8);
    }


    /**
     * Logique centralisée pour magnétiser et ajouter un élément (chauffant ou thermostat)
     * à la position souhaitée (x, y).
     */
    public int ajouterElementMagnetise(String typeElement, String nom, int xSouhaite, int ySouhaite, int largeur, int longueur) {
        requirePiece();
        enregistrerEtatPourUndo(); // Enregistrement avant modification

        int id;
        double angleFinal = 0.0;
        Point positionFinale = new Point(xSouhaite, ySouhaite);

        MurProcheInfo infoMur = piece.trouverMurLePlusProche(xSouhaite, ySouhaite);
        if (infoMur != null) {
            // Mur trouvé, appliquer l'alignement
            Mur murProche = infoMur.getMur();
            Point pointAccrochage = infoMur.getPointAccrochage();

            // A. Calculer l'angle du mur
            angleFinal = murProche.calculerAngleDegres();

            // B. Calculer la position ajustée (le coin inférieur gauche de l'élément)
            // Cette méthode doit calculer le décalage pour que l'élément soit collé au mur
            // et tourné selon angleFinal.
            positionFinale = piece.calculerPointAncrage(
                        murProche,
                        pointAccrochage,
                        largeur,
                        longueur,
                        angleFinal
                );

            }
        // --- 2. Création et Ajout de l'objet de Domaine ---
        if (typeElement.equalsIgnoreCase("thermostat")) {
            Thermostat thermostat = new Thermostat(nom, positionFinale.x, positionFinale.y, largeur, longueur);
            thermostat.setAngle(angleFinal);
            id = piece.ajouterElementChauffant(thermostat);
            selection.setSelection(id, SelectionService.TypeElement.THERMOSTAT);
        } else { // ElementChauffant
            ElementChauffant element = new ElementChauffant(nom, positionFinale.x, positionFinale.y, largeur, longueur);
            element.setAngle(angleFinal);
            id = piece.ajouterElementChauffant(element);
            selection.setSelection(id, SelectionService.TypeElement.ELEMENT_CHAUFFANT);
        }

        return id;
        }


    public void supprimerElementChauffant(ElementChauffantDTO dto) {
        requirePiece();
        if (dto == null) return;

        enregistrerEtatPourUndo();
        
        if (dto.getId() > 0) {
            piece.supprimerElementChauffant(dto.getId());
        } else {
            piece.supprimerElementChauffantParNomEtPosition(dto.getNom(), dto.getX(), dto.getY());
        }
    }

    public void supprimerElementChauffant(int id) {
        requirePiece();
        enregistrerEtatPourUndo();
        
        piece.supprimerElementChauffant(id);
        selection.clearSiSelectionne(id);
    }

    public void supprimerElementSelectionne() {
        if (!selection.hasSelection()) return;

        Integer selId = selection.getSelectedId();
        if (selection.isMeubleSelected()) {
            supprimerMeuble(selId);
        } else if (selection.isElementChauffantOuThermostatSelected()) {
            supprimerElementChauffant(selId);
        }
    }

    public ElementChauffantDTO getElementChauffantSelectionne() {
        return GetElementChauffant();
    }

    public ElementChauffantDTO GetElementChauffant() {
        if (selection.isElementChauffantOuThermostatSelected()) {
            return GetElementChauffantDTO(selection.getSelectedId());
        }
        return new ElementChauffantDTO();
    }

    public ElementChauffantDTO GetElementChauffantDTO(int id) {
        requirePiece();
        return piece.trouverElementChauffant(id)
                .map(ElementChauffantMapper::toDTO)
                .orElseThrow(() -> new IllegalArgumentException("Élément chauffant inconnu: " + id));
    }

    public List<ElementChauffantDTO> getElementsChauffants() {
        requirePiece();
        return piece.getElementsChauffants().stream()
                .map(ElementChauffantMapper::toDTO)
                .collect(Collectors.toList());
    }

    // ==================== VALIDATION ====================

    /**
     * Version qui retourne un message d’erreur (ou null si OK).
     * Utilisée par PanelControlesHeatMyFloor.
     */
    public String validerPositionMeubleAvecMessage(int x, int y, int largeur, int longueur) {
        requirePiece();
        return piece.validerPositionMeubleAvecMessage(x, y, largeur, longueur, null);
    }

    /**
     * Version booléenne legacy (true = OK, false = erreur).
     */
    @Deprecated
    public boolean validerPositionMeuble(int x, int y, int largeur, int longueur) {
        requirePiece();
        return piece.validerPositionMeubleAvecMessage(x, y, largeur, longueur, null) == null;
    }

    public boolean validerPositionElementChauffant(int x, int y) {
        requirePiece();
        return piece.validerPositionElementChauffant(x, y);
    }

    public String validerPositionElementChauffantAvecMessage(int x, int y, int largeur, int longueur) {
        requirePiece();
        return piece.validerPositionElementChauffantAvecMessage(x, y, largeur, longueur);
    }

    public void deplacerElementChauffant(int id, int x, int y) {
        deplacerElementChauffant(id, x, y, true);
    }

    /**
     * Déplace un élément chauffant avec option d'enregistrer l'état pour undo
     * @param id ID de l'élément chauffant
     * @param x Nouvelle position X
     * @param y Nouvelle position Y
     * @param enregistrerPourUndo Si true, enregistre l'état pour undo (par défaut true)
     */
    public void deplacerElementChauffant(int id, int x, int y, boolean enregistrerPourUndo) {
        requirePiece();
        
        // Vérifier si la position change vraiment avant d'enregistrer
        if (enregistrerPourUndo) {
            piece.trouverElementChauffant(id).ifPresent(e -> {
                if (e.getX() != x || e.getY() != y) {
                    enregistrerEtatPourUndo();
                }
            });
        }
        
        piece.deplacerElementChauffant(id, new Point(x, y));
    }

    /**
     * Met à jour l'angle de rotation d'un élément chauffant
     * @param id ID de l'élément chauffant
     * @param angle Nouvel angle en degrés
     */
    public void modifierAngleElementChauffant(int id, double angle) {
        requirePiece();
        piece.trouverElementChauffant(id).ifPresent(e -> {
            e.setAngle(angle);
        });
    }

    /**
     * Met à jour l'angle de rotation d'un meuble
     * @param id ID du meuble
     * @param angle Nouvel angle en degrés
     */
    public void modifierAngleMeuble(int id, double angle) {
        requirePiece();
        piece.trouverMeuble(id).ifPresent(m -> {
            m.setAngle(angle);
        });
    }

    public void redimensionnerElementChauffant(int id, int largeur, int longueur) {
        requirePiece();
        
        // Vérifier si les dimensions changent vraiment avant d'enregistrer
        piece.trouverElementChauffant(id).ifPresent(e -> {
            if (e.getLargeur() != largeur || e.getLongueur() != longueur) {
                enregistrerEtatPourUndo();
            }
        });
        
        piece.redimensionnerElementChauffant(id, largeur, longueur);
    }

    // ==================== FIL CHAUFFANT ====================

    public FliChauffantDTO getFilChauffant() {
        requirePiece();
        FilChauffant fil = piece.getFilChauffant();
        if (fil == null) return null;
        FliChauffantDTO dto = new FliChauffantDTO();
        dto.setNom(fil.getNom());
        // Utiliser longueurSouhaitee si disponible, sinon la longueur calculée
        int longueur = fil.getLongueurSouhaitee() > 0 ? fil.getLongueurSouhaitee() : (int)fil.calculerLongueurTotale();
        dto.setLongueur(longueur);
        dto.setDistanceEnroulement(fil.getDistanceFil());
        dto.setActif(fil.estActif());
        dto.setChemin(fil.getChemin());
        return dto;
    }

    public FliChauffantDTO GetFliChauffant() {
        return getFilChauffant();
    }

    public void activerFilChauffant() {
        requirePiece();
        FilChauffant fil = piece.getFilChauffant();
        if (fil != null) {
            fil.setActif(true);
        }
    }

    public void desactiverFilChauffant() {
        requirePiece();
        FilChauffant fil = piece.getFilChauffant();
        if (fil != null) {
            fil.setActif(false);
        }
    }

    public void mettreAJourDistanceFil(int distance) {
        requirePiece();
        FilChauffant fil = piece.getFilChauffant();
        if (fil != null) {
            fil.setDistanceFil(distance);
        }
    }

    /**
     * Calcule le chemin du fil chauffant
     * @param distanceEntreFils Distance souhaitée entre les fils (en pouces)
     * @param longueurSouhaitee Longueur souhaitée du fil (en pouces)
     * @return true si un chemin a été trouvé, false sinon
     */
    public boolean calculerCheminFil(int distanceEntreFils, int longueurSouhaitee) {
        requirePiece();
        FilChauffant fil = piece.getFilChauffant();
        if (fil == null) {
            return false;
        }
        
        enregistrerEtatPourUndo();
        
        // Mettre à jour la distance et la longueur souhaitée dans le fil
        fil.setDistanceFil(distanceEntreFils);
        fil.setLongueurSouhaitee(longueurSouhaitee);
        
        List<Point> chemin = CalculateurCheminFil.calculerChemin(
            piece, fil, distanceEntreFils, longueurSouhaitee);
        
        if (chemin != null && !chemin.isEmpty()) {
            fil.setChemin(chemin);
            return true;
        }
        
        return false;
    }

    /**
     * Obtient le chemin actuel du fil chauffant
     */
    public List<Point> getCheminFil() {
        requirePiece();
        FilChauffant fil = piece.getFilChauffant();
        if (fil == null) {
            return new ArrayList<>();
        }
        return fil.getChemin();
    }

    /**
     * Met à jour le chemin du fil chauffant (pour modification manuelle)
     */
    public void mettreAJourCheminFil(List<Point> nouveauChemin) {
        requirePiece();
        FilChauffant fil = piece.getFilChauffant();
        if (fil == null) {
            return;
        }
        enregistrerEtatPourUndo();
        fil.setChemin(nouveauChemin);
    }

    /**
     * Recalcule le chemin à partir d'une intersection en utilisant Dijkstra
     */
    public List<Point> recalculerCheminAvecDijkstra(
            domaine.graphe.Graphe.Intersection intersection, List<Point> cheminPartiel, int longueurSouhaitee) {
        requirePiece();
        FilChauffant fil = piece.getFilChauffant();
        if (fil == null) {
            return null;
        }
        
        Graphe graphe = piece.getGraphe();
        if (graphe == null) {
            return null;
        }
        
        if (!graphe.estGenere()) {
            graphe.genererGraphe();
        }
        
        // Utiliser la longueur fournie, ou calculer depuis le fil
        if (longueurSouhaitee <= 0) {
            longueurSouhaitee = (int) fil.calculerLongueurTotale();
            if (longueurSouhaitee == 0) {
                longueurSouhaitee = 1000; // Valeur par défaut
            }
        }
        
        // Récupérer la distance entre les fils pour l'utiliser comme distance minimale aux murs
        int distanceEntreFils = fil.getDistanceFil();
        if (distanceEntreFils <= 0) {
            distanceEntreFils = 6; // Valeur par défaut
        }
        
        return CalculateurCheminDijkstra.recalculerCheminAvecDijkstra(
            piece, graphe, intersection, cheminPartiel, longueurSouhaitee, distanceEntreFils);
    }

    // ==================== GRAPHE / INTERSECTIONS ====================

    public void genererGraphe() {
        requirePiece();
        piece.genererGraphe();
    }

    public void configurerMembrane(int espacement, int translationX, int translationY) {
        requirePiece();
        enregistrerEtatPourUndo();
        Graphe graphe = piece.getGraphe();
        if (graphe != null) {
            graphe.setEspacement(espacement);
            graphe.setTranslationX(translationX);
            graphe.setTranslationY(translationY);
            graphe.setConfirmee(false); // Réinitialiser l'état confirmé pour permettre l'affichage des parties qui dépassent
            graphe.genererGraphe();
        }
    }

    /**
     * Confirme la membrane : garde la position actuelle, complète les intersections manquantes,
     * supprime celles hors de la pièce et celles dans ou trop près des meubles
     * @param espacement L'espacement entre intersections
     * @param translationX La translation X actuelle (sera conservée)
     * @param translationY La translation Y actuelle (sera conservée)
     */
    public void confirmerMembrane(int espacement, int translationX, int translationY) {
        requirePiece();
        enregistrerEtatPourUndo();
        
        Graphe graphe = piece.getGraphe();
        if (graphe == null) return;
        
        // Garder les translations telles quelles
        graphe.setEspacement(espacement);
        graphe.setTranslationX(translationX);
        graphe.setTranslationY(translationY);
        graphe.setConfirmee(true); // Marquer comme confirmée
        graphe.genererGrapheAvecValidation(piece); // Générer avec validation pour supprimer les intersections invalides
    }

    /**
     * Obtient l'espacement entre intersections du graphe
     * @return L'espacement en 32èmes de pouce, ou 3*32 par défaut
     */
    public int getEspacementGraphe() {
        if (piece == null) return 3 * 32;
        Graphe graphe = piece.getGraphe();
        if (graphe == null) return 3 * 32;
        return graphe.getEspacement();
    }
    
    /**
     * Obtient l'espacement entre intersections du graphe en pouces (double)
     * @return L'espacement en pouces, ou 3.0 par défaut
     */
    public double getEspacementGrapheEnPouces() {
        if (piece == null) return 3.0;
        Graphe graphe = piece.getGraphe();
        if (graphe == null) return 3.0;
        return graphe.getEspacementEnPouces();
    }

    /**
     * Obtient le graphe de la pièce
     */
    public Graphe getGraphePiece() {
        if (piece == null) return null;
        return piece.getGraphe();
    }

    /**
     * Obtient la translation X du graphe en 32èmes de pouce
     */
    public int getTranslationXGraphe() {
        if (piece == null) return 0;
        Graphe graphe = piece.getGraphe();
        if (graphe == null) return 0;
        return graphe.getTranslationX();
    }
    
    /**
     * Obtient la translation X du graphe en pouces (double)
     */
    public double getTranslationXGrapheEnPouces() {
        if (piece == null) return 0.0;
        Graphe graphe = piece.getGraphe();
        if (graphe == null) return 0.0;
        return graphe.getTranslationXEnPouces();
    }

    /**
     * Obtient la translation Y du graphe en 32èmes de pouce
     */
    public int getTranslationYGraphe() {
        if (piece == null) return 0;
        Graphe graphe = piece.getGraphe();
        if (graphe == null) return 0;
        return graphe.getTranslationY();
    }
    
    /**
     * Obtient la translation Y du graphe en pouces (double)
     */
    public double getTranslationYGrapheEnPouces() {
        if (piece == null) return 0.0;
        Graphe graphe = piece.getGraphe();
        if (graphe == null) return 0.0;
        return graphe.getTranslationYEnPouces();
    }

    public void activerIntersection(int x, int y) {
        requirePiece();
        piece.activerIntersection(x, y);
    }

    public void desactiverToutesIntersections() {
        requirePiece();
        piece.desactiverToutesIntersections();
    }

    public void creerPieceAvecIntersections(List<String> idsIntersections) {
        requirePiece();
        piece.creerPieceAvecIntersections(idsIntersections);
    }

    public List<String> getIdsIntersectionsActives() {
        requirePiece();
        return piece.getIdsIntersectionsActives();
    }

    public boolean estPieceValideAvecIntersections() {
        requirePiece();
        return piece.estPieceValideAvecIntersections();
    }

    public boolean estPieceValide() {
        return piece != null;
    }

    // ==================== UNDO / REDO ====================

    /**
     * Enregistre l'état actuel de la pièce pour pouvoir l'annuler plus tard
     * Doit être appelé AVANT chaque opération modifiante
     */
    public void enregistrerEtatPourUndo() {
        if (piece == null) return;
        PieceDTO etatActuel = PieceMapper.toDTO(piece);
        undoRedo.enregistrerEtat(etatActuel);
    }

    /**
     * Annule la dernière opération
     * @return true si une opération a été annulée, false sinon
     */
    public boolean Undo() {
        if (!undoRedo.peutUndo()) {
            return false;
        }
        
        // Sauvegarder l'état actuel pour le redo
        if (piece != null) {
            PieceDTO etatActuel = PieceMapper.toDTO(piece);
            undoRedo.enregistrerPourRedo(etatActuel);
        }
        
        // Récupérer l'état précédent
        PieceDTO etatPrecedent = undoRedo.undo();
        if (etatPrecedent != null) {
            restaurerPieceDepuisDTO(etatPrecedent);
            return true;
        }
        return false;
    }

    /**
     * Rétablit la dernière opération annulée
     * @return true si une opération a été rétablie, false sinon
     */
    public boolean Redo() {
        if (!undoRedo.peutRedo()) {
            return false;
        }
        
        // Sauvegarder l'état actuel pour le undo (sans vider la pile redo)
        if (piece != null) {
            PieceDTO etatActuel = PieceMapper.toDTO(piece);
            undoRedo.ajouterAUndoSansViderRedo(etatActuel);
        }
        
        // Récupérer l'état suivant
        PieceDTO etatSuivant = undoRedo.redo();
        if (etatSuivant != null) {
            restaurerPieceDepuisDTO(etatSuivant);
            return true;
        }
        return false;
    }

    /**
     * Restaure une pièce depuis un DTO (utilisé pour undo/redo)
     */
    private void restaurerPieceDepuisDTO(PieceDTO dto) {
        if (dto == null) return;
        
        // Créer une nouvelle pièce avec les dimensions du DTO
        this.piece = PieceFactory.creerRectangulaire(dto.getLargeur(), dto.getLongueur());
        
        // Restaurer le fil chauffant
        if (dto.getFliChauffant() != null) {
            FilChauffant fil = piece.getFilChauffant();
            if (fil != null) {
                fil.setNom(dto.getFliChauffant().getNom());
                fil.setDistanceFil((int) dto.getFliChauffant().getDistanceEnroulement());
                fil.setActif(dto.getFliChauffant().isActif());
            }
        }
        
        // Restaurer les meubles
        if (dto.getMeubles() != null) {
            for (MeubleDTO meubleDTO : dto.getMeubles()) {
                try {
                    Meuble meuble = MeubleMapper.fromDTO(meubleDTO);
                    // Préserver l'ID original et l'angle
                    meuble.setId(meubleDTO.getId());
                    meuble.setAngle(meubleDTO.getAngle());
                    piece.ajouterMeuble(meuble);
                } catch (Exception e) {
                    System.err.println("Erreur lors de la restauration du meuble: " + e.getMessage());
                }
            }
        }
        
        // Restaurer les éléments chauffants
        if (dto.getElementsChauffants() != null) {
            for (ElementChauffantDTO elementDTO : dto.getElementsChauffants()) {
                try {
                    ElementChauffant element;
                    if ("Thermostat".equals(elementDTO.getType())) {
                        element = new Thermostat(
                            elementDTO.getNom(),
                            elementDTO.getX(),
                            elementDTO.getY(),
                            elementDTO.getLargeur(),
                            elementDTO.getLongueur()
                        );
                        if (elementDTO.isActif()) {
                            ((Thermostat) element).setEnMarche(true);
                        }
                    } else {
                        element = new ElementChauffant(
                            elementDTO.getNom(),
                            elementDTO.getX(),
                            elementDTO.getY(),
                            elementDTO.getLargeur(),
                            elementDTO.getLongueur()
                        );
                    }
                    // Préserver l'ID original et l'angle
                    element.setId(elementDTO.getId());
                    element.setAngle(elementDTO.getAngle());
                    piece.ajouterElementChauffant(element);
                } catch (Exception e) {
                    System.err.println("Erreur lors de la restauration de l'élément chauffant: " + e.getMessage());
                }
            }
        }
        
        // Effacer la sélection après restauration
        selection.clear();
    }

    /**
     * Vérifie s'il y a des opérations à annuler
     */
    public boolean peutUndo() {
        return undoRedo.peutUndo();
    }

    /**
     * Vérifie s'il y a des opérations à rétablir
     */
    public boolean peutRedo() {
        return undoRedo.peutRedo();
    }

    // ==================== SAUVEGARDE / CHARGEMENT ====================

    /**
     * Sauvegarde l'état actuel de la pièce dans un fichier
     * @param fichier Le fichier où sauvegarder
     * @throws IOException Si une erreur d'écriture survient
     */
    public void sauvegarder(File fichier) throws IOException {
        requirePiece();
        
        EtatPiece etat = convertirPieceEnEtat();
        
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fichier))) {
            oos.writeObject(etat);
        }
    }

    /**
     * Charge une pièce depuis un fichier
     * @param fichier Le fichier à charger
     * @throws IOException Si une erreur de lecture survient
     * @throws ClassNotFoundException Si la classe n'est pas trouvée
     */
    public void charger(File fichier) throws IOException, ClassNotFoundException {
        EtatPiece etat;
        
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fichier))) {
            etat = (EtatPiece) ois.readObject();
        }
        
        restaurerPieceDepuisEtat(etat);
        selection.clear();
        undoRedo.clear();
    }

    /**
     * Convertit la pièce actuelle en état sérialisable
     */
    private EtatPiece convertirPieceEnEtat() {
        EtatPiece etat = new EtatPiece();
        etat.setLargeur(piece.getLargeur());
        etat.setLongueur(piece.getLongueur());
        etat.setPoints(new java.util.ArrayList<>(piece.getPoints()));
        
        // Convertir les meubles
        List<EtatPiece.EtatMeuble> meublesEtat = new java.util.ArrayList<>();
        for (Meuble m : piece.getMeubles()) {
            EtatPiece.EtatMeuble em = new EtatPiece.EtatMeuble();
            em.setId(m.getId());
            em.setType(m.getClass().getSimpleName());
            em.setNom(m.getNom());
            em.setX(m.getX());
            em.setY(m.getY());
            em.setLargeur(m.getLargeur());
            em.setLongueur(m.getLongueur());
            em.setAngle(m.getAngle());
            
            if (m instanceof MeubleAvecDrain d) {
                em.setDrainX(d.getDrainXRelatif());
                em.setDrainY(d.getDrainYRelatif());
                em.setDiametreDrain(d.getDiametreDrain());
            }
            
            meublesEtat.add(em);
        }
        etat.setMeubles(meublesEtat);
        
        // Convertir les éléments chauffants
        List<EtatPiece.EtatElementChauffant> elementsEtat = new java.util.ArrayList<>();
        for (ElementChauffant e : piece.getElementsChauffants()) {
            EtatPiece.EtatElementChauffant ee = new EtatPiece.EtatElementChauffant();
            ee.setId(e.getId());
            ee.setType(e.getClass().getSimpleName());
            ee.setNom(e.getNom());
            ee.setX(e.getX());
            ee.setY(e.getY());
            ee.setLargeur(e.getLargeur());
            ee.setLongueur(e.getLongueur());
            ee.setAngle(e.getAngle());
            ee.setActif(e.isActif());
            elementsEtat.add(ee);
        }
        etat.setElementsChauffants(elementsEtat);
        
        // Convertir les zones d'interdiction
        List<EtatPiece.EtatZone> zonesInterdictionEtat = new java.util.ArrayList<>();
        for (ZoneInterdiction z : piece.getZonesInterdiction()) {
            EtatPiece.EtatZone ez = new EtatPiece.EtatZone();
            ez.setId(z.getId());
            ez.setType("ZoneInterdiction");
            ez.setNom(z.getNom());
            ez.setX(z.getPosition().x);
            ez.setY(z.getPosition().y);
            ez.setLargeur(z.getLargeur());
            ez.setLongueur(z.getLongueur());
            zonesInterdictionEtat.add(ez);
        }
        etat.setZonesInterdiction(zonesInterdictionEtat);
        
        // Convertir les zones tampon
        List<EtatPiece.EtatZone> zonesTamponEtat = new java.util.ArrayList<>();
        for (ZoneTampon z : piece.getZonesTampon()) {
            EtatPiece.EtatZone ez = new EtatPiece.EtatZone();
            ez.setId(z.getId());
            ez.setType("ZoneTampon");
            ez.setNom(z.getNom());
            ez.setX(z.getPosition().x);
            ez.setY(z.getPosition().y);
            ez.setLargeur(z.getLargeur());
            ez.setLongueur(z.getLongueur());
            ez.setDistance(z.getDistance());
            zonesTamponEtat.add(ez);
        }
        etat.setZonesTampon(zonesTamponEtat);
        
        // Convertir le fil chauffant
        FilChauffant fil = piece.getFilChauffant();
        if (fil != null) {
            EtatPiece.EtatFilChauffant ef = new EtatPiece.EtatFilChauffant();
            ef.setNom(fil.getNom());
            ef.setLargeur(fil.getLargeur());
            ef.setLongueur(fil.getLongueur());
            ef.setDistanceFil(fil.getDistanceFil());
            ef.setActif(fil.estActif());
            // TODO: Sauvegarder le chemin du fil quand il sera implémenté
            etat.setFilChauffant(ef);
        }
        
        return etat;
    }

    /**
     * Restaure une pièce depuis un état sérialisé
     */
    private void restaurerPieceDepuisEtat(EtatPiece etat) {
        // Créer la pièce
        if (etat.getPoints() != null && etat.getPoints().size() >= 3) {
            this.piece = PieceFactory.creerIrreguliere(etat.getPoints());
        } else {
            this.piece = PieceFactory.creerRectangulaire(etat.getLargeur(), etat.getLongueur());
        }
        
        // Restaurer les meubles
        if (etat.getMeubles() != null) {
            for (EtatPiece.EtatMeuble em : etat.getMeubles()) {
                try {
                    Meuble m = MeubleFactory.creerDepuisType(em.getType(), 
                        new Point(em.getX(), em.getY()), em.getLargeur(), em.getLongueur());
                    m.setId(em.getId());
                    m.setNom(em.getNom());
                    m.setAngle(em.getAngle());
                    
                    if (m instanceof MeubleAvecDrain d && em.getDrainX() != null && em.getDrainY() != null) {
                        d.setDrainPositionRelative(em.getDrainX(), em.getDrainY());
                        if (em.getDiametreDrain() != null) {
                            d.setDiametreDrain(em.getDiametreDrain());
                        }
                    }
                    
                    piece.ajouterMeuble(m);
                } catch (Exception e) {
                    System.err.println("Erreur lors de la restauration du meuble: " + e.getMessage());
                }
            }
        }
        
        // Restaurer les éléments chauffants
        if (etat.getElementsChauffants() != null) {
            for (EtatPiece.EtatElementChauffant ee : etat.getElementsChauffants()) {
                try {
                    ElementChauffant element;
                    if ("Thermostat".equals(ee.getType())) {
                        element = new Thermostat(ee.getNom(), ee.getX(), ee.getY(), 
                            ee.getLargeur(), ee.getLongueur());
                    } else {
                        element = new ElementChauffant(ee.getNom(), ee.getX(), ee.getY(), 
                            ee.getLargeur(), ee.getLongueur());
                    }
                    element.setId(ee.getId());
                    element.setAngle(ee.getAngle());
                    element.setActif(ee.isActif());
                    piece.ajouterElementChauffant(element);
                } catch (Exception e) {
                    System.err.println("Erreur lors de la restauration de l'élément chauffant: " + e.getMessage());
                }
            }
        }
        
        // Restaurer les zones d'interdiction
        if (etat.getZonesInterdiction() != null) {
            for (EtatPiece.EtatZone ez : etat.getZonesInterdiction()) {
                try {
                    ZoneInterdiction zone = new ZoneInterdiction(ez.getX(), ez.getY(), 
                        ez.getLargeur(), ez.getLongueur(), ez.getNom());
                    piece.ajouterZoneInterdiction(zone);
                } catch (Exception e) {
                    System.err.println("Erreur lors de la restauration de la zone d'interdiction: " + e.getMessage());
                }
            }
        }
        
        // Restaurer les zones tampon
        if (etat.getZonesTampon() != null) {
            for (EtatPiece.EtatZone ez : etat.getZonesTampon()) {
                try {
                    ZoneTampon zone = new ZoneTampon(ez.getX(), ez.getY(), 
                        ez.getLargeur(), ez.getLongueur(), 
                        ez.getDistance() != null ? ez.getDistance() : 0.0, 
                        ez.getNom());
                    piece.ajouterZoneTampon(zone);
                } catch (Exception e) {
                    System.err.println("Erreur lors de la restauration de la zone tampon: " + e.getMessage());
                }
            }
        }
        
        // Restaurer le fil chauffant
        if (etat.getFilChauffant() != null) {
            EtatPiece.EtatFilChauffant ef = etat.getFilChauffant();
            FilChauffant fil = piece.getFilChauffant();
            if (fil != null) {
                fil.setNom(ef.getNom());
                fil.setDistanceFil(ef.getDistanceFil());
                fil.setActif(ef.isActif());
                // TODO: Restaurer le chemin du fil quand il sera implémenté
            }
        }
    }

    // ==================== ZONES ====================

    public int ajouterZoneInterdiction(String nom, int x, int y, int largeur, int longueur) {
        requirePiece();
        enregistrerEtatPourUndo();
        ZoneInterdiction zone = new ZoneInterdiction(x, y, largeur, longueur, nom);
        return piece.ajouterZoneInterdiction(zone);
    }

    public int ajouterZoneTampon(String nom, int x, int y, int largeur, int longueur, double distance) {
        requirePiece();
        enregistrerEtatPourUndo();
        ZoneTampon zone = new ZoneTampon(x, y, largeur, longueur, distance, nom);
        return piece.ajouterZoneTampon(zone);
    }

    public void supprimerZoneInterdiction(int id) {
        requirePiece();
        enregistrerEtatPourUndo();
        piece.supprimerZoneInterdiction(id);
        selection.clearSiSelectionne(id);
    }

    public void supprimerZoneTampon(int id) {
        requirePiece();
        enregistrerEtatPourUndo();
        piece.supprimerZoneTampon(id);
        selection.clearSiSelectionne(id);
    }

    // ==================== GETTERS ZONES ====================

    public ZoneInterdictionDTO getZoneInterdictionSelectionnee() {
        if (selection.isZoneInterdictionSelected()) {
            return getZoneInterdictionDTO(selection.getSelectedId());
        }
        return null;
    }

    public ZoneTamponDTO getZoneTamponSelectionnee() {
        if (selection.isZoneTamponSelected()) {
            return getZoneTamponDTO(selection.getSelectedId());
        }
        return null;
    }

    private ZoneInterdictionDTO getZoneInterdictionDTO(int id) {
        requirePiece();
        return piece.trouverZoneInterdiction(id)
                .map(z -> {
                    ZoneInterdictionDTO dto = new ZoneInterdictionDTO();
                    dto.setId(z.getId());
                    java.awt.Point pos = z.getPosition();
                    dto.setPosition(new java.awt.Point(pos.x, pos.y));
                    dto.setLargeur(z.getLargeur());
                    dto.setLongueur(z.getLongueur());
                    dto.setNom(z.getNom());
                    return dto;
                })
                .orElse(null);
    }

    private ZoneTamponDTO getZoneTamponDTO(int id) {
        requirePiece();
        return piece.trouverZoneTampon(id)
                .map(z -> {
                    ZoneTamponDTO dto = new ZoneTamponDTO();
                    dto.setId(z.getId());
                    java.awt.Point pos = z.getPosition();
                    dto.setPosition(new java.awt.Point(pos.x, pos.y));
                    dto.setLargeur(z.getLargeur());
                    dto.setLongueur(z.getLongueur());
                    dto.setDistance(z.getDistance());
                    dto.setNom(z.getNom());
                    return dto;
                })
                .orElse(null);
    }

    // ==================== MODIFICATION ZONES ====================

    public void deplacerZoneInterdiction(int id, int x, int y) {
        requirePiece();
        enregistrerEtatPourUndo();
        piece.deplacerZoneInterdiction(id, new Point(x, y));
    }

    public void deplacerZoneTampon(int id, int x, int y) {
        requirePiece();
        enregistrerEtatPourUndo();
        piece.deplacerZoneTampon(id, new Point(x, y));
    }

    public void redimensionnerZoneInterdiction(int id, int largeur, int longueur) {
        requirePiece();
        enregistrerEtatPourUndo();
        piece.redimensionnerZoneInterdiction(id, largeur, longueur);
    }

    public void redimensionnerZoneTampon(int id, int largeur, int longueur) {
        requirePiece();
        enregistrerEtatPourUndo();
        piece.redimensionnerZoneTampon(id, largeur, longueur);
    }

    public void deplacerZoneSelectionnee(int x, int y) {
        if (selection.isZoneInterdictionSelected()) {
            deplacerZoneInterdiction(selection.getSelectedId(), x, y);
        } else if (selection.isZoneTamponSelected()) {
            deplacerZoneTampon(selection.getSelectedId(), x, y);
        }
    }

    public void redimensionnerZoneSelectionnee(int largeur, int longueur) {
        if (selection.isZoneInterdictionSelected()) {
            redimensionnerZoneInterdiction(selection.getSelectedId(), largeur, longueur);
        } else if (selection.isZoneTamponSelected()) {
            redimensionnerZoneTampon(selection.getSelectedId(), largeur, longueur);
        }
    }

    public void supprimerZoneSelectionnee() {
        if (selection.isZoneInterdictionSelected()) {
            supprimerZoneInterdiction(selection.getSelectedId());
        } else if (selection.isZoneTamponSelected()) {
            supprimerZoneTampon(selection.getSelectedId());
        }
    }

    // ==================== HELPER ====================

    private void requirePiece() {
        if (piece == null) {
            throw new IllegalStateException("Aucune pièce courante. Créez d'abord la pièce.");
        }
    }
}
