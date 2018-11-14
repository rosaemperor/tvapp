package com.qubuxing.qbx.utils;

import java.io.File;

public interface DownCallback {
    void onProgress(String currentLength);

    void onFinish(File file);

    void onFailure();
}
