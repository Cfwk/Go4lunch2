package cfwz.skiti.go4lunch.utils.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import java.util.Calendar;


public class DeviceBootReceiver extends BroadcastReceiver {
    public static final long INTERVAL = AlarmManager.INTERVAL_FIFTEEN_MINUTES;
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            this.setAlarmRepeating(context);
            Toast.makeText(context, "Alarm Set", Toast.LENGTH_SHORT).show();
        }
    }

    private void setAlarmRepeating(Context context){
        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        manager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), INTERVAL, pendingIntent);
    }
}