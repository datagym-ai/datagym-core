package ai.datagym.application.media.entity;

import com.eforce21.lib.bin.file.entity.BinFileEntity;

import javax.persistence.*;

@Entity
@DiscriminatorValue(value = "local_image")
public class LocalImage extends Media {
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "fk_bin_file_id",
            foreignKey = @ForeignKey(name = "fk_localimage_binfileentity"),
            referencedColumnName = "id")
    private BinFileEntity binFileEntity;

    @Column(name = "width")
    private int width;

    @Column(name = "height")
    private int height;

    public LocalImage() {
        super();
    }

    public BinFileEntity getBinFileEntity() {
        return binFileEntity;
    }

    public void setBinFileEntity(BinFileEntity binFileEntity) {
        this.binFileEntity = binFileEntity;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
