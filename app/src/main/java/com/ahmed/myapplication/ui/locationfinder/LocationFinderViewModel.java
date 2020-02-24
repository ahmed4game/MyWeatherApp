package com.ahmed.myapplication.ui.locationfinder;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.ahmed.myapplication.model.CurrentWeatherModel;
import com.ahmed.myapplication.models.LocationModel;

public class LocationFinderViewModel extends ViewModel {

    private MutableLiveData<CurrentWeatherModel> currentWeatherModelMutableLiveData;
    private MutableLiveData<LocationModel> locationModelMutableLiveData;

    public MutableLiveData<LocationModel> getLocationModelMutableLiveData() {
        return locationModelMutableLiveData;
    }

    public LocationFinderViewModel() {
        currentWeatherModelMutableLiveData = new MutableLiveData<>();
        locationModelMutableLiveData = new MutableLiveData<>();
    }

    public MutableLiveData<CurrentWeatherModel> getCurrentWeatherModelMutableLiveData() {
        return currentWeatherModelMutableLiveData;
    }

    public void setCurrentWeatherModelMutableLiveData(CurrentWeatherModel weatherModel) {
        if (currentWeatherModelMutableLiveData != null) {
            this.currentWeatherModelMutableLiveData.setValue(weatherModel);
        }
    }

    public void setLocationModelMutableLiveData(LocationModel locationModel) {
        if (locationModelMutableLiveData != null) {
            this.locationModelMutableLiveData.setValue(locationModel);
        }
    }

    private void CallNetworkData(){
        //        activity.apiService.getCurrentWeather("Bangalore",getString(R.string.weather_api_secret))
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribeWith(new DisposableSingleObserver<CurrentWeatherModel>() {
//                    @Override
//                    public void onSuccess(CurrentWeatherModel currentWeatherModel) {
//                        activity.showToast("Success fetching Weather Data: "+currentWeatherModel.toString());
//                        mViewModel.setCurrentWeatherModelMutableLiveData(currentWeatherModel);
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        activity.showToast("Error fetching Weather Data. Error:"+e.getLocalizedMessage());
//                    }
//                });
    }
}
