package com.eforce21.cloud.login.client.ctx;

import okhttp3.Request;
import okio.Timeout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.IOException;

public class MockRetrofitCall<T> implements Call<T> {

    private Response<T> fakeResponse;

    public MockRetrofitCall(Response<T> fakeResponse) {
        this.fakeResponse = fakeResponse;
    }

    @Override
    public Response<T> execute() throws IOException {
        return fakeResponse;
    }

    @Override
    public void enqueue(Callback<T> callback) {

    }

    @Override
    public boolean isExecuted() {
        return false;
    }

    @Override
    public void cancel() {

    }

    @Override
    public boolean isCanceled() {
        return false;
    }

    @Override
    public Call<T> clone() {
        return null;
    }

    @Override
    public Request request() {
        return null;
    }

    @Override
    public Timeout timeout() {
        return null;
    }
}
