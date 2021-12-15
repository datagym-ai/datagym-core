package ai.datagym.application.testUtils;

import ai.datagym.application.security.models.viewModles.UserMinInfoViewModel;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class UserInfoUtils {
    public static final String USER_NAME = "TestId " + UUID.randomUUID();
    public static final String USER_ID = "1";

    public static UserMinInfoViewModel createUserMinInfoViewModel() {
        return new UserMinInfoViewModel() {{
            setName(USER_NAME);
            setId(USER_ID);
        }};
    }

    public static List<UserMinInfoViewModel> createUserMinInfoViewModelList(int count) {
        return IntStream.range(0, count)
                .mapToObj(index -> new UserMinInfoViewModel() {{
                    setName(USER_NAME + index);
                    setId(USER_ID + index);
                }}).collect(Collectors.toList());
    }
}
