package ai.datagym.application.labelConfiguration.entity.geometry;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.io.Serializable;

@Entity
@DiscriminatorValue(value = "polygon")
public class LcEntryPolygon extends LcEntryGeometry implements Serializable {
    public LcEntryPolygon() {
        super();
    }
}
