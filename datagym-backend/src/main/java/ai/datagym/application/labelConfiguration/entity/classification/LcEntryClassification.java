package ai.datagym.application.labelConfiguration.entity.classification;

import ai.datagym.application.labelConfiguration.entity.LcEntry;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;
import java.util.Objects;

@MappedSuperclass
public abstract class LcEntryClassification  extends LcEntry implements Serializable {
    @Column(name = "is_required", columnDefinition = "boolean default false")
    private boolean required = false;

    public LcEntryClassification() {
        super();
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LcEntryClassification)) return false;
        LcEntryClassification that = (LcEntryClassification) o;
        return isRequired() == that.isRequired();
    }

    @Override
    public int hashCode() {
        return Objects.hash(isRequired());
    }
}
