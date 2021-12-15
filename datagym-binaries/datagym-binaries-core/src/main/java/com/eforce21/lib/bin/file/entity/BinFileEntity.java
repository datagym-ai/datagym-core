package com.eforce21.lib.bin.file.entity;

import javax.persistence.*;

@Entity
@Table(name = "bin_file")
public class BinFileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String mime;

    @Column(nullable = false)
    private long size;

    @Column(name = "data_id", nullable = false)
    private String dataId;

    @Column(name = "ts_create", nullable = false)
    private long tsCreate;

    /**
     * Mark file usable as cover image in a bunch of files.
     */
    @Column(name = "cover", nullable = false)
    private boolean cover;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMime() {
        return mime;
    }

    public void setMime(String mime) {
        this.mime = mime;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getDataId() {
        return dataId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    public long getTsCreate() {
        return tsCreate;
    }

    public void setTsCreate(long tsCreate) {
        this.tsCreate = tsCreate;
    }

    public boolean isCover() {
        return cover;
    }

    public void setCover(boolean cover) {
        this.cover = cover;
    }

    @Override
    public String toString() {
        return "BinFileEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", mime='" + mime + '\'' +
                ", size=" + size +
                ", dataId='" + dataId + '\'' +
                ", tsCreate='" + tsCreate + '\'' +
                ", cover='" + cover + '\'' +
                '}';
    }
}
