package ai.datagym.application.dataset.models.dataset.bindingModels;

import ai.datagym.application.dataset.annotations.UniqueDatasetName;
import ai.datagym.application.project.entity.MediaType;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@UniqueDatasetName
public class DatasetCreateBindingModel {
    @NotNull
    @NotEmpty(message = "You need to select an owner")
    private String owner;

    @NotNull
    @NotEmpty(message = "Name can't be empty")
    @Size(min = 1, max = 128, message = "Name must be less than 128 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_ ]{1,128}$")
    private String name;

    @Length(max = 128)
    @Pattern(regexp = "^([a-zA-Z0-9]|[_ ,.]|[-]|[àâçéèêëîïôûùüÿñæœ]|[äöüßÄÖÜ]|[\n\r])*$")
    private String shortDescription;

    private MediaType mediaType;

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public void setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
    }
}
