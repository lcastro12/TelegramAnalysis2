package org.telegram.messenger;

import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Locale;
import org.telegram.messenger.time.FastDateFormat;

public class FileLog {
    private static volatile FileLog Instance = null;
    private File currentFile = null;
    private FastDateFormat dateFormat = null;
    private DispatchQueue logQueue = null;
    private File networkFile = null;
    private OutputStreamWriter streamWriter = null;

    public static FileLog getInstance() {
        FileLog localInstance = Instance;
        if (localInstance == null) {
            synchronized (FileLog.class) {
                try {
                    localInstance = Instance;
                    if (localInstance == null) {
                        FileLog localInstance2 = new FileLog();
                        try {
                            Instance = localInstance2;
                            localInstance = localInstance2;
                        } catch (Throwable th) {
                            Throwable th2 = th;
                            localInstance = localInstance2;
                            throw th2;
                        }
                    }
                } catch (Throwable th3) {
                    th2 = th3;
                    throw th2;
                }
            }
        }
        return localInstance;
    }

    public FileLog() {
        if (BuildVars.DEBUG_VERSION) {
            this.dateFormat = FastDateFormat.getInstance("dd_MM_yyyy_HH_mm_ss", Locale.US);
            try {
                File sdCard = ApplicationLoader.applicationContext.getExternalFilesDir(null);
                if (sdCard != null) {
                    File dir = new File(sdCard.getAbsolutePath() + "/logs");
                    dir.mkdirs();
                    this.currentFile = new File(dir, this.dateFormat.format(System.currentTimeMillis()) + ".txt");
                    try {
                        this.logQueue = new DispatchQueue("logQueue");
                        this.currentFile.createNewFile();
                        this.streamWriter = new OutputStreamWriter(new FileOutputStream(this.currentFile));
                        this.streamWriter.write("-----start log " + this.dateFormat.format(System.currentTimeMillis()) + "-----\n");
                        this.streamWriter.flush();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }

    public static String getNetworkLogPath() {
        if (!BuildVars.DEBUG_VERSION) {
            return "";
        }
        try {
            File sdCard = ApplicationLoader.applicationContext.getExternalFilesDir(null);
            if (sdCard == null) {
                return "";
            }
            File dir = new File(sdCard.getAbsolutePath() + "/logs");
            dir.mkdirs();
            getInstance().networkFile = new File(dir, getInstance().dateFormat.format(System.currentTimeMillis()) + "_net.txt");
            return getInstance().networkFile.getAbsolutePath();
        } catch (Throwable e) {
            e.printStackTrace();
            return "";
        }
    }

    public static void m610e(final String tag, final String message, final Throwable exception) {
        if (BuildVars.DEBUG_VERSION) {
            Log.e(tag, message, exception);
            if (getInstance().streamWriter != null) {
                getInstance().logQueue.postRunnable(new Runnable() {
                    public void run() {
                        try {
                            FileLog.getInstance().streamWriter.write(FileLog.getInstance().dateFormat.format(System.currentTimeMillis()) + " E/" + tag + "﹕ " + message + "\n");
                            FileLog.getInstance().streamWriter.write(exception.toString());
                            FileLog.getInstance().streamWriter.flush();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
    }

    public static void m609e(final String tag, final String message) {
        if (BuildVars.DEBUG_VERSION) {
            Log.e(tag, message);
            if (getInstance().streamWriter != null) {
                getInstance().logQueue.postRunnable(new Runnable() {
                    public void run() {
                        try {
                            FileLog.getInstance().streamWriter.write(FileLog.getInstance().dateFormat.format(System.currentTimeMillis()) + " E/" + tag + "﹕ " + message + "\n");
                            FileLog.getInstance().streamWriter.flush();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
    }

    public static void m611e(final String tag, final Throwable e) {
        if (BuildVars.DEBUG_VERSION) {
            e.printStackTrace();
            if (getInstance().streamWriter != null) {
                getInstance().logQueue.postRunnable(new Runnable() {
                    public void run() {
                        try {
                            FileLog.getInstance().streamWriter.write(FileLog.getInstance().dateFormat.format(System.currentTimeMillis()) + " E/" + tag + "﹕ " + e + "\n");
                            for (StackTraceElement el : e.getStackTrace()) {
                                FileLog.getInstance().streamWriter.write(FileLog.getInstance().dateFormat.format(System.currentTimeMillis()) + " E/" + tag + "﹕ " + el + "\n");
                            }
                            FileLog.getInstance().streamWriter.flush();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            } else {
                e.printStackTrace();
            }
        }
    }

    public static void m608d(final String tag, final String message) {
        if (BuildVars.DEBUG_VERSION) {
            Log.d(tag, message);
            if (getInstance().streamWriter != null) {
                getInstance().logQueue.postRunnable(new Runnable() {
                    public void run() {
                        try {
                            FileLog.getInstance().streamWriter.write(FileLog.getInstance().dateFormat.format(System.currentTimeMillis()) + " D/" + tag + "﹕ " + message + "\n");
                            FileLog.getInstance().streamWriter.flush();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
    }

    public static void m612w(final String tag, final String message) {
        if (BuildVars.DEBUG_VERSION) {
            Log.w(tag, message);
            if (getInstance().streamWriter != null) {
                getInstance().logQueue.postRunnable(new Runnable() {
                    public void run() {
                        try {
                            FileLog.getInstance().streamWriter.write(FileLog.getInstance().dateFormat.format(System.currentTimeMillis()) + " W/" + tag + ": " + message + "\n");
                            FileLog.getInstance().streamWriter.flush();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
    }

    public static void cleanupLogs() {
        for (File file : new File(ApplicationLoader.applicationContext.getExternalFilesDir(null).getAbsolutePath() + "/logs").listFiles()) {
            if ((getInstance().currentFile == null || !file.getAbsolutePath().equals(getInstance().currentFile.getAbsolutePath())) && (getInstance().networkFile == null || !file.getAbsolutePath().equals(getInstance().networkFile.getAbsolutePath()))) {
                file.delete();
            }
        }
    }
}
