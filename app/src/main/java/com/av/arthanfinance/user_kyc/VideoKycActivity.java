package com.av.arthanfinance.user_kyc;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.av.arthanfinance.R;
import com.av.arthanfinance.databinding.ActivityVideoKycBinding;

public class VideoKycActivity extends AppCompatActivity {
    private ActivityVideoKycBinding videoKycBinding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        videoKycBinding = ActivityVideoKycBinding.inflate(getLayoutInflater());
        setContentView(videoKycBinding.getRoot());
    }
}