package ai.datagym.application.accountmanagement.client.model;

public class OrgDataMinTO {
    private String sub;
    private String name;
    private boolean personal;

    public OrgDataMinTO() {
    }

    public OrgDataMinTO(String sub, String name, boolean personal) {
        this.sub = sub;
        this.name = name;
        this.personal = personal;
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

    public boolean isPersonal() {
        return personal;
    }

    public void setPersonal(boolean personal) {
        this.personal = personal;
    }

    @Override
    public String toString() {
        return "OrgDataMinTO{" +
                "sub='" + sub + '\'' +
                ", name='" + name + '\'' +
                ", personal=" + personal +
                '}';
    }
}
