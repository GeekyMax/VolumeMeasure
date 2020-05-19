package com.geekymax.volumemeasure.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.geekymax.volumemeasure.R;

public class SettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("设置");
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

            addPreferencesFromResource(R.xml.root_preferences);
            ListPreference language = findPreference("language");
            if (language != null) {
                language.setOnPreferenceChangeListener((preference, newValue) -> {
                    //重启MainActivity
                    Intent intent = new Intent(getContext(), SettingsActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    return true;
                });
            }
            SwitchPreferenceCompat upload = findPreference("upload");
            EditTextPreference webhook = findPreference("upload_url");
            upload.setOnPreferenceChangeListener((preference, newValue) -> {
                if (Boolean.parseBoolean(newValue.toString())) {
                    // 开启
                    webhook.setVisible(true);
                } else {
                    // 关闭
                    webhook.setVisible(false);
                }
                return true;
            });
            if (!upload.isChecked()) {
                webhook.setVisible(false);
            }


        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

}