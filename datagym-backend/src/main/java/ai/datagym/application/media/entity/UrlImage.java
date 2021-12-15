package ai.datagym.application.media.entity;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = "url_image")
public class UrlImage extends Media {
    @Column(name = "image_url", length = 1256)
    private String url;

    public UrlImage() {
        super();
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
