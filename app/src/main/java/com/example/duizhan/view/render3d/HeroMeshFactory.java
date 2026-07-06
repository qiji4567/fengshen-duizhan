package com.example.duizhan.view.render3d;

import android.graphics.Color;

import com.example.duizhan.game.HeroVisualProfile;

import java.util.List;

final class HeroMeshFactory {
    private final MeshBuilder builder = new MeshBuilder();
    private final Vector3 pivot = new Vector3();

    List<MeshTriangle> build(HeroVisualProfile profile, MeshPose pose) {
        builder.clear();
        float s = profile.scale;
        int robe = profile.robeColor;
        int trim = profile.trimColor;
        int skin = profile.skinColor;
        int hair = profile.hairColor;
        int dark = MeshBuilder.shade(robe, 0.55f);

        addShadow(s);
        addLeg(-0.14f * s, pose.leftLeg, robe, dark, skin, s);
        addLeg(0.14f * s, pose.rightLeg, robe, dark, skin, s);
        addTorso(profile, robe, trim, dark, s, pose.bodyLean);
        addHead(profile, skin, hair, trim, s);
        addSignature(profile, trim, s);
        addArm(profile, -0.24f * s, pose.leftArm, robe, dark, skin, s, false);
        addArm(profile, 0.24f * s, pose.rightArm, robe, dark, skin, s, true);
        addWeapon(profile, pose.rightArm, trim, s);
        return builder.build();
    }

    private void addShadow(float s) {
        builder.addBox(0f, 0.02f, 0f, 0.42f * s, 0.02f, 0.28f * s, Color.argb(70, 0, 0, 0));
    }

    private void addTorso(HeroVisualProfile profile, int robe, int trim, int dark, float s, float lean) {
        float h = torsoHeight(profile) * s;
        float leanZ = lean * 0.06f * s;
        builder.addBox(leanZ * 0.2f, h * 0.55f, leanZ, 0.28f * s, h, 0.18f * s, dark);
        builder.addBox(leanZ * 0.2f, h * 0.55f, leanZ + 0.01f, 0.24f * s, h * 0.92f, 0.14f * s, robe);
        builder.addBox(leanZ * 0.15f, h * 0.42f, h * 0.08f + leanZ, 0.26f * s, 0.06f * s, 0.04f * s, trim);
        if (profile.body == HeroVisualProfile.BodyKind.MONKEY_KING) {
            builder.addBox(0f, h * 0.95f, 0.05f, 0.28f * s, 0.05f * s, 0.05f * s, trim);
        }
    }

    private void addHead(HeroVisualProfile profile, int skin, int hair, int trim, float s) {
        float baseY = torsoHeight(profile) * s + 0.08f * s;
        switch (profile.body) {
            case MONKEY_KING:
                builder.addSphere(0f, baseY + 0.16f * s, 0f, 0.13f * s, skin, 4, 6);
                builder.addBox(0f, baseY + 0.24f * s, 0.04f, 0.2f * s, 0.04f * s, 0.06f * s, hair);
                break;
            case PIG_DEMON:
                builder.addSphere(0f, baseY + 0.14f * s, 0.04f * s, 0.15f * s, Color.rgb(252, 165, 165), 4, 6);
                builder.addBox(0f, baseY + 0.1f * s, 0.12f * s, 0.1f * s, 0.08f * s, 0.08f * s, Color.rgb(190, 80, 80));
                break;
            case BULL_DEMON:
                builder.addSphere(0f, baseY + 0.14f * s, 0f, 0.12f * s, skin, 4, 6);
                builder.addBox(-0.1f * s, baseY + 0.22f * s, 0f, 0.04f * s, 0.1f * s, 0.04f * s, Color.rgb(40, 20, 10));
                builder.addBox(0.1f * s, baseY + 0.22f * s, 0f, 0.04f * s, 0.1f * s, 0.04f * s, Color.rgb(40, 20, 10));
                break;
            case SKELETON:
                builder.addSphere(0f, baseY + 0.13f * s, 0f, 0.11f * s, Color.rgb(248, 250, 252), 4, 6);
                break;
            case GHOST_JUDGE:
                builder.addSphere(0f, baseY + 0.13f * s, 0f, 0.12f * s, Color.rgb(74, 222, 128), 4, 6);
                if (profile.crown) {
                    builder.addBox(0f, baseY + 0.24f * s, 0f, 0.14f * s, 0.04f * s, 0.1f * s, Color.rgb(30, 41, 59));
                }
                break;
            case DRAGON_KING:
                builder.addSphere(0f, baseY + 0.13f * s, 0f, 0.12f * s, skin, 4, 6);
                builder.addBox(-0.12f * s, baseY + 0.2f * s, -0.02f * s, 0.04f * s, 0.08f * s, 0.06f * s, trim);
                builder.addBox(0.12f * s, baseY + 0.2f * s, -0.02f * s, 0.04f * s, 0.08f * s, 0.06f * s, trim);
                break;
            case BUDDHA:
                builder.addSphere(0f, baseY + 0.14f * s, 0f, 0.14f * s, skin, 4, 6);
                builder.addSphere(0f, baseY + 0.2f * s, 0.05f * s, 0.03f * s, trim, 3, 6);
                break;
            case EMPEROR:
                builder.addSphere(0f, baseY + 0.13f * s, 0f, 0.12f * s, skin, 4, 6);
                builder.addBox(0f, baseY + 0.24f * s, 0f, 0.14f * s, 0.05f * s, 0.12f * s, trim);
                break;
            default:
                builder.addSphere(0f, baseY + 0.13f * s, 0f, 0.11f * s, skin, 4, 6);
                builder.addSphere(0f, baseY + 0.2f * s, 0f, 0.12f * s, hair, 4, 7);
                if (profile.thirdEye) {
                    builder.addSphere(0f, baseY + 0.16f * s, 0.1f * s, 0.02f * s, trim, 3, 5);
                }
                if (profile.crown) {
                    builder.addBox(0f, baseY + 0.24f * s, 0f, 0.12f * s, 0.04f * s, 0.1f * s, trim);
                }
                break;
        }
    }

    private void addSignature(HeroVisualProfile profile, int trim, float s) {
        if (profile.wings) {
            builder.addBox(-0.22f * s, 0.42f * s, -0.08f * s, 0.16f * s, 0.02f * s, 0.12f * s,
                    Color.argb(200, 147, 197, 253));
            builder.addBox(0.22f * s, 0.42f * s, -0.08f * s, 0.16f * s, 0.02f * s, 0.12f * s,
                    Color.argb(200, 147, 197, 253));
        }
        if (profile.body == HeroVisualProfile.BodyKind.CHILD_WARRIOR) {
            builder.addSphere(-0.12f * s, 0.08f * s, 0.08f * s, 0.04f * s, trim, 3, 5);
            builder.addSphere(0.12f * s, 0.08f * s, 0.08f * s, 0.04f * s, trim, 3, 5);
        }
    }

    private void addLeg(float offsetX, float swingRad, int robe, int dark, int skin, float s) {
        float hipY = 0.22f * s;
        float kick = (float) Math.sin(swingRad) * 0.05f * s;
        float reach = (float) Math.cos(swingRad) * 0.04f * s;
        builder.addBox(offsetX, hipY + 0.08f * s, kick, 0.07f * s, 0.16f * s, 0.07f * s, MeshBuilder.shade(robe, 0.82f));
        builder.addBox(offsetX + reach, hipY - 0.08f * s, kick + 0.02f * s, 0.06f * s, 0.16f * s, 0.06f * s, dark);
        builder.addBox(offsetX + reach, hipY - 0.2f * s, kick + 0.03f * s, 0.07f * s, 0.05f * s, 0.09f * s, skin);
    }

    private void addArm(HeroVisualProfile profile, float offsetX, float swingRad, int robe, int dark,
                        int skin, float s, boolean weaponSide) {
        float y = torsoHeight(profile) * s * 0.72f;
        float forward = (float) Math.sin(swingRad) * 0.05f * s;
        float lift = (float) Math.cos(swingRad) * 0.03f * s;
        builder.addBox(offsetX, y, forward, 0.05f * s, 0.14f * s, 0.05f * s, MeshBuilder.shade(robe, 0.86f));
        builder.addBox(offsetX + lift * 0.4f, y - 0.14f * s, forward + 0.04f * s,
                0.045f * s, 0.12f * s, 0.045f * s, dark);
        if (weaponSide) {
            builder.addSphere(offsetX + lift * 0.4f, y - 0.2f * s, forward + 0.05f * s, 0.035f * s, skin, 3, 5);
        }
    }

    private void addWeapon(HeroVisualProfile profile, float armSwing, int trim, float s) {
        float y = torsoHeight(profile) * s * 0.55f;
        float handX = 0.28f * s + (float) Math.cos(armSwing) * 0.03f * s;
        float handY = y - 0.2f * s;
        float handZ = 0.06f * s + (float) Math.sin(armSwing) * 0.05f * s;
        pivot.set(handX, handY, handZ);
        int metal = trim;
        int dark = MeshBuilder.shade(metal, 0.55f);
        switch (profile.weapon) {
            case GOLDEN_STAFF:
                builder.addBox(pivot.x, pivot.y - 0.12f * s, pivot.z, 0.04f * s, 0.34f * s, 0.04f * s, metal);
                builder.addSphere(pivot.x, pivot.y + 0.08f * s, pivot.z, 0.05f * s, metal, 3, 5);
                break;
            case RAKE:
                builder.addBox(pivot.x, pivot.y, pivot.z, 0.03f * s, 0.28f * s, 0.03f * s, dark);
                for (int i = -2; i <= 2; i++) {
                    builder.addBox(pivot.x + i * 0.04f * s, pivot.y - 0.14f * s, pivot.z,
                            0.02f * s, 0.06f * s, 0.02f * s, metal);
                }
                break;
            case BOW:
                builder.addBox(pivot.x, pivot.y, pivot.z, 0.02f * s, 0.22f * s, 0.02f * s, dark);
                builder.addBox(pivot.x, pivot.y - 0.08f * s, pivot.z + 0.04f * s, 0.12f * s, 0.02f * s, 0.02f * s, metal);
                break;
            case PAGODA:
                builder.addBox(pivot.x, pivot.y, pivot.z, 0.08f * s, 0.14f * s, 0.08f * s, metal);
                builder.addBox(pivot.x, pivot.y + 0.04f * s, pivot.z, 0.06f * s, 0.04f * s, 0.06f * s, Color.rgb(30, 64, 175));
                break;
            case TRIDENT:
                builder.addBox(pivot.x, pivot.y, pivot.z, 0.025f * s, 0.28f * s, 0.025f * s, dark);
                builder.addBox(pivot.x, pivot.y + 0.12f * s, pivot.z, 0.12f * s, 0.02f * s, 0.02f * s, metal);
                break;
            case AXE:
                builder.addBox(pivot.x, pivot.y, pivot.z, 0.03f * s, 0.24f * s, 0.03f * s, dark);
                builder.addBox(pivot.x + 0.06f * s, pivot.y + 0.08f * s, pivot.z, 0.08f * s, 0.05f * s, 0.03f * s, metal);
                break;
            case SPEAR:
            case FIRE_SPEAR:
                builder.addBox(pivot.x, pivot.y, pivot.z, 0.025f * s, 0.3f * s, 0.025f * s,
                        profile.weapon == HeroVisualProfile.SigWeapon.FIRE_SPEAR
                                ? Color.rgb(251, 146, 60) : dark);
                builder.addBox(pivot.x, pivot.y + 0.14f * s, pivot.z, 0.05f * s, 0.03f * s, 0.05f * s, metal);
                break;
            case LOTUS:
                for (int i = 0; i < 6; i++) {
                    float ang = (float) (i * Math.PI / 3f);
                    builder.addBox(pivot.x + (float) Math.cos(ang) * 0.05f * s,
                            pivot.y - 0.04f * s,
                            pivot.z + (float) Math.sin(ang) * 0.05f * s,
                            0.03f * s, 0.02f * s, 0.05f * s, Color.rgb(244, 114, 182));
                }
                break;
            case GOURD:
                builder.addSphere(pivot.x, pivot.y - 0.04f * s, pivot.z, 0.06f * s, Color.rgb(22, 101, 52), 4, 6);
                builder.addSphere(pivot.x, pivot.y + 0.02f * s, pivot.z, 0.04f * s, Color.rgb(34, 139, 58), 3, 5);
                break;
            case FAN:
                builder.addBox(pivot.x, pivot.y, pivot.z, 0.02f * s, 0.12f * s, 0.02f * s, dark);
                builder.addBox(pivot.x, pivot.y - 0.06f * s, pivot.z + 0.04f * s, 0.14f * s, 0.02f * s, 0.08f * s, Color.rgb(239, 68, 68));
                break;
            default:
                builder.addBox(pivot.x, pivot.y, pivot.z, 0.025f * s, 0.28f * s, 0.025f * s, metal);
                builder.addBox(pivot.x, pivot.y - 0.12f * s, pivot.z, 0.06f * s, 0.03f * s, 0.02f * s, dark);
                break;
        }
    }

    private float torsoHeight(HeroVisualProfile profile) {
        if (profile.body == HeroVisualProfile.BodyKind.GIANT) {
            return 0.52f;
        }
        if (profile.body == HeroVisualProfile.BodyKind.CHILD_WARRIOR) {
            return 0.34f;
        }
        return 0.42f;
    }
}
