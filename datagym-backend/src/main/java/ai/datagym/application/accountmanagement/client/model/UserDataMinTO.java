package ai.datagym.application.accountmanagement.client.model;

public class UserDataMinTO {

    private String sub;

    private String name;

    public UserDataMinTO() {
    }

    public UserDataMinTO(String sub, String name) {
        this.sub = sub;
        this.name = name;
    }

    public String getSub() {
        return sub;
    }

    public void setSub(String sub) {
        this.sub = sub;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "UserDataMinTO{" +
                "sub='" + sub + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
