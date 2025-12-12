package domaine.meuble;

import domaine.Ids;
import domaine.dto.MeubleDTO;
import java.awt.Point;
import java.text.Normalizer;

/**
 * Fabrique pour créer des meubles avec IDs automatiques
 */
public final class MeubleFactory {
    private MeubleFactory() {}
    
    /**
     * Normalise une chaîne en enlevant les accents et en la mettant en minuscules
     * Utile pour comparer des types de meubles qui peuvent avoir des accents
     */
    private static String normaliserType(String type) {
        if (type == null) return null;
        // Enlever les accents (décomposer puis filtrer les diacritiques)
        String normalise = Normalizer.normalize(type, Normalizer.Form.NFD);
        String sansAccents = normalise.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return sansAccents.toLowerCase();
    }

    /**
     * Crée un meuble sans drain
     * Types: placard, armoire
     *
     * Note: Le nom est initialisé à "" car il sera défini par le contrôleur via setNom()
     * après la création du meuble.
     *
     * @param type Le type de meuble (placard, armoire)
     * @param pos La position du meuble
     * @param largeur La largeur en pouces
     * @param longueur La longueur en pouces
     * @return Le meuble créé avec un ID automatique
     * @throws IllegalArgumentException si le type n'est pas reconnu
     */
    public static Meuble creerSansDrain(String type, Point pos, int largeur, int longueur) {
        Meuble m;
        String typeNormalise = normaliserType(type);

        switch (typeNormalise) {
            case "placard" -> m = new Placard("", pos.x, pos.y, largeur, longueur);
            case "armoire" -> m = new Armoire("", pos.x, pos.y, largeur, longueur);
            default -> throw new IllegalArgumentException("Type de meuble sans drain inconnu: " + type);
        }

        m.setId(Ids.next());
        return m;
    }

    /**
     * Crée un meuble avec drain
     * Types: douche, bain, toilette, vanité
     *
     * Note: Le nom est initialisé à "" car il sera défini par le contrôleur via setNom()
     * après la création du meuble.
     *
     * @param type Le type de meuble (douche, bain, toilette, vanite)
     * @param pos La position du meuble
     * @param largeur La largeur en pouces
     * @param longueur La longueur en pouces
     * @return Le meuble créé avec un ID automatique
     * @throws IllegalArgumentException si le type n'est pas reconnu
     */
    public static Meuble creerAvecDrain(String type, Point pos, int largeur, int longueur) {
        Meuble m;
        String typeNormalise = normaliserType(type);

        switch (typeNormalise) {
            case "douche" -> m = new Douche("", pos.x, pos.y, largeur, longueur);
            case "bain" -> m = new Bain("", pos.x, pos.y, largeur, longueur);
            case "toilette" -> m = new Toilette("", pos.x, pos.y, largeur, longueur);
            case "vanite" -> m = new Vanite("", pos.x, pos.y, largeur, longueur);
            default -> throw new IllegalArgumentException("Type de meuble avec drain inconnu: " + type);
        }

        m.setId(Ids.next());
        return m;
    }

    /**
     * Crée un meuble à partir d'un DTO
     * Cette méthode détermine automatiquement si le meuble a un drain ou non
     * et appelle la méthode appropriée de la factory.
     *
     * @param dto Le DTO contenant les données du meuble
     * @return Le meuble créé avec un ID automatique
     * @throws IllegalArgumentException si le DTO est null ou si le type n'est pas reconnu
     */
    public static Meuble creerDepuisDTO(MeubleDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("DTO ne peut pas être null");
        }

        Point position = new Point(dto.getX(), dto.getY());


        Meuble m;
        if (dto.isaDrain()) {
            m = creerAvecDrain(dto.getType(), position, dto.getLargeur(), dto.getLongueur());
        } else {
            m = creerSansDrain(dto.getType(), position, dto.getLargeur(), dto.getLongueur());
        }

        // Définir le nom et l'angle depuis le DTO
        m.setAngle(dto.getAngle());
        m.setNom(dto.getNom());

        // Si c'est un meuble avec drain et que le DTO a une position de drain, la définir
        if (dto.isaDrain() && m instanceof MeubleAvecDrain mDrain && dto.getPositionDrain() != null) {
            mDrain.setDrainPosition(dto.getDrainX(), dto.getDrainY());
        }

        return m;
    }

    /**
     * Crée un meuble à partir d'un type (nom de classe)
     * Utilisé pour la restauration depuis un état sauvegardé
     * @param type Le nom de la classe (ex: "Douche", "Placard")
     * @param pos La position du meuble
     * @param largeur La largeur en pouces
     * @param longueur La longueur en pouces
     * @return Le meuble créé
     */
    public static Meuble creerDepuisType(String type, Point pos, int largeur, int longueur) {
        String typeNormalise = normaliserType(type);
        
        // Meubles avec drain
        if (typeNormalise.equals("douche") || typeNormalise.equals("bain") || 
            typeNormalise.equals("toilette") || typeNormalise.equals("vanite")) {
            return creerAvecDrain(typeNormalise, pos, largeur, longueur);
        }
        
        // Meubles sans drain
        if (typeNormalise.equals("placard") || typeNormalise.equals("armoire")) {
            return creerSansDrain(typeNormalise, pos, largeur, longueur);
        }
        
        throw new IllegalArgumentException("Type de meuble inconnu: " + type);
    }
}

