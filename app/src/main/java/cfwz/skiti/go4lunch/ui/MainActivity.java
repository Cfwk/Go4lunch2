package cfwz.skiti.go4lunch.ui;

import android.Manifest;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import cfwz.skiti.go4lunch.R;
import cfwz.skiti.go4lunch.api.GoogleAutoCompleteCalls;
import cfwz.skiti.go4lunch.api.GooglePlaceDetailsCalls;
import cfwz.skiti.go4lunch.api.GooglePlaceSearchCalls;
import cfwz.skiti.go4lunch.api.RestaurantsHelper;
import cfwz.skiti.go4lunch.model.autocomplete.AutoCompleteResult;
import cfwz.skiti.go4lunch.model.googleplaces.ResultDetails;
import cfwz.skiti.go4lunch.model.googleplaces.ResultSearch;
import cfwz.skiti.go4lunch.ui.loggin.LogginActivity;
import cfwz.skiti.go4lunch.ui.map.MapViewModel;
import cfwz.skiti.go4lunch.ui.restaurant_profile.ProfileActivity;
import cfwz.skiti.go4lunch.ui.settings.SettingsActivity;
import jp.wasabeef.glide.transformations.BlurTransformation;
import pub.devrel.easypermissions.EasyPermissions;

import static android.app.PendingIntent.getActivity;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, GooglePlaceSearchCalls.Callbacks, GooglePlaceDetailsCalls.Callbacks, GoogleAutoCompleteCalls.Callbacks, LocationListener {
    Toolbar toolbar;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ImageView backgroundView;
    ImageView imageProfileView;
    TextView nameUser;
    TextView emailUser;

    private static final String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    public MapViewModel mViewModel;
    public List<ResultDetails> mResultDetailsList = new ArrayList<>();
    private int resultSize;
    public MutableLiveData<List<ResultDetails>> mLiveData = new MutableLiveData<>();
    private static final int SIGN_OUT_TASK = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(navView, navController);
        checkLocationPermission();
        mViewModel = new ViewModelProvider(this).get(MapViewModel.class);
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this.getApplicationContext());
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                double currentLatitude = location.getLatitude();
                double currentLongitude = location.getLongitude();
                mViewModel.updateCurrentUserPosition(new LatLng(currentLatitude, currentLongitude));
                GooglePlaceSearchCalls.fetchNearbyRestaurants(this, mViewModel.getCurrentUserPositionFormatted());
            }
        });
        this.configureToolBar();
        this.configureDrawerLayout();
        this.configureNavigationView();
        if (!isCurrentUserLogged()){startSignInActivity();}
        this.updateUI();

    }

    protected OnFailureListener onFailureListener(){
        return e -> Toast.makeText(getApplicationContext(), getString(R.string.error_unknown_error), Toast.LENGTH_LONG).show();
    }

    @Nullable
    public FirebaseUser getCurrentUser(){ return FirebaseAuth.getInstance().getCurrentUser(); }

    protected Boolean isCurrentUserLogged(){ return (this.getCurrentUser() != null); }

    private void startSignInActivity (){
        Intent intent = new Intent(this, LogginActivity.class);
        startActivity(intent);
    }

    private void updateUI(){
        if (isCurrentUserLogged()) {
            if (this.getCurrentUser().getPhotoUrl() != null) {
                Glide.with(this)
                        .load(this.getCurrentUser().getPhotoUrl())
                        .apply(RequestOptions.circleCropTransform())
                        .into(imageProfileView);
            } else {
                Glide.with(this)
                        .load(R.drawable.default_image)
                        .apply(RequestOptions.circleCropTransform())
                        .into(imageProfileView);
            }
            String email = TextUtils.isEmpty(this.getCurrentUser().getEmail()) ? getString(R.string.info_no_email_found) : this.getCurrentUser().getEmail();
            String username = TextUtils.isEmpty(this.getCurrentUser().getDisplayName()) ? getString(R.string.info_no_username_found) : this.getCurrentUser().getDisplayName();
            this.nameUser.setText(username);
            this.emailUser.setText(email);
            Glide.with(this)
                    .load(R.drawable.side_nav_bar)
                    .apply(RequestOptions.bitmapTransform(new BlurTransformation(30)))
                    .into(backgroundView);
        }
    }

    private void configureToolBar(){
        this.toolbar = findViewById(R.id.activity_main_toolbar);
        setSupportActionBar(toolbar);
    }

    private void configureDrawerLayout() {
        this.drawerLayout = findViewById(R.id.main_drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    @Override
    protected void onResume() {
        this.updateUI();
        super.onResume();
    }

    private void configureNavigationView() {
        this.navigationView = findViewById(R.id.activity_main_nav_view);
        final View headerLayout = navigationView.getHeaderView(0);
        imageProfileView = headerLayout.findViewById(R.id.imageProfileView);
        emailUser = headerLayout.findViewById(R.id.emailUser);
        nameUser = headerLayout.findViewById(R.id.nameUser);
        backgroundView = headerLayout.findViewById(R.id.side_nav_bar);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_appbar, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (this.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            this.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.nav_lunch:
                RestaurantsHelper.getBooking(getCurrentUser().getUid(),getTodayDate()).addOnCompleteListener(bookingTask -> {
                    if (bookingTask.isSuccessful()){
                        if (bookingTask.getResult().isEmpty()){
                            Toast.makeText(this, getResources().getString(R.string.drawer_no_restaurant_booked), Toast.LENGTH_SHORT).show();
                        }else{
                            Map<String,Object> extra = new HashMap<>();
                            for (QueryDocumentSnapshot booking : bookingTask.getResult()){
                                extra.put("PlaceDetailResult",booking.getData().get("restaurantId"));
                            }
                            Intent intent = new Intent(this, ProfileActivity.class);
                                for (Object key : extra.keySet()) {
                                    String mKey = (String)key;
                                    String value = (String) extra.get(key);
                                    intent.putExtra(mKey, value);
                                }
                            startActivity(intent);
                        }
                    }
                });
                break;
            case R.id.nav_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_logout:
                this.signOutUserFromFirebase();
            default:
                break;
        }
        this.drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    protected String getTodayDate(){
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        return df.format(c.getTime());
    }

    private void signOutUserFromFirebase(){
        AuthUI.getInstance()
                .signOut(this)
                .addOnSuccessListener(this, this.updateUIAfterRESTRequestsCompleted());
    }

    private OnSuccessListener<Void> updateUIAfterRESTRequestsCompleted(){
        return aVoid -> {
            if (MainActivity.SIGN_OUT_TASK == SIGN_OUT_TASK) {
                startSignInActivity();
            }
        };
    }

    @Override
    public void onLocationChanged(Location location) {
        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();

        mViewModel.updateCurrentUserPosition(new LatLng(currentLatitude, currentLongitude));
    }

    public boolean checkLocationPermission() {
        return EasyPermissions.hasPermissions(getApplicationContext(), perms);
    }

    @Override
    public void onResponse(@Nullable AutoCompleteResult autoCompleteResult) {
        resultSize = autoCompleteResult.getPredictions().size();
        AutoCompleteToDetails(autoCompleteResult);
    }

    @Override
    public void onResponse(@Nullable ResultDetails resultDetails) {
        if (resultDetails.getTypes().contains("restaurant")){
            mResultDetailsList.add(resultDetails); }
        else {
            resultSize--;
        }if (mResultDetailsList.size()==resultSize){
            mLiveData.setValue(mResultDetailsList);
        }
    }

    @Override
    public void onResponse(@Nullable List<ResultSearch> resultSearchList) {
        resultSize=resultSearchList.size();
        SearchToDetails(resultSearchList);
    }

    private void SearchToDetails(List<ResultSearch> resultSearchList) {
        mResultDetailsList.clear();
        for (int i = 0; i < resultSearchList.size(); i++) {
            GooglePlaceDetailsCalls.fetchPlaceDetails(this, resultSearchList.get(i).getPlaceId());
        }
    }

    private void AutoCompleteToDetails(AutoCompleteResult autoCompleteResult) {
        mResultDetailsList.clear();
        for (int i = 0; i < autoCompleteResult.getPredictions().size(); i++) {
            GooglePlaceDetailsCalls.fetchPlaceDetails(this, autoCompleteResult.getPredictions().get(i).getPlaceId());
        }
    }

    public void GoogleAutoCompleteSearch(String query) {
        GoogleAutoCompleteCalls.fetchAutoCompleteResult(this, query, mViewModel.getCurrentUserPositionFormatted());
    }

    @Override
    public void onFailure() {
        Toast.makeText(this.getApplicationContext(), getResources().getString(R.string.no_restaurant_error_message), Toast.LENGTH_SHORT).show();
    }

    public void resetList() {
        GooglePlaceSearchCalls.fetchNearbyRestaurants(this, mViewModel.getCurrentUserPositionFormatted());
    }
}
