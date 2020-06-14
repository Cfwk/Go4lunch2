package cfwz.skiti.go4lunch.utils.Notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import cfwz.skiti.go4lunch.R;
import cfwz.skiti.go4lunch.api.RestaurantsHelper;
import cfwz.skiti.go4lunch.api.UserHelper;
import cfwz.skiti.go4lunch.model.GooglePlaces.ResultDetails;
import cfwz.skiti.go4lunch.stream.GooglePlaceDetails;
import cfwz.skiti.go4lunch.stream.GooglePlaceDetailsCalls;
import cfwz.skiti.go4lunch.utils.MainActivity;

/**
 * Created by Skiti on 14/06/2020
 */

public class AlarmReceiver extends BroadcastReceiver implements GooglePlaceDetailsCalls.Callbacks {

    public static final String NOTIFICATION_CHANNEL_ID = "10001";
    public static final String NOTIFICATION_CHANNEL_NAME = "Go4Lunch";

    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mBuilder;
    private List<String> usersList;
    private String mRestaurantName;
    private String mRestaurantAddress;
    private Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.mContext = context;
        usersList = new ArrayList<>();
        if (FirebaseAuth.getInstance().getCurrentUser() != null){
            RestaurantsHelper.getBooking(FirebaseAuth.getInstance().getCurrentUser().getUid(),getTodayDate()).addOnCompleteListener(restaurantTask -> {
                if (restaurantTask.isSuccessful()){
                    if (!(restaurantTask.getResult().isEmpty())){ // User did a booking for today
                        Log.e("TAG", "onReceive: Sending notifications" );
                        for (DocumentSnapshot restaurant : restaurantTask.getResult()){
                            RestaurantsHelper.getTodayBooking(restaurant.getData().get("restaurantId").toString(), getTodayDate()).addOnCompleteListener(bookingTask -> {
                                if (bookingTask.isSuccessful()){
                                    for (QueryDocumentSnapshot booking : bookingTask.getResult()){
                                        UserHelper.getWorkmate(booking.getData().get("userId").toString()).addOnCompleteListener(userTask -> {
                                            if (userTask.isSuccessful()){
                                                if (!(userTask.getResult().getData().get("uid").toString().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))){
                                                    Log.e("TAG", "ALARM_RECEIVER | User : " + userTask.getResult().getData().get("username") );
                                                    String username = userTask.getResult().getData().get("username").toString();
                                                    usersList.add(username);
                                                }
                                            }
                                            if (usersList.size() == bookingTask.getResult().size() - 1){
                                                GooglePlaceDetailsCalls.fetchPlaceDetails(this,restaurant.getData().get("restaurantId").toString());
                                            }
                                        });
                                    }
                                    Log.e("TAG", "onReceive: " + usersList.toString() );

                                }
                            });
                        }
                    }else{
                        Log.e("TAG", "onReceive: No booking for this user today" );
                    }
                }
            });
        }
    }

    /**
     * Create and push the notification
     */
    public void sendNotification(String users)
    {
        Log.e("TAG", "sendNotification: USERS " + users );
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(mContext , MainActivity.class);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, NotificationHelper.ALARM_TYPE_RTC, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        //Build notification
        Notification repeatedNotification = buildLocalNotification(mContext, pendingIntent, users).build();

        mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
        {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, importance);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            mBuilder.setChannelId(NOTIFICATION_CHANNEL_ID);
            mNotificationManager.createNotificationChannel(notificationChannel);
        }
        mNotificationManager.notify(NotificationHelper.ALARM_TYPE_RTC, repeatedNotification);
    }

    public NotificationCompat.Builder buildLocalNotification(Context mContext, PendingIntent pendingIntent, String users) {
        Log.e("TAG", "buildLocalNotification: USERS " + users );
        mBuilder = new NotificationCompat.Builder(mContext,NOTIFICATION_CHANNEL_ID);
        mBuilder.setSmallIcon(R.mipmap.ic_launcher);
        mBuilder.setContentTitle(mContext.getResources().getString(R.string.notification_title))
                .setContentText(mContext.getResources().getString(R.string.notification_message))
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(users))
                .setAutoCancel(true);

        return mBuilder;
    }

    protected String getTodayDate(){
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        return df.format(c.getTime());
    }

    @Override
    public void onResponse(@Nullable ResultDetails resultDetails) {
        mRestaurantName = resultDetails.getName();
        mRestaurantAddress = resultDetails.getVicinity();
        if (usersList.size() > 0){
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < usersList.size(); i++){
                sb.append(usersList.get(i));
                if (!(i == usersList.size() - 1)){
                    sb.append(", ");
                }
            }
            sendNotification(mContext.getResources().getString(
                    R.string.notification_message_big,
                    mRestaurantName,
                    mRestaurantAddress,
                    sb));
        }else{
            sendNotification(mContext.getResources().getString(
                    R.string.notification_message_big,
                    mRestaurantName,
                    mRestaurantAddress,
                    mContext.getResources().getString(R.string.notification_message_no_workmates)));
        }
    }

    @Override
    public void onFailure() {

    }
}