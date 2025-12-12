package domaine.dto;

import domaine.piece.Piece;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper pour convertir Piece en PieceDTO
 */
public final class PieceMapper {
    private PieceMapper() {}

    public static PieceDTO toDTO(Piece p) {
        if (p == null) {
            return null;
        }

        PieceDTO dto = new PieceDTO();
        dto.setLargeur(p.getLargeur());
        dto.setLongueur(p.getLongueur());

        // Convertir les points du contour
        dto.setPoints(p.getPoints());

        // Convertir les meubles en DTOs
        List<MeubleDTO> meubles = p.getMeubles().stream()
                .map(MeubleMapper::toDTO)
                .collect(Collectors.toList());
        dto.setMeubles(meubles);

        // Convertir les éléments chauffants en DTOs
        List<ElementChauffantDTO> elementsChauffants = p.getElementsChauffants().stream()
                .map(ElementChauffantMapper::toDTO)
                .collect(Collectors.toList());
        dto.setElementsChauffants(elementsChauffants);

        // Convertir le fil chauffant
        FliChauffantDTO filChauffantDTO = null;
        if (p.getFilChauffant() != null) {
            filChauffantDTO = new FliChauffantDTO();
            filChauffantDTO.setNom(p.getFilChauffant().getNom());
            // Utiliser la longueur souhaitée si disponible, sinon la longueur de la pièce
            int longueur = p.getFilChauffant().getLongueurSouhaitee();
            if (longueur == 0) {
                longueur = p.getFilChauffant().getLongueur();
            }
            filChauffantDTO.setLongueur(longueur);
            filChauffantDTO.setDistanceEnroulement(p.getFilChauffant().getDistanceFil());
            filChauffantDTO.setActif(p.getFilChauffant().estActif());
            filChauffantDTO.setChemin(p.getFilChauffant().getChemin());
        }
        dto.setFliChauffant(filChauffantDTO);

        // Convertir les zones d'interdiction
        List<ZoneInterdictionDTO> zonesInterdiction = p.getZonesInterdiction().stream()
                .map(z -> {
                    ZoneInterdictionDTO zdto = new ZoneInterdictionDTO();
                    zdto.setId(z.getId());
                    // ZoneInterdiction hérite de Zone qui a getPosition()
                    java.awt.Point pos = z.getPosition();
                    zdto.setPosition(new java.awt.Point(pos.x, pos.y));
                    zdto.setLargeur(z.getLargeur());
                    zdto.setLongueur(z.getLongueur());
                    zdto.setNom(z.getNom());
                    return zdto;
                })
                .collect(Collectors.toList());
        dto.setZonesInterdiction(zonesInterdiction);

        // Convertir les zones tampon
        List<ZoneTamponDTO> zonesTampon = p.getZonesTampon().stream()
                .map(z -> {
                    ZoneTamponDTO zdto = new ZoneTamponDTO();
                    zdto.setId(z.getId());
                    java.awt.Point pos = z.getPosition();
                    zdto.setPosition(new java.awt.Point(pos.x, pos.y));
                    zdto.setLargeur(z.getLargeur());
                    zdto.setLongueur(z.getLongueur());
                    zdto.setDistance(z.getDistance());
                    zdto.setNom(z.getNom());
                    return zdto;
                })
                .collect(Collectors.toList());
        dto.setZonesTampon(zonesTampon);

        return dto;
    }
}

