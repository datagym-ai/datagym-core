package api;

public class BinFileTO {

    private long id;

    private String name;

    private String mime;

    private long size;

    private long tsCreate;

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
        return "BinFileTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", mime='" + mime + '\'' +
                ", size=" + size +
                ", tsCreate=" + tsCreate +
                ", cover=" + cover +
                '}';
    }

}
