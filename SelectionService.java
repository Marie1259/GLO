package domaine;

import domaine.piece.Piece;
import java.awt.Point;

/**
 * Ce service gère l'objet sélectionné via un id (pas d'objets de domaine côté UI).
 * Amélioré pour gérer différents types d'éléments sélectionnables.
 */
public final class SelectionService {
    private Integer selectedId;  // null = rien
    private TypeElement typeElement; // Type d'élément sélectionné

    public enum TypeElement {
        MEUBLE, ELEMENT_CHAUFFANT, THERMOSTAT, ZONE_INTERDICTION, ZONE_TAMPON
    }

    public Integer getSelectedId() {
        return selectedId;
    }

    public TypeElement getTypeElement() {
        return typeElement;
    }

    public void clear() {
        selectedId = null;
        typeElement = null;
    }

    public void setSelectedId(Integer id) {
        selectedId = id;
        // Type sera déterminé automatiquement par le contrôleur selon le contexte
    }
    public Integer getIdSelectionne() {
        return selectedId;
    }


    public void setSelection(Integer id, TypeElement type) {
        selectedId = id;
        typeElement = type;
    }

    // ==================== MÉTHODES UTILITAIRES ====================

    /**
     * Vérifie s'il y a une sélection active
     */
    public boolean hasSelection() {
        return selectedId != null;
    }

    /**
     * Vérifie si un meuble est sélectionné
     */
    public boolean isMeubleSelected() {
        return hasSelection() && typeElement == TypeElement.MEUBLE;
    }

    /**
     * Vérifie si un élément chauffant est sélectionné
     */
    public boolean isElementChauffantSelected() {
        return hasSelection() && typeElement == TypeElement.ELEMENT_CHAUFFANT;
    }

    /**
     * Vérifie si un thermostat est sélectionné
     */
    public boolean isThermostatSelected() {
        return hasSelection() && typeElement == TypeElement.THERMOSTAT;
    }

    /**
     * Vérifie si un élément chauffant (thermostat ou élément chauffant) est sélectionné
     */
    public boolean isElementChauffantOuThermostatSelected() {
        return hasSelection() && (typeElement == TypeElement.ELEMENT_CHAUFFANT ||
                typeElement == TypeElement.THERMOSTAT);
    }

    /**
     * Vérifie si une zone d'interdiction est sélectionnée
     */
    public boolean isZoneInterdictionSelected() {
        return hasSelection() && typeElement == TypeElement.ZONE_INTERDICTION;
    }

    /**
     * Vérifie si une zone tampon est sélectionnée
     */
    public boolean isZoneTamponSelected() {
        return hasSelection() && typeElement == TypeElement.ZONE_TAMPON;
    }

    /**
     * Vérifie si une zone (interdiction ou tampon) est sélectionnée
     */
    public boolean isZoneSelected() {
        return hasSelection() && (typeElement == TypeElement.ZONE_INTERDICTION ||
                typeElement == TypeElement.ZONE_TAMPON);
    }

    /**
     * Vérifie si l'ID donné correspond à la sélection actuelle
     */
    public boolean isSelected(int id) {
        return hasSelection() && selectedId.equals(id);
    }

    /**
     * Vérifie si l'ID donné correspond à la sélection actuelle et est du type spécifié
     */
    public boolean isSelected(int id, TypeElement type) {
        return isSelected(id) && typeElement == type;
    }

    /**
     * Définit la sélection en détectant automatiquement le type depuis la pièce
     * @param piece La pièce pour déterminer le type de l'élément
     * @param id L'ID de l'élément à sélectionner
     */
    public void setSelectionAvecDetectionType(Piece piece, Integer id) {
        if (id == null) {
            clear();
            return;
        }
        if (piece == null) {
            clear();
            return;
        }
        TypeElement type = piece.trouverTypeElement(id);
        if (type != null) {
            setSelection(id, type);
        } else {
            clear();
        }
    }

    /**
     * Gère la sélection depuis un clic de souris (délègue à Piece pour trouver l'objet)
     * @param piece La pièce pour trouver l'objet au point donné
     * @param point Le point du clic
     */
    public void gererClicSouris(Piece piece, Point point) {
        if (piece == null || point == null) {
            clear();
            return;
        }
        Integer idTrouve = piece.trouverObjetId(point);
        if (idTrouve != null) {
            TypeElement type = piece.trouverTypeElement(idTrouve);
            setSelection(idTrouve, type);
        } else {
            clear();
        }
    }

    /**
     * Nettoie la sélection si l'ID donné correspond à la sélection actuelle
     */
    public void clearSiSelectionne(int id) {
        if (isSelected(id)) {
            clear();
        }
    }
}

