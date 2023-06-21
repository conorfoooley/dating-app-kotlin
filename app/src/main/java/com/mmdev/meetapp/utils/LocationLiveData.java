package com.mmdev.meetapp.utils;

import android.location.Location;
import androidx.lifecycle.LiveData;
import com.mmdev.meetapp.services.LocationService;

public class LocationLiveData extends LiveData<Location>
{

	LocationService.LocationListener locationListener = new LocationService.LocationListener("Provider") {
		@Override
		public void onLocationChanged(Location location) { setValue(location); }
	};

	@Override
	protected void onActive() {
		LocationService.addListener(locationListener);
	}

	@Override
	protected void onInactive() {
		LocationService.removeListener(locationListener);
	}

}
