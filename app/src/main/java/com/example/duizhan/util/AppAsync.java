package com.example.duizhan.util;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class AppAsync {
    private static final ExecutorService IO_EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    private AppAsync() {
    }

    public static void runOnIo(Runnable task) {
        if (task == null) {
            return;
        }
        IO_EXECUTOR.execute(task);
    }

    public static void runOnMain(Runnable task) {
        if (task == null) {
            return;
        }
        if (Looper.myLooper() == Looper.getMainLooper()) {
            task.run();
        } else {
            MAIN_HANDLER.post(task);
        }
    }

    public static void runOnIoThenMain(Runnable ioTask, Runnable mainTask) {
        runOnIo(() -> {
            if (ioTask != null) {
                ioTask.run();
            }
            if (mainTask != null) {
                runOnMain(mainTask);
            }
        });
    }

    public static void shutdown() {
        IO_EXECUTOR.shutdownNow();
    }
}
