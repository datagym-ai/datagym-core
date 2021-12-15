package ai.datagym.application.labelIteration.models.change.update;

import javax.validation.constraints.Pattern;

public class SelectChangeUpdateBindingModel extends LcEntryChangeUpdateBindingModel {

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
