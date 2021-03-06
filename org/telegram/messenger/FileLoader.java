package org.telegram.messenger;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import org.telegram.messenger.FileLoadOperation.FileLoadOperationDelegate;
import org.telegram.messenger.FileUploadOperation.FileUploadOperationDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.Audio;
import org.telegram.tgnet.TLRPC.Document;
import org.telegram.tgnet.TLRPC.DocumentAttribute;
import org.telegram.tgnet.TLRPC.FileLocation;
import org.telegram.tgnet.TLRPC.InputEncryptedFile;
import org.telegram.tgnet.TLRPC.InputFile;
import org.telegram.tgnet.TLRPC.Message;
import org.telegram.tgnet.TLRPC.PhotoSize;
import org.telegram.tgnet.TLRPC.TL_documentAttributeFilename;
import org.telegram.tgnet.TLRPC.TL_messageMediaAudio;
import org.telegram.tgnet.TLRPC.TL_messageMediaDocument;
import org.telegram.tgnet.TLRPC.TL_messageMediaPhoto;
import org.telegram.tgnet.TLRPC.TL_messageMediaVideo;
import org.telegram.tgnet.TLRPC.TL_messageMediaWebPage;
import org.telegram.tgnet.TLRPC.TL_messageService;
import org.telegram.tgnet.TLRPC.TL_photoCachedSize;
import org.telegram.tgnet.TLRPC.Video;

public class FileLoader {
    private static volatile FileLoader Instance = null;
    public static final int MEDIA_DIR_AUDIO = 1;
    public static final int MEDIA_DIR_CACHE = 4;
    public static final int MEDIA_DIR_DOCUMENT = 3;
    public static final int MEDIA_DIR_IMAGE = 0;
    public static final int MEDIA_DIR_VIDEO = 2;
    private LinkedList<FileLoadOperation> audioLoadOperationQueue = new LinkedList();
    private int currentAudioLoadOperationsCount = 0;
    private int currentLoadOperationsCount = 0;
    private int currentPhotoLoadOperationsCount = 0;
    private int currentUploadOperationsCount = 0;
    private int currentUploadSmallOperationsCount = 0;
    private FileLoaderDelegate delegate = null;
    private volatile DispatchQueue fileLoaderQueue = new DispatchQueue("fileUploadQueue");
    private ConcurrentHashMap<String, FileLoadOperation> loadOperationPaths = new ConcurrentHashMap();
    private LinkedList<FileLoadOperation> loadOperationQueue = new LinkedList();
    private HashMap<Integer, File> mediaDirs = null;
    private LinkedList<FileLoadOperation> photoLoadOperationQueue = new LinkedList();
    private ConcurrentHashMap<String, FileUploadOperation> uploadOperationPaths = new ConcurrentHashMap();
    private ConcurrentHashMap<String, FileUploadOperation> uploadOperationPathsEnc = new ConcurrentHashMap();
    private LinkedList<FileUploadOperation> uploadOperationQueue = new LinkedList();
    private HashMap<String, Long> uploadSizes = new HashMap();
    private LinkedList<FileUploadOperation> uploadSmallOperationQueue = new LinkedList();

    public interface FileLoaderDelegate {
        void fileDidFailedLoad(String str, int i);

        void fileDidFailedUpload(String str, boolean z);

        void fileDidLoaded(String str, File file, int i);

        void fileDidUploaded(String str, InputFile inputFile, InputEncryptedFile inputEncryptedFile, byte[] bArr, byte[] bArr2, long j);

        void fileLoadProgressChanged(String str, float f);

        void fileUploadProgressChanged(String str, float f, boolean z);
    }

    public static FileLoader getInstance() {
        FileLoader localInstance = Instance;
        if (localInstance == null) {
            synchronized (FileLoader.class) {
                try {
                    localInstance = Instance;
                    if (localInstance == null) {
                        FileLoader localInstance2 = new FileLoader();
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

    public void setMediaDirs(HashMap<Integer, File> dirs) {
        this.mediaDirs = dirs;
    }

    public File checkDirectory(int type) {
        return (File) this.mediaDirs.get(Integer.valueOf(type));
    }

    public File getDirectory(int type) {
        File dir = (File) this.mediaDirs.get(Integer.valueOf(type));
        if (dir == null && type != 4) {
            dir = (File) this.mediaDirs.get(Integer.valueOf(4));
        }
        try {
            if (!dir.isDirectory()) {
                dir.mkdirs();
            }
        } catch (Exception e) {
        }
        return dir;
    }

    public void cancelUploadFile(final String location, final boolean enc) {
        this.fileLoaderQueue.postRunnable(new Runnable() {
            public void run() {
                FileUploadOperation operation;
                if (enc) {
                    operation = (FileUploadOperation) FileLoader.this.uploadOperationPathsEnc.get(location);
                } else {
                    operation = (FileUploadOperation) FileLoader.this.uploadOperationPaths.get(location);
                }
                FileLoader.this.uploadSizes.remove(location);
                if (operation != null) {
                    FileLoader.this.uploadOperationPathsEnc.remove(location);
                    FileLoader.this.uploadOperationQueue.remove(operation);
                    FileLoader.this.uploadSmallOperationQueue.remove(operation);
                    operation.cancel();
                }
            }
        });
    }

    public void checkUploadNewDataAvailable(String location, boolean encrypted, long finalSize) {
        final boolean z = encrypted;
        final String str = location;
        final long j = finalSize;
        this.fileLoaderQueue.postRunnable(new Runnable() {
            public void run() {
                FileUploadOperation operation;
                if (z) {
                    operation = (FileUploadOperation) FileLoader.this.uploadOperationPathsEnc.get(str);
                } else {
                    operation = (FileUploadOperation) FileLoader.this.uploadOperationPaths.get(str);
                }
                if (operation != null) {
                    operation.checkNewDataAvailable(j);
                } else if (j != 0) {
                    FileLoader.this.uploadSizes.put(str, Long.valueOf(j));
                }
            }
        });
    }

    public void uploadFile(String location, boolean encrypted, boolean small) {
        uploadFile(location, encrypted, small, 0);
    }

    public void uploadFile(String location, boolean encrypted, boolean small, int estimatedSize) {
        if (location != null) {
            final boolean z = encrypted;
            final String str = location;
            final int i = estimatedSize;
            final boolean z2 = small;
            this.fileLoaderQueue.postRunnable(new Runnable() {

                class C14491 implements FileUploadOperationDelegate {

                    class C03522 implements Runnable {
                        C03522() {
                        }

                        public void run() {
                            if (z) {
                                FileLoader.this.uploadOperationPathsEnc.remove(str);
                            } else {
                                FileLoader.this.uploadOperationPaths.remove(str);
                            }
                            if (FileLoader.this.delegate != null) {
                                FileLoader.this.delegate.fileDidFailedUpload(str, z);
                            }
                            FileUploadOperation operation;
                            if (z2) {
                                FileLoader.this.currentUploadSmallOperationsCount = FileLoader.this.currentUploadSmallOperationsCount - 1;
                                if (FileLoader.this.currentUploadSmallOperationsCount < 1) {
                                    operation = (FileUploadOperation) FileLoader.this.uploadSmallOperationQueue.poll();
                                    if (operation != null) {
                                        FileLoader.this.currentUploadSmallOperationsCount = FileLoader.this.currentUploadSmallOperationsCount + 1;
                                        operation.start();
                                        return;
                                    }
                                    return;
                                }
                                return;
                            }
                            FileLoader.this.currentUploadOperationsCount = FileLoader.this.currentUploadOperationsCount - 1;
                            if (FileLoader.this.currentUploadOperationsCount < 1) {
                                operation = (FileUploadOperation) FileLoader.this.uploadOperationQueue.poll();
                                if (operation != null) {
                                    FileLoader.this.currentUploadOperationsCount = FileLoader.this.currentUploadOperationsCount + 1;
                                    operation.start();
                                }
                            }
                        }
                    }

                    C14491() {
                    }

                    public void didFinishUploadingFile(FileUploadOperation operation, InputFile inputFile, InputEncryptedFile inputEncryptedFile, byte[] key, byte[] iv) {
                        final InputFile inputFile2 = inputFile;
                        final InputEncryptedFile inputEncryptedFile2 = inputEncryptedFile;
                        final byte[] bArr = key;
                        final byte[] bArr2 = iv;
                        final FileUploadOperation fileUploadOperation = operation;
                        FileLoader.this.fileLoaderQueue.postRunnable(new Runnable() {
                            public void run() {
                                if (z) {
                                    FileLoader.this.uploadOperationPathsEnc.remove(str);
                                } else {
                                    FileLoader.this.uploadOperationPaths.remove(str);
                                }
                                FileUploadOperation operation;
                                if (z2) {
                                    FileLoader.this.currentUploadSmallOperationsCount = FileLoader.this.currentUploadSmallOperationsCount - 1;
                                    if (FileLoader.this.currentUploadSmallOperationsCount < 1) {
                                        operation = (FileUploadOperation) FileLoader.this.uploadSmallOperationQueue.poll();
                                        if (operation != null) {
                                            FileLoader.this.currentUploadSmallOperationsCount = FileLoader.this.currentUploadSmallOperationsCount + 1;
                                            operation.start();
                                        }
                                    }
                                } else {
                                    FileLoader.this.currentUploadOperationsCount = FileLoader.this.currentUploadOperationsCount - 1;
                                    if (FileLoader.this.currentUploadOperationsCount < 1) {
                                        operation = (FileUploadOperation) FileLoader.this.uploadOperationQueue.poll();
                                        if (operation != null) {
                                            FileLoader.this.currentUploadOperationsCount = FileLoader.this.currentUploadOperationsCount + 1;
                                            operation.start();
                                        }
                                    }
                                }
                                if (FileLoader.this.delegate != null) {
                                    FileLoader.this.delegate.fileDidUploaded(str, inputFile2, inputEncryptedFile2, bArr, bArr2, fileUploadOperation.getTotalFileSize());
                                }
                            }
                        });
                    }

                    public void didFailedUploadingFile(FileUploadOperation operation) {
                        FileLoader.this.fileLoaderQueue.postRunnable(new C03522());
                    }

                    public void didChangedUploadProgress(FileUploadOperation operation, float progress) {
                        if (FileLoader.this.delegate != null) {
                            FileLoader.this.delegate.fileUploadProgressChanged(str, progress, z);
                        }
                    }
                }

                public void run() {
                    if (z) {
                        if (FileLoader.this.uploadOperationPathsEnc.containsKey(str)) {
                            return;
                        }
                    } else if (FileLoader.this.uploadOperationPaths.containsKey(str)) {
                        return;
                    }
                    int esimated = i;
                    if (!(esimated == 0 || ((Long) FileLoader.this.uploadSizes.get(str)) == null)) {
                        esimated = 0;
                        FileLoader.this.uploadSizes.remove(str);
                    }
                    FileUploadOperation operation = new FileUploadOperation(str, z, esimated);
                    if (z) {
                        FileLoader.this.uploadOperationPathsEnc.put(str, operation);
                    } else {
                        FileLoader.this.uploadOperationPaths.put(str, operation);
                    }
                    operation.delegate = new C14491();
                    if (z2) {
                        if (FileLoader.this.currentUploadSmallOperationsCount < 1) {
                            FileLoader.this.currentUploadSmallOperationsCount = FileLoader.this.currentUploadSmallOperationsCount + 1;
                            operation.start();
                            return;
                        }
                        FileLoader.this.uploadSmallOperationQueue.add(operation);
                    } else if (FileLoader.this.currentUploadOperationsCount < 1) {
                        FileLoader.this.currentUploadOperationsCount = FileLoader.this.currentUploadOperationsCount + 1;
                        operation.start();
                    } else {
                        FileLoader.this.uploadOperationQueue.add(operation);
                    }
                }
            });
        }
    }

    public void cancelLoadFile(Video video) {
        cancelLoadFile(video, null, null, null, null);
    }

    public void cancelLoadFile(Document document) {
        cancelLoadFile(null, document, null, null, null);
    }

    public void cancelLoadFile(Audio audio) {
        cancelLoadFile(null, null, audio, null, null);
    }

    public void cancelLoadFile(PhotoSize photo) {
        cancelLoadFile(null, null, null, photo.location, null);
    }

    public void cancelLoadFile(FileLocation location, String ext) {
        cancelLoadFile(null, null, null, location, ext);
    }

    private void cancelLoadFile(Video video, Document document, Audio audio, FileLocation location, String locationExt) {
        if (video != null || location != null || document != null || audio != null) {
            final Video video2 = video;
            final FileLocation fileLocation = location;
            final String str = locationExt;
            final Document document2 = document;
            final Audio audio2 = audio;
            this.fileLoaderQueue.postRunnable(new Runnable() {
                public void run() {
                    String fileName = null;
                    if (video2 != null) {
                        fileName = FileLoader.getAttachFileName(video2);
                    } else if (fileLocation != null) {
                        fileName = FileLoader.getAttachFileName(fileLocation, str);
                    } else if (document2 != null) {
                        fileName = FileLoader.getAttachFileName(document2);
                    } else if (audio2 != null) {
                        fileName = FileLoader.getAttachFileName(audio2);
                    }
                    if (fileName != null) {
                        FileLoadOperation operation = (FileLoadOperation) FileLoader.this.loadOperationPaths.get(fileName);
                        if (operation != null) {
                            FileLoader.this.loadOperationPaths.remove(fileName);
                            if (audio2 != null) {
                                FileLoader.this.audioLoadOperationQueue.remove(operation);
                            } else if (fileLocation != null) {
                                FileLoader.this.photoLoadOperationQueue.remove(operation);
                            } else {
                                FileLoader.this.loadOperationQueue.remove(operation);
                            }
                            operation.cancel();
                        }
                    }
                }
            });
        }
    }

    public boolean isLoadingFile(final String fileName) {
        final Semaphore semaphore = new Semaphore(0);
        final Boolean[] result = new Boolean[1];
        this.fileLoaderQueue.postRunnable(new Runnable() {
            public void run() {
                result[0] = Boolean.valueOf(FileLoader.this.loadOperationPaths.containsKey(fileName));
                semaphore.release();
            }
        });
        try {
            semaphore.acquire();
        } catch (Throwable e) {
            FileLog.m611e("tmessages", e);
        }
        return result[0].booleanValue();
    }

    public void loadFile(Video video, boolean force) {
        boolean z;
        if (video == null || video.key == null) {
            z = false;
        } else {
            z = true;
        }
        loadFile(video, null, null, null, null, 0, force, z);
    }

    public void loadFile(PhotoSize photo, String ext, boolean cacheOnly) {
        boolean z;
        FileLocation fileLocation = photo.location;
        int i = photo.size;
        if (cacheOnly || ((photo != null && photo.size == 0) || photo.location.key != null)) {
            z = true;
        } else {
            z = false;
        }
        loadFile(null, null, null, fileLocation, ext, i, false, z);
    }

    public void loadFile(Document document, boolean force, boolean cacheOnly) {
        boolean z;
        if (cacheOnly || !(document == null || document.key == null)) {
            z = true;
        } else {
            z = false;
        }
        loadFile(null, document, null, null, null, 0, force, z);
    }

    public void loadFile(Audio audio, boolean force) {
        boolean z;
        if (audio == null || audio.key == null) {
            z = false;
        } else {
            z = true;
        }
        loadFile(null, null, audio, null, null, 0, false, z);
    }

    public void loadFile(FileLocation location, String ext, int size, boolean cacheOnly) {
        boolean z = cacheOnly || size == 0 || !(location == null || location.key == null);
        loadFile(null, null, null, location, ext, size, true, z);
    }

    private void loadFile(Video video, Document document, Audio audio, FileLocation location, String locationExt, int locationSize, boolean force, boolean cacheOnly) {
        final Video video2 = video;
        final FileLocation fileLocation = location;
        final String str = locationExt;
        final Document document2 = document;
        final Audio audio2 = audio;
        final boolean z = force;
        final int i = locationSize;
        final boolean z2 = cacheOnly;
        this.fileLoaderQueue.postRunnable(new Runnable() {
            public void run() {
                int maxCount = 1;
                String fileName = null;
                if (video2 != null) {
                    fileName = FileLoader.getAttachFileName(video2);
                } else if (fileLocation != null) {
                    fileName = FileLoader.getAttachFileName(fileLocation, str);
                } else if (document2 != null) {
                    fileName = FileLoader.getAttachFileName(document2);
                } else if (audio2 != null) {
                    fileName = FileLoader.getAttachFileName(audio2);
                }
                if (fileName != null && !fileName.contains("-2147483648")) {
                    FileLoadOperation operation = (FileLoadOperation) FileLoader.this.loadOperationPaths.get(fileName);
                    if (operation == null) {
                        File tempDir = FileLoader.this.getDirectory(4);
                        File storeDir = tempDir;
                        int type = 4;
                        if (video2 != null) {
                            operation = new FileLoadOperation(video2);
                            type = 2;
                        } else if (fileLocation != null) {
                            operation = new FileLoadOperation(fileLocation, str, i);
                            type = 0;
                        } else if (document2 != null) {
                            operation = new FileLoadOperation(document2);
                            type = 3;
                        } else if (audio2 != null) {
                            operation = new FileLoadOperation(audio2);
                            type = 1;
                        }
                        if (!z2) {
                            storeDir = FileLoader.this.getDirectory(type);
                        }
                        operation.setPaths(storeDir, tempDir);
                        final String finalFileName = fileName;
                        final int finalType = type;
                        FileLoader.this.loadOperationPaths.put(fileName, operation);
                        operation.setDelegate(new FileLoadOperationDelegate() {
                            public void didFinishLoadingFile(FileLoadOperation operation, File finalFile) {
                                if (FileLoader.this.delegate != null) {
                                    FileLoader.this.delegate.fileDidLoaded(finalFileName, finalFile, finalType);
                                }
                                FileLoader.this.checkDownloadQueue(audio2, fileLocation, finalFileName);
                            }

                            public void didFailedLoadingFile(FileLoadOperation operation, int canceled) {
                                FileLoader.this.checkDownloadQueue(audio2, fileLocation, finalFileName);
                                if (FileLoader.this.delegate != null) {
                                    FileLoader.this.delegate.fileDidFailedLoad(finalFileName, canceled);
                                }
                            }

                            public void didChangedLoadProgress(FileLoadOperation operation, float progress) {
                                if (FileLoader.this.delegate != null) {
                                    FileLoader.this.delegate.fileLoadProgressChanged(finalFileName, progress);
                                }
                            }
                        });
                        if (z) {
                            maxCount = 3;
                        }
                        if (audio2 != null) {
                            if (FileLoader.this.currentAudioLoadOperationsCount < maxCount) {
                                FileLoader.this.currentAudioLoadOperationsCount = FileLoader.this.currentAudioLoadOperationsCount + 1;
                                operation.start();
                            } else if (z) {
                                FileLoader.this.audioLoadOperationQueue.add(0, operation);
                            } else {
                                FileLoader.this.audioLoadOperationQueue.add(operation);
                            }
                        } else if (fileLocation != null) {
                            if (FileLoader.this.currentPhotoLoadOperationsCount < maxCount) {
                                FileLoader.this.currentPhotoLoadOperationsCount = FileLoader.this.currentPhotoLoadOperationsCount + 1;
                                operation.start();
                            } else if (z) {
                                FileLoader.this.photoLoadOperationQueue.add(0, operation);
                            } else {
                                FileLoader.this.photoLoadOperationQueue.add(operation);
                            }
                        } else if (FileLoader.this.currentLoadOperationsCount < maxCount) {
                            FileLoader.this.currentLoadOperationsCount = FileLoader.this.currentLoadOperationsCount + 1;
                            operation.start();
                        } else if (z) {
                            FileLoader.this.loadOperationQueue.add(0, operation);
                        } else {
                            FileLoader.this.loadOperationQueue.add(operation);
                        }
                    } else if (z) {
                        LinkedList<FileLoadOperation> downloadQueue;
                        if (audio2 != null) {
                            downloadQueue = FileLoader.this.audioLoadOperationQueue;
                        } else if (fileLocation != null) {
                            downloadQueue = FileLoader.this.photoLoadOperationQueue;
                        } else {
                            downloadQueue = FileLoader.this.loadOperationQueue;
                        }
                        if (downloadQueue != null) {
                            int index = downloadQueue.indexOf(operation);
                            if (index != -1) {
                                downloadQueue.remove(index);
                                downloadQueue.add(0, operation);
                                operation.setForceRequest(true);
                            }
                        }
                    }
                }
            }
        });
    }

    private void checkDownloadQueue(final Audio audio, final FileLocation location, final String arg1) {
        this.fileLoaderQueue.postRunnable(new Runnable() {
            public void run() {
                int maxCount = 3;
                FileLoader.this.loadOperationPaths.remove(arg1);
                FileLoadOperation operation;
                if (audio != null) {
                    FileLoader.this.currentAudioLoadOperationsCount = FileLoader.this.currentAudioLoadOperationsCount - 1;
                    if (!FileLoader.this.audioLoadOperationQueue.isEmpty()) {
                        if (!((FileLoadOperation) FileLoader.this.audioLoadOperationQueue.get(0)).isForceRequest()) {
                            maxCount = 1;
                        }
                        if (FileLoader.this.currentAudioLoadOperationsCount < maxCount) {
                            operation = (FileLoadOperation) FileLoader.this.audioLoadOperationQueue.poll();
                            if (operation != null) {
                                FileLoader.this.currentAudioLoadOperationsCount = FileLoader.this.currentAudioLoadOperationsCount + 1;
                                operation.start();
                            }
                        }
                    }
                } else if (location != null) {
                    FileLoader.this.currentPhotoLoadOperationsCount = FileLoader.this.currentPhotoLoadOperationsCount - 1;
                    if (!FileLoader.this.photoLoadOperationQueue.isEmpty()) {
                        if (!((FileLoadOperation) FileLoader.this.photoLoadOperationQueue.get(0)).isForceRequest()) {
                            maxCount = 1;
                        }
                        if (FileLoader.this.currentPhotoLoadOperationsCount < maxCount) {
                            operation = (FileLoadOperation) FileLoader.this.photoLoadOperationQueue.poll();
                            if (operation != null) {
                                FileLoader.this.currentPhotoLoadOperationsCount = FileLoader.this.currentPhotoLoadOperationsCount + 1;
                                operation.start();
                            }
                        }
                    }
                } else {
                    FileLoader.this.currentLoadOperationsCount = FileLoader.this.currentLoadOperationsCount - 1;
                    if (!FileLoader.this.loadOperationQueue.isEmpty()) {
                        if (!((FileLoadOperation) FileLoader.this.loadOperationQueue.get(0)).isForceRequest()) {
                            maxCount = 1;
                        }
                        if (FileLoader.this.currentLoadOperationsCount < maxCount) {
                            operation = (FileLoadOperation) FileLoader.this.loadOperationQueue.poll();
                            if (operation != null) {
                                FileLoader.this.currentLoadOperationsCount = FileLoader.this.currentLoadOperationsCount + 1;
                                operation.start();
                            }
                        }
                    }
                }
            }
        });
    }

    public void setDelegate(FileLoaderDelegate delegate) {
        this.delegate = delegate;
    }

    public static File getPathToMessage(Message message) {
        if (message == null) {
            return new File("");
        }
        ArrayList<PhotoSize> sizes;
        PhotoSize sizeFull;
        if (message instanceof TL_messageService) {
            if (message.action.photo != null) {
                sizes = message.action.photo.sizes;
                if (sizes.size() > 0) {
                    sizeFull = getClosestPhotoSizeWithSize(sizes, AndroidUtilities.getPhotoSize());
                    if (sizeFull != null) {
                        return getPathToAttach(sizeFull);
                    }
                }
            }
        } else if (message.media instanceof TL_messageMediaVideo) {
            return getPathToAttach(message.media.video);
        } else {
            if (message.media instanceof TL_messageMediaDocument) {
                return getPathToAttach(message.media.document);
            }
            if (message.media instanceof TL_messageMediaAudio) {
                return getPathToAttach(message.media.audio);
            }
            if (message.media instanceof TL_messageMediaPhoto) {
                sizes = message.media.photo.sizes;
                if (sizes.size() > 0) {
                    sizeFull = getClosestPhotoSizeWithSize(sizes, AndroidUtilities.getPhotoSize());
                    if (sizeFull != null) {
                        return getPathToAttach(sizeFull);
                    }
                }
            } else if ((message.media instanceof TL_messageMediaWebPage) && message.media.webpage.photo != null) {
                sizes = message.media.webpage.photo.sizes;
                if (sizes.size() > 0) {
                    sizeFull = getClosestPhotoSizeWithSize(sizes, AndroidUtilities.getPhotoSize());
                    if (sizeFull != null) {
                        return getPathToAttach(sizeFull);
                    }
                }
            }
        }
        return new File("");
    }

    public static File getExistPathToAttach(TLObject attach) {
        File attachPath = new File(getInstance().getDirectory(4), getAttachFileName(attach));
        return attachPath.exists() ? attachPath : getPathToAttach(attach);
    }

    public static File getPathToAttach(TLObject attach) {
        return getPathToAttach(attach, null, false);
    }

    public static File getPathToAttach(TLObject attach, boolean forceCache) {
        return getPathToAttach(attach, null, forceCache);
    }

    public static File getPathToAttach(TLObject attach, String ext, boolean forceCache) {
        File dir = null;
        if (forceCache) {
            dir = getInstance().getDirectory(4);
        } else if (attach instanceof Video) {
            if (((Video) attach).key != null) {
                dir = getInstance().getDirectory(4);
            } else {
                dir = getInstance().getDirectory(2);
            }
        } else if (attach instanceof Document) {
            if (((Document) attach).key != null) {
                dir = getInstance().getDirectory(4);
            } else {
                dir = getInstance().getDirectory(3);
            }
        } else if (attach instanceof PhotoSize) {
            PhotoSize photoSize = (PhotoSize) attach;
            if (photoSize.location == null || photoSize.location.key != null || ((photoSize.location.volume_id == -2147483648L && photoSize.location.local_id < 0) || photoSize.size < 0)) {
                dir = getInstance().getDirectory(4);
            } else {
                dir = getInstance().getDirectory(0);
            }
        } else if (attach instanceof Audio) {
            if (((Audio) attach).key != null) {
                dir = getInstance().getDirectory(4);
            } else {
                dir = getInstance().getDirectory(1);
            }
        } else if (attach instanceof FileLocation) {
            FileLocation fileLocation = (FileLocation) attach;
            if (fileLocation.key != null || (fileLocation.volume_id == -2147483648L && fileLocation.local_id < 0)) {
                dir = getInstance().getDirectory(4);
            } else {
                dir = getInstance().getDirectory(0);
            }
        }
        if (dir == null) {
            return new File("");
        }
        return new File(dir, getAttachFileName(attach, ext));
    }

    public static PhotoSize getClosestPhotoSizeWithSize(ArrayList<PhotoSize> sizes, int side) {
        return getClosestPhotoSizeWithSize(sizes, side, false);
    }

    public static PhotoSize getClosestPhotoSizeWithSize(ArrayList<PhotoSize> sizes, int side, boolean byMinSide) {
        if (sizes == null || sizes.isEmpty()) {
            return null;
        }
        int lastSide = 0;
        PhotoSize closestObject = null;
        Iterator i$ = sizes.iterator();
        while (i$.hasNext()) {
            PhotoSize obj = (PhotoSize) i$.next();
            if (obj != null) {
                int currentSide;
                if (byMinSide) {
                    currentSide = obj.f138h >= obj.f139w ? obj.f139w : obj.f138h;
                    if (closestObject == null || ((side > 100 && closestObject.location != null && closestObject.location.dc_id == Integer.MIN_VALUE) || (obj instanceof TL_photoCachedSize) || (side > lastSide && lastSide < currentSide))) {
                        closestObject = obj;
                        lastSide = currentSide;
                    }
                } else {
                    currentSide = obj.f139w >= obj.f138h ? obj.f139w : obj.f138h;
                    if (closestObject == null || ((side > 100 && closestObject.location != null && closestObject.location.dc_id == Integer.MIN_VALUE) || (obj instanceof TL_photoCachedSize) || (currentSide <= side && lastSide < currentSide))) {
                        closestObject = obj;
                        lastSide = currentSide;
                    }
                }
            }
        }
        return closestObject;
    }

    public static String getFileExtension(File file) {
        String name = file.getName();
        try {
            return name.substring(name.lastIndexOf(".") + 1);
        } catch (Exception e) {
            return "";
        }
    }

    public static String getDocumentFileName(Document document) {
        if (document != null) {
            if (document.file_name != null) {
                return document.file_name;
            }
            Iterator i$ = document.attributes.iterator();
            while (i$.hasNext()) {
                DocumentAttribute documentAttribute = (DocumentAttribute) i$.next();
                if (documentAttribute instanceof TL_documentAttributeFilename) {
                    return documentAttribute.file_name;
                }
            }
        }
        return "";
    }

    public static String getAttachFileName(TLObject attach) {
        return getAttachFileName(attach, null);
    }

    public static String getAttachFileName(TLObject attach, String ext) {
        StringBuilder append;
        if (attach instanceof Video) {
            Video video = (Video) attach;
            append = new StringBuilder().append(video.dc_id).append("_").append(video.id).append(".");
            if (ext == null) {
                ext = "mp4";
            }
            return append.append(ext).toString();
        } else if (attach instanceof Document) {
            Document document = (Document) attach;
            String docExt = getDocumentFileName(document);
            if (docExt != null) {
                int idx = docExt.lastIndexOf(".");
                if (idx != -1) {
                    docExt = docExt.substring(idx);
                    if (docExt.length() <= 1) {
                        return document.dc_id + "_" + document.id + docExt;
                    }
                    return document.dc_id + "_" + document.id;
                }
            }
            docExt = "";
            if (docExt.length() <= 1) {
                return document.dc_id + "_" + document.id;
            }
            return document.dc_id + "_" + document.id + docExt;
        } else if (attach instanceof PhotoSize) {
            PhotoSize photo = (PhotoSize) attach;
            if (photo.location == null) {
                return "";
            }
            append = new StringBuilder().append(photo.location.volume_id).append("_").append(photo.location.local_id).append(".");
            if (ext == null) {
                ext = "jpg";
            }
            return append.append(ext).toString();
        } else if (attach instanceof Audio) {
            Audio audio = (Audio) attach;
            append = new StringBuilder().append(audio.dc_id).append("_").append(audio.id).append(".");
            if (ext == null) {
                ext = "ogg";
            }
            return append.append(ext).toString();
        } else if (!(attach instanceof FileLocation)) {
            return "";
        } else {
            FileLocation location = (FileLocation) attach;
            append = new StringBuilder().append(location.volume_id).append("_").append(location.local_id).append(".");
            if (ext == null) {
                ext = "jpg";
            }
            return append.append(ext).toString();
        }
    }

    public void deleteFiles(final ArrayList<File> files, final int type) {
        if (files != null && !files.isEmpty()) {
            this.fileLoaderQueue.postRunnable(new Runnable() {
                public void run() {
                    for (int a = 0; a < files.size(); a++) {
                        File file = (File) files.get(a);
                        if (file.exists()) {
                            try {
                                if (!file.delete()) {
                                    file.deleteOnExit();
                                }
                            } catch (Throwable e) {
                                FileLog.m611e("tmessages", e);
                            }
                        }
                        try {
                            File qFile = new File(file.getParentFile(), "q_" + file.getName());
                            if (qFile.exists() && !qFile.delete()) {
                                qFile.deleteOnExit();
                            }
                        } catch (Throwable e2) {
                            FileLog.m611e("tmessages", e2);
                        }
                    }
                    if (type == 2) {
                        ImageLoader.getInstance().clearMemory();
                    }
                }
            });
        }
    }
}
