package Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;

import com.google.gson.Gson;

import java.util.List;

import DataModels.Repository;


public class SharedFuncations {

    public static SharedPreferences pref;
    public static SharedPreferences.Editor editor;

    public static void InitSharedPreferance(Context context) {

        pref = context.getSharedPreferences("ZadTask", 0);
        editor = pref.edit();

        if (pref.getString("data", "Null").equals("Null"))
            editor.putString("data", "");

        editor.commit();

    }


    public static void ClearSavedData(Context context) {

        pref = context.getSharedPreferences("ZadTask", 0);
        editor = pref.edit();

        editor.putString("data", "");
        editor.commit();

    }


    public static void SaveReposToShared(List<Repository> current, Context context) {

        InitSharedPreferance(context);

        Gson gson = new Gson();
        String json = gson.toJson(current);
        editor.putString("data", json);
        editor.commit();

    }


    public static boolean isNetworkConnected(Context context) {

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }


}
