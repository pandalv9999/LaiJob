package com.laioffer.githubexample.ui.HomeList;



import androidx.lifecycle.MutableLiveData;

import com.laioffer.githubexample.base.BaseRepository;
import com.laioffer.githubexample.remote.response.Job;
import com.laioffer.githubexample.remote.response.RemoteResponse;
import com.laioffer.githubexample.util.Config;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeListRepository extends BaseRepository {
    public MutableLiveData<List<Job>> searchNearby() {
        final MutableLiveData<List<Job>> result = new MutableLiveData<>();
        // retrieve user-related data
        Call<RemoteResponse<List<Job>>> call = apiService.search(Config.latitude, Config.longitude);
        call.enqueue(new Callback<RemoteResponse<List<Job>>>() {
            @Override
            public void onResponse(Call<RemoteResponse<List<Job>>> call, Response<RemoteResponse<List<Job>>> response) {
                result.postValue(response.body().response);
            }

            @Override
            public void onFailure(Call<RemoteResponse<List<Job>>> call, Throwable t) {
                result.postValue(null);
            }
        });

        return result;
    }

}
