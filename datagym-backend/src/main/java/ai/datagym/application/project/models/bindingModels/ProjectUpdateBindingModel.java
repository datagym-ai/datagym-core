package ai.datagym.application.project.models.bindingModels;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectUpdateBindingModel {
    @NotNull
    @NotEmpty(message = "Name can't be empty")
    @Size(min = 1, max = 128, message = "Name must be less than 128 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_ ]{1,128}$")
    private String name;

    @NotNull
    @NotEmpty(message = "Short description must not be empty")
    @Size(min = 1, max = 128, message = "Short Description must be less than 128 characters")
    @Pattern(regexp = "^([a-zA-Z0-9]|[_ ,.]|[-]|[àâçéèêëîïôûùüÿñæœ]|[äöüßÄÖÜ])*$")
    private String shortDescription;

    @Length(max = 1024)
    // Alphanumeric chars, underscore, space, comma, dot, minus sign and newline
    @Pattern(regexp = "^([a-zA-Z0-9]|[_ ,.]|[-]|[àâçéèêëîïôûùüÿñæœ]|[äöüßÄÖÜ]|[\n\r])*$")
    private String description;

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
}
