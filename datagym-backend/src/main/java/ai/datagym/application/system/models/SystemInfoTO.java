package ai.datagym.application.system.models;

public class SystemInfoTO {
    private SystemInfoAppTO app;
    private SystemInfoServerTO server;
    private DataGymEnvironment environment;

    public SystemInfoTO() {
    }

    public SystemInfoAppTO getApp() {
        return app;
    }

    public void setApp(SystemInfoAppTO app) {
        this.app = app;
    }

    public SystemInfoServerTO getServer() {
        return server;
    }

    public void setServer(SystemInfoServerTO server) {
        this.server = server;
    }

    public DataGymEnvironment getEnvironment() {
        return environment;
    }

    public void setEnvironment(DataGymEnvironment environment) {
        this.environment = environment;
    }
}
