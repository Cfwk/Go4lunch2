package cfwz.skiti.go4lunch.ui.map;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import cfwz.skiti.go4lunch.R;
import cfwz.skiti.go4lunch.api.GoogleAutoCompleteCalls;
import cfwz.skiti.go4lunch.api.GooglePlaceDetailsCalls;
import cfwz.skiti.go4lunch.api.GooglePlaceSearchCalls;
import cfwz.skiti.go4lunch.model.autocomplete.AutoCompleteResult;
import cfwz.skiti.go4lunch.model.googleplaces.ResultDetails;
import cfwz.skiti.go4lunch.model.googleplaces.ResultSearch;


public class MapViewModel extends ViewModel {
    public final MutableLiveData<LatLng> currentUserPosition = new MutableLiveData<>();
    public final MutableLiveData<String> currentUserUID = new MutableLiveData<>();
    public final MutableLiveData<Integer> currentUserZoom = new MutableLiveData<>();
    public final MutableLiveData<Integer> currentUserRadius = new MutableLiveData<>();
    public List<ResultDetails> mResultDetailsList = new ArrayList<>();
    public int resultSize;


    public void updateCurrentUserPosition(LatLng latLng){
        currentUserPosition.setValue(latLng);
    }
    public LatLng getCurrentUserPosition(){
        return currentUserPosition.getValue();
    }
    public void updateCurrentUserZoom(int zoom){
        currentUserZoom.setValue(zoom);
    }
    public Integer getCurrentUserZoom(){return currentUserZoom.getValue();}
    public void updateCurrentUserRadius(int radius){
        currentUserRadius.setValue(radius);
    }
    public Integer getCurrentUserRadius(){return currentUserRadius.getValue();}
    public String getCurrentUserPositionFormatted(){
        String location = currentUserPosition.getValue().toString().replace("lat/lng: (", "");
        return location.replace(")", ""); }
    public void updateCurrentUserUID(String uid){
        currentUserUID.setValue(uid);
    }
    public String getCurrentUserUID() {
        return currentUserUID.getValue();
    }
}
