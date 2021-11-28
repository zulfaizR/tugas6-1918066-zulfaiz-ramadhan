package com.example.tugas6_1918066;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.SyncHttpClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;

import cz.msebera.android.httpclient.Header;

public class WeatherCityWorkmanager extends Worker {

    private static final String CHANNEL_ID = "Work_Manager_Channel01";
    private static final CharSequence CHANNEL_NAME = "WorkManagerChannel";
    public static  final  String EXTRA_CITY = "city" ;
    private static final String TAG = WeatherCityWorkmanager.class.getSimpleName();
    private Result resultStatus;

    public WeatherCityWorkmanager(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String city = getInputData().getString(EXTRA_CITY);
        return getCurrentWeather(city);
    }

    private Result getCurrentWeather(String city){
        Log.d(TAG,"getCurrentWeather : Started.....");
        Looper.prepare();
        SyncHttpClient client = new SyncHttpClient();
        String url = "http://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + BuildConfig.ApiKey;
        client.get(url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String result = new String(responseBody);
                Log.d(TAG, result);

                try {
                    JSONObject responseObject = new JSONObject(result);
                    String currentWeather = responseObject.getJSONArray("weather").getJSONObject(0).getString("main");
                    String description = responseObject.getJSONArray("weather").getJSONObject(0).getString("description");
                    double tempInKelvin = responseObject.getJSONObject("main").getDouble("temp");
                    double tempInCelcius = tempInKelvin - 273;
                    String temperature = new DecimalFormat("##.##").format(tempInCelcius);

                    String title = "Weather of " + city;
                    String message =  currentWeather + ", " + description + " with " + temperature + " celcius";
                    int notifId = 201;
                    showNotification(getApplicationContext(), title, message, notifId);

                    Log.d(TAG, "onSuccess: finished");
                    resultStatus = Result.success();
                } catch (JSONException e){
                    e.printStackTrace();
                    showNotification(getApplicationContext(), "Not Success ApiKey not connected", e.getMessage(), 201);
                    Log.d(TAG,"onSuccess: Failed");
                    resultStatus = Result.failure();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                showNotification(getApplicationContext(), "Not Success", error.getMessage(), 201);
                Log.d(TAG,"onFailure: Failed");
                resultStatus = Result.failure();
            }
        });

        return resultStatus;
    }

    private void showNotification(Context context, String title, String message, int notifId){
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(title)
                .setSmallIcon(R.drawable.ic_baseline_cloud_queue_24)
                .setContentText(message)
                .setColor(ContextCompat.getColor(context, android.R.color.transparent))
                .setVibrate(new long[]{1000, 1000, 1000, 1000})
                .setSound(alarmSound)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{1000, 1000, 1000, 1000});
            mBuilder.setChannelId(CHANNEL_ID);

            if(notificationManager != null){
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }
        Notification notification = mBuilder.build();

        if(notificationManager != null){
            notificationManager.notify(notifId,notification);
        }
    }
}

