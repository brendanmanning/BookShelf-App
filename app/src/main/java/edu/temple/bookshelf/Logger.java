package edu.temple.bookshelf;

import android.content.SharedPreferences;

import java.util.Iterator;
import java.util.Map;

public class Logger {

    public static void logInternalStorageState(SharedPreferences sharedPreferences) {

        Map<String, ?> prefs = sharedPreferences.getAll();
        Iterator keyIterator = prefs.keySet().iterator();
        while(keyIterator.hasNext()) {
            String key = keyIterator.next().toString();

            if(key.startsWith("PLAYER_BOOK_PROGRESS/id=")) {
                System.out.println(key + "   ->   progress=" + sharedPreferences.getInt(key, -9999));
            }

            else if(key.equals("PLAYER_CURRENT_BOOK")) {
                System.out.println(key + "   ->   id=" + sharedPreferences.getInt(key, -9999));
            }

        }



    }

}
