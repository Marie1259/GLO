package domaine.dto;

import domaine.meuble.Meuble;
import domaine.meuble.MeubleAvecDrain;
import domaine.meuble.MeubleFactory;

import java.awt.Point;

/**
 * Mapper pour convertir Meuble <-> MeubleDTO
 */
public final class MeubleMapper {

    private MeubleMapper() {}

    /**
     * Convertit un Meuble en MeubleDTO
     */
    public static MeubleDTO toDTO(Meuble m) {
        if (m == null) return null;

        MeubleDTO dto = new MeubleDTO();

        // --- attributs de base ---
        dto.setId(m.getId());
        dto.setNom(m.getNom());
        dto.setLargeur(m.getLargeur());
        dto.setLongueur(m.getLongueur());
        dto.setType(m.getType());
        dto.setX(m.getX());
        dto.setY(m.getY());
        dto.setAngle(m.getAngle());

        // info drain (booléen)
        dto.setaDrain(m.aDrain());

        // --- si meuble avec drain, remplir les infos drain ---
        if (m instanceof MeubleAvecDrain) {
            MeubleAvecDrain d = (MeubleAvecDrain) m;

            // position absolue du drain
            Point posDrain = new Point(d.getDrainX(), d.getDrainY());
            dto.setPositionDrain(posDrain);

            // diamètre
            dto.setDiametreDrain(d.getDiametreDrain());
        }

        return dto;
    }

    /**
     * Convertit un MeubleDTO en Meuble (utilise la factory)
     */
    public static Meuble fromDTO(MeubleDTO dto) {
        if (dto == null) return null;

        // La factory gère la création selon type + aDrain
        Meuble m = MeubleFactory.creerDepuisDTO(dto);

        // Restaurer l'angle
        m.setAngle(dto.getAngle());

        // Si c'est un meuble avec drain, on applique diamètre et position
        if (m instanceof MeubleAvecDrain) {
            MeubleAvecDrain d = (MeubleAvecDrain) m;

            d.setDiametreDrain(dto.getDiametreDrain());

            if (dto.getPositionDrain() != null) {
                Point p = dto.getPositionDrain();
                d.setDrainPosition(p.x, p.y);
            }
        }

        return m;
    }
}
