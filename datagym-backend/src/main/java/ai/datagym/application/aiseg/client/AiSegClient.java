package ai.datagym.application.aiseg.client;

import ai.datagym.application.aiseg.model.aiseg.AiSegCalculate;
import ai.datagym.application.aiseg.model.aiseg.AiSegPrefetch;
import ai.datagym.application.aiseg.model.aiseg.AiSegResponse;
import ai.datagym.application.aiseg.model.preLabel.PreLabelRequest;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.POST;
import retrofit2.http.Path;

import java.util.List;
import java.util.Map;

/**
 * Retrofit2 Client for the AiSegmentation REST-Interface
 */
public interface AiSegClient {

    @POST("prepare")
    Call<Void> prepare(@Body AiSegPrefetch aiSegPrefetch);

    @POST("calculate")
    Call<AiSegResponse> calculate(@Body AiSegCalculate aiSegCalculate);

    @DELETE("finish/{imageId}")
    Call<Void> finish(@Path("imageId") String imageId);

    @DELETE("finishUserSession/{userSessionUUID}")
    Call<Void> finishUserSession(@Path("userSessionUUID") String userSessionUUID);

    @POST("preLabel")
    Call<Map<String, List<Map<String, Object>>>> preLabelImage(@Body PreLabelRequest preLabelCalculateRequest);
}
