package ai.datagym.application.testUtils;

import ai.datagym.application.labelIteration.entity.geometry.PointPojo;
import ai.datagym.application.labelIteration.models.bindingModels.PointPojoBindingModel;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PointPojoUtils {
    public static final String POINT_POJO_ID = "TestId " + UUID.randomUUID();

    private static final Long TIME = System.currentTimeMillis();

    public static PointPojo createPointPojo() {
        return new PointPojo() {{
            setId(POINT_POJO_ID);
            setX(0.5);
            setY(1.0);
        }};
    }

    public static List<PointPojo> createPointPojoList(int count) {
        return IntStream.range(0, count)
                .mapToObj(index -> new PointPojo() {{
                    setId(POINT_POJO_ID);
                    setX(0.0 + index);
                    setY(0.0 + index);
                }}).collect(Collectors.toList());
    }

    public static List<PointPojoBindingModel> createPointPojoBindingModels(int count) {
        return IntStream.range(0, count)
                .mapToObj(index -> new PointPojoBindingModel() {{
                    setId(POINT_POJO_ID);
                    setX(0.0 + index);
                    setY(0.0 + index);
                }}).collect(Collectors.toList());
    }
}
