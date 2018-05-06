package org.telegram.messenger;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.Audio;
import org.telegram.tgnet.TLRPC.Document;
import org.telegram.tgnet.TLRPC.FileLocation;
import org.telegram.tgnet.TLRPC.InputFileLocation;
import org.telegram.tgnet.TLRPC.TL_audio;
import org.telegram.tgnet.TLRPC.TL_audioEncrypted;
import org.telegram.tgnet.TLRPC.TL_document;
import org.telegram.tgnet.TLRPC.TL_documentEncrypted;
import org.telegram.tgnet.TLRPC.TL_error;
import org.telegram.tgnet.TLRPC.TL_fileEncryptedLocation;
import org.telegram.tgnet.TLRPC.TL_fileLocation;
import org.telegram.tgnet.TLRPC.TL_inputAudioFileLocation;
import org.telegram.tgnet.TLRPC.TL_inputDocumentFileLocation;
import org.telegram.tgnet.TLRPC.TL_inputEncryptedFileLocation;
import org.telegram.tgnet.TLRPC.TL_inputFileLocation;
import org.telegram.tgnet.TLRPC.TL_inputVideoFileLocation;
import org.telegram.tgnet.TLRPC.TL_upload_file;
import org.telegram.tgnet.TLRPC.TL_upload_getFile;
import org.telegram.tgnet.TLRPC.TL_video;
import org.telegram.tgnet.TLRPC.TL_videoEncrypted;
import org.telegram.tgnet.TLRPC.Video;

public class FileLoadOperation {
    private static final int bigFileSizeFrom = 1048576;
    private static final int downloadChunkSize = 32768;
    private static final int downloadChunkSizeBig = 131072;
    private static final int maxDownloadRequests = 4;
    private static final int maxDownloadRequestsBig = 2;
    private static final int stateDownloading = 1;
    private static final int stateFailed = 2;
    private static final int stateFinished = 3;
    private static final int stateIdle = 0;
    private File cacheFileFinal;
    private File cacheFileTemp;
    private File cacheIvTemp;
    private int currentDownloadChunkSize;
    private int currentMaxDownloadRequests;
    private int datacenter_id;
    private ArrayList<RequestInfo> delayedRequestInfos;
    private FileLoadOperationDelegate delegate;
    private int downloadedBytes;
    private String ext;
    private RandomAccessFile fileOutputStream;
    private RandomAccessFile fiv;
    private boolean isForceRequest = false;
    private byte[] iv;
    private byte[] key;
    private InputFileLocation location;
    private int nextDownloadOffset = 0;
    private int renameRetryCount;
    private ArrayList<RequestInfo> requestInfos;
    private int requestsCount;
    private volatile int state = 0;
    private File storePath = null;
    private File tempPath = null;
    private int totalBytesCount;

    class C03421 implements Runnable {
        C03421() {
        }

        public void run() {
            FileLoadOperation.this.delegate.didFailedLoadingFile(FileLoadOperation.this, 0);
        }
    }

    class C03432 implements Runnable {
        C03432() {
        }

        public void run() {
            FileLoadOperation.this.delegate.didFailedLoadingFile(FileLoadOperation.this, 0);
        }
    }

    class C03443 implements Runnable {
        C03443() {
        }

        public void run() {
            FileLoadOperation.this.delegate.didFailedLoadingFile(FileLoadOperation.this, 0);
        }
    }

    class C03454 implements Runnable {
        C03454() {
        }

        public void run() {
            FileLoadOperation.this.delegate.didFailedLoadingFile(FileLoadOperation.this, 0);
        }
    }

    class C03465 implements Runnable {
        C03465() {
        }

        public void run() {
            if (FileLoadOperation.this.totalBytesCount == 0 || FileLoadOperation.this.downloadedBytes != FileLoadOperation.this.totalBytesCount) {
                FileLoadOperation.this.startDownloadRequest();
                return;
            }
            try {
                FileLoadOperation.this.onFinishLoadingFile();
            } catch (Exception e) {
                FileLoadOperation.this.delegate.didFailedLoadingFile(FileLoadOperation.this, 0);
            }
        }
    }

    class C03476 implements Runnable {
        C03476() {
        }

        public void run() {
            if (FileLoadOperation.this.state != 3 && FileLoadOperation.this.state != 2) {
                FileLoadOperation.this.state = 2;
                FileLoadOperation.this.cleanup();
                if (FileLoadOperation.this.requestInfos != null) {
                    Iterator i$ = FileLoadOperation.this.requestInfos.iterator();
                    while (i$.hasNext()) {
                        RequestInfo requestInfo = (RequestInfo) i$.next();
                        if (requestInfo.requestToken != 0) {
                            ConnectionsManager.getInstance().cancelRequest(requestInfo.requestToken, true);
                        }
                    }
                }
                FileLoadOperation.this.delegate.didFailedLoadingFile(FileLoadOperation.this, 1);
            }
        }
    }

    class C03487 implements Runnable {
        C03487() {
        }

        public void run() {
            try {
                FileLoadOperation.this.onFinishLoadingFile();
            } catch (Exception e) {
                FileLoadOperation.this.delegate.didFailedLoadingFile(FileLoadOperation.this, 0);
            }
        }
    }

    public interface FileLoadOperationDelegate {
        void didChangedLoadProgress(FileLoadOperation fileLoadOperation, float f);

        void didFailedLoadingFile(FileLoadOperation fileLoadOperation, int i);

        void didFinishLoadingFile(FileLoadOperation fileLoadOperation, File file);
    }

    private static class RequestInfo {
        private int offset;
        private int requestToken;
        private TL_upload_file response;

        private RequestInfo() {
            this.requestToken = 0;
            this.offset = 0;
            this.response = null;
        }
    }

    public FileLoadOperation(FileLocation photoLocation, String extension, int size) {
        if (photoLocation instanceof TL_fileEncryptedLocation) {
            this.location = new TL_inputEncryptedFileLocation();
            this.location.id = photoLocation.volume_id;
            this.location.volume_id = photoLocation.volume_id;
            this.location.access_hash = photoLocation.secret;
            this.location.local_id = photoLocation.local_id;
            this.iv = new byte[32];
            System.arraycopy(photoLocation.iv, 0, this.iv, 0, this.iv.length);
            this.key = photoLocation.key;
            this.datacenter_id = photoLocation.dc_id;
        } else if (photoLocation instanceof TL_fileLocation) {
            this.location = new TL_inputFileLocation();
            this.location.volume_id = photoLocation.volume_id;
            this.location.secret = photoLocation.secret;
            this.location.local_id = photoLocation.local_id;
            this.datacenter_id = photoLocation.dc_id;
        }
        this.totalBytesCount = size;
        if (extension == null) {
            extension = "jpg";
        }
        this.ext = extension;
    }

    public FileLoadOperation(Video videoLocation) {
        if (videoLocation instanceof TL_videoEncrypted) {
            this.location = new TL_inputEncryptedFileLocation();
            this.location.id = videoLocation.id;
            this.location.access_hash = videoLocation.access_hash;
            this.datacenter_id = videoLocation.dc_id;
            this.iv = new byte[32];
            System.arraycopy(videoLocation.iv, 0, this.iv, 0, this.iv.length);
            this.key = videoLocation.key;
        } else if (videoLocation instanceof TL_video) {
            this.location = new TL_inputVideoFileLocation();
            this.datacenter_id = videoLocation.dc_id;
            this.location.id = videoLocation.id;
            this.location.access_hash = videoLocation.access_hash;
        }
        this.totalBytesCount = videoLocation.size;
        this.ext = ".mp4";
    }

    public FileLoadOperation(Audio audioLocation) {
        if (audioLocation instanceof TL_audioEncrypted) {
            this.location = new TL_inputEncryptedFileLocation();
            this.location.id = audioLocation.id;
            this.location.access_hash = audioLocation.access_hash;
            this.datacenter_id = audioLocation.dc_id;
            this.iv = new byte[32];
            System.arraycopy(audioLocation.iv, 0, this.iv, 0, this.iv.length);
            this.key = audioLocation.key;
        } else if (audioLocation instanceof TL_audio) {
            this.location = new TL_inputAudioFileLocation();
            this.datacenter_id = audioLocation.dc_id;
            this.location.id = audioLocation.id;
            this.location.access_hash = audioLocation.access_hash;
        }
        this.totalBytesCount = audioLocation.size;
        this.ext = ".ogg";
    }

    public FileLoadOperation(Document documentLocation) {
        if (documentLocation instanceof TL_documentEncrypted) {
            this.location = new TL_inputEncryptedFileLocation();
            this.location.id = documentLocation.id;
            this.location.access_hash = documentLocation.access_hash;
            this.datacenter_id = documentLocation.dc_id;
            this.iv = new byte[32];
            System.arraycopy(documentLocation.iv, 0, this.iv, 0, this.iv.length);
            this.key = documentLocation.key;
        } else if (documentLocation instanceof TL_document) {
            this.location = new TL_inputDocumentFileLocation();
            this.datacenter_id = documentLocation.dc_id;
            this.location.id = documentLocation.id;
            this.location.access_hash = documentLocation.access_hash;
        }
        this.totalBytesCount = documentLocation.size;
        this.ext = FileLoader.getDocumentFileName(documentLocation);
        if (this.ext != null) {
            int idx = this.ext.lastIndexOf(".");
            if (idx != -1) {
                this.ext = this.ext.substring(idx);
                if (this.ext.length() <= 1) {
                    this.ext = "";
                    return;
                }
                return;
            }
        }
        this.ext = "";
    }

    public void setForceRequest(boolean forceRequest) {
        this.isForceRequest = forceRequest;
    }

    public boolean isForceRequest() {
        return this.isForceRequest;
    }

    public void setPaths(File store, File temp) {
        this.storePath = store;
        this.tempPath = temp;
    }

    public void start() {
        if (this.state == 0) {
            this.currentDownloadChunkSize = this.totalBytesCount >= 1048576 ? 131072 : 32768;
            this.currentMaxDownloadRequests = this.totalBytesCount >= 1048576 ? 2 : 4;
            this.requestInfos = new ArrayList(this.currentMaxDownloadRequests);
            this.delayedRequestInfos = new ArrayList(this.currentMaxDownloadRequests - 1);
            this.state = 1;
            if (this.location == null) {
                Utilities.stageQueue.postRunnable(new C03421());
                return;
            }
            String fileNameTemp;
            String fileNameFinal;
            String fileNameIv = null;
            if (this.location.volume_id == 0 || this.location.local_id == 0) {
                fileNameTemp = this.datacenter_id + "_" + this.location.id + ".temp";
                fileNameFinal = this.datacenter_id + "_" + this.location.id + this.ext;
                if (this.key != null) {
                    fileNameIv = this.datacenter_id + "_" + this.location.id + ".iv";
                }
                if (this.datacenter_id == 0 || this.location.id == 0) {
                    cleanup();
                    Utilities.stageQueue.postRunnable(new C03443());
                    return;
                }
            }
            fileNameTemp = this.location.volume_id + "_" + this.location.local_id + ".temp";
            fileNameFinal = this.location.volume_id + "_" + this.location.local_id + "." + this.ext;
            if (this.key != null) {
                fileNameIv = this.location.volume_id + "_" + this.location.local_id + ".iv";
            }
            if (this.datacenter_id == Integer.MIN_VALUE || this.location.volume_id == -2147483648L || this.datacenter_id == 0) {
                cleanup();
                Utilities.stageQueue.postRunnable(new C03432());
                return;
            }
            this.cacheFileFinal = new File(this.storePath, fileNameFinal);
            if (!(!this.cacheFileFinal.exists() || this.totalBytesCount == 0 || ((long) this.totalBytesCount) == this.cacheFileFinal.length())) {
                this.cacheFileFinal.delete();
            }
            if (this.cacheFileFinal.exists()) {
                try {
                    onFinishLoadingFile();
                    return;
                } catch (Exception e) {
                    this.delegate.didFailedLoadingFile(this, 0);
                    return;
                }
            }
            this.cacheFileTemp = new File(this.tempPath, fileNameTemp);
            if (this.cacheFileTemp.exists()) {
                this.downloadedBytes = (int) this.cacheFileTemp.length();
                int i = (this.downloadedBytes / this.currentDownloadChunkSize) * this.currentDownloadChunkSize;
                this.downloadedBytes = i;
                this.nextDownloadOffset = i;
            }
            if (BuildVars.DEBUG_VERSION) {
                FileLog.m608d("tmessages", "start loading file to temp = " + this.cacheFileTemp + " final = " + this.cacheFileFinal);
            }
            if (fileNameIv != null) {
                this.cacheIvTemp = new File(this.tempPath, fileNameIv);
                try {
                    this.fiv = new RandomAccessFile(this.cacheIvTemp, "rws");
                    long len = this.cacheIvTemp.length();
                    if (len <= 0 || len % 32 != 0) {
                        this.downloadedBytes = 0;
                    } else {
                        this.fiv.read(this.iv, 0, 32);
                    }
                } catch (Throwable e2) {
                    FileLog.m611e("tmessages", e2);
                    this.downloadedBytes = 0;
                }
            }
            try {
                this.fileOutputStream = new RandomAccessFile(this.cacheFileTemp, "rws");
                if (this.downloadedBytes != 0) {
                    this.fileOutputStream.seek((long) this.downloadedBytes);
                }
            } catch (Throwable e22) {
                FileLog.m611e("tmessages", e22);
            }
            if (this.fileOutputStream == null) {
                cleanup();
                Utilities.stageQueue.postRunnable(new C03454());
                return;
            }
            Utilities.stageQueue.postRunnable(new C03465());
        }
    }

    public void cancel() {
        Utilities.stageQueue.postRunnable(new C03476());
    }

    private void cleanup() {
        try {
            if (this.fileOutputStream != null) {
                try {
                    this.fileOutputStream.getChannel().close();
                } catch (Throwable e) {
                    FileLog.m611e("tmessages", e);
                }
                this.fileOutputStream.close();
                this.fileOutputStream = null;
            }
        } catch (Throwable e2) {
            FileLog.m611e("tmessages", e2);
        }
        try {
            if (this.fiv != null) {
                this.fiv.close();
                this.fiv = null;
            }
        } catch (Throwable e22) {
            FileLog.m611e("tmessages", e22);
        }
        if (this.delayedRequestInfos != null) {
            for (int a = 0; a < this.delayedRequestInfos.size(); a++) {
                RequestInfo requestInfo = (RequestInfo) this.delayedRequestInfos.get(a);
                if (requestInfo.response != null) {
                    requestInfo.response.disableFree = false;
                    requestInfo.response.freeResources();
                }
            }
            this.delayedRequestInfos.clear();
        }
    }

    private void onFinishLoadingFile() throws Exception {
        if (this.state == 1) {
            this.state = 3;
            cleanup();
            if (this.cacheIvTemp != null) {
                this.cacheIvTemp.delete();
                this.cacheIvTemp = null;
            }
            if (!(this.cacheFileTemp == null || this.cacheFileTemp.renameTo(this.cacheFileFinal))) {
                if (BuildVars.DEBUG_VERSION) {
                    FileLog.m609e("tmessages", "unable to rename temp = " + this.cacheFileTemp + " to final = " + this.cacheFileFinal + " retry = " + this.renameRetryCount);
                }
                this.renameRetryCount++;
                if (this.renameRetryCount < 3) {
                    this.state = 1;
                    Utilities.stageQueue.postRunnable(new C03487(), 200);
                    return;
                }
                this.cacheFileFinal = this.cacheFileTemp;
            }
            if (BuildVars.DEBUG_VERSION) {
                FileLog.m609e("tmessages", "finished downloading file to " + this.cacheFileFinal);
            }
            this.delegate.didFinishLoadingFile(this, this.cacheFileFinal);
        }
    }

    private void processRequestResult(RequestInfo requestInfo, TL_error error) {
        this.requestInfos.remove(requestInfo);
        if (error == null) {
            try {
                if (this.downloadedBytes != requestInfo.offset) {
                    if (this.state == 1) {
                        this.delayedRequestInfos.add(requestInfo);
                        requestInfo.response.disableFree = true;
                    }
                } else if (requestInfo.response.bytes == null || requestInfo.response.bytes.limit() == 0) {
                    onFinishLoadingFile();
                } else {
                    if (this.key != null) {
                        Utilities.aesIgeEncryption(requestInfo.response.bytes.buffer, this.key, this.iv, false, true, 0, requestInfo.response.bytes.limit());
                    }
                    if (this.fileOutputStream != null) {
                        this.fileOutputStream.getChannel().write(requestInfo.response.bytes.buffer);
                    }
                    if (this.fiv != null) {
                        this.fiv.seek(0);
                        this.fiv.write(this.iv);
                    }
                    int currentBytesSize = requestInfo.response.bytes.limit();
                    this.downloadedBytes += currentBytesSize;
                    if (this.totalBytesCount > 0 && this.state == 1) {
                        this.delegate.didChangedLoadProgress(this, Math.min(1.0f, ((float) this.downloadedBytes) / ((float) this.totalBytesCount)));
                    }
                    for (int a = 0; a < this.delayedRequestInfos.size(); a++) {
                        RequestInfo delayedRequestInfo = (RequestInfo) this.delayedRequestInfos.get(a);
                        if (this.downloadedBytes == delayedRequestInfo.offset) {
                            this.delayedRequestInfos.remove(a);
                            processRequestResult(delayedRequestInfo, null);
                            delayedRequestInfo.response.disableFree = false;
                            delayedRequestInfo.response.freeResources();
                            break;
                        }
                    }
                    if (currentBytesSize != this.currentDownloadChunkSize) {
                        onFinishLoadingFile();
                    } else if ((this.totalBytesCount == this.downloadedBytes || this.downloadedBytes % this.currentDownloadChunkSize != 0) && (this.totalBytesCount <= 0 || this.totalBytesCount <= this.downloadedBytes)) {
                        onFinishLoadingFile();
                    } else {
                        startDownloadRequest();
                    }
                }
            } catch (Throwable e) {
                cleanup();
                this.delegate.didFailedLoadingFile(this, 0);
                FileLog.m611e("tmessages", e);
            }
        } else if (error.text.contains("FILE_MIGRATE_")) {
            Integer val;
            Scanner scanner = new Scanner(error.text.replace("FILE_MIGRATE_", ""));
            scanner.useDelimiter("");
            try {
                val = Integer.valueOf(scanner.nextInt());
            } catch (Exception e2) {
                val = null;
            }
            if (val == null) {
                cleanup();
                this.delegate.didFailedLoadingFile(this, 0);
                return;
            }
            this.datacenter_id = val.intValue();
            this.nextDownloadOffset = 0;
            startDownloadRequest();
        } else if (error.text.contains("OFFSET_INVALID")) {
            if (this.downloadedBytes % this.currentDownloadChunkSize == 0) {
                try {
                    onFinishLoadingFile();
                    return;
                } catch (Throwable e3) {
                    FileLog.m611e("tmessages", e3);
                    cleanup();
                    this.delegate.didFailedLoadingFile(this, 0);
                    return;
                }
            }
            cleanup();
            this.delegate.didFailedLoadingFile(this, 0);
        } else if (error.text.contains("RETRY_LIMIT")) {
            cleanup();
            this.delegate.didFailedLoadingFile(this, 2);
        } else {
            if (this.location != null) {
                FileLog.m609e("tmessages", "" + this.location + " id = " + this.location.id + " local_id = " + this.location.local_id + " access_hash = " + this.location.access_hash + " volume_id = " + this.location.volume_id + " secret = " + this.location.secret);
            }
            cleanup();
            this.delegate.didFailedLoadingFile(this, 0);
        }
    }

    private void startDownloadRequest() {
        if (this.state != 1) {
            return;
        }
        if ((this.totalBytesCount <= 0 || this.nextDownloadOffset < this.totalBytesCount) && this.requestInfos.size() + this.delayedRequestInfos.size() < this.currentMaxDownloadRequests) {
            int count = 1;
            if (this.totalBytesCount > 0) {
                count = Math.max(0, (this.currentMaxDownloadRequests - this.requestInfos.size()) - this.delayedRequestInfos.size());
            }
            int a = 0;
            while (a < count) {
                if (this.totalBytesCount <= 0 || this.nextDownloadOffset < this.totalBytesCount) {
                    boolean isLast;
                    int i;
                    if (this.totalBytesCount <= 0 || a == count - 1 || (this.totalBytesCount > 0 && this.nextDownloadOffset + this.currentDownloadChunkSize >= this.totalBytesCount)) {
                        isLast = true;
                    } else {
                        isLast = false;
                    }
                    TL_upload_getFile req = new TL_upload_getFile();
                    req.location = this.location;
                    req.offset = this.nextDownloadOffset;
                    req.limit = this.currentDownloadChunkSize;
                    this.nextDownloadOffset += this.currentDownloadChunkSize;
                    final RequestInfo requestInfo = new RequestInfo();
                    this.requestInfos.add(requestInfo);
                    requestInfo.offset = req.offset;
                    ConnectionsManager instance = ConnectionsManager.getInstance();
                    RequestDelegate c14488 = new RequestDelegate() {
                        public void run(TLObject response, TL_error error) {
                            requestInfo.response = (TL_upload_file) response;
                            FileLoadOperation.this.processRequestResult(requestInfo, error);
                        }
                    };
                    if (this.isForceRequest) {
                        i = 32;
                    } else {
                        i = 0;
                    }
                    requestInfo.requestToken = instance.sendRequest(req, c14488, null, i | 2, this.datacenter_id, this.requestsCount % 2 == 0 ? 2 : ConnectionsManager.ConnectionTypeDownload2, isLast);
                    this.requestsCount++;
                    a++;
                } else {
                    return;
                }
            }
        }
    }

    public void setDelegate(FileLoadOperationDelegate delegate) {
        this.delegate = delegate;
    }
}
