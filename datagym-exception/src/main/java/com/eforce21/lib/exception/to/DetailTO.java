package com.eforce21.lib.exception.to;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Detail to wrap multiple messages/causes in one exception.
 *
 * @author thomas.kuhlins
 */
public class DetailTO implements Serializable {

    private static final long serialVersionUID = 6017358357564164882L;

    /**
     * Name of the field that causes the error.
     */
    private String name;

    /**
     * Error message key for i18n.
     */
    private String key;

    /**
     * Parameters for substitution in message translation.
     */
    private List<String> params = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public List<String> getParams() {
        return params;
    }

    public void addParam(String param) {
        this.params.add(param);
    }

    @Override
    public String toString() {
        return "DetailTO [name=" + name + ", key=" + key + ", params=" + params + "]";
    }


}
