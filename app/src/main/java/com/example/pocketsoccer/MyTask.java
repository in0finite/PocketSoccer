package com.example.pocketsoccer;

import android.os.AsyncTask;

public class MyTask extends AsyncTask<String, Integer, String[]> {

    private Runnable m_runnable;


    public MyTask(Runnable runnable) {
        this.m_runnable = runnable;
    }

    @Override
    protected String[] doInBackground(String... params) {

        try {
            while (true) {
                Thread.sleep(50);
                if (this.isCancelled())
                    break;
                this.publishProgress();
            }
        } catch (InterruptedException e) {
            //e.printStackTrace();
        }

        System.out.println("Task done");

        return new String[0];
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(String[] result) {
        super.onPostExecute(result);
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);

        // we are on UI thread

        m_runnable.run();

    }

}
