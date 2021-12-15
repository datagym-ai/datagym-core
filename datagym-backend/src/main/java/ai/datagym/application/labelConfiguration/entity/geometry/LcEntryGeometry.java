package ai.datagym.application.labelConfiguration.entity.geometry;

import ai.datagym.application.labelConfiguration.entity.LcEntry;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;
import java.util.Objects;

@MappedSuperclass
public abstract class LcEntryGeometry extends LcEntry implements Serializable {
    @Column(name = "color")
    private String color;

    @Column(name = "shortcut")
    private String shortcut;

    public LcEntryGeometry() {
        super();
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getShortcut() {
        return shortcut;
    }

    public void setShortcut(String shortcut) {
        this.shortcut = shortcut;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LcEntryGeometry)) return false;
        if (!super.equals(o)) return false;
        LcEntryGeometry that = (LcEntryGeometry) o;
        return Objects.equals(getColor(), that.getColor()) &&
                Objects.equals(getShortcut(), that.getShortcut());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getColor(), getShortcut());
    }
}
