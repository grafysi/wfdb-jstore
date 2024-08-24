package io.graphys.wfdbjstore.recordstore;

import io.graphys.wfdbjstore.recordstore.io.CacheInputCoordinator;
import io.graphys.wfdbjstore.recordstore.io.CacheInputCoordinator.InputBackend;
import io.graphys.wfdbjstore.recordstore.io.CacheInputCoordinatorImpl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

public class StorageContext {
    private static final int COORDINATOR_BUF_SIZE = 4096;
    private final Map<String, CacheInputCoordinator> headerCoordinators = new HashMap<>();
    private final Map<String, CacheInputCoordinator> signalCoordinators = new HashMap<>();
    private final ReentrantLock headerLock = new ReentrantLock();
    private final ReentrantLock signalLock = new ReentrantLock();
    private static final StorageContext singleton;
    private final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

    static {
        singleton = new StorageContext();
    }

    public static StorageContext get() {
        return singleton;
    }

    private StorageContext() {

    }

    public void awaitPrepareHeader(String header, PathInfo pathInfo) throws IOException {
        if (!header.endsWith(".hea")) header = header + ".hea";

        var coordinator = getHeaderCoordinator(header, pathInfo);
        coordinator.awaitPrepareCache(false);
    }

    public InputStream prepareSignal(String fileName, PathInfo pathInfo) throws IOException {
        var coordinator = getSignalCoordinator(fileName, pathInfo);
        coordinator.prepareCache(false);
        return coordinator.getInput(InputBackend.CACHE_FILE_RANDOM_ACCESS);
    }

    private CacheInputCoordinator getCoordinatorHelper(Map<String, CacheInputCoordinator> coordinators,
                                                       ReentrantLock lock,
                                                       String fileName, PathInfo pathInfo) throws IOException {
        var id = formIdFrom(fileName, pathInfo.getDbInfo());
        lock.lock();
        try {
            CacheInputCoordinator coordinator;
            if ((coordinator = coordinators.get(id)) == null) {
                coordinator = new CacheInputCoordinatorImpl(
                        pathInfo.formRemoteURIWith(fileName).toURL(),
                        new File(pathInfo.formLocalURIWith(fileName)),
                        executorService,
                        COORDINATOR_BUF_SIZE);
                coordinators.put(id, coordinator);
            }
            return coordinator;
        } finally {
            lock.unlock();
        }
    }

    private CacheInputCoordinator getHeaderCoordinator(String fileName, PathInfo pathInfo) throws IOException {
        return getCoordinatorHelper(headerCoordinators, headerLock, fileName, pathInfo);
    }

    private CacheInputCoordinator getSignalCoordinator(String fileName, PathInfo pathInfo) throws IOException {
        return getCoordinatorHelper(signalCoordinators, signalLock, fileName, pathInfo);
    }



    private String formIdFrom(String fileName, DatabaseInfo dbInfo) {
        return dbInfo.name() + "-" + dbInfo.version() + "-" + fileName;
    }
}




























