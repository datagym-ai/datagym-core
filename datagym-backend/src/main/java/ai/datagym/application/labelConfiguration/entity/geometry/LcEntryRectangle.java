package ai.datagym.application.labelConfiguration.entity.geometry;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.io.Serializable;

@Entity
@DiscriminatorValue(value = "rectangle")
public class LcEntryRectangle extends LcEntryGeometry implements Serializable {

    public LcEntryRectangle() {
        super();
    }
}
