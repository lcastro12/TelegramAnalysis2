package net.hockeyapp.android.tasks;

import android.content.Context;
import java.net.URL;
import net.hockeyapp.android.listeners.DownloadFileListener;

public class GetFileSizeTask extends DownloadFileTask {
    private long size;

    public GetFileSizeTask(Context context, String urlString, DownloadFileListener notifier) {
        super(context, urlString, notifier);
    }

    protected Long doInBackground(Void... args) {
        try {
            return Long.valueOf((long) createConnection(new URL(getURLString()), 6).getContentLength());
        } catch (Exception e) {
            e.printStackTrace();
            return Long.valueOf(0);
        }
    }

    protected void onProgressUpdate(Integer... args) {
    }

    protected void onPostExecute(Long result) {
        this.size = result.longValue();
        if (this.size > 0) {
            this.notifier.downloadSuccessful(this);
        } else {
            this.notifier.downloadFailed(this, Boolean.valueOf(false));
        }
    }

    public long getSize() {
        return this.size;
    }
}
