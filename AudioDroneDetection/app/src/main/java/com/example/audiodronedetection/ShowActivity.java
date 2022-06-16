package com.example.audiodronedetection;

import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.JLibrosa.FileFormatNotSupportedException;
import com.example.JLibrosa.JLibrosa;
import com.example.JLibrosa.WavFileException;

import java.io.File;
import java.io.IOException;

public class ShowActivity extends AppCompatActivity {

    private TextView tvResults;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);

        tvResults = findViewById(R.id.tvRes);

        JLibrosa jLibrosa = new JLibrosa();
        int i = 0;
        int length = 800;
        int f_lenght = 44100;

        String pathTarget = Environment.getExternalStorageDirectory().toString() + "/AudioRecording.wav";
        String pathDataset = Environment.getExternalStorageDirectory().toString() + "/yes_drone";
        File directory = new File(pathDataset);
        File[] files = directory.listFiles();

        // Elaborazione del dataset
        float audioFeatureValues [][] = new float[length][];
        float meanMFCCValues [][] = new float[length][40];
        for(i = 0; i < length; i++)
        {
            String name = files[i].getName();
            try {
                audioFeatureValues[i] = jLibrosa.loadAndRead(pathDataset + "/" + name, -1,1);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (WavFileException e) {
                e.printStackTrace();
            } catch (FileFormatNotSupportedException e) {
                e.printStackTrace();
            }
            float mfccValues [][] =  jLibrosa.generateMFCCFeatures(audioFeatureValues[i], -1, 40);
            meanMFCCValues[i] = jLibrosa.generateMeanMFCCFeatures(mfccValues, mfccValues.length, mfccValues[0].length);


            //Elaborazione del segnale registrato

            float audioFeatureTarget [] = new float[f_lenght];
            float meanMFCCTarget [] = new float[40];

            try {
                audioFeatureTarget = jLibrosa.loadAndRead(pathTarget, -1,1);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (WavFileException e) {
                e.printStackTrace();
            } catch (FileFormatNotSupportedException e) {
                e.printStackTrace();
            }
            float mfcctarget [][] =  jLibrosa.generateMFCCFeatures(audioFeatureTarget, -1, 40);
            meanMFCCTarget = jLibrosa.generateMeanMFCCFeatures(mfcctarget, mfcctarget.length, mfcctarget[0].length);

            // Elaborazione per predire se è un drone oppure no
            float diff[][] = new float[length][40];

            float min = 0;
            for(int k = 0; k< length; k++)
            {
                float sum[] = new float[1332];
                for (int j=0; j < 40;j++)
                {
                    diff[k][j] = Math.abs(Math.abs(meanMFCCValues[k][j]) - Math.abs(meanMFCCTarget[j]));
                    // Accetto una differenza di 1
                    sum[k] = sum[k] + diff[k][j];
                    // Sulla somma quindi deve essere al massimo 40
                }
                if(k == 0)
                {
                    min = sum[k];
                }
                else if(sum[k] < min)
                {
                    min = sum[k];
                    Log.i("SHOW", String.valueOf(min));
                }
            }
            if(min > 80)
            {
                tvResults.setText("Not a Drone");
            }
            else
            {
                tvResults.setText("It is a Drone");
            }
        }
    }
}
