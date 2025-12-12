package domaine;

import domaine.dto.PieceDTO;
import java.util.Stack;

/**
 * Gestionnaire des opérations d'annulation et de rétablissement
 * Utilise une approche par snapshot (sauvegarde de l'état complet de la pièce)
 */
public class UndoRedo {
    private Stack<PieceDTO> pileUndo;
    private Stack<PieceDTO> pileRedo;
    private static final int LIMITE_MAX = 100; // Limite pour éviter une consommation mémoire excessive

    public UndoRedo() {
        this.pileUndo = new Stack<>();
        this.pileRedo = new Stack<>();
    }

    /**
     * Enregistre un état de la pièce pour pouvoir l'annuler plus tard
     * @param etat L'état de la pièce à sauvegarder (via PieceDTO)
     */
    public void enregistrerEtat(PieceDTO etat) {
        if (etat == null) return;
        
        // Créer une copie profonde de l'état pour éviter les références partagées
        PieceDTO copie = copierEtat(etat);
        
        // Ajouter à la pile undo
        pileUndo.push(copie);
        
        // Limiter la taille de la pile pour éviter une consommation mémoire excessive
        if (pileUndo.size() > LIMITE_MAX) {
            // Retirer le plus ancien état (en bas de la pile)
            Stack<PieceDTO> nouvellePile = new Stack<>();
            for (int i = 1; i < pileUndo.size(); i++) {
                nouvellePile.push(pileUndo.get(i));
            }
            pileUndo = nouvellePile;
        }
        
        // Vider la pile redo quand on fait une nouvelle action
        pileRedo.clear();
    }

    /**
     * Ajoute un état à la pile undo sans vider la pile redo
     * Utilisé lors d'un redo pour pouvoir undo l'état restauré
     * @param etat L'état de la pièce à sauvegarder
     */
    public void ajouterAUndoSansViderRedo(PieceDTO etat) {
        if (etat == null) return;
        
        // Créer une copie profonde de l'état pour éviter les références partagées
        PieceDTO copie = copierEtat(etat);
        
        // Ajouter à la pile undo
        pileUndo.push(copie);
        
        // Limiter la taille de la pile pour éviter une consommation mémoire excessive
        if (pileUndo.size() > LIMITE_MAX) {
            // Retirer le plus ancien état (en bas de la pile)
            Stack<PieceDTO> nouvellePile = new Stack<>();
            for (int i = 1; i < pileUndo.size(); i++) {
                nouvellePile.push(pileUndo.get(i));
            }
            pileUndo = nouvellePile;
        }
        
        // NE PAS vider la pile redo ici (contrairement à enregistrerEtat)
    }

    /**
     * Annule la dernière opération
     * @return L'état précédent de la pièce, ou null si rien à annuler
     */
    public PieceDTO undo() {
        if (pileUndo.isEmpty()) {
            return null;
        }
        
        PieceDTO etatPrecedent = pileUndo.pop();
        return copierEtat(etatPrecedent);
    }

    /**
     * Rétablit la dernière opération annulée
     * @return L'état suivant de la pièce, ou null si rien à rétablir
     */
    public PieceDTO redo() {
        if (pileRedo.isEmpty()) {
            return null;
        }
        
        PieceDTO etatSuivant = pileRedo.pop();
        return copierEtat(etatSuivant);
    }

    /**
     * Enregistre un état dans la pile redo (utilisé lors d'un undo)
     */
    public void enregistrerPourRedo(PieceDTO etat) {
        if (etat == null) return;
        PieceDTO copie = copierEtat(etat);
        pileRedo.push(copie);
        
        // Limiter la taille de la pile redo
        if (pileRedo.size() > LIMITE_MAX) {
            Stack<PieceDTO> nouvellePile = new Stack<>();
            for (int i = 1; i < pileRedo.size(); i++) {
                nouvellePile.push(pileRedo.get(i));
            }
            pileRedo = nouvellePile;
        }
    }

    /**
     * Crée une copie profonde d'un PieceDTO pour éviter les références partagées
     */
    private PieceDTO copierEtat(PieceDTO original) {
        if (original == null) return null;
        
        PieceDTO copie = new PieceDTO();
        copie.setLargeur(original.getLargeur());
        copie.setLongueur(original.getLongueur());
        
        // Copier les meubles (copie profonde)
        if (original.getMeubles() != null) {
            java.util.List<domaine.dto.MeubleDTO> meublesCopies = new java.util.ArrayList<>();
            for (domaine.dto.MeubleDTO m : original.getMeubles()) {
                if (m != null) {
                    meublesCopies.add(copierMeubleDTO(m));
                }
            }
            copie.setMeubles(meublesCopies);
        }
        
        // Copier les éléments chauffants (copie profonde)
        if (original.getElementsChauffants() != null) {
            java.util.List<domaine.dto.ElementChauffantDTO> elementsCopies = new java.util.ArrayList<>();
            for (domaine.dto.ElementChauffantDTO e : original.getElementsChauffants()) {
                if (e != null) {
                    elementsCopies.add(copierElementChauffantDTO(e));
                }
            }
            copie.setElementsChauffants(elementsCopies);
        }
        
        // Copier le fil chauffant (si nécessaire, créer une copie)
        if (original.getFliChauffant() != null) {
            domaine.dto.FliChauffantDTO filCopie = new domaine.dto.FliChauffantDTO();
            filCopie.setNom(original.getFliChauffant().getNom());
            filCopie.setLongueur(original.getFliChauffant().getLongueur());
            filCopie.setDistanceEnroulement(original.getFliChauffant().getDistanceEnroulement());
            filCopie.setActif(original.getFliChauffant().isActif());
            copie.setFliChauffant(filCopie);
        }
        
        return copie;
    }

    /**
     * Crée une copie profonde d'un MeubleDTO
     */
    private domaine.dto.MeubleDTO copierMeubleDTO(domaine.dto.MeubleDTO original) {
        if (original == null) return null;
        
        domaine.dto.MeubleDTO copie = new domaine.dto.MeubleDTO();
        copie.setId(original.getId());
        copie.setNom(original.getNom());
        copie.setX(original.getX());
        copie.setY(original.getY());
        copie.setLargeur(original.getLargeur());
        copie.setLongueur(original.getLongueur());
        copie.setType(original.getType());
        copie.setaDrain(original.isaDrain());
        copie.setDiametreDrain(original.getDiametreDrain());
        
        if (original.getPositionDrain() != null) {
            copie.setPositionDrain(new java.awt.Point(original.getPositionDrain().x, original.getPositionDrain().y));
        }
        
        return copie;
    }

    /**
     * Crée une copie profonde d'un ElementChauffantDTO
     */
    private domaine.dto.ElementChauffantDTO copierElementChauffantDTO(domaine.dto.ElementChauffantDTO original) {
        if (original == null) return null;
        
        domaine.dto.ElementChauffantDTO copie = new domaine.dto.ElementChauffantDTO();
        copie.setId(original.getId());
        copie.setNom(original.getNom());
        copie.setX(original.getX());
        copie.setY(original.getY());
        copie.setLargeur(original.getLargeur());
        copie.setLongueur(original.getLongueur());
        copie.setType(original.getType());
        copie.setActif(original.isActif());
        
        if (original.getPosition() != null) {
            copie.setPosition(new java.awt.Point(original.getPosition().x, original.getPosition().y));
        }
        
        return copie;
    }

    /**
     * Vérifie s'il y a des opérations à annuler
     */
    public boolean peutUndo() {
        return !pileUndo.isEmpty();
    }

    /**
     * Vérifie s'il y a des opérations à rétablir
     */
    public boolean peutRedo() {
        return !pileRedo.isEmpty();
    }

    /**
     * Vide toutes les piles (utile lors de la création d'une nouvelle pièce)
     */
    public void clear() {
        pileUndo.clear();
        pileRedo.clear();
    }
}
