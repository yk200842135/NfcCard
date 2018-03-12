/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.reformer.cardemulate;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Utility class for persisting account numbers to disk.
 *
 * <p>The default SharedPreferences instance is used as the backing storage. Values are cached
 * in memory for performance.
 *
 * <p>This class is thread-safe.
 */
public class AccountStorage {
    private static final String PREF_ACCOUNT_NUMBER = "card_pref";
    private static final String PREF_ACCOUNT_KEY = "key_pref";
    private static final String PREF_ACCOUNT_TOKEN = "token_pref";
    private static final String PREF_ACCOUNT_SHAKE = "shake_pref";
    private static final String PREF_ACCOUNT_FEEL = "feel_pref";
    private static final String DEFAULT_ACCOUNT_NUMBER = "12345678";
    private static final String DEFAULT_ACCOUNT_KEY = "n46jF1uYE93LPg8V";
    private static final String TAG = "AccountStorage";
    private static String sAccount = null;
    private static String sKey = null;
    private static String sToken = null;
    private static final Object sAccountLock = new Object();

    public static void SetAccount(Context c, String s) {
        synchronized(sAccountLock) {
            Log.i(TAG, "Setting account number: " + s);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
            prefs.edit().putString(PREF_ACCOUNT_NUMBER, s).commit();
            sAccount = s;
        }
    }

    public static String GetAccount(Context c) {
        synchronized (sAccountLock) {
            if (sAccount == null) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
                String account = prefs.getString(PREF_ACCOUNT_NUMBER, DEFAULT_ACCOUNT_NUMBER);
                sAccount = account;
            }
            return sAccount;
        }
    }

    public static void SetKey(Context c, String s) {
        synchronized(sAccountLock) {
            Log.i(TAG, "Setting account number: " + s);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
            prefs.edit().putString(PREF_ACCOUNT_KEY, s).commit();
            sKey = s;
        }
    }

    public static String GetKey(Context c) {
        synchronized (sAccountLock) {
            if (sKey == null) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
                String account = prefs.getString(PREF_ACCOUNT_KEY, DEFAULT_ACCOUNT_KEY);
                sKey = account;
            }
            return sKey;
        }
    }

    public static void SetToken(Context c, String s) {
        synchronized(sAccountLock) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
            prefs.edit().putString(PREF_ACCOUNT_TOKEN, s).commit();
        }
    }

    public static String GetToken(Context c) {
        synchronized (sAccountLock) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
            return prefs.getString(PREF_ACCOUNT_TOKEN, "");
        }
    }

    public static void SetShake(Context c, boolean b) {
        synchronized(sAccountLock) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
            prefs.edit().putBoolean(PREF_ACCOUNT_SHAKE, b).commit();
        }
    }

    public static boolean GetShake(Context c) {
        synchronized (sAccountLock) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
            return prefs.getBoolean(PREF_ACCOUNT_SHAKE, false);
        }
    }

    public static void SetFeel(Context c, boolean b) {
        synchronized(sAccountLock) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
            prefs.edit().putBoolean(PREF_ACCOUNT_FEEL, b).commit();
        }
    }

    public static boolean GetFeel(Context c) {
        synchronized (sAccountLock) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
            return prefs.getBoolean(PREF_ACCOUNT_FEEL, false);
        }
    }
}
