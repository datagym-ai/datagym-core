package ai.datagym.application.labelConfiguration.entity.geometry;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.io.Serializable;

@Entity
@DiscriminatorValue(value = "point")
public class LcEntryPoint extends LcEntryGeometry implements Serializable {
    public LcEntryPoint() {
        super();
    }
}
