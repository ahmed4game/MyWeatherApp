package com.ahmed.myapplication.retrofit;

import com.ahmed.myapplication.model.CurrentWeatherModel;

import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface APIService {

    @GET("data/2.5/weather")
    Single<CurrentWeatherModel> getCurrentWeather(@Query("q") String cityName, @Query("appid") String appId);
}
