package com.example.duizhan.data.replay;

import android.content.Context;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public final class ReplayStorage {
    public static final String GZIP_PREFIX = "gz:";
    public static final String FILE_PREFIX = "file:";

    private static final String REPLAY_DIR = "replays";

    private ReplayStorage() {
    }

    public static byte[] gzipUtf8(String json) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream(Math.max(8192, json.length() / 3));
        try (GZIPOutputStream gzip = new GZIPOutputStream(bytes)) {
            gzip.write(json.getBytes(StandardCharsets.UTF_8));
        }
        return bytes.toByteArray();
    }

    static String gunzipUtf8(byte[] gzipBytes) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream(Math.max(8192, gzipBytes.length * 4));
        byte[] buffer = new byte[8192];
        try (GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(gzipBytes))) {
            int read;
            while ((read = gzip.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
        }
        return output.toString(StandardCharsets.UTF_8.name());
    }

    public static boolean writeFile(Context context, long recordId, byte[] gzipBytes) {
        if (context == null || recordId <= 0L || gzipBytes == null || gzipBytes.length == 0) {
            return false;
        }
        File file = replayFile(context, recordId);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            return false;
        }
        try (FileOutputStream output = new FileOutputStream(file)) {
            output.write(gzipBytes);
            return true;
        } catch (IOException ignored) {
            return false;
        }
    }

    public static boolean writeReplayFile(Context context, long recordId, ReplayRecorder replayRecorder) {
        if (context == null || recordId <= 0L || replayRecorder == null || replayRecorder.frameCount() == 0) {
            return false;
        }
        File file = replayFile(context, recordId);
        File tempFile = new File(file.getParentFile(), recordId + ".replay.tmp");
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            return false;
        }
        try (FileOutputStream output = new FileOutputStream(tempFile);
             GZIPOutputStream gzip = new GZIPOutputStream(output);
             Writer writer = new OutputStreamWriter(gzip, StandardCharsets.UTF_8)) {
            replayRecorder.writeForPersistence(writer);
        } catch (IOException | RuntimeException exception) {
            tempFile.delete();
            return false;
        }
        if (file.exists() && !file.delete()) {
            tempFile.delete();
            return false;
        }
        if (!tempFile.renameTo(file)) {
            tempFile.delete();
            return false;
        }
        return true;
    }

    static byte[] readFile(Context context, long recordId) {
        if (context == null || recordId <= 0L) {
            return null;
        }
        File file = replayFile(context, recordId);
        if (!file.isFile()) {
            return null;
        }
        try (FileInputStream input = new FileInputStream(file)) {
            ByteArrayOutputStream output = new ByteArrayOutputStream((int) Math.min(file.length(), Integer.MAX_VALUE));
            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
            return output.toByteArray();
        } catch (IOException ignored) {
            return null;
        }
    }

    public static int countStoredFrames(Context context, String storedReplay) {
        if (storedReplay == null || storedReplay.length() == 0) {
            return 0;
        }
        if (storedReplay.startsWith(FILE_PREFIX)) {
            try {
                long recordId = Long.parseLong(storedReplay.substring(FILE_PREFIX.length()));
                File file = replayFile(context, recordId);
                if (!file.isFile()) {
                    return 0;
                }
                try (FileInputStream input = new FileInputStream(file);
                     GZIPInputStream gzip = new GZIPInputStream(input);
                     Reader reader = new InputStreamReader(gzip, StandardCharsets.UTF_8)) {
                    return countFramesInJsonStream(reader);
                }
            } catch (IOException | NumberFormatException ignored) {
                return 0;
            }
        }
        if (storedReplay.startsWith(GZIP_PREFIX)) {
            try {
                byte[] compressed = android.util.Base64.decode(
                        storedReplay.substring(GZIP_PREFIX.length()), android.util.Base64.NO_WRAP);
                try (InputStream input = new ByteArrayInputStream(compressed);
                     GZIPInputStream gzip = new GZIPInputStream(input);
                     Reader reader = new InputStreamReader(gzip, StandardCharsets.UTF_8)) {
                    return countFramesInJsonStream(reader);
                }
            } catch (IOException | IllegalArgumentException ignored) {
                return 0;
            }
        }
        return countFrameMarkers(storedReplay);
    }

    private static int countFramesInJsonStream(Reader reader) throws IOException {
        char[] buffer = new char[8192];
        char[] marker = {'"', 't', '"', ':'};
        int matched = 0;
        int total = 0;
        int read;
        while ((read = reader.read(buffer)) != -1) {
            for (int i = 0; i < read; i++) {
                char c = buffer[i];
                if (c == marker[matched]) {
                    matched++;
                    if (matched == marker.length) {
                        total++;
                        matched = 0;
                    }
                } else {
                    matched = c == marker[0] ? 1 : 0;
                }
            }
        }
        return total;
    }

    private static int countFrameMarkers(String text) {
        if (text == null || text.length() == 0) {
            return 0;
        }
        int count = 0;
        int index = -1;
        while ((index = text.indexOf("\"t\":", index + 1)) >= 0) {
            count++;
        }
        return count;
    }

    public static String decodeStored(Context context, String storedReplay) {
        if (storedReplay == null || storedReplay.length() == 0) {
            return storedReplay;
        }
        if (storedReplay.startsWith(FILE_PREFIX)) {
            try {
                long recordId = Long.parseLong(storedReplay.substring(FILE_PREFIX.length()));
                byte[] gzipBytes = readFile(context, recordId);
                if (gzipBytes == null || gzipBytes.length == 0) {
                    return "";
                }
                return gunzipUtf8(gzipBytes);
            } catch (IOException | NumberFormatException ignored) {
                return "";
            }
        }
        if (storedReplay.startsWith(GZIP_PREFIX)) {
            try {
                byte[] compressed = android.util.Base64.decode(
                        storedReplay.substring(GZIP_PREFIX.length()), android.util.Base64.NO_WRAP);
                return gunzipUtf8(compressed);
            } catch (IOException | IllegalArgumentException ignored) {
                return "";
            }
        }
        return storedReplay;
    }

    private static File replayFile(Context context, long recordId) {
        return new File(new File(context.getFilesDir(), REPLAY_DIR), recordId + ".replay.gz");
    }
}
