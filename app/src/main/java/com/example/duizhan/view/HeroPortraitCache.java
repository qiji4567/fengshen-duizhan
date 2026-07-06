package com.example.duizhan.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.LruCache;

import com.example.duizhan.game.HeroType;

import java.io.IOException;
import java.io.InputStream;

public final class HeroPortraitCache {
    private static final HeroPortraitRenderer RENDERER = new HeroPortraitRenderer();
    private static final LruCache<String, Bitmap> CACHE = new LruCache<>(96);
    private static Context appContext;

    private HeroPortraitCache() {
    }

    public static void init(Context context) {
        appContext = context.getApplicationContext();
    }

    public static Bitmap get(HeroType type, int width, int height) {
        if (type == null || width <= 0 || height <= 0) {
            return null;
        }
        String key = type.name() + "@" + width + "x" + height;
        Bitmap cached = CACHE.get(key);
        if (cached != null && !cached.isRecycled()) {
            return cached;
        }
        Bitmap bitmap = loadAssetPortrait(type, width, height);
        if (bitmap == null) {
            bitmap = RENDERER.render(type, Math.max(width, height));
            if (bitmap != null && (bitmap.getWidth() != width || bitmap.getHeight() != height)) {
                bitmap = scaleCenterCrop(bitmap, width, height);
            }
        }
        if (bitmap != null) {
            CACHE.put(key, bitmap);
        }
        return bitmap;
    }

    public static Bitmap thumb(HeroType type) {
        return get(type, HeroPortraitRenderer.THUMB_SIZE, HeroPortraitRenderer.THUMB_SIZE);
    }

    public static Bitmap preview(HeroType type) {
        return get(type, HeroPortraitRenderer.PREVIEW_SIZE, HeroPortraitRenderer.PREVIEW_SIZE);
    }

    public static Bitmap showcase(HeroType type) {
        return get(type, HeroPortraitRenderer.SHOWCASE_WIDTH, HeroPortraitRenderer.SHOWCASE_HEIGHT);
    }

    private static Bitmap loadAssetPortrait(HeroType type, int width, int height) {
        if (appContext == null) {
            return null;
        }
        String[] paths = {
                "heroes/" + type.name() + ".png",
                "heroes/" + type.name().toLowerCase() + ".png",
                "heroes/" + type.name() + ".jpg",
                "heroes/" + type.name().toLowerCase() + ".jpg"
        };
        for (String path : paths) {
            try (InputStream input = appContext.getAssets().open(path)) {
                Bitmap decoded = BitmapFactory.decodeStream(input);
                if (decoded != null) {
                    return scaleCenterCrop(decoded, width, height);
                }
            } catch (IOException ignored) {
            }
        }
        return null;
    }

    private static Bitmap scaleCenterCrop(Bitmap source, int width, int height) {
        if (source.getWidth() == width && source.getHeight() == height) {
            return source;
        }
        float scale = Math.max(width / (float) source.getWidth(), height / (float) source.getHeight());
        int scaledWidth = Math.max(width, Math.round(source.getWidth() * scale));
        int scaledHeight = Math.max(height, Math.round(source.getHeight() * scale));
        Bitmap scaled = Bitmap.createScaledBitmap(source, scaledWidth, scaledHeight, true);
        int x = Math.max(0, (scaledWidth - width) / 2);
        int y = Math.max(0, (scaledHeight - height) / 2);
        int cropWidth = Math.min(width, scaled.getWidth() - x);
        int cropHeight = Math.min(height, scaled.getHeight() - y);
        Bitmap cropped = Bitmap.createBitmap(scaled, x, y, cropWidth, cropHeight);
        if (scaled != source && scaled != cropped) {
            scaled.recycle();
        }
        return cropped;
    }
}
