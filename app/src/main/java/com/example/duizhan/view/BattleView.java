package com.example.duizhan.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.example.duizhan.R;
import com.example.duizhan.game.GameEntity;
import com.example.duizhan.game.GameSnapshot;
import com.example.duizhan.game.Projectile;
import com.example.duizhan.game.SkillSlot;
import com.example.duizhan.game.Team;
import com.example.duizhan.game.UnitKind;
import com.example.duizhan.game.VisualEffect;
import com.example.duizhan.game.EffectKind;
import com.example.duizhan.game.GameConfig;
import com.example.duizhan.game.MonsterCamp;
import com.example.duizhan.game.util.GameMath;
import com.example.duizhan.game.util.GameTerrain;
import com.example.duizhan.game.util.TeamStyle;
import com.example.duizhan.ui.util.DimenUtils;
import com.example.duizhan.ui.util.UiTextUtils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BattleView extends View {
    private static final long MANUAL_CAMERA_HOLD_MS = 3500L;
    private static final float BIG_MAP_SCALE = 5f;
    private static final float MIN_ZOOM = 0.45f;
    private static final float MAX_ZOOM = 2.4f;
    private static final float MINI_MAP_MAX_WIDTH_DP = 156f;

    public enum CameraFollowMode {
        BLUE,
        RED,
        THIRD,
        FREE
    }

    public interface ActionListener {
        void onMove(float x, float y);
    }

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF terrainRect = new RectF();
    private final RectF hudRect = new RectF();
    private final RectF hudZoomRect = new RectF();
    private final RectF hudCloseRect = new RectF();
    private final RectF hudOpenRect = new RectF();
    private final RectF miniMapRect = new RectF();
    private final RectF bigMapRect = new RectF();
    private final RectF bigMapCloseRect = new RectF();
    private final RectF miniViewportRect = new RectF();
    private final List<GameEntity> drawEntities = new ArrayList<>();
    private final List<Projectile> drawProjectiles = new ArrayList<>();
    private final List<VisualEffect> drawEffects = new ArrayList<>();
    private final List<float[]> blueTrail = new ArrayList<>();
    private final List<float[]> redTrail = new ArrayList<>();
    private GameSnapshot snapshot;
    private ActionListener actionListener;
    private float scale = 1f;
    private float baseScale = 1f;
    private float zoomFactor = 1f;
    private float offsetX;
    private float offsetY;
    private float cameraX;
    private float cameraY;
    private float joyCx;
    private float joyCy;
    private float defaultJoyCx;
    private float defaultJoyCy;
    private float knobX;
    private float knobY;
    private float joyRadius;
    private boolean draggingJoystick;
    private int joystickPointerId = -1;
    private boolean draggingCamera;
    private boolean draggingMiniMap;
    private int miniMapPointerId = -1;
    private boolean draggingBigMap;
    private int bigMapPointerId = -1;
    private boolean bigMapVisible;
    private boolean pinchZooming;
    private long lastMiniMapTapMs;
    private float lastMiniMapTapX;
    private float lastMiniMapTapY;
    private float pinchStartDistance;
    private float pinchStartZoom;
    private boolean pendingCameraDrag;
    private boolean pendingHudTouch;
    private HudMode hudMode = HudMode.COMPACT;
    private float cameraDownX;
    private float cameraDownY;
    private float lastCameraDragX;
    private float lastCameraDragY;
    private long manualCameraUntilMs;
    private CameraFollowMode cameraFollowMode = CameraFollowMode.BLUE;
    private boolean replayMode;
    private SkillSlot skillPreviewSlot;
    private long skillPreviewUntilMs;
    private Shader backgroundShader;
    private final HeroHumanoidRenderer heroRenderer = new HeroHumanoidRenderer();
    private final MinionCreatureRenderer minionRenderer = new MinionCreatureRenderer();
    private final EquipmentVfxRenderer equipmentVfxRenderer = new EquipmentVfxRenderer();
    private final CombatVfxRenderer combatVfxRenderer = new CombatVfxRenderer();
    private long lastDrawNanos;

    private enum HudMode {
        COMPACT,
        EXPANDED,
        HIDDEN
    }

    public BattleView(Context context) {
        super(context);
        init();
    }

    public BattleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void setActionListener(ActionListener actionListener) {
        if (actionListener == null && draggingJoystick) {
            resetJoystick(false);
        }
        this.actionListener = actionListener;
    }

    @Override
    protected void onDetachedFromWindow() {
        releaseTouchGesture(false);
        super.onDetachedFromWindow();
    }

    /** Clears manual camera drag without resetting an active joystick. */
    public void releaseCameraGesture() {
        endCameraDrag();
        pendingHudTouch = false;
        manualCameraUntilMs = 0L;
        invalidate();
    }

    private void endCameraDrag() {
        draggingCamera = false;
        draggingMiniMap = false;
        draggingBigMap = false;
        miniMapPointerId = -1;
        bigMapPointerId = -1;
        pendingCameraDrag = false;
    }

    private boolean canPinchZoom(MotionEvent event) {
        if (event.getPointerCount() < 2) {
            return false;
        }
        for (int i = 0; i < Math.min(2, event.getPointerCount()); i++) {
            float x = event.getX(i);
            float y = event.getY(i);
            if (isJoystickTouch(x, y) || isMiniMapTouch(x, y) || isBigMapTouch(x, y)
                    || isHudControlTouch(x, y) || isSkillPanelTouch(x, y)) {
                return false;
            }
        }
        return true;
    }

    private boolean isSkillPanelTouch(float x, float y) {
        float panelWidth = dp(220f);
        float panelHeight = dp(220f);
        float left = getWidth() - dp(12f) - panelWidth;
        float top = getHeight() - dp(8f) - panelHeight;
        return x >= left && x <= getWidth() && y >= top && y <= getHeight();
    }

    /** Clears all touch state including joystick. */
    public void releaseTouchGesture() {
        releaseTouchGesture(true);
    }

    private void releaseTouchGesture(boolean notifyMove) {
        if (draggingJoystick) {
            resetJoystick(notifyMove);
        }
        releaseCameraGesture();
    }

    /** Re-sync engine move input when joystick is still held after overlay button clicks. */
    public void syncMoveInput() {
        if (!draggingJoystick) {
            return;
        }
        float inputRadius = joystickInputRadius();
        notifyMove((knobX - joyCx) / inputRadius, (knobY - joyCy) / inputRadius);
    }

    public void setReplayMode(boolean replayMode) {
        this.replayMode = replayMode;
        if (replayMode) {
            releaseTouchGesture();
            cameraFollowMode = CameraFollowMode.THIRD;
        }
    }

    public void setCameraFollowMode(CameraFollowMode mode) {
        if (mode == null) {
            return;
        }
        cameraFollowMode = mode;
        if (mode == CameraFollowMode.FREE) {
            manualCameraUntilMs = System.currentTimeMillis() + MANUAL_CAMERA_HOLD_MS * 4;
        } else {
            manualCameraUntilMs = 0L;
        }
        invalidate();
    }

    public CameraFollowMode getCameraFollowMode() {
        return cameraFollowMode;
    }

    public void clearTrails() {
        blueTrail.clear();
        redTrail.clear();
    }

    public void appendTrailPoint(float blueX, float blueY, float redX, float redY) {
        blueTrail.add(new float[]{blueX, blueY});
        redTrail.add(new float[]{redX, redY});
        int maxTrailPoints = replayMode ? 320 : 800;
        if (blueTrail.size() > maxTrailPoints) {
            blueTrail.remove(0);
            redTrail.remove(0);
        }
    }

    public void setSnapshot(GameSnapshot snapshot) {
        this.snapshot = snapshot;
        drawEntities.clear();
        drawProjectiles.clear();
        drawEffects.clear();
        if (snapshot != null) {
            drawEntities.addAll(snapshot.entities);
            drawProjectiles.addAll(snapshot.projectiles);
            drawEffects.addAll(snapshot.effects);
            if (snapshot.snapCameraToBlueHero) {
                snapCameraToFountain();
            }
        }
        invalidate();
    }

    public GameSnapshot getSnapshot() {
        return snapshot;
    }

    public void showSkillPreview(SkillSlot slot) {
        skillPreviewSlot = slot;
        skillPreviewUntilMs = System.currentTimeMillis() + 720L;
        invalidate();
    }

    private void snapCameraToFountain() {
        cameraX = GameConfig.fountainX(Team.BLUE);
        cameraY = GameConfig.fountainY(Team.BLUE);
        manualCameraUntilMs = 0L;
        draggingCamera = false;
        pendingCameraDrag = false;
        resetJoystick();
        clampCamera();
    }

    private void init() {
        paint.setTextAlign(Paint.Align.CENTER);
        setFocusable(true);
        setFocusableInTouchMode(true);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        layoutMiniMapRect();
        baseScale = Math.max(w / GameConfig.WORLD_WIDTH, h / GameConfig.VISIBLE_WORLD_HEIGHT);
        applyZoomFactor(zoomFactor, w / 2f, h / 2f, false);
        cameraX = GameConfig.fountainX(Team.BLUE);
        cameraY = GameConfig.fountainY(Team.BLUE);
        updateCamera();
        joyRadius = Math.max(dp(62f), h * 0.13f);
        defaultJoyCx = joyRadius + dp(34f);
        defaultJoyCy = h - joyRadius - dp(30f);
        joyCx = defaultJoyCx;
        joyCy = defaultJoyCy;
        knobX = joyCx;
        knobY = joyCy;
        backgroundShader = new RadialGradient(w / 2f, h / 2f, w * 0.7f,
                Color.rgb(37, 99, 80), Color.rgb(15, 23, 42), Shader.TileMode.CLAMP);
    }

    private void layoutMiniMapRect() {
        if (getWidth() == 0 || getHeight() == 0) {
            miniMapRect.setEmpty();
            bigMapRect.setEmpty();
            bigMapCloseRect.setEmpty();
            return;
        }
        float miniMapWidth = Math.min(dp(MINI_MAP_MAX_WIDTH_DP), getWidth() * 0.22f);
        float miniMapHeight = miniMapWidth * GameConfig.WORLD_HEIGHT / GameConfig.WORLD_WIDTH;
        float right = getWidth() - dp(10f);
        float top = dp(56f);
        float skillPanelTop = getHeight() - dp(8f) - dp(220f);
        float maxBottom = skillPanelTop - dp(10f);
        if (top + miniMapHeight > maxBottom) {
            miniMapHeight = Math.max(dp(96f), maxBottom - top);
            miniMapWidth = miniMapHeight * GameConfig.WORLD_WIDTH / GameConfig.WORLD_HEIGHT;
        }
        miniMapRect.set(right - miniMapWidth, top, right, top + miniMapHeight);
        layoutBigMapRect();
    }

    private void layoutBigMapRect() {
        if (!bigMapVisible || miniMapRect.isEmpty()) {
            bigMapRect.setEmpty();
            bigMapCloseRect.setEmpty();
            return;
        }
        float width = miniMapRect.width() * BIG_MAP_SCALE;
        float height = miniMapRect.height() * BIG_MAP_SCALE;
        width = Math.min(width, getWidth() * 0.92f);
        height = Math.min(height, getHeight() * 0.86f);
        float left = (getWidth() - width) / 2f;
        float top = (getHeight() - height) / 2f;
        bigMapRect.set(left, top, left + width, top + height);
        bigMapCloseRect.set(bigMapRect.right - dp(38f), bigMapRect.top + dp(8f),
                bigMapRect.right - dp(8f), bigMapRect.top + dp(38f));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        long now = System.nanoTime();
        if (lastDrawNanos > 0L) {
            float dt = (now - lastDrawNanos) / 1_000_000_000f;
            equipmentVfxRenderer.advance(dt);
            combatVfxRenderer.advance(dt);
        }
        lastDrawNanos = now;
        super.onDraw(canvas);
        updateCamera();
        drawMap(canvas);
        canvas.save();
        canvas.translate(offsetX, offsetY);
        canvas.scale(scale, scale);
        drawWorld(canvas);
        canvas.restore();
        drawHeroScreenLabels(canvas);
        drawJoystick(canvas);
        drawHud(canvas);
        drawMiniMap(canvas);
        drawBigMap(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        layoutMiniMapRect();
        int action = event.getActionMasked();
        int pointerIndex = event.getActionIndex();
        float x = event.getX(pointerIndex);
        float y = event.getY(pointerIndex);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                if (event.getPointerCount() == 2 && canPinchZoom(event) && !replayMode) {
                    beginPinchZoom(event);
                    return true;
                }
                if (isJoystickTouch(x, y)) {
                    if (action == MotionEvent.ACTION_DOWN) {
                        endCameraDrag();
                        pendingHudTouch = false;
                    }
                    joystickPointerId = event.getPointerId(pointerIndex);
                    draggingJoystick = true;
                    joyCx = x;
                    joyCy = y;
                    knobX = joyCx;
                    knobY = joyCy;
                    notifyMove(0f, 0f);
                    invalidate();
                    return true;
                }
                if (action == MotionEvent.ACTION_DOWN) {
                    if (draggingJoystick) {
                        resetJoystick();
                    }
                    if (bigMapVisible && !isBigMapTouch(x, y) && !isBigMapCloseTouch(x, y)
                            && !isMiniMapTouch(x, y) && !isHudControlTouch(x, y)) {
                        bigMapVisible = false;
                        layoutBigMapRect();
                        invalidate();
                    }
                    if (isBigMapCloseTouch(x, y)) {
                        bigMapVisible = false;
                        layoutBigMapRect();
                        invalidate();
                        return true;
                    }
                    if (isBigMapTouch(x, y)) {
                        endCameraDrag();
                        draggingBigMap = true;
                        draggingCamera = true;
                        pendingCameraDrag = false;
                        bigMapPointerId = event.getPointerId(pointerIndex);
                        cameraDownX = x;
                        cameraDownY = y;
                        lastCameraDragX = x;
                        lastCameraDragY = y;
                        holdManualCamera();
                        updateCameraFromMapRect(x, y, bigMapRect);
                        invalidate();
                        return true;
                    }
                    if (isHudControlTouch(x, y)) {
                        pendingHudTouch = true;
                        cameraDownX = x;
                        cameraDownY = y;
                        invalidate();
                        return true;
                    }
                    if (isMiniMapTouch(x, y)) {
                        endCameraDrag();
                        draggingMiniMap = true;
                        draggingCamera = true;
                        pendingCameraDrag = false;
                        miniMapPointerId = event.getPointerId(pointerIndex);
                        cameraDownX = x;
                        cameraDownY = y;
                        lastCameraDragX = x;
                        lastCameraDragY = y;
                        holdManualCamera();
                        updateCameraFromMapRect(x, y, miniMapRect);
                        invalidate();
                        return true;
                    }
                    endCameraDrag();
                    pendingCameraDrag = true;
                    draggingCamera = false;
                    cameraDownX = x;
                    cameraDownY = y;
                    lastCameraDragX = x;
                    lastCameraDragY = y;
                    invalidate();
                }
                return true;
            case MotionEvent.ACTION_MOVE:
                if (pinchZooming && event.getPointerCount() >= 2) {
                    updatePinchZoom(event);
                    return true;
                }
                if (draggingBigMap) {
                    int bigIndex = findBigMapPointerIndex(event);
                    if (bigIndex >= 0) {
                        updateCameraDrag(event.getX(bigIndex), event.getY(bigIndex));
                        return true;
                    }
                }
                if (draggingMiniMap) {
                    int miniIndex = findMiniMapPointerIndex(event);
                    if (miniIndex >= 0) {
                        updateCameraDrag(event.getX(miniIndex), event.getY(miniIndex));
                        return true;
                    }
                }
                if (draggingJoystick) {
                    int joyIndex = findJoystickPointerIndex(event);
                    if (joyIndex >= 0) {
                        updateJoystick(event.getX(joyIndex), event.getY(joyIndex));
                        return true;
                    }
                }
                float moveX = event.getX();
                float moveY = event.getY();
                if (pendingHudTouch) {
                    if (GameMath.distance(moveX, moveY, cameraDownX, cameraDownY) > dp(10f)) {
                        pendingHudTouch = false;
                    }
                    return true;
                }
                if (pendingCameraDrag) {
                    if (GameMath.distance(moveX, moveY, cameraDownX, cameraDownY) <= dp(10f)) {
                        return true;
                    }
                    pendingCameraDrag = false;
                    draggingCamera = true;
                    holdManualCamera();
                }
                if (draggingCamera) {
                    updateCameraDrag(moveX, moveY);
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                if (pinchZooming && event.getPointerCount() <= 2) {
                    pinchZooming = false;
                    performClick();
                    return true;
                }
                if (draggingJoystick && event.getPointerId(pointerIndex) == joystickPointerId) {
                    resetJoystick();
                    performClick();
                    return true;
                }
                if (draggingBigMap && event.getPointerId(pointerIndex) == bigMapPointerId) {
                    endCameraDrag();
                    performClick();
                    return true;
                }
                if (draggingMiniMap && event.getPointerId(pointerIndex) == miniMapPointerId) {
                    if (GameMath.distance(x, y, cameraDownX, cameraDownY) <= dp(10f)) {
                        long now = System.currentTimeMillis();
                        if (now - lastMiniMapTapMs < 420L
                                && GameMath.distance(x, y, lastMiniMapTapX, lastMiniMapTapY) <= dp(24f)) {
                            bigMapVisible = true;
                            layoutBigMapRect();
                            lastMiniMapTapMs = 0L;
                        } else {
                            lastMiniMapTapMs = now;
                            lastMiniMapTapX = x;
                            lastMiniMapTapY = y;
                        }
                    }
                    endCameraDrag();
                    performClick();
                    return true;
                }
                if (action == MotionEvent.ACTION_UP) {
                    if (draggingCamera) {
                        endCameraDrag();
                        performClick();
                        return true;
                    }
                    if (pendingHudTouch) {
                        pendingHudTouch = false;
                        handleHudTouch(x, y);
                        performClick();
                        return true;
                    }
                    if (pendingCameraDrag) {
                        pendingCameraDrag = false;
                        focusCameraOnScreenPoint(x, y);
                        performClick();
                        return true;
                    }
                    performClick();
                }
                return true;
            case MotionEvent.ACTION_CANCEL:
                pinchZooming = false;
                releaseTouchGesture(isAttachedToWindow());
                return true;
            default:
                break;
        }
        return true;
    }

    private int findBigMapPointerIndex(MotionEvent event) {
        if (bigMapPointerId < 0) {
            return draggingBigMap ? 0 : -1;
        }
        for (int i = 0; i < event.getPointerCount(); i++) {
            if (event.getPointerId(i) == bigMapPointerId) {
                return i;
            }
        }
        return -1;
    }

    private int findMiniMapPointerIndex(MotionEvent event) {
        if (miniMapPointerId < 0) {
            return draggingMiniMap ? 0 : -1;
        }
        for (int i = 0; i < event.getPointerCount(); i++) {
            if (event.getPointerId(i) == miniMapPointerId) {
                return i;
            }
        }
        return -1;
    }

    private int findJoystickPointerIndex(MotionEvent event) {
        if (joystickPointerId < 0) {
            return draggingJoystick ? 0 : -1;
        }
        for (int i = 0; i < event.getPointerCount(); i++) {
            if (event.getPointerId(i) == joystickPointerId) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    private void updateJoystick(float x, float y) {
        float dx = x - joyCx;
        float dy = y - joyCy;
        float len = Math.max(1f, GameMath.distance(x, y, joyCx, joyCy));
        float inputRadius = joystickInputRadius();
        float limited = Math.min(len, inputRadius);
        knobX = joyCx + dx / len * limited;
        knobY = joyCy + dy / len * limited;
        notifyMove((knobX - joyCx) / inputRadius, (knobY - joyCy) / inputRadius);
        invalidate();
    }

    private void resetJoystick() {
        resetJoystick(true);
    }

    private void resetJoystick(boolean notifyMove) {
        draggingJoystick = false;
        joystickPointerId = -1;
        joyCx = defaultJoyCx;
        joyCy = defaultJoyCy;
        knobX = joyCx;
        knobY = joyCy;
        if (notifyMove) {
            notifyMove(0f, 0f);
        }
        invalidate();
    }

    private void updateCameraDrag(float x, float y) {
        if (draggingBigMap) {
            updateCameraFromMapRect(x, y, bigMapRect);
            lastCameraDragX = x;
            lastCameraDragY = y;
            invalidate();
            return;
        }
        if (draggingMiniMap) {
            updateCameraFromMapRect(x, y, miniMapRect);
            lastCameraDragX = x;
            lastCameraDragY = y;
            invalidate();
            return;
        }
        cameraX -= (x - lastCameraDragX) / Math.max(0.01f, scale);
        cameraY -= (y - lastCameraDragY) / Math.max(0.01f, scale);
        holdManualCamera();
        clampCamera();
        lastCameraDragX = x;
        lastCameraDragY = y;
        invalidate();
    }

    private void updateCameraFromMapRect(float x, float y, RectF mapRect) {
        float clampedX = GameMath.clamp(x, mapRect.left, mapRect.right);
        float clampedY = GameMath.clamp(y, mapRect.top, mapRect.bottom);
        cameraX = (clampedX - mapRect.left) / Math.max(1f, mapRect.width()) * GameConfig.WORLD_WIDTH;
        cameraY = (clampedY - mapRect.top) / Math.max(1f, mapRect.height()) * GameConfig.WORLD_HEIGHT;
        holdManualCamera();
        clampCamera();
    }

    /** Tap the battlefield to center the camera on that world location. */
    private void focusCameraOnScreenPoint(float screenX, float screenY) {
        if (isMiniMapTouch(screenX, screenY) || isBigMapTouch(screenX, screenY)
                || isHudControlTouch(screenX, screenY) || isSkillPanelTouch(screenX, screenY)) {
            return;
        }
        cameraX = (screenX - offsetX) / Math.max(0.01f, scale);
        cameraY = (screenY - offsetY) / Math.max(0.01f, scale);
        holdManualCamera();
        clampCamera();
    }

    private void holdManualCamera() {
        long holdMs = cameraFollowMode == CameraFollowMode.FREE
                ? MANUAL_CAMERA_HOLD_MS * 4
                : MANUAL_CAMERA_HOLD_MS;
        manualCameraUntilMs = System.currentTimeMillis() + holdMs;
    }

    private boolean isMiniMapTouch(float x, float y) {
        return miniMapRect.width() > 0f && miniMapRect.height() > 0f && miniMapRect.contains(x, y);
    }

    private boolean isBigMapTouch(float x, float y) {
        return bigMapVisible && bigMapRect.width() > 0f && bigMapRect.height() > 0f
                && bigMapRect.contains(x, y);
    }

    private boolean isBigMapCloseTouch(float x, float y) {
        return bigMapVisible && bigMapCloseRect.contains(x, y);
    }

    private void notifyMove(float x, float y) {
        if (!isAttachedToWindow() || actionListener == null) {
            return;
        }
        actionListener.onMove(x, y);
    }

    private boolean isJoystickTouch(float x, float y) {
        if (replayMode) {
            return false;
        }
        boolean inDefaultPad = GameMath.distance(x, y, defaultJoyCx, defaultJoyCy) <= joyRadius * 1.7f;
        boolean inControlZone = x <= getWidth() * 0.46f && y >= getHeight() * 0.46f;
        return inDefaultPad || inControlZone;
    }

    private boolean isHudControlTouch(float x, float y) {
        if (hudMode == HudMode.HIDDEN) {
            return hudOpenRect.contains(x, y);
        }
        return hudZoomRect.contains(x, y) || hudCloseRect.contains(x, y);
    }

    private void handleHudTouch(float x, float y) {
        if (hudMode == HudMode.HIDDEN && hudOpenRect.contains(x, y)) {
            hudMode = HudMode.COMPACT;
            invalidate();
            return;
        }
        if (hudCloseRect.contains(x, y)) {
            hudMode = HudMode.HIDDEN;
            invalidate();
            return;
        }
        if (hudZoomRect.contains(x, y)) {
            hudMode = hudMode == HudMode.EXPANDED ? HudMode.COMPACT : HudMode.EXPANDED;
            invalidate();
        }
    }

    private float joystickInputRadius() {
        return Math.max(dp(34f), joyRadius * 0.58f);
    }

    private void updateCamera() {
        if (getWidth() == 0 || getHeight() == 0) {
            return;
        }
        boolean shouldFollowHero = !draggingCamera
                && cameraFollowMode != CameraFollowMode.FREE
                && System.currentTimeMillis() >= manualCameraUntilMs;
        if (shouldFollowHero && cameraFollowMode == CameraFollowMode.THIRD) {
            GameEntity blue = findBlueHero();
            GameEntity red = findRedHero();
            if (blue != null && red != null) {
                cameraX += ((blue.x + red.x) / 2f + 80f - cameraX) * 0.15f;
                cameraY += ((blue.y + red.y) / 2f - cameraY) * 0.15f;
            } else {
                GameEntity follow = blue != null ? blue : red;
                if (follow != null) {
                    cameraX += (follow.x + 160f - cameraX) * 0.18f;
                    cameraY += (follow.y - cameraY) * 0.18f;
                }
            }
        } else if (shouldFollowHero) {
            GameEntity follow = cameraFollowMode == CameraFollowMode.RED ? findRedHero() : findBlueHero();
            if (follow != null) {
                cameraX += (follow.x + 160f - cameraX) * 0.18f;
                cameraY += (follow.y - cameraY) * 0.18f;
            }
        }
        clampCamera();
    }

    private void beginPinchZoom(MotionEvent event) {
        endCameraDrag();
        pendingCameraDrag = false;
        pendingHudTouch = false;
        pinchZooming = true;
        pinchStartDistance = Math.max(1f, pointerDistance(event));
        pinchStartZoom = zoomFactor;
        holdManualCamera();
    }

    private void updatePinchZoom(MotionEvent event) {
        float distance = Math.max(1f, pointerDistance(event));
        float nextZoom = pinchStartZoom * (distance / pinchStartDistance);
        float focalX = (event.getX(0) + event.getX(1)) / 2f;
        float focalY = (event.getY(0) + event.getY(1)) / 2f;
        applyZoomFactor(nextZoom, focalX, focalY, true);
        invalidate();
    }

    private float pointerDistance(MotionEvent event) {
        if (event.getPointerCount() < 2) {
            return 0f;
        }
        float dx = event.getX(0) - event.getX(1);
        float dy = event.getY(0) - event.getY(1);
        return GameMath.distance(0f, 0f, dx, dy);
    }

    private void applyZoomFactor(float nextZoom, float focalX, float focalY, boolean keepFocalPoint) {
        float oldScale = scale;
        zoomFactor = GameMath.clamp(nextZoom, MIN_ZOOM, MAX_ZOOM);
        scale = baseScale * zoomFactor;
        if (keepFocalPoint && getWidth() > 0 && getHeight() > 0) {
            float worldX = (focalX - offsetX) / Math.max(0.01f, oldScale);
            float worldY = (focalY - offsetY) / Math.max(0.01f, oldScale);
            offsetX = focalX - worldX * scale;
            offsetY = focalY - worldY * scale;
            cameraX = (getWidth() / 2f - offsetX) / Math.max(0.01f, scale);
            cameraY = (getHeight() / 2f - offsetY) / Math.max(0.01f, scale);
        }
        clampCamera();
    }

    private void clampCamera() {
        float viewportW = getWidth() / Math.max(0.01f, scale);
        float viewportH = getHeight() / Math.max(0.01f, scale);
        cameraX = GameMath.clamp(cameraX, viewportW / 2f, GameConfig.WORLD_WIDTH - viewportW / 2f);
        cameraY = GameMath.clamp(cameraY, viewportH / 2f, GameConfig.WORLD_HEIGHT - viewportH / 2f);
        offsetX = getWidth() / 2f - cameraX * scale;
        offsetY = getHeight() / 2f - cameraY * scale;
    }

    private GameEntity findBlueHero() {
        for (GameEntity entity : drawEntities) {
            if (entity.team == Team.BLUE && entity.kind == UnitKind.HERO) {
                return entity;
            }
        }
        return null;
    }

    private GameEntity findRedHero() {
        for (GameEntity entity : drawEntities) {
            if (entity.team == Team.RED && entity.kind == UnitKind.HERO) {
                return entity;
            }
        }
        return null;
    }

    private void drawTrails(Canvas canvas) {
        drawTrail(canvas, blueTrail, Color.argb(150, 96, 165, 250));
        drawTrail(canvas, redTrail, Color.argb(150, 248, 113, 113));
    }

    private void drawTrail(Canvas canvas, List<float[]> trail, int color) {
        if (trail.size() < 2) {
            return;
        }
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(worldDp(replayMode ? 3f : 4f));
        paint.setColor(color);
        int step = replayMode && trail.size() > 180 ? 2 : 1;
        for (int i = step; i < trail.size(); i += step) {
            float[] prev = trail.get(i - step);
            float[] current = trail.get(i);
            canvas.drawLine(prev[0], prev[1], current[0], current[1], paint);
        }
    }

    private void drawSkillPreview(Canvas canvas) {
        if (skillPreviewSlot == null || System.currentTimeMillis() > skillPreviewUntilMs) {
            return;
        }
        GameEntity hero = findBlueHero();
        if (hero == null || !hero.alive) {
            return;
        }
        float facing = hero.facingRad;
        float range = skillPreviewSlot == SkillSlot.ULTIMATE ? 520f
                : skillPreviewSlot == SkillSlot.SECONDARY ? 360f : 430f;
        int color = skillPreviewSlot == SkillSlot.ULTIMATE
                ? Color.rgb(250, 204, 21) : Color.rgb(96, 165, 250);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(54, Color.red(color), Color.green(color), Color.blue(color)));
        if (skillPreviewSlot == SkillSlot.PRIMARY) {
            canvas.save();
            canvas.translate(hero.x, hero.y);
            canvas.rotate((float) Math.toDegrees(facing));
            terrainRect.set(0f, -range * 0.23f, range, range * 0.23f);
            canvas.drawRoundRect(terrainRect, 28f, 28f, paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(worldDp(3f));
            paint.setColor(Color.argb(175, Color.red(color), Color.green(color), Color.blue(color)));
            canvas.drawRoundRect(terrainRect, 28f, 28f, paint);
            canvas.restore();
        } else {
            float targetX = hero.x + (float) Math.cos(facing) * range * 0.52f;
            float targetY = hero.y + (float) Math.sin(facing) * range * 0.52f;
            float radius = range * (skillPreviewSlot == SkillSlot.ULTIMATE ? 0.58f : 0.38f);
            canvas.drawCircle(targetX, targetY, radius, paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(worldDp(3f));
            paint.setColor(Color.argb(175, Color.red(color), Color.green(color), Color.blue(color)));
            canvas.drawCircle(targetX, targetY, radius, paint);
        }
    }

    private void drawMap(Canvas canvas) {
        if (backgroundShader != null) {
            paint.setShader(backgroundShader);
        }
        canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
        paint.setShader(null);
    }

    private void drawWorld(Canvas canvas) {
        drawTerrain(canvas);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(worldDp(18f));
        paint.setColor(Color.argb(90, 148, 163, 184));
        canvas.drawLine(GameConfig.BLUE_BASE_X + 160f, GameConfig.LANE_Y,
                GameConfig.RED_BASE_X - 160f, GameConfig.LANE_Y, paint);
        drawTerrainLabel(canvas, (GameConfig.BLUE_BASE_X + GameConfig.RED_BASE_X) / 2f, GameConfig.LANE_Y - 36f,
                getContext().getString(R.string.battle_lane_label), Color.rgb(226, 232, 240));
        drawFountain(canvas, GameConfig.fountainX(Team.BLUE), GameConfig.fountainY(Team.BLUE),
                TeamStyle.color(com.example.duizhan.game.Team.BLUE),
                getContext().getString(R.string.battle_fountain_blue));
        drawFountain(canvas, GameConfig.fountainX(Team.RED), GameConfig.fountainY(Team.RED),
                TeamStyle.color(com.example.duizhan.game.Team.RED),
                getContext().getString(R.string.battle_fountain_red));
        for (MonsterCamp camp : MonsterCamp.ALL) {
            drawCamp(canvas, camp.x, camp.y, camp.shortLabel);
        }
        drawTrails(canvas);
        drawSkillPreview(canvas);
        paint.setStrokeWidth(worldDp(2f));
        paint.setColor(Color.argb(70, 255, 255, 255));
        canvas.drawRect(GameConfig.MIN_X - 30f, GameConfig.MIN_Y - 40f,
                GameConfig.MAX_X + 30f, GameConfig.MAX_Y + 40f, paint);

        for (VisualEffect effect : drawEffects) {
            if (effect.kind != EffectKind.TEXT) {
                combatVfxRenderer.drawEffect(canvas, effect);
            }
        }
        for (GameEntity entity : drawEntities) {
            if (isHiddenFromPlayer(entity)) {
                continue;
            }
            drawEntity(canvas, entity);
        }
        pruneHeroRenderer();
        pruneMinionRenderer();
        for (Projectile projectile : drawProjectiles) {
            combatVfxRenderer.drawProjectile(canvas, projectile);
        }
        for (VisualEffect effect : drawEffects) {
            if (effect.kind == EffectKind.TEXT) {
                combatVfxRenderer.drawEffect(canvas, effect);
            }
        }
        drawEntityOverlays(canvas);
    }

    private void pruneMinionRenderer() {
        Set<Long> activeIds = new HashSet<>();
        for (GameEntity entity : drawEntities) {
            if (entity.kind == UnitKind.MINION || entity.kind == UnitKind.RANGED_MINION
                    || entity.kind == UnitKind.BRUTE || entity.kind == UnitKind.MONSTER) {
                activeIds.add(entity.id);
            }
        }
        minionRenderer.prune(activeIds);
    }

    private void pruneHeroRenderer() {
        Set<Long> activeHeroIds = new HashSet<>();
        for (GameEntity entity : drawEntities) {
            if (entity.kind == UnitKind.HERO) {
                activeHeroIds.add(entity.id);
            }
        }
        heroRenderer.prune(activeHeroIds);
    }

    private void drawEntity(Canvas canvas, GameEntity entity) {
        if (!entity.alive && entity.kind != UnitKind.HERO && entity.kind != UnitKind.TOWER) {
            return;
        }
        int color = TeamStyle.color(entity.team);
        drawEntityGroundPresence(canvas, entity, color);
        if (entity.kind == UnitKind.TOWER) {
            drawTower(canvas, entity, color);
        } else if (entity.kind == UnitKind.MONSTER) {
            drawMonster(canvas, entity);
        } else {
            drawUnit(canvas, entity, color);
        }
    }

    private void drawEntityOverlays(Canvas canvas) {
        for (GameEntity entity : drawEntities) {
            if (isHiddenFromPlayer(entity)) {
                continue;
            }
            if (!entity.alive && entity.kind != UnitKind.HERO && entity.kind != UnitKind.TOWER) {
                continue;
            }
            drawHealth(canvas, entity);
        }
    }

    private void drawEntityGroundPresence(Canvas canvas, GameEntity entity, int color) {
        float radius = Math.max(8f, entity.radius);
        float alphaScale = entity.alive ? 1f : 0.45f;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb((int) (72 * alphaScale), 0, 0, 0));
        canvas.drawOval(entity.x - radius * 1.15f, entity.y + radius * 0.56f,
                entity.x + radius * 1.15f, entity.y + radius * 1.08f, paint);
        if (entity.kind == UnitKind.HERO) {
            float spellPulse = Math.max(entity.skillTimer / Math.max(0.01f, entity.skillCooldown),
                    entity.ultimateTimer / Math.max(0.01f, entity.ultimateCooldown));
            int glowAlpha = (int) (36 + 86 * Math.min(1f, spellPulse));
            paint.setShader(new RadialGradient(entity.x, entity.y, radius * (2.2f + spellPulse * 1.2f),
                    new int[]{
                            Color.argb(glowAlpha, Color.red(color), Color.green(color), Color.blue(color)),
                            Color.argb(0, Color.red(color), Color.green(color), Color.blue(color))
                    },
                    new float[]{0.18f, 1f}, Shader.TileMode.CLAMP));
            canvas.drawCircle(entity.x, entity.y, radius * (2.2f + spellPulse * 1.2f), paint);
            paint.setShader(null);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(worldDp(2.2f));
            paint.setColor(Color.argb(145, Color.red(color), Color.green(color), Color.blue(color)));
            canvas.drawCircle(entity.x, entity.y + radius * 0.15f, radius * 1.34f, paint);
            paint.setStrokeWidth(worldDp(1.1f));
            paint.setColor(Color.argb(100, 255, 255, 255));
            canvas.drawCircle(entity.x, entity.y + radius * 0.15f, radius * 1.0f, paint);
        } else if (entity.kind == UnitKind.MONSTER) {
            paint.setShader(new RadialGradient(entity.x, entity.y, radius * 2.4f,
                    new int[]{Color.argb(82, 245, 158, 11), Color.TRANSPARENT},
                    new float[]{0.12f, 1f}, Shader.TileMode.CLAMP));
            canvas.drawCircle(entity.x, entity.y, radius * 2.4f, paint);
            paint.setShader(null);
        }
    }

    private void drawTerrain(Canvas canvas) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(34, 120, 72));
        canvas.drawRect(GameConfig.MIN_X, GameConfig.MIN_Y, GameConfig.MAX_X, GameConfig.MAX_Y, paint);

        for (GameTerrain.EllipseRegion patch : GameTerrain.GRASS_PATCHES) {
            paint.setColor(Color.rgb(46, 140, 82));
            terrainRect.set(patch.cx - patch.rx, patch.cy - patch.ry, patch.cx + patch.rx, patch.cy + patch.ry);
            canvas.drawOval(terrainRect, paint);
            paint.setColor(Color.argb(90, 74, 222, 128));
            canvas.drawOval(terrainRect, paint);
        }

        drawRiver(canvas);

        for (GameTerrain.EllipseRegion pond : GameTerrain.PONDS) {
            drawPond(canvas, pond);
        }

        for (GameTerrain.EllipseRegion brush : GameTerrain.BRUSHES) {
            drawBrush(canvas, brush.cx, brush.cy, brush.rx, brush.ry);
            drawTerrainLabel(canvas, brush.cx, brush.cy - brush.ry - 28f,
                    getContext().getString(R.string.battle_terrain_brush), Color.rgb(187, 247, 208));
        }

        for (GameTerrain.RectRegion wall : GameTerrain.WALLS) {
            drawWall(canvas, wall.left, wall.top, wall.right, wall.bottom);
            drawTerrainLabel(canvas, (wall.left + wall.right) / 2f, wall.top - 24f,
                    getContext().getString(R.string.battle_terrain_wall), Color.rgb(231, 229, 228));
        }

        for (int i = 0; i < GameTerrain.TREE_X.length; i++) {
            drawTreeCluster(canvas, GameTerrain.TREE_X[i], GameTerrain.TREE_Y[i]);
        }
    }

    private void drawPond(Canvas canvas, GameTerrain.EllipseRegion pond) {
        terrainRect.set(pond.cx - pond.rx, pond.cy - pond.ry, pond.cx + pond.rx, pond.cy + pond.ry);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(185, 14, 116, 144));
        canvas.drawOval(terrainRect, paint);
        paint.setColor(Color.argb(170, 56, 189, 248));
        canvas.drawOval(terrainRect, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(worldDp(4f));
        paint.setColor(Color.argb(120, 186, 230, 253));
        canvas.drawOval(terrainRect, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(110, 224, 242, 254));
        canvas.drawCircle(pond.cx - pond.rx * 0.28f, pond.cy - pond.ry * 0.12f, Math.min(pond.rx, pond.ry) * 0.18f, paint);
        canvas.drawCircle(pond.cx + pond.rx * 0.22f, pond.cy + pond.ry * 0.18f, Math.min(pond.rx, pond.ry) * 0.14f, paint);
        drawTerrainLabel(canvas, pond.cx, pond.cy,
                getContext().getString(R.string.battle_terrain_pond), Color.rgb(186, 230, 253));
    }

    private void drawTerrainLabel(Canvas canvas, float x, float y, String label, int color) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(150, 15, 23, 42));
        paint.setTextSize(worldSp(18f));
        float textWidth = paint.measureText(label);
        canvas.drawRoundRect(x - textWidth / 2f - 10f, y - 16f, x + textWidth / 2f + 10f, y + 12f, 8f, 8f, paint);
        paint.setColor(color);
        canvas.drawText(label, x, y + 4f, paint);
    }

    private void drawRiver(Canvas canvas) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(worldDp(92f));
        paint.setColor(Color.argb(175, 14, 116, 144));
        canvas.drawLine(GameTerrain.RIVER_X1, GameTerrain.RIVER_Y1, GameTerrain.RIVER_X2, GameTerrain.RIVER_Y2, paint);
        paint.setStrokeWidth(worldDp(44f));
        paint.setColor(Color.argb(165, 56, 189, 248));
        canvas.drawLine(GameTerrain.RIVER_X1 + 30f, GameTerrain.RIVER_Y1 + 16f,
                GameTerrain.RIVER_X2 - 25f, GameTerrain.RIVER_Y2 - 18f, paint);
        paint.setStrokeWidth(worldDp(16f));
        paint.setColor(Color.argb(130, 224, 242, 254));
        canvas.drawLine(GameTerrain.RIVER_X1 + 48f, GameTerrain.RIVER_Y1 + 24f,
                GameTerrain.RIVER_X2 - 42f, GameTerrain.RIVER_Y2 - 26f, paint);
        paint.setStrokeCap(Paint.Cap.BUTT);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(145, 56, 189, 248));
        for (MonsterCamp camp : MonsterCamp.ALL) {
            terrainRect.set(camp.x - 192f, camp.y - 124f, camp.x + 192f, camp.y + 124f);
            canvas.drawOval(terrainRect, paint);
        }

        drawTerrainLabel(canvas, 1820f, 1805f,
                getContext().getString(R.string.battle_terrain_river), Color.rgb(186, 230, 253));
    }

    private void drawBrush(Canvas canvas, float cx, float cy, float rx, float ry) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(16, 110, 62));
        terrainRect.set(cx - rx, cy - ry, cx + rx, cy + ry);
        canvas.drawOval(terrainRect, paint);
        paint.setColor(Color.rgb(34, 150, 88));
        canvas.drawOval(terrainRect, paint);
        paint.setColor(Color.argb(180, 74, 222, 128));
        for (int i = -4; i <= 4; i++) {
            canvas.drawCircle(cx + i * rx * 0.18f, cy + (i % 2 == 0 ? -ry * 0.22f : ry * 0.20f),
                    Math.min(rx, ry) * 0.28f, paint);
        }
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(worldDp(4f));
        paint.setColor(Color.argb(150, 134, 239, 172));
        canvas.drawOval(terrainRect, paint);
    }

    private void drawWall(Canvas canvas, float left, float top, float right, float bottom) {
        terrainRect.set(left, top, right, bottom);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(68, 64, 60));
        canvas.drawRoundRect(terrainRect, 18f, 18f, paint);
        paint.setColor(Color.rgb(120, 113, 108));
        float brickHeight = Math.max(18f, (bottom - top) / 3f);
        for (float y = top + 8f; y < bottom - 6f; y += brickHeight) {
            float offset = ((int) ((y - top) / brickHeight) % 2) * 22f;
            for (float x = left + 10f + offset; x < right - 10f; x += 44f) {
                canvas.drawRect(x, y, Math.min(x + 36f, right - 8f), y + brickHeight - 6f, paint);
            }
        }
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(worldDp(6f));
        paint.setColor(Color.rgb(214, 211, 209));
        canvas.drawRoundRect(terrainRect, 18f, 18f, paint);
    }

    private void drawTreeCluster(Canvas canvas, float x, float y) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(120, 53, 15));
        canvas.drawRect(x - 8f, y + 24f, x + 12f, y + 96f, paint);
        canvas.drawRect(x + 34f, y + 30f, x + 48f, y + 88f, paint);
        paint.setColor(Color.rgb(21, 128, 61));
        canvas.drawCircle(x - 48f, y + 12f, 46f, paint);
        canvas.drawCircle(x + 4f, y - 28f, 54f, paint);
        canvas.drawCircle(x + 62f, y + 16f, 42f, paint);
        paint.setColor(Color.rgb(34, 160, 82));
        canvas.drawCircle(x - 18f, y - 8f, 34f, paint);
        canvas.drawCircle(x + 36f, y + 2f, 30f, paint);
    }

    private boolean isHiddenFromPlayer(GameEntity entity) {
        if (replayMode) {
            return false;
        }
        if (entity.team != Team.RED || entity.kind != UnitKind.HERO || !entity.alive) {
            return false;
        }
        GameEntity blue = findBlueHero();
        return blue != null && !GameTerrain.canSee(blue, entity);
    }

    private void drawTower(Canvas canvas, GameEntity entity, int color) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(55, Color.red(color), Color.green(color), Color.blue(color)));
        canvas.drawCircle(entity.x, entity.y, entity.attackRange, paint);
        paint.setColor(color);
        canvas.drawRect(entity.x - 34f, entity.y - 52f, entity.x + 34f, entity.y + 52f, paint);
        paint.setColor(Color.rgb(248, 250, 252));
        canvas.drawRect(entity.x - 20f, entity.y - 78f, entity.x + 20f, entity.y - 52f, paint);
    }

    private void drawUnit(Canvas canvas, GameEntity entity, int color) {
        if (entity.kind == UnitKind.HERO) {
            drawHeroUnit(canvas, entity, color);
            return;
        }
        if (entity.hitTimer > 0f) {
            combatVfxRenderer.drawEntityHitFlash(canvas, entity.x, entity.y, entity.radius, entity.hitTimer, color);
        }
        if (entity.alive) {
            minionRenderer.draw(canvas, paint, entity, worldDp(1.6f));
        } else {
            minionRenderer.drawDead(canvas, paint, entity, worldDp(1.6f));
        }
    }

    private void drawHeroUnit(Canvas canvas, GameEntity entity, int color) {
        boolean stealthVisual = entity.alive
                && (entity.stealthTimer > 0f || GameTerrain.isInBrush(entity.x, entity.y));
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(38, Color.red(color), Color.green(color), Color.blue(color)));
        canvas.drawCircle(entity.x, entity.y, entity.attackRange, paint);
        if (entity.hitTimer > 0f) {
            combatVfxRenderer.drawEntityHitFlash(canvas, entity.x, entity.y, entity.radius, entity.hitTimer, color);
        }
        drawHeroAuras(canvas, entity, color, stealthVisual);
        if (entity.alive) {
            heroRenderer.draw(canvas, paint, entity, color, worldDp(2f));
            equipmentVfxRenderer.draw(canvas, paint, entity, worldDp(2f));
        } else {
            heroRenderer.drawDead(canvas, paint, entity, color, worldDp(2f));
        }
        if (entity.mimicTimer > 0f) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(worldDp(4f));
            paint.setColor(Color.rgb(250, 204, 21));
            canvas.drawCircle(entity.x, entity.y, entity.radius * 2.2f, paint);
        }
    }

    private void drawHeroAuras(Canvas canvas, GameEntity entity, int color, boolean stealthVisual) {
        if (entity.shieldTimer > 0f) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(worldDp(entity.barrierHp > 0f ? 8f : 5f));
            paint.setColor(Color.rgb(34, 197, 94));
            canvas.drawCircle(entity.x, entity.y, entity.radius * 2.1f, paint);
        }
        if (entity.blockTimer > 0f) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(worldDp(5f));
            paint.setColor(Color.rgb(226, 232, 240));
            canvas.drawCircle(entity.x, entity.y, entity.radius * 2.6f, paint);
        }
        if (entity.damageBoostTimer > 0f) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(worldDp(5f));
            paint.setColor(Color.rgb(250, 204, 21));
            canvas.drawCircle(entity.x, entity.y, entity.radius * 3f, paint);
        }
        if (entity.redBuffTimer > 0f) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(worldDp(4f));
            paint.setColor(Color.rgb(239, 68, 68));
            canvas.drawCircle(entity.x, entity.y, entity.radius * 1.8f, paint);
        }
        if (entity.blueBuffTimer > 0f) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(worldDp(4f));
            paint.setColor(Color.rgb(96, 165, 250));
            canvas.drawCircle(entity.x, entity.y, entity.radius * 2.3f, paint);
        }
        if (stealthVisual) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(worldDp(4f));
            paint.setColor(Color.rgb(134, 239, 172));
            canvas.drawCircle(entity.x, entity.y, entity.radius * 2.5f, paint);
        }
        if (entity.recallChannelTimer > 0f) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(worldDp(6f));
            paint.setColor(Color.rgb(34, 197, 94));
            canvas.drawCircle(entity.x, entity.y, entity.radius * 3.4f, paint);
        }
    }

    private void drawMonster(Canvas canvas, GameEntity entity) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(45, 245, 158, 11));
        canvas.drawCircle(entity.x, entity.y, entity.attackRange, paint);
        if (entity.hitTimer > 0f) {
            combatVfxRenderer.drawEntityHitFlash(canvas, entity.x, entity.y, entity.radius, entity.hitTimer,
                    Color.rgb(245, 158, 11));
        }
        if (entity.alive) {
            minionRenderer.draw(canvas, paint, entity, worldDp(1.8f));
        } else {
            minionRenderer.drawDead(canvas, paint, entity, worldDp(1.8f));
        }
    }

    private void drawHealth(Canvas canvas, GameEntity entity) {
        float w = entity.kind == UnitKind.TOWER ? 128f : entity.kind == UnitKind.HERO ? 104f : 72f;
        float h = entity.kind == UnitKind.HERO ? 11f : entity.kind == UnitKind.TOWER ? 13f : 10f;
        float y = entity.y - entity.radius - 32f;
        if (entity.kind == UnitKind.HERO) {
            y -= 26f;
        }
        paint.setTextAlign(Paint.Align.CENTER);
        if (entity.kind == UnitKind.HERO) {
            y -= 8f;
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(235, 7, 12, 24));
        canvas.drawRoundRect(entity.x - w / 2f, y, entity.x + w / 2f, y + h, 4f, 4f, paint);
        paint.setColor(TeamStyle.color(entity.team));
        float hpRate = Math.max(0f, entity.hp) / Math.max(1f, entity.maxHp);
        canvas.drawRoundRect(entity.x - w / 2f, y, entity.x - w / 2f + w * hpRate, y + h, 4f, 4f, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(worldDp(1.4f));
        paint.setColor(Color.argb(210, 248, 250, 252));
        canvas.drawRoundRect(entity.x - w / 2f, y, entity.x + w / 2f, y + h, 4f, 4f, paint);
        paint.setStyle(Paint.Style.FILL);
        if (entity.kind == UnitKind.HERO) {
            float manaY = y + h + 4f;
            paint.setColor(Color.argb(235, 8, 20, 38));
            canvas.drawRoundRect(entity.x - w / 2f, manaY, entity.x + w / 2f, manaY + 5f, 3f, 3f, paint);
            paint.setColor(Color.rgb(56, 189, 248));
            float manaRate = heroResourceRate(entity);
            canvas.drawRoundRect(entity.x - w / 2f, manaY, entity.x - w / 2f + w * manaRate,
                    manaY + 5f, 3f, 3f, paint);
            paint.setColor(Color.rgb(250, 204, 21));
            float expRate = entity.nextExp <= 0 ? 0f : Math.min(1f, entity.exp / (float) entity.nextExp);
            canvas.drawRect(entity.x - w / 2f, manaY + 7f,
                    entity.x - w / 2f + w * expRate, manaY + 10f, paint);
            if (entity.barrierHp > 0f && entity.shieldTimer > 0f) {
                paint.setColor(Color.rgb(134, 239, 172));
                float shieldRate = Math.min(1f, entity.barrierHp / Math.max(1f, entity.maxHp * 0.24f));
                canvas.drawRect(entity.x - w / 2f, manaY + 12f,
                        entity.x - w / 2f + w * shieldRate, manaY + 15f, paint);
            }
        } else if (entity.kind == UnitKind.MONSTER) {
            paint.setColor(Color.rgb(254, 243, 199));
            paint.setTextSize(worldSp(12f));
            canvas.drawText(entity.name, entity.x, y - 5f, paint);
        }
        paint.setTextAlign(Paint.Align.LEFT);
    }

    private float heroResourceRate(GameEntity entity) {
        float primary = cooldownReadyRate(entity.skillTimer, entity.skillCooldown);
        float secondary = cooldownReadyRate(entity.secondarySkillTimer, entity.secondarySkillCooldown);
        float ultimate = cooldownReadyRate(entity.ultimateTimer, entity.ultimateCooldown);
        float talent = cooldownReadyRate(entity.talentTimer, entity.talentCooldown);
        return Math.max(0f, Math.min(1f, (primary + secondary + ultimate + talent) / 4f));
    }

    private float cooldownReadyRate(float remaining, float max) {
        if (max <= 0f) {
            return 1f;
        }
        return 1f - Math.max(0f, Math.min(1f, remaining / max));
    }

    private String shortHudName(String name) {
        if (name == null) {
            return "";
        }
        int split = name.indexOf('·');
        if (split >= 0 && split < name.length() - 1) {
            return name.substring(split + 1);
        }
        int space = name.lastIndexOf(' ');
        if (space >= 0 && space < name.length() - 1) {
            return name.substring(space + 1);
        }
        return name;
    }

    private void drawHeroScreenLabels(Canvas canvas) {
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setStyle(Paint.Style.FILL);
        for (GameEntity entity : drawEntities) {
            if (entity.kind != UnitKind.HERO || isHiddenFromPlayer(entity)) {
                continue;
            }
            float sx = entity.x * scale + offsetX;
            float sy = entity.y * scale + offsetY - entity.radius * scale - dp(34f);
            String label = heroHudLabel(entity);
            if (!entity.alive) {
                label = heroDisplayName(entity) + " · "
                        + UiTextUtils.respawn(getContext(), entity.respawnTimer);
                sy -= dp(6f);
            }
            paint.setTextSize(sp(13f));
            paint.setFakeBoldText(true);
            float textWidth = paint.measureText(label);
            float padH = dp(7f);
            float padTop = dp(13f);
            float padBottom = dp(5f);
            paint.setColor(Color.argb(235, 15, 23, 42));
            canvas.drawRoundRect(sx - textWidth / 2f - padH, sy - padTop,
                    sx + textWidth / 2f + padH, sy + padBottom, dp(5f), dp(5f), paint);
            int nameColor = entity.team == Team.BLUE
                    ? Color.rgb(147, 197, 253) : Color.rgb(252, 165, 165);
            paint.setColor(entity.alive ? nameColor : Color.rgb(203, 213, 225));
            canvas.drawText(label, sx, sy, paint);
            paint.setFakeBoldText(false);
        }
        paint.setTextAlign(Paint.Align.LEFT);
    }

    private String heroHudLabel(GameEntity entity) {
        return "Lv." + entity.level + " " + heroDisplayName(entity);
    }

    private String heroDisplayName(GameEntity entity) {
        if (entity.heroType != null && entity.heroType.label != null && entity.heroType.label.length() > 0) {
            return shortHudName(entity.heroType.label);
        }
        return shortHudName(entity.name);
    }

    private void drawProjectile(Canvas canvas, Projectile projectile) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(65, Color.red(projectile.color), Color.green(projectile.color), Color.blue(projectile.color)));
        canvas.drawCircle(projectile.x, projectile.y, projectile.radius * 2.6f, paint);
        paint.setColor(projectile.color);
        canvas.drawCircle(projectile.x, projectile.y, projectile.radius, paint);
        if (projectile.label != null && projectile.label.length() > 0) {
            paint.setTextSize(worldSp(15f));
            paint.setColor(Color.WHITE);
            canvas.drawText(projectile.label, projectile.x, projectile.y - 12f, paint);
        }
    }

    private void drawEffect(Canvas canvas, VisualEffect effect) {
        float progress = effect.maxTtl <= 0f ? 0f : 1f - effect.ttl / effect.maxTtl;
        int alpha = Math.max(0, Math.min(255, (int) (210 * effect.ttl / Math.max(0.01f, effect.maxTtl))));
        paint.setColor(Color.argb(alpha, Color.red(effect.color), Color.green(effect.color), Color.blue(effect.color)));
        if (effect.kind == EffectKind.RING) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(worldDp(8f - progress * 5f));
            canvas.drawCircle(effect.x, effect.y, effect.radius * (0.65f + progress * 0.45f), paint);
        } else if (effect.kind == EffectKind.LINE) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(worldDp(9f));
            canvas.drawLine(effect.x, effect.y, effect.x2, effect.y2, paint);
        } else {
            paint.setStyle(Paint.Style.FILL);
            paint.setTextSize(worldSp(24f * effect.textScale));
            paint.setFakeBoldText(effect.textScale > 1.05f);
            String label = effect.text == null ? "" : effect.text;
            paint.setColor(Color.argb(alpha, 15, 23, 42));
            canvas.drawText(label, effect.x + worldDp(2f), effect.y + worldDp(2f), paint);
            paint.setColor(Color.argb(alpha, Color.red(effect.color), Color.green(effect.color), Color.blue(effect.color)));
            canvas.drawText(label, effect.x, effect.y, paint);
            paint.setFakeBoldText(false);
        }
    }

    private void drawCamp(Canvas canvas, float x, float y, String label) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(50, 245, 158, 11));
        canvas.drawCircle(x, y, 62f, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(worldDp(3f));
        paint.setColor(Color.argb(140, 245, 158, 11));
        canvas.drawCircle(x, y, 62f, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(worldSp(18f));
        paint.setColor(Color.rgb(254, 243, 199));
        canvas.drawText(label, x, y + 6f, paint);
    }

    private void drawFountain(Canvas canvas, float x, float y, int color, String label) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(34, 34, 197, 94));
        canvas.drawCircle(x, y, GameConfig.FOUNTAIN_RADIUS, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(worldDp(6f));
        paint.setColor(Color.argb(155, Color.red(color), Color.green(color), Color.blue(color)));
        canvas.drawCircle(x, y, GameConfig.FOUNTAIN_RADIUS, paint);
        paint.setStrokeWidth(worldDp(2f));
        paint.setColor(Color.argb(135, 134, 239, 172));
        canvas.drawCircle(x, y, GameConfig.FOUNTAIN_RADIUS * 0.62f, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(worldSp(20f));
        paint.setColor(Color.rgb(187, 247, 208));
        canvas.drawText(label, x, y + 7f, paint);
    }

    private void drawJoystick(Canvas canvas) {
        if (replayMode) {
            return;
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(90, 15, 23, 42));
        canvas.drawCircle(joyCx, joyCy, joyRadius, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(3f));
        paint.setColor(Color.argb(150, 226, 232, 240));
        canvas.drawCircle(joyCx, joyCy, joyRadius, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(200, 248, 250, 252));
        canvas.drawCircle(knobX, knobY, joyRadius * 0.38f, paint);
    }

    private void drawHud(Canvas canvas) {
        if (snapshot == null) {
            return;
        }
        if (hudMode == HudMode.HIDDEN) {
            drawHudOpenButton(canvas);
            return;
        }
        if (hudMode == HudMode.COMPACT) {
            drawCompactHud(canvas);
            return;
        }
        drawExpandedHud(canvas);
    }

    private void drawHudOpenButton(Canvas canvas) {
        hudOpenRect.set(dp(16f), dp(58f), dp(104f), dp(94f));
        hudZoomRect.setEmpty();
        hudCloseRect.setEmpty();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(195, 15, 23, 42));
        canvas.drawRoundRect(hudOpenRect, dp(8f), dp(8f), paint);
        paint.setTextSize(sp(13f));
        paint.setColor(Color.rgb(250, 204, 21));
        canvas.drawText(getContext().getString(R.string.battle_hud_open),
                hudOpenRect.centerX(), hudOpenRect.centerY() + dp(5f), paint);
    }

    private void drawCompactHud(Canvas canvas) {
        hudOpenRect.setEmpty();
        hudRect.set(dp(16f), dp(58f), dp(286f), dp(116f));
        hudZoomRect.set(hudRect.right - dp(76f), hudRect.top + dp(10f),
                hudRect.right - dp(44f), hudRect.top + dp(42f));
        hudCloseRect.set(hudRect.right - dp(38f), hudRect.top + dp(10f),
                hudRect.right - dp(6f), hudRect.top + dp(42f));

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(190, 15, 23, 42));
        canvas.drawRoundRect(hudRect, dp(8f), dp(8f), paint);
        paint.setTextSize(sp(13f));
        paint.setColor(Color.WHITE);
        canvas.drawText(getContext().getString(R.string.battle_view_hud_compact_format,
                        drawEntities.size(), snapshot.blueGold, snapshot.blueLevel,
                        snapshot.blueTowerHp, snapshot.redTowerHp),
                hudRect.left + dp(12f), hudRect.top + dp(36f), paint);
        drawHudButton(canvas, hudZoomRect, getContext().getString(R.string.battle_hud_expand));
        drawHudButton(canvas, hudCloseRect, getContext().getString(R.string.battle_hud_close));
    }

    private void drawExpandedHud(Canvas canvas) {
        hudOpenRect.setEmpty();
        hudRect.set(dp(16f), dp(58f), dp(410f), dp(172f));
        hudZoomRect.set(hudRect.right - dp(76f), hudRect.top + dp(10f),
                hudRect.right - dp(44f), hudRect.top + dp(42f));
        hudCloseRect.set(hudRect.right - dp(38f), hudRect.top + dp(10f),
                hudRect.right - dp(6f), hudRect.top + dp(42f));

        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(sp(15f));
        paint.setColor(Color.argb(190, 15, 23, 42));
        canvas.drawRoundRect(hudRect, dp(8f), dp(8f), paint);
        paint.setColor(Color.WHITE);
        canvas.drawText(UiTextUtils.battleHud(getContext(), drawEntities.size(),
                drawProjectiles.size(), drawEffects.size()), dp(188f), dp(82f), paint);
        paint.setTextSize(sp(13f));
        paint.setColor(Color.rgb(226, 232, 240));
        canvas.drawText(UiTextUtils.battleCombatLine(getContext(), snapshot), dp(213f), dp(106f), paint);
        canvas.drawText(UiTextUtils.battleStats(getContext(), snapshot), dp(213f), dp(128f), paint);
        paint.setColor(Color.rgb(250, 204, 21));
        canvas.drawText(UiTextUtils.equipmentLineOne(getContext(), snapshot), dp(213f), dp(150f), paint);
        canvas.drawText(UiTextUtils.equipmentLineTwo(getContext(), snapshot), dp(213f), dp(168f), paint);
        drawHudButton(canvas, hudZoomRect, getContext().getString(R.string.battle_hud_collapse));
        drawHudButton(canvas, hudCloseRect, getContext().getString(R.string.battle_hud_close));
    }

    private void drawHudButton(Canvas canvas, RectF rect, String text) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(205, 30, 41, 59));
        canvas.drawRoundRect(rect, dp(6f), dp(6f), paint);
        paint.setTextSize(sp(15f));
        paint.setFakeBoldText(true);
        paint.setColor(Color.WHITE);
        canvas.drawText(text, rect.centerX(), rect.centerY() + dp(5f), paint);
        paint.setFakeBoldText(false);
    }

    private void drawMiniMap(Canvas canvas) {
        layoutMiniMapRect();
        if (snapshot == null) {
            return;
        }
        drawMapWindow(canvas, miniMapRect, dp(8f), dp(1.2f), dp(1.6f), dp(2.6f), dp(5.7f), dp(4.2f), dp(3.3f), dp(3.2f), dp(2.1f), dp(1.5f));
    }

    private void drawBigMap(Canvas canvas) {
        if (!bigMapVisible || snapshot == null || bigMapRect.isEmpty()) {
            return;
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(120, 2, 6, 23));
        canvas.drawRect(0f, 0f, getWidth(), getHeight(), paint);

        drawMapWindow(canvas, bigMapRect, dp(12f), dp(2.4f), dp(2.8f), dp(4.8f), dp(9.5f), dp(7.2f), dp(5.8f), dp(5.6f), dp(3.8f), dp(2.8f));

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(220, 30, 41, 59));
        canvas.drawRoundRect(bigMapCloseRect, dp(6f), dp(6f), paint);
        paint.setTextSize(sp(16f));
        paint.setColor(Color.WHITE);
        canvas.drawText(getContext().getString(R.string.battle_hud_close),
                bigMapCloseRect.centerX(), bigMapCloseRect.centerY() + dp(5f), paint);
    }

    private void drawMapWindow(Canvas canvas, RectF mapRect, float cornerRadius, float borderWidth,
                               float terrainStroke, float campRadius, float heroOuterRadius,
                               float heroInnerRadius, float monsterRadius, float bruteRadius,
                               float minionRadius, float viewportStroke) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(188, 15, 23, 42));
        canvas.drawRoundRect(mapRect, cornerRadius, cornerRadius, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(70, 34, 197, 94));
        float laneY = mapY(mapRect, GameConfig.LANE_Y);
        canvas.drawRect(mapRect.left + dp(5f), laneY - dp(3f),
                mapRect.right - dp(5f), laneY + dp(3f), paint);

        drawMapTerrain(canvas, mapRect, terrainStroke);
        drawMapCamps(canvas, mapRect, campRadius);

        for (GameEntity entity : drawEntities) {
            drawMapEntity(canvas, mapRect, entity, heroOuterRadius, heroInnerRadius,
                    monsterRadius, bruteRadius, minionRadius);
        }
        drawMapViewport(canvas, mapRect, viewportStroke);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(borderWidth);
        paint.setColor(Color.argb(150, 226, 232, 240));
        canvas.drawRoundRect(mapRect, cornerRadius, cornerRadius, paint);
    }

    private void drawMapTerrain(Canvas canvas, RectF mapRect, float strokeWidth) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(strokeWidth);
        paint.setColor(Color.argb(150, 56, 189, 248));
        canvas.drawLine(mapX(mapRect, GameTerrain.RIVER_X1), mapY(mapRect, GameTerrain.RIVER_Y1),
                mapX(mapRect, GameTerrain.RIVER_X2), mapY(mapRect, GameTerrain.RIVER_Y2), paint);

        paint.setStyle(Paint.Style.FILL);
        for (GameTerrain.EllipseRegion brush : GameTerrain.BRUSHES) {
            paint.setColor(Color.argb(150, 34, 197, 94));
            terrainRect.set(mapX(mapRect, brush.cx - brush.rx), mapY(mapRect, brush.cy - brush.ry),
                    mapX(mapRect, brush.cx + brush.rx), mapY(mapRect, brush.cy + brush.ry));
            canvas.drawOval(terrainRect, paint);
        }
        for (GameTerrain.RectRegion wall : GameTerrain.WALLS) {
            paint.setColor(Color.argb(180, 168, 162, 158));
            canvas.drawRect(mapX(mapRect, wall.left), mapY(mapRect, wall.top),
                    mapX(mapRect, wall.right), mapY(mapRect, wall.bottom), paint);
        }
        for (GameTerrain.EllipseRegion pond : GameTerrain.PONDS) {
            paint.setColor(Color.argb(170, 56, 189, 248));
            terrainRect.set(mapX(mapRect, pond.cx - pond.rx), mapY(mapRect, pond.cy - pond.ry),
                    mapX(mapRect, pond.cx + pond.rx), mapY(mapRect, pond.cy + pond.ry));
            canvas.drawOval(terrainRect, paint);
        }
    }

    private void drawMapCamps(Canvas canvas, RectF mapRect, float radius) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(120, 245, 158, 11));
        for (MonsterCamp camp : MonsterCamp.ALL) {
            canvas.drawCircle(mapX(mapRect, camp.x), mapY(mapRect, camp.y), radius, paint);
        }
    }

    private void drawMapEntity(Canvas canvas, RectF mapRect, GameEntity entity,
                               float heroOuterRadius, float heroInnerRadius, float monsterRadius,
                               float bruteRadius, float minionRadius) {
        if (!entity.alive && entity.kind != UnitKind.HERO && entity.kind != UnitKind.TOWER) {
            return;
        }
        float x = mapX(mapRect, entity.x);
        float y = mapY(mapRect, entity.y);
        int color = TeamStyle.color(entity.team);
        if (!entity.alive) {
            color = Color.argb(110, Color.red(color), Color.green(color), Color.blue(color));
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        if (entity.kind == UnitKind.TOWER) {
            float size = heroInnerRadius;
            canvas.drawRect(x - size, y - size, x + size, y + size, paint);
            return;
        }
        if (entity.kind == UnitKind.HERO) {
            paint.setColor(color);
            canvas.drawCircle(x, y, heroOuterRadius, paint);
            paint.setColor(Color.rgb(255, 224, 196));
            canvas.drawCircle(x, y - heroInnerRadius * 0.45f, heroInnerRadius * 0.55f, paint);
            paint.setColor(color);
            canvas.drawRect(x - heroInnerRadius * 0.35f, y - heroInnerRadius * 0.1f,
                    x + heroInnerRadius * 0.35f, y + heroInnerRadius * 0.95f, paint);
            return;
        }
        if (entity.kind == UnitKind.MONSTER) {
            paint.setColor(Color.rgb(245, 158, 11));
            canvas.drawCircle(x, y, monsterRadius, paint);
            return;
        }
        float radius = entity.kind == UnitKind.BRUTE ? bruteRadius : minionRadius;
        canvas.drawCircle(x, y, radius, paint);
    }

    private void drawMapViewport(Canvas canvas, RectF mapRect, float strokeWidth) {
        float viewportW = getWidth() / Math.max(0.01f, scale);
        float viewportH = getHeight() / Math.max(0.01f, scale);
        miniViewportRect.set(
                mapX(mapRect, cameraX - viewportW / 2f),
                mapY(mapRect, cameraY - viewportH / 2f),
                mapX(mapRect, cameraX + viewportW / 2f),
                mapY(mapRect, cameraY + viewportH / 2f));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(strokeWidth);
        paint.setColor(Color.argb(220, 248, 250, 252));
        canvas.drawRect(miniViewportRect, paint);
    }

    private float mapX(RectF mapRect, float worldX) {
        return mapRect.left + worldX / GameConfig.WORLD_WIDTH * mapRect.width();
    }

    private float mapY(RectF mapRect, float worldY) {
        return mapRect.top + worldY / GameConfig.WORLD_HEIGHT * mapRect.height();
    }

    private float dp(float value) {
        return DimenUtils.dpF(getContext(), value);
    }

    private float sp(float value) {
        return DimenUtils.spF(getContext(), value);
    }

    private float worldDp(float value) {
        return dp(value) / Math.max(0.01f, scale);
    }

    private float worldSp(float value) {
        return sp(value) / Math.max(0.01f, scale);
    }

}
