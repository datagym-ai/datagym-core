package ai.datagym.application.labelIteration.models.change.viewModels;

import ai.datagym.application.labelConfiguration.entity.LcEntryType;
import ai.datagym.application.labelIteration.entity.FrameType;

public class SelectChangeViewModel extends LcEntryChangeViewModel {

    private String selectKey;

    public SelectChangeViewModel(String id, Integer frame, FrameType frameType,
                                 String selectKey) {
        super(id, frame, frameType, LcEntryType.SELECT);
        this.selectKey = selectKey;
    }

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
