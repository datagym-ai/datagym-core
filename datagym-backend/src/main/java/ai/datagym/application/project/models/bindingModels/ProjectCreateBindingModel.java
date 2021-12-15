package ai.datagym.application.project.models.bindingModels;

import ai.datagym.application.project.annotations.UniqueProjectName;
import ai.datagym.application.project.entity.MediaType;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@UniqueProjectName
public class ProjectCreateBindingModel {

    @NotNull
    @NotEmpty(message = "You need to select an owner")
    @Size(min = 1, max = 128)
    private String owner;

    @NotNull
    @Size(min = 1, max = 128, message = "Name must be less than 128 characters")
    @NotEmpty(message = "Name can't be empty")
    @Pattern(regexp = "^[a-zA-Z0-9_ ]{1,128}$")
    private String name;

    @Size(min = 1, max = 128, message = "Short Description must be less than 128 characters")
    @NotNull
    @NotEmpty(message = "Short description can't be empty")
    @Pattern(regexp = "^([a-zA-Z0-9]|[_ ,.]|[-]|[àâçéèêëîïôûùüÿñæœ]|[äöüßÄÖÜ])*$")
    private String shortDescription;

    @Length(max = 1024)
    // Alphanumeric chars, underscore, space, comma, dot, minus sign and newline
    @Pattern(regexp = "^([a-zA-Z0-9]|[_ ,.]|[-]|[àâçéèêëîïôûùüÿñæœ]|[äöüßÄÖÜ]|[\n\r])*$")
    private String description;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public void setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    @Override
    public String toString() {
        return "ProjectCreateBindingModel{" +
                "owner='" + owner + '\'' +
                ", name='" + name + '\'' +
                ", shortDescription='" + shortDescription + '\'' +
                ", description='" + description + '\'' +
                ", mediaType=" + mediaType +
                '}';
    }
}
