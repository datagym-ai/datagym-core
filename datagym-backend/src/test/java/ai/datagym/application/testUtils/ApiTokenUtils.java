package ai.datagym.application.testUtils;

import ai.datagym.application.externalAPI.entity.ApiToken;
import ai.datagym.application.externalAPI.models.bindingModels.ApiTokenCreateBindingModel;
import ai.datagym.application.externalAPI.models.viewModels.ApiTokenViewModel;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ApiTokenUtils {
    public static final String API_TOKEN_ID = "TestId " + UUID.randomUUID();
    public static final String API_TOKEN_NAME = "ApiTokenTestName";

    private static final Long TIME = new Date().getTime();

    public static ApiToken createTestApiToken() {
        return new ApiToken() {{
            setId(API_TOKEN_ID);
            setName(API_TOKEN_NAME);
            setOwner("eforce21");
            setDeleted(false);
            setDeleteTime(null);
            setCreatedAt(TIME);
            setLastUsed(null);
        }};
    }

    public static List<ApiToken> createTestApiTokenLists(int count) {
        return IntStream.range(0, count)
                .mapToObj(index -> new ApiToken() {{
                    setId(API_TOKEN_ID + index);
                    setName(API_TOKEN_NAME+ index);
                    setOwner("eforce21");
                    setDeleted(false);
                    setDeleteTime(null);
                    setCreatedAt(TIME);
                    setLastUsed(null);
                }})
                .collect(Collectors.toList());
    }

    public static ApiTokenCreateBindingModel createTestApiTokenCreateBindingModel() {
        return new ApiTokenCreateBindingModel() {{
            setName(API_TOKEN_NAME);
            setOwner("eforce21");
        }};
    }

    public static ApiTokenViewModel createTestApiTokenViewModel() {
        return new ApiTokenViewModel() {{
            setId(API_TOKEN_ID);
            setName(API_TOKEN_NAME);
            setOwner("eforce21");
            setDeleted(false);
            setDeleteTime(null);
            setCreatedAt(TIME);
            setLastUsed(null);
        }};
    }

    public static List<ApiTokenViewModel> createTestApiTokenViewModels(int count) {
        return IntStream.range(0, count)
                .mapToObj(index -> new ApiTokenViewModel() {{
                    setId(API_TOKEN_ID + index);
                    setName(API_TOKEN_NAME+ index);
                    setOwner("eforce21");
                    setDeleted(false);
                    setDeleteTime(null);
                    setCreatedAt(TIME);
                    setLastUsed(null);
                }})
                .collect(Collectors.toList());
    }
}
