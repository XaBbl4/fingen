package com.yoshione.fingen.dropbox;

import android.os.AsyncTask;
import android.util.Log;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.yoshione.fingen.interfaces.IOnComplete;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by slv on 01.11.2016.
 * a
 */

public class DeleteTask extends AsyncTask {

    private DbxClientV2 dbxClient;
    private File file;
    private IOnComplete mOnComplete;

    public DeleteTask(DbxClientV2 dbxClient, File file, IOnComplete onComplete) {
        this.dbxClient = dbxClient;
        this.file = file;
        mOnComplete = onComplete;
    }

    @Override
    protected Object doInBackground(Object[] params) {
        try {
            // Delete from Dropbox
            InputStream inputStream = new FileInputStream(file);
            dbxClient.files().deleteV2("/" + file.getName());
            Log.d("Delete Status", "Success");
        } catch (DbxException e) {
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

