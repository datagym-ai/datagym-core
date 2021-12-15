package ai.datagym.application.labelTask.service;

import org.springframework.context.ApplicationEvent;

public class CustomUserTaskDummyEvent extends ApplicationEvent {
    private String owner;

    /**
     * Create a new {@code ApplicationEvent}.
     *
     * @param source the object on which the event initially occurred or with
     *               which the event is associated (never {@code null})
     */
    public CustomUserTaskDummyEvent(Object source, String owner) {
        super(source);
        this.owner = owner;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }
}
