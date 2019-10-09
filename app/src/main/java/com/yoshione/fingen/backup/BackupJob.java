package com.yoshione.fingen.backup;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.preference.PreferenceManager;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.WriteMode;
import com.evernote.android.job.DailyJob;
import com.evernote.android.job.JobRequest;
import com.yoshione.fingen.BuildConfig;
import com.yoshione.fingen.DBHelper;
import com.yoshione.fingen.FGApplication;
import com.yoshione.fingen.FgConst;
import com.yoshione.fingen.csv.CsvImporter;
import com.yoshione.fingen.dao.TransactionsDAO;
import com.yoshione.fingen.dropbox.DeleteTask;
import com.yoshione.fingen.dropbox.DropboxClient;
import com.yoshione.fingen.dropbox.UploadTask;
import com.yoshione.fingen.interfaces.IOnComplete;
import com.yoshione.fingen.model.Transaction;
import com.yoshione.fingen.utils.FileUtils;
import com.yoshione.fingen.utils.PrefUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by slv on 27.10.2017.
 * /
 */

public class BackupJob extends DailyJob {

    public static final String TAG = "job_backup_tag";

    public static int schedule() {
        // schedule between 1 and 6 AM
        return DailyJob.schedule(new JobRequest.Builder(TAG),
                TimeUnit.HOURS.toMillis(01) + TimeUnit.MINUTES.toMillis(00),
                TimeUnit.HOURS.toMillis(03) + TimeUnit.MINUTES.toMillis(00));
    }

    @NonNull
    @Override
    protected DailyJobResult onRunDailyJob(Params params) {
        if (!BuildConfig.FLAVOR.equals("nd")) {
            Context context = FGApplication.getContext();
            SharedPreferences dropboxPrefs = context.getSharedPreferences("com.yoshione.fingen.dropbox", Context.MODE_PRIVATE);
            String token = dropboxPrefs.getString("dropbox-token", null);
            /****************/
            List<Transaction> transactions;
            transactions = TransactionsDAO.getInstance(context).getTransactionsByDepartmentDate(context);

            /***********/

            try {
                File zip = DBHelper.getInstance(context).backupDB(true);
                /****************/
                String Dep = "";
                Dep = "" + PrefUtils.getDefaultDepartment(context);

                String path = FileUtils.getExtFingenBackupFolder() + "CSV_Export_" + Dep + ".csv";

                File CSVBackupFile = FileUtils.zip(new String[]{context.getDatabasePath("fingen.db").toString()}, path);
                if (CSVBackupFile.exists()) {
//                try {
                    CSVBackupFile.delete();
  /*              }
               catch (IOException ee) {
                    ee.printStackTrace();
                    return;
                }*/
                }
                final CsvImporter csvImporter = new CsvImporter(context, path, 0, true);
                /************/

                if (token != null && zip != null) {
                    /************/
//                csvImporter.setmCsvImportProgressChangeListener(myCsvImportProgressChangeListener);

                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            csvImporter.saveCSV(transactions, true);
                        }
                    });
                    t.start();
                    /************/
                    Thread tt = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            new DeleteTask(DropboxClient.getClient(token), CSVBackupFile, new IOnComplete() {
                                @Override
                                public void onComplete() {
                                }
                            }).execute();
                        }
                    });
                    tt.start();
                    try {
                        tt.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    Thread ttt = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            new UploadTask(DropboxClient.getClient(token), zip, new IOnComplete() {
                                @Override
                                public void onComplete() {
                                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                                    prefs.edit().putLong(FgConst.PREF_SHOW_LAST_SUCCESFUL_BACKUP_TO_DROPBOX, new Date().getTime()).apply();
                                }
                            }).execute();
                        }
                    });
                    ttt.start();
/**************/
                    Thread tttt = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            new UploadTask(DropboxClient.getClient(token), CSVBackupFile, new IOnComplete() {
                                @Override
                                public void onComplete() {
                                }
                            }).execute();
                        }
                    });
                    tttt.start();
                    try {
                        tttt.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    /*************/
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return DailyJobResult.SUCCESS;
    }
}
