package ai.datagym.application.externalAPI.models.bindingModels;

import ai.datagym.application.externalAPI.annotations.UniqueApiTokenName;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@UniqueApiTokenName
public class ApiTokenCreateBindingModel {
    @NotNull
    @NotEmpty
    @Size(min = 1, max = 30)
    private String name;

    @NotNull
    @NotEmpty
    private String owner;

    public ApiTokenCreateBindingModel() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }
}
