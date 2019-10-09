package com.yoshione.fingen.backup;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

/**
 * Created by slv on 27.10.2017.
 *
 */

public class RestoreJobCreator implements JobCreator {

    @Override
    @Nullable
    public Job create(@NonNull String tag) {
        switch (tag) {
            case RestoreJob.TAG:
                return new RestoreJob();
            default:
                return null;
        }
    }
}
