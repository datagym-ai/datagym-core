package ai.datagym.application.dataset.models.awsS3.bindingModels;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class AwsS3CredentialsUpdateKeysBindingModel {
    @NotNull
    @NotEmpty
    private String accessKey;

    @NotNull
    @NotEmpty
    private String secretKey;

    public AwsS3CredentialsUpdateKeysBindingModel() {
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }
}
