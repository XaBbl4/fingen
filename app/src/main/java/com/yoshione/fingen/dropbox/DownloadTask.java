package com.yoshione.fingen.dropbox;

import android.os.AsyncTask;
import android.util.Log;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.yoshione.fingen.interfaces.IOnComplete;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by slv on 01.11.2016.
 * a
 */

public class DownloadTask extends AsyncTask {

    private DbxClientV2 dbxClient;
    private File file;
    private IOnComplete mOnComplete;

    public DownloadTask(DbxClientV2 dbxClient, File file, IOnComplete onComplete) {
        this.dbxClient = dbxClient;
        this.file = file;
        mOnComplete = onComplete;
    }

    @Override
    protected Object doInBackground(Object[] params) {
        try {
            // Upload to Dropbox
            OutputStream outputStream = new FileOutputStream(file);
            dbxClient.files().download("/" + file.getName()) //Path in the user's Dropbox to save the file.
            .download(outputStream);
            //.uploadAndFinish(inputStream);
            Log.d("Download Status", "Success");
        } catch (DbxException e) {
            Log.d("Download Status", "Bad");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        if (mOnComplete != null) {
            mOnComplete.onComplete();
        }
    }
}

