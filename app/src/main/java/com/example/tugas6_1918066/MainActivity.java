package com.example.tugas6_1918066;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private Button btnOneTimeTask, btnPeriodicTask, btnCancelPeriodicTask;
    private Spinner spCity;
    private TextView tvStatus;
    private PeriodicWorkRequest periodicWorkRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnOneTimeTask = findViewById(R.id.btn_one_time_task);
        btnPeriodicTask = findViewById(R.id.btn_periodic_task);
        btnCancelPeriodicTask = findViewById(R.id.btn_cancel_periodic_task);
        spCity = findViewById(R.id.sp_city);
        tvStatus = findViewById(R.id.tv_status);


        btnOneTimeTask.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                startOneTimeTask();

            }
        });

        btnPeriodicTask.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                startPeriodicTask();
            }
        });
        btnCancelPeriodicTask.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                cancelPeriodicTask();
            }
        });
    }
    private void startOneTimeTask(){
        tvStatus.setText("Status : " );

        Data data = new Data.Builder()
                .putString(WeatherCityWorkmanager.EXTRA_CITY,spCity.getSelectedItem().toString())
                .build();

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest oneTimeWorkRequest = new OneTimeWorkRequest.Builder(WeatherCityWorkmanager.class)
                .setInputData(data)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(MainActivity.this).enqueue(oneTimeWorkRequest);

        WorkManager.getInstance(MainActivity.this)
                .getWorkInfoByIdLiveData(oneTimeWorkRequest.getId())
                .observe(MainActivity.this, workInfo -> {
                    String status = workInfo.getState().name();
                    tvStatus.append("\n" + status);
                });
    }
    private void startPeriodicTask(){
        tvStatus.setText("Status : " );

        Data data = new Data.Builder()
                .putString(WeatherCityWorkmanager.EXTRA_CITY,spCity.getSelectedItem().toString())
                .build();

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        periodicWorkRequest = new PeriodicWorkRequest.Builder(WeatherCityWorkmanager.class, 15, TimeUnit.MINUTES)
                .setInputData(data)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(MainActivity.this).enqueue(periodicWorkRequest);

        WorkManager.getInstance(MainActivity.this)
                .getWorkInfoByIdLiveData(periodicWorkRequest.getId())
                .observe(MainActivity.this, workInfo -> {
                    String status = workInfo.getState().name();
                    tvStatus.append("\n" + status);

                    btnCancelPeriodicTask.setEnabled(false);

                    if (workInfo.getState() == WorkInfo.State.ENQUEUED){
                        btnCancelPeriodicTask.setEnabled(true);
                    }
                });
    }
    private void cancelPeriodicTask(){
        WorkManager.getInstance(MainActivity.this).cancelWorkById(periodicWorkRequest.getId());
    }
}
