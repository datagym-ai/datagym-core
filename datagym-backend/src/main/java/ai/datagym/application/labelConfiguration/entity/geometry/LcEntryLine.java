package ai.datagym.application.labelConfiguration.entity.geometry;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.io.Serializable;

@Entity
@DiscriminatorValue(value = "line")
public class LcEntryLine extends LcEntryGeometry implements Serializable {
    public LcEntryLine() {
        super();
    }
}
