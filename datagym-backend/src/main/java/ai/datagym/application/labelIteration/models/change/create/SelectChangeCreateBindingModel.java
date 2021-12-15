package ai.datagym.application.labelIteration.models.change.create;

import javax.validation.constraints.Pattern;

public class SelectChangeCreateBindingModel extends LcEntryChangeCreateBindingModel {

    @Pattern(regexp = "^[a-zA-Z0-9_ -]*$")
    private String selectKey;

    public String getSelectKey() {
        return selectKey;
    }

    public void setSelectKey(String selectKey) {
        this.selectKey = selectKey;
    }

    @Override
    public String toString() {
        return "SelectChangeCreateBindingModel{" +
                "selectKey='" + selectKey + '\'' +
                '}';
    }
}
