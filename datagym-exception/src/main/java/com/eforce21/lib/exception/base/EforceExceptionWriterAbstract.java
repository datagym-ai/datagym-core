package com.eforce21.lib.exception.base;

/**
 * Abstract {@link EforceExceptionWriter} providing getters/setters for
 * order and produces.
 *
 * @author thomas.kuhlins
 */
public abstract class EforceExceptionWriterAbstract implements EforceExceptionWriter {

    protected int order = 0;

    protected String produces;

    @Override
    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public String getProduces() {
        return produces;
    }

    public void setProduces(String produces) {
        this.produces = produces;
    }

}
