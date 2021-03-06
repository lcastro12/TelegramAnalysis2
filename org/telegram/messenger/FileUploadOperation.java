package org.telegram.messenger;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Locale;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.NativeByteBuffer;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.InputEncryptedFile;
import org.telegram.tgnet.TLRPC.InputFile;
import org.telegram.tgnet.TLRPC.TL_boolTrue;
import org.telegram.tgnet.TLRPC.TL_error;
import org.telegram.tgnet.TLRPC.TL_inputEncryptedFileBigUploaded;
import org.telegram.tgnet.TLRPC.TL_inputEncryptedFileUploaded;
import org.telegram.tgnet.TLRPC.TL_inputFile;
import org.telegram.tgnet.TLRPC.TL_inputFileBig;
import org.telegram.tgnet.TLRPC.TL_upload_saveBigFilePart;
import org.telegram.tgnet.TLRPC.TL_upload_saveFilePart;

public class FileUploadOperation {
    private long currentFileId;
    private int currentPartNum = 0;
    private long currentUploaded = 0;
    public FileUploadOperationDelegate delegate;
    private int estimatedSize = 0;
    private String fileKey;
    private int fingerprint = 0;
    private boolean isBigFile = false;
    private boolean isEncrypted = false;
    private boolean isLastPart = false;
    private byte[] iv;
    private byte[] ivChange;
    private byte[] key;
    private MessageDigest mdEnc = null;
    private byte[] readBuffer;
    private int requestToken = 0;
    private int saveInfoTimes = 0;
    private boolean started = false;
    public int state = 0;
    private FileInputStream stream;
    private long totalFileSize = 0;
    private int totalPartsCount = 0;
    private int uploadChunkSize = 32768;
    private int uploadStartTime = 0;
    private String uploadingFilePath;

    class C03641 implements Runnable {
        C03641() {
        }

        public void run() {
            FileUploadOperation.this.startUploadRequest();
        }
    }

    public interface FileUploadOperationDelegate {
        void didChangedUploadProgress(FileUploadOperation fileUploadOperation, float f);

        void didFailedUploadingFile(FileUploadOperation fileUploadOperation);

        void didFinishUploadingFile(FileUploadOperation fileUploadOperation, InputFile inputFile, InputEncryptedFile inputEncryptedFile, byte[] bArr, byte[] bArr2);
    }

    class C14513 implements RequestDelegate {
        C14513() {
        }

        public void run(TLObject response, TL_error error) {
            FileUploadOperation.this.requestToken = 0;
            if (error != null) {
                FileUploadOperation.this.delegate.didFailedUploadingFile(FileUploadOperation.this);
                FileUploadOperation.this.cleanup();
            } else if (response instanceof TL_boolTrue) {
                FileUploadOperation.this.currentPartNum = FileUploadOperation.this.currentPartNum + 1;
                FileUploadOperation.this.delegate.didChangedUploadProgress(FileUploadOperation.this, ((float) FileUploadOperation.this.currentUploaded) / ((float) FileUploadOperation.this.totalFileSize));
                if (FileUploadOperation.this.isLastPart) {
                    FileUploadOperation.this.state = 3;
                    if (FileUploadOperation.this.key == null) {
                        InputFile result;
                        if (FileUploadOperation.this.isBigFile) {
                            result = new TL_inputFileBig();
                        } else {
                            result = new TL_inputFile();
                            result.md5_checksum = String.format(Locale.US, "%32s", new Object[]{new BigInteger(1, FileUploadOperation.this.mdEnc.digest()).toString(16)}).replace(' ', '0');
                        }
                        result.parts = FileUploadOperation.this.currentPartNum;
                        result.id = FileUploadOperation.this.currentFileId;
                        result.name = FileUploadOperation.this.uploadingFilePath.substring(FileUploadOperation.this.uploadingFilePath.lastIndexOf("/") + 1);
                        FileUploadOperation.this.delegate.didFinishUploadingFile(FileUploadOperation.this, result, null, null, null);
                        FileUploadOperation.this.cleanup();
                        return;
                    }
                    InputEncryptedFile result2;
                    if (FileUploadOperation.this.isBigFile) {
                        result2 = new TL_inputEncryptedFileBigUploaded();
                    } else {
                        result2 = new TL_inputEncryptedFileUploaded();
                        result2.md5_checksum = String.format(Locale.US, "%32s", new Object[]{new BigInteger(1, FileUploadOperation.this.mdEnc.digest()).toString(16)}).replace(' ', '0');
                    }
                    result2.parts = FileUploadOperation.this.currentPartNum;
                    result2.id = FileUploadOperation.this.currentFileId;
                    result2.key_fingerprint = FileUploadOperation.this.fingerprint;
                    FileUploadOperation.this.delegate.didFinishUploadingFile(FileUploadOperation.this, null, result2, FileUploadOperation.this.key, FileUploadOperation.this.iv);
                    FileUploadOperation.this.cleanup();
                    return;
                }
                FileUploadOperation.this.startUploadRequest();
            } else {
                FileUploadOperation.this.delegate.didFailedUploadingFile(FileUploadOperation.this);
                FileUploadOperation.this.cleanup();
            }
        }
    }

    public FileUploadOperation(String location, boolean encrypted, int estimated) {
        this.uploadingFilePath = location;
        this.isEncrypted = encrypted;
        this.estimatedSize = estimated;
    }

    public long getTotalFileSize() {
        return this.totalFileSize;
    }

    public void start() {
        if (this.state == 0) {
            this.state = 1;
            Utilities.stageQueue.postRunnable(new C03641());
        }
    }

    public void cancel() {
        if (this.state != 3) {
            this.state = 2;
            if (this.requestToken != 0) {
                ConnectionsManager.getInstance().cancelRequest(this.requestToken, true);
            }
            this.delegate.didFailedUploadingFile(this);
            cleanup();
        }
    }

    private void cleanup() {
        ApplicationLoader.applicationContext.getSharedPreferences("uploadinfo", 0).edit().remove(this.fileKey + "_time").remove(this.fileKey + "_size").remove(this.fileKey + "_uploaded").remove(this.fileKey + "_id").remove(this.fileKey + "_iv").remove(this.fileKey + "_key").remove(this.fileKey + "_ivc").commit();
        try {
            if (this.stream != null) {
                this.stream.close();
                this.stream = null;
            }
        } catch (Throwable e) {
            FileLog.m611e("tmessages", e);
        }
    }

    protected void checkNewDataAvailable(final long finalSize) {
        Utilities.stageQueue.postRunnable(new Runnable() {
            public void run() {
                if (!(FileUploadOperation.this.estimatedSize == 0 || finalSize == 0)) {
                    FileUploadOperation.this.estimatedSize = 0;
                    FileUploadOperation.this.totalFileSize = finalSize;
                    FileUploadOperation.this.totalPartsCount = ((int) ((FileUploadOperation.this.totalFileSize + ((long) FileUploadOperation.this.uploadChunkSize)) - 1)) / FileUploadOperation.this.uploadChunkSize;
                    if (FileUploadOperation.this.started) {
                        FileUploadOperation.this.storeFileUploadInfo(ApplicationLoader.applicationContext.getSharedPreferences("uploadinfo", 0));
                    }
                }
                if (FileUploadOperation.this.requestToken == 0) {
                    FileUploadOperation.this.startUploadRequest();
                }
            }
        });
    }

    private void storeFileUploadInfo(SharedPreferences preferences) {
        Editor editor = preferences.edit();
        editor.putInt(this.fileKey + "_time", this.uploadStartTime);
        editor.putLong(this.fileKey + "_size", this.totalFileSize);
        editor.putLong(this.fileKey + "_id", this.currentFileId);
        editor.remove(this.fileKey + "_uploaded");
        if (this.isEncrypted) {
            editor.putString(this.fileKey + "_iv", Utilities.bytesToHex(this.iv));
            editor.putString(this.fileKey + "_ivc", Utilities.bytesToHex(this.ivChange));
            editor.putString(this.fileKey + "_key", Utilities.bytesToHex(this.key));
        }
        editor.commit();
    }

    private void startUploadRequest() {
        if (this.state == 1) {
            try {
                int read;
                int toAdd;
                NativeByteBuffer nativeByteBuffer;
                int a;
                TLObject finalRequest;
                this.started = true;
                if (this.stream == null) {
                    File cacheFile = new File(this.uploadingFilePath);
                    this.stream = new FileInputStream(cacheFile);
                    if (this.estimatedSize != 0) {
                        this.totalFileSize = (long) this.estimatedSize;
                    } else {
                        this.totalFileSize = cacheFile.length();
                    }
                    if (this.totalFileSize > 10485760) {
                        this.isBigFile = true;
                    } else {
                        try {
                            this.mdEnc = MessageDigest.getInstance("MD5");
                        } catch (Throwable e) {
                            FileLog.m611e("tmessages", e);
                        }
                    }
                    this.uploadChunkSize = (int) Math.max(32, ((this.totalFileSize + 3072000) - 1) / 3072000);
                    if (1024 % this.uploadChunkSize != 0) {
                        int chunkSize = 64;
                        while (this.uploadChunkSize > chunkSize) {
                            chunkSize *= 2;
                        }
                        this.uploadChunkSize = chunkSize;
                    }
                    this.uploadChunkSize *= 1024;
                    this.totalPartsCount = ((int) ((this.totalFileSize + ((long) this.uploadChunkSize)) - 1)) / this.uploadChunkSize;
                    this.readBuffer = new byte[this.uploadChunkSize];
                    this.fileKey = Utilities.MD5(this.uploadingFilePath + (this.isEncrypted ? "enc" : ""));
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("uploadinfo", 0);
                    long fileSize = preferences.getLong(this.fileKey + "_size", 0);
                    this.uploadStartTime = (int) (System.currentTimeMillis() / 1000);
                    boolean rewrite = false;
                    if (this.estimatedSize == 0 && fileSize == this.totalFileSize) {
                        this.currentFileId = preferences.getLong(this.fileKey + "_id", 0);
                        int date = preferences.getInt(this.fileKey + "_time", 0);
                        long uploadedSize = preferences.getLong(this.fileKey + "_uploaded", 0);
                        if (this.isEncrypted) {
                            String ivString = preferences.getString(this.fileKey + "_iv", null);
                            String keyString = preferences.getString(this.fileKey + "_key", null);
                            if (ivString == null || keyString == null) {
                                rewrite = true;
                            } else {
                                this.key = Utilities.hexToBytes(keyString);
                                this.iv = Utilities.hexToBytes(ivString);
                                if (this.key == null || this.iv == null || this.key.length != 32 || this.iv.length != 32) {
                                    rewrite = true;
                                } else {
                                    this.ivChange = new byte[32];
                                    System.arraycopy(this.iv, 0, this.ivChange, 0, 32);
                                }
                            }
                        }
                        if (rewrite || date == 0) {
                            rewrite = true;
                        } else {
                            if (this.isBigFile && date < this.uploadStartTime - 86400) {
                                date = 0;
                            } else if (!this.isBigFile && ((float) date) < ((float) this.uploadStartTime) - 5400.0f) {
                                date = 0;
                            }
                            if (date != 0) {
                                if (uploadedSize > 0) {
                                    this.currentUploaded = uploadedSize;
                                    this.currentPartNum = (int) (uploadedSize / ((long) this.uploadChunkSize));
                                    if (this.isBigFile) {
                                        this.stream.skip(uploadedSize);
                                        if (this.isEncrypted) {
                                            String ivcString = preferences.getString(this.fileKey + "_ivc", null);
                                            if (ivcString != null) {
                                                this.ivChange = Utilities.hexToBytes(ivcString);
                                                if (this.ivChange == null || this.ivChange.length != 32) {
                                                    rewrite = true;
                                                    this.currentUploaded = 0;
                                                    this.currentPartNum = 0;
                                                }
                                            } else {
                                                rewrite = true;
                                                this.currentUploaded = 0;
                                                this.currentPartNum = 0;
                                            }
                                        }
                                    } else {
                                        for (int b = 0; ((long) b) < this.currentUploaded / ((long) this.uploadChunkSize); b++) {
                                            read = this.stream.read(this.readBuffer);
                                            toAdd = 0;
                                            if (this.isEncrypted && read % 16 != 0) {
                                                toAdd = 0 + (16 - (read % 16));
                                            }
                                            nativeByteBuffer = new NativeByteBuffer(read + toAdd);
                                            if (read != this.uploadChunkSize || this.totalPartsCount == this.currentPartNum + 1) {
                                                this.isLastPart = true;
                                            }
                                            nativeByteBuffer.writeBytes(this.readBuffer, 0, read);
                                            if (this.isEncrypted) {
                                                for (a = 0; a < toAdd; a++) {
                                                    nativeByteBuffer.writeByte(0);
                                                }
                                                Utilities.aesIgeEncryption(nativeByteBuffer.buffer, this.key, this.ivChange, true, true, 0, read + toAdd);
                                            }
                                            nativeByteBuffer.rewind();
                                            this.mdEnc.update(nativeByteBuffer.buffer);
                                            nativeByteBuffer.reuse();
                                        }
                                    }
                                } else {
                                    rewrite = true;
                                }
                            }
                        }
                    } else {
                        rewrite = true;
                    }
                    if (rewrite) {
                        if (this.isEncrypted) {
                            this.iv = new byte[32];
                            this.key = new byte[32];
                            this.ivChange = new byte[32];
                            Utilities.random.nextBytes(this.iv);
                            Utilities.random.nextBytes(this.key);
                            System.arraycopy(this.iv, 0, this.ivChange, 0, 32);
                        }
                        this.currentFileId = Utilities.random.nextLong();
                        if (this.estimatedSize == 0) {
                            storeFileUploadInfo(preferences);
                        }
                    }
                    if (this.isEncrypted) {
                        try {
                            MessageDigest md = MessageDigest.getInstance("MD5");
                            byte[] arr = new byte[64];
                            System.arraycopy(this.key, 0, arr, 0, 32);
                            System.arraycopy(this.iv, 0, arr, 32, 32);
                            byte[] digest = md.digest(arr);
                            for (a = 0; a < 4; a++) {
                                this.fingerprint |= ((digest[a] ^ digest[a + 4]) & 255) << (a * 8);
                            }
                        } catch (Throwable e2) {
                            FileLog.m611e("tmessages", e2);
                        }
                    }
                } else if (this.estimatedSize == 0) {
                    if (this.saveInfoTimes >= 4) {
                        this.saveInfoTimes = 0;
                    }
                    if ((this.isBigFile && this.currentUploaded % 1048576 == 0) || (!this.isBigFile && this.saveInfoTimes == 0)) {
                        Editor editor = ApplicationLoader.applicationContext.getSharedPreferences("uploadinfo", 0).edit();
                        editor.putLong(this.fileKey + "_uploaded", this.currentUploaded);
                        if (this.isEncrypted) {
                            editor.putString(this.fileKey + "_ivc", Utilities.bytesToHex(this.ivChange));
                        }
                        editor.commit();
                    }
                    this.saveInfoTimes++;
                }
                if (this.estimatedSize != 0) {
                    if (this.currentUploaded + ((long) this.uploadChunkSize) > this.stream.getChannel().size()) {
                        return;
                    }
                }
                read = this.stream.read(this.readBuffer);
                toAdd = 0;
                if (this.isEncrypted && read % 16 != 0) {
                    toAdd = 0 + (16 - (read % 16));
                }
                nativeByteBuffer = new NativeByteBuffer(read + toAdd);
                if (read != this.uploadChunkSize || (this.estimatedSize == 0 && this.totalPartsCount == this.currentPartNum + 1)) {
                    this.isLastPart = true;
                }
                nativeByteBuffer.writeBytes(this.readBuffer, 0, read);
                if (this.isEncrypted) {
                    for (a = 0; a < toAdd; a++) {
                        nativeByteBuffer.writeByte(0);
                    }
                    Utilities.aesIgeEncryption(nativeByteBuffer.buffer, this.key, this.ivChange, true, true, 0, read + toAdd);
                }
                nativeByteBuffer.rewind();
                if (!this.isBigFile) {
                    this.mdEnc.update(nativeByteBuffer.buffer);
                }
                TLObject req;
                if (this.isBigFile) {
                    req = new TL_upload_saveBigFilePart();
                    req.file_part = this.currentPartNum;
                    req.file_id = this.currentFileId;
                    if (this.estimatedSize != 0) {
                        req.file_total_parts = -1;
                    } else {
                        req.file_total_parts = this.totalPartsCount;
                    }
                    req.bytes = nativeByteBuffer;
                    finalRequest = req;
                } else {
                    req = new TL_upload_saveFilePart();
                    req.file_part = this.currentPartNum;
                    req.file_id = this.currentFileId;
                    req.bytes = nativeByteBuffer;
                    finalRequest = req;
                }
                this.currentUploaded += (long) read;
                this.requestToken = ConnectionsManager.getInstance().sendRequest(finalRequest, new C14513(), 0, 4);
            } catch (Throwable e22) {
                FileLog.m611e("tmessages", e22);
                this.delegate.didFailedUploadingFile(this);
                cleanup();
            }
        }
    }
}
