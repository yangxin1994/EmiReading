package com.emi.emireading.core.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import com.emi.emireading.EmiReadingApplication;


/**
 * @author :zhoujian
 * @description : 共享文件存储工具类
 * @company :翼迈科技
 * @date: 2017年6月29日下午 03:00
 * @Email: 971613168@qq.com
 */

public final class PreferenceUtils {

    /**
     * 清空数据
     */
    public static void reset(final Context ctx) {
        Editor edit = PreferenceManager.getDefaultSharedPreferences(ctx).edit();
        edit.clear();
        edit.apply();
    }

    private SharedPreferences mPreference;

    public PreferenceUtils() {
        this(EmiReadingApplication.getAppContext(), PreferenceKeys.PREFERENCE);
    }

    public PreferenceUtils(final Context context, String sharedPreferencesName) {
        this.mPreference = context.getApplicationContext().getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE);
    }

    public String get(String key, String defValue) {
        return mPreference.getString(key, defValue);
    }

    public boolean get(String key, boolean defValue) {
        return mPreference.getBoolean(key, defValue);
    }

    public int get(String key, int defValue) {
        return mPreference.getInt(key, defValue);
    }

    public float get(String key, float defValue) {
        return mPreference.getFloat(key, defValue);
    }

    public static String getString(String key, String defValue) {
        if (EmiReadingApplication.getAppContext() != null) {
            return PreferenceManager.getDefaultSharedPreferences(EmiReadingApplication.getAppContext()).getString(key, defValue);
        }
        return defValue;
    }

    public static long getLong(String key, long defValue) {
        if (EmiReadingApplication.getAppContext() != null) {
            return PreferenceManager.getDefaultSharedPreferences(EmiReadingApplication.getAppContext()).getLong(key, defValue);
        }
        return defValue;
    }

    public static float getFloat(String key, float defValue) {
        if (EmiReadingApplication.getAppContext() != null) {
            return PreferenceManager.getDefaultSharedPreferences(EmiReadingApplication.getAppContext()).getFloat(key, defValue);
        }
        return defValue;
    }

    public static void put(String key, String value) {
        putString(key, value);
    }

    public static void put(String key, int value) {
        putInt(key, value);
    }

    public static void put(String key, float value) {
        putFloat(key, value);
    }

    public static void put(String key, boolean value) {
        putBoolean(key, value);
    }

    public static void putFloat(String key, float value) {
        if (EmiReadingApplication.getAppContext() != null) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(EmiReadingApplication.getAppContext());
            Editor editor = sharedPreferences.edit();
            editor.putFloat(key, value);
            editor.apply();
        }
    }

    public static SharedPreferences getPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(EmiReadingApplication.getAppContext());
    }

    public static int getInt(String key, int defValue) {
        if (EmiReadingApplication.getAppContext() != null) {
            return PreferenceManager.getDefaultSharedPreferences(EmiReadingApplication.getAppContext()).getInt(key, defValue);
        }
        return defValue;
    }

    public static boolean getBoolean(String key, boolean defValue) {
        if (EmiReadingApplication.getAppContext() != null) {
            return PreferenceManager.getDefaultSharedPreferences(EmiReadingApplication.getAppContext()).getBoolean(key, defValue);
        }
        return defValue;
    }

    public static void putStringProcess(String key, String value) {
        if (EmiReadingApplication.getAppContext() != null) {
            SharedPreferences sharedPreferences = EmiReadingApplication.getAppContext().getSharedPreferences("preference_mu", Context.MODE_MULTI_PROCESS);
            Editor editor = sharedPreferences.edit();
            editor.putString(key, value);
            editor.apply();
        }
    }

    public static void putIntProcess(String key, int value) {
        if (EmiReadingApplication.getAppContext() != null) {
            SharedPreferences sharedPreferences = EmiReadingApplication.getAppContext().getSharedPreferences("preference_mu", Context.MODE_MULTI_PROCESS);
            Editor editor = sharedPreferences.edit();
            editor.putInt(key, value);
            editor.apply();
        }
    }

    public static int getIntProcess(String key, int defValue) {
        if (EmiReadingApplication.getAppContext() != null) {
            SharedPreferences sharedPreferences = EmiReadingApplication.getAppContext().getSharedPreferences("preference_mu", Context.MODE_MULTI_PROCESS);
            return sharedPreferences.getInt(key, defValue);
        }
        return defValue;
    }

    public static void putLongProcess(String key, long value) {
        if (EmiReadingApplication.getAppContext() != null) {
            SharedPreferences sharedPreferences = EmiReadingApplication.getAppContext().getSharedPreferences("preference_mu", Context.MODE_MULTI_PROCESS);
            Editor editor = sharedPreferences.edit();
            editor.putLong(key, value);
            editor.apply();
        }
    }

    public static long getLongProcess(String key, long defValue) {
        if (EmiReadingApplication.getAppContext() != null) {
            SharedPreferences sharedPreferences = EmiReadingApplication.getAppContext().getSharedPreferences("preference_mu", Context.MODE_MULTI_PROCESS);
            return sharedPreferences.getLong(key, defValue);
        }
        return defValue;
    }

    public static String getStringProcess(String key, String defValue) {
        if (EmiReadingApplication.getAppContext() != null) {
            SharedPreferences sharedPreferences = EmiReadingApplication.getAppContext().getSharedPreferences("preference_mu", Context.MODE_MULTI_PROCESS);
            return sharedPreferences.getString(key, defValue);
        }
        return defValue;
    }

    public static boolean hasString(String key) {
        if (EmiReadingApplication.getAppContext() != null) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(EmiReadingApplication.getAppContext());
            return sharedPreferences.contains(key);
        }
        return false;
    }

    public static void putString(String key, String value) {
        if (EmiReadingApplication.getAppContext() != null) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(EmiReadingApplication.getAppContext());
            Editor editor = sharedPreferences.edit();
            editor.putString(key, value);
            editor.apply();
        }
    }

    public static void putLong(String key, long value) {
        if (EmiReadingApplication.getAppContext() != null) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(EmiReadingApplication.getAppContext());
            Editor editor = sharedPreferences.edit();
            editor.putLong(key, value);
            editor.apply();
        }
    }

    public static void putBoolean(String key, boolean value) {
        if (EmiReadingApplication.getAppContext() != null) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(EmiReadingApplication.getAppContext());
            Editor editor = sharedPreferences.edit();
            editor.putBoolean(key, value);
            editor.apply();
        }
    }

    public static void putInt(String key, int value) {
        if (EmiReadingApplication.getAppContext() != null) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(EmiReadingApplication.getAppContext());
            Editor editor = sharedPreferences.edit();
            editor.putInt(key, value);
            editor.apply();
        }
    }

    public static void remove(String... keys) {
        if (keys != null && EmiReadingApplication.getAppContext() != null) {
            SharedPreferences sharedPreferences = EmiReadingApplication.getAppContext().getSharedPreferences("preference_mu", Context.MODE_MULTI_PROCESS);
            Editor editor = sharedPreferences.edit();
            for (String key : keys) {
                editor.remove(key);
            }
            editor.apply();
        }
    }
}
