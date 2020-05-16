package com.geekymax.volumemeasure.manager;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.geekymax.volumemeasure.R;
import com.geekymax.volumemeasure.constant.SettingConstant;

import java.util.function.Consumer;

/**
 * 全局设置信息管理
 */
public class SettingManager {

    private static SettingManager instance;
    private Context context;

    private SettingManager() {
    }

    public static SettingManager getInstance() {
        if (instance == null) {
            instance = new SettingManager();
        }
        return instance;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public String getSettingValue(Context context, String key, String defaultValue) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(key, defaultValue);
    }

    public boolean getSettingBoolean(Context context, String key, boolean defaultValue) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        return sharedPreferences.getBoolean(key, defaultValue);
    }

    public String getMeasurer(Context context) {
        return getSettingValue(context, "measurer", SettingConstant.MEASURER_DEFAULT);
    }

    public String getThemeColor(Context context) {
        return getSettingValue(context, "theme_color", SettingConstant.THEME_COLOR_DEFAULT);
    }

    public boolean isShowARLabel(Context context) {
        return getSettingBoolean(context, "show_ar_label", false);
    }

    public boolean autoSaveHistory(Context context) {
        return getSettingBoolean(context, "memory", false);

    }

    public boolean isUpload(Context context) {
        return getSettingBoolean(context, "upload", false);

    }

    public String getWebhookUrl(Context context) {
        return getSettingValue(context, "webhook", "");

    }

    // 获取online measurer url, 如果为空就使用默认
    public String getMeasurerWebhookUrl() {
        String url = getSettingValue(context, "measurer_webhook", "");
        if (url.equals("")) {
            url = context.getResources().getString(R.string.default_measurer_url);
        }
        return url;
    }
}
