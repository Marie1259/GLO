package domaine.dto;

import domaine.chauffage.ElementChauffant;
import java.awt.Point;

/**
 * Mapper pour convertir ElementChauffant en ElementChauffantDTO
 */
public final class ElementChauffantMapper {
    private ElementChauffantMapper() {}

    public static ElementChauffantDTO toDTO(ElementChauffant e) {
        if (e == null) {
            return null;
        }

        ElementChauffantDTO dto = new ElementChauffantDTO();
        dto.setId(e.getId());
        dto.setNom(e.getNom());
        dto.setPosition(new Point(e.getX(), e.getY()));
        dto.setLargeur(e.getLargeur());
        dto.setLongueur(e.getLongueur());
        dto.setType(e.getClass().getSimpleName());
        dto.setAngle(e.getAngle());

        // Pour Thermostat
        if (e instanceof domaine.chauffage.Thermostat) {
            domaine.chauffage.Thermostat t = (domaine.chauffage.Thermostat) e;
            dto.setActif(t.estEnMarche());
        }

        return dto;
    }
}

