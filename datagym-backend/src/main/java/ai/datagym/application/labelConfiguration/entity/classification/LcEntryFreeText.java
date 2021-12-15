package ai.datagym.application.labelConfiguration.entity.classification;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.io.Serializable;
import java.util.Objects;

@Entity
@DiscriminatorValue(value = "free_text")
public class LcEntryFreeText extends LcEntryClassification implements Serializable {
    @Column(name = "max_length")
    private Integer maxLength = 255;

    public LcEntryFreeText() {
        super();
    }

    public Integer getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(Integer maxLength) {
        this.maxLength = maxLength;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LcEntryFreeText)) return false;
        if (!super.equals(o)) return false;
        LcEntryFreeText that = (LcEntryFreeText) o;
        return Objects.equals(getMaxLength(), that.getMaxLength());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getMaxLength());
    }
}
