package ai.datagym.application.externalAPI.models.bindingModels;

import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class ExternalApiCreateDatasetBindingModel {
    @NotNull
    @NotEmpty(message = "Name can't be empty")
    @Size(min = 1, max = 128, message = "Name must be less than 128 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_ ]{1,128}$")
    private String name;

    @Length(max = 128)
    @Pattern(regexp = "^[a-zA-Z0-9_ -]*$")
    private String shortDescription;

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
}
