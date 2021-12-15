package ai.datagym.application.externalAPI.models.viewModels.jsonUploadErrorTOs;

public class JsonUploadErrorTO {
    private String internal_media_ID;
    private String message;
    private String lcEntryKey;
    private String lcEntryType;

    public JsonUploadErrorTO() {
    }

    public String getInternal_media_ID() {
        return internal_media_ID;
    }

    public void setInternal_media_ID(String internal_media_ID) {
        this.internal_media_ID = internal_media_ID;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getLcEntryKey() {
        return lcEntryKey;
    }

    public void setLcEntryKey(String lcEntryKey) {
        this.lcEntryKey = lcEntryKey;
    }

    public String getLcEntryType() {
        return lcEntryType;
    }

    public void setLcEntryType(String lcEntryType) {
        this.lcEntryType = lcEntryType;
    }
}
