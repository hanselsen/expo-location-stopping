package com.myapp;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

public class Telegram {
    private final static String TAG = "Telegram";
    private static final ConcurrentLinkedQueue<String> messages = new ConcurrentLinkedQueue<>();
    private static final Semaphore mutex = new Semaphore(1, true);

    public static void Log(String message) {
//        if(message != null) return;
        messages.add(message);
        if(!mutex.tryAcquire()) return;
        flush();
    }

    private static void flush() {
        Thread t = new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    while (!messages.isEmpty()) {
                        String msg = messages.poll();
                        URL url;
                        InputStreamReader reader = null;
                        InputStream in = null;
                        BufferedReader bufferedReader = null;
                        HttpURLConnection urlConnection = null;
                        try {
                            String message = msg
                                    .replace("_", "\\_")
                                    .replace("|", ":");
                            url = new URL("https://api.telegram.org/bot5805621729:AAEY_Osx2wbzzLu1cqNY1PsIwEmWCL1Sq-s/sendMessage?chat_id=-801662178&parse_mode=markdown&text=" + message);
                            urlConnection = (HttpURLConnection) url.openConnection();
                            in = new BufferedInputStream(urlConnection.getInputStream());
                            reader = new InputStreamReader(in);
                            bufferedReader = new BufferedReader(reader);
                            String res = bufferedReader.readLine();
                            Log.d(TAG, "Telegram message send");
                            Log.d(TAG, res);
                            Thread.sleep(200);
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            if (urlConnection != null) {
                                urlConnection.disconnect();
                            }
                            if (bufferedReader != null) {
                                try {
                                    bufferedReader.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            if (in != null) {
                                try {
                                    in.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            if (reader != null) {
                                try {
                                    reader.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                } finally {
                    mutex.release();
                }
            }
        };
        t.start();
    }
}
