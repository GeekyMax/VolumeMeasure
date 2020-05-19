package com.geekymax.volumemeasure.activity;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.geekymax.volumemeasure.manager.SettingManager;

import java.util.Locale;

public class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Resources resources = getResources();
        Configuration configuration = resources.getConfiguration();
        Locale locale = SettingManager.getInstance().getLocale(this);
        configuration.setLocale(locale);
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());//更新配置
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
