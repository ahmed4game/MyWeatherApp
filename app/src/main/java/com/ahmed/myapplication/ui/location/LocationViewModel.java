package com.ahmed.myapplication.ui.location;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.ahmed.myapplication.models.LocationModel;

public class LocationViewModel extends ViewModel {
    // TODO: Implement the ViewModel
    private MutableLiveData<LocationModel> locationLiveData;

    public LocationViewModel() {
        locationLiveData = new MutableLiveData<>();
    }

    public MutableLiveData<LocationModel> getLocationLiveData() {
        return locationLiveData;
    }

    public void setLocationLiveData(LocationModel locationModel) {
        if (locationLiveData != null) {
            this.locationLiveData.setValue(locationModel);
        }
    }
}
