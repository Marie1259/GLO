package domaine.dto;

import java.awt.Point;

/**
 * DTO pour la communication des données d'intersection
 */
public class IntersectionDTO {
    private String id;
    private Point position;
    private boolean active;

    public IntersectionDTO() {
    }

    public IntersectionDTO(String id, Point position) {
        this.id = id;
        this.position = position;
        this.active = false;
    }

    // === Getters et Setters ===
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Point getPosition() {
        return position;
    }

    public void setPosition(Point position) {
        this.position = position;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
