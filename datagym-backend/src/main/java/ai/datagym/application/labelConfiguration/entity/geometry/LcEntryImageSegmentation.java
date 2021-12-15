package ai.datagym.application.labelConfiguration.entity.geometry;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.io.Serializable;

@Entity
@DiscriminatorValue(value = "segmentation")
public class LcEntryImageSegmentation extends LcEntryGeometry implements Serializable {
    public LcEntryImageSegmentation() {
        super();
    }
}
