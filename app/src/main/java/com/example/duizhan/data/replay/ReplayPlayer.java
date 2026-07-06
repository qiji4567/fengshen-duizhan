package com.example.duizhan.data.replay;

import com.example.duizhan.game.GameEntity;
import com.example.duizhan.game.EntityFactory;
import com.example.duizhan.game.GameConfig;
import com.example.duizhan.game.GameSnapshot;
import com.example.duizhan.game.HeroType;
import com.example.duizhan.game.ItemType;
import com.example.duizhan.game.MinionType;
import com.example.duizhan.game.MonsterCamp;
import com.example.duizhan.game.Team;
import com.example.duizhan.game.TowerTier;
import com.example.duizhan.game.UnitKind;

public final class ReplayPlayer {
    private static final int LEGACY_MINION_WAVES = 3;

    private final ReplayData data;
    private int frameIndex;

    public ReplayPlayer(ReplayData data) {
        this.data = data == null ? new ReplayData() : data;
    }

    public int frameCount() {
        return data.frames.size();
    }

    public int currentIndex() {
        return frameIndex;
    }

    public void seek(int index) {
        if (data.frames.isEmpty()) {
            frameIndex = 0;
            return;
        }
        frameIndex = Math.max(0, Math.min(index, data.frames.size() - 1));
    }

    public ReplayFrame currentFrame() {
        if (data.frames.isEmpty()) {
            return null;
        }
        return data.frames.get(frameIndex);
    }

    public boolean advance() {
        if (frameIndex >= data.frames.size() - 1) {
            return false;
        }
        frameIndex++;
        return true;
    }

    public GameSnapshot buildSnapshot(HeroType blueHero, HeroType redHero, String blueName, String redName) {
        ReplayFrame frame = currentFrame();
        GameSnapshot snapshot = new GameSnapshot();
        if (frame == null) {
            return snapshot;
        }
        snapshot.blueTowerHp = frame.blueTowerHp;
        snapshot.redTowerHp = frame.redTowerHp;
        snapshot.blueKills = frame.blueKills;
        snapshot.redKills = frame.redKills;
        snapshot.blueHeroHp = frame.blueHp;
        snapshot.blueHeroMaxHp = frame.blueMaxHp;

        if (frame.hasFullWorld() && !frame.entities.isEmpty()) {
            for (ReplayEntityState state : frame.entities) {
                snapshot.entities.add(state.toEntity());
            }
            for (ReplayProjectileState state : frame.projectiles) {
                snapshot.projectiles.add(state.toProjectile());
            }
            for (ReplayEffectState state : frame.effects) {
                snapshot.effects.add(state.toEffect());
            }
            fillSnapshotMeta(snapshot, blueHero, redHero, blueName, redName);
            return snapshot;
        }

        snapshot.entities.add(buildLegacyTower(Team.BLUE, TowerTier.HIGHLAND,
                GameConfig.BLUE_HIGHLAND_TOWER_X, GameConfig.LANE_Y, "蓝方高地塔", frame.blueTowerHp));
        snapshot.entities.add(buildLegacyTower(Team.BLUE, TowerTier.MIDDLE,
                GameConfig.BLUE_MIDDLE_TOWER_X, GameConfig.LANE_Y, "蓝方中塔", 1f));
        snapshot.entities.add(buildLegacyTower(Team.BLUE, TowerTier.OUTER,
                GameConfig.BLUE_OUTER_TOWER_X, GameConfig.LANE_Y, "蓝方外塔", 1f));
        snapshot.entities.add(buildLegacyTower(Team.RED, TowerTier.OUTER,
                GameConfig.RED_OUTER_TOWER_X, GameConfig.LANE_Y, "红方外塔", 1f));
        snapshot.entities.add(buildLegacyTower(Team.RED, TowerTier.MIDDLE,
                GameConfig.RED_MIDDLE_TOWER_X, GameConfig.LANE_Y, "红方中塔", 1f));
        snapshot.entities.add(buildLegacyTower(Team.RED, TowerTier.HIGHLAND,
                GameConfig.RED_HIGHLAND_TOWER_X, GameConfig.LANE_Y, "红方高地塔", frame.redTowerHp));
        snapshot.entities.add(buildLegacyHero(Team.BLUE, blueHero, blueName, validX(frame.blueX, Team.BLUE), validY(frame.blueY, Team.BLUE),
                frame.blueHp, frame.blueMaxHp, frame, true));
        snapshot.entities.add(buildLegacyHero(Team.RED, redHero, redName, validX(frame.redX, Team.RED), validY(frame.redY, Team.RED),
                frame.redHp, frame.redMaxHp, frame, false));
        addLegacyMinions(snapshot, frame);
        addLegacyMonsters(snapshot, frame);
        fillSnapshotMeta(snapshot, blueHero, redHero, blueName, redName);
        return snapshot;
    }

    private void fillSnapshotMeta(GameSnapshot snapshot, HeroType blueHero, HeroType redHero,
                                  String blueName, String redName) {
        GameEntity blue = findHero(snapshot, Team.BLUE);
        GameEntity red = findHero(snapshot, Team.RED);
        if (blue != null) {
            if (blue.heroType == null) {
                blue.heroType = blueHero;
            }
            if (blue.name == null || blue.name.length() == 0) {
                blue.name = blueName;
            }
            snapshot.blueHeroHp = blue.hp;
            snapshot.blueHeroMaxHp = blue.maxHp;
            snapshot.blueWeaponName = itemLabel(blue.weapon);
            snapshot.blueArmorName = itemLabel(blue.armor);
            snapshot.blueBootsName = itemLabel(blue.boots);
            snapshot.blueHatName = itemLabel(blue.hat);
            snapshot.blueRelicName = itemLabel(blue.relic);
        }
        if (red != null) {
            if (red.heroType == null) {
                red.heroType = redHero;
            }
            if (red.name == null || red.name.length() == 0) {
                red.name = redName;
            }
        }
    }

    private GameEntity findHero(GameSnapshot snapshot, Team team) {
        for (GameEntity entity : snapshot.entities) {
            if (entity.team == team && entity.kind == UnitKind.HERO) {
                return entity;
            }
        }
        return null;
    }

    private String itemLabel(ItemType item) {
        return item == null ? null : item.label;
    }

    private GameEntity buildLegacyTower(Team team, TowerTier tier, float x, float y, String name, float hp) {
        GameEntity tower = EntityFactory.tower(team, tier, x, y, name);
        if (hp > 1f) {
            tower.hp = Math.min(tower.maxHp, hp);
            tower.alive = tower.hp > 0f;
        }
        return tower;
    }

    private GameEntity buildLegacyHero(Team team, HeroType heroType, String name, float x, float y,
                                       float hp, float maxHp, ReplayFrame frame, boolean blueSide) {
        GameEntity entity = new GameEntity(team, UnitKind.HERO, x, y);
        entity.name = name;
        entity.heroType = heroType;
        entity.maxHp = maxHp > 1f ? maxHp : 1800f;
        entity.hp = hp > 0f ? hp : entity.maxHp;
        entity.alive = entity.hp > 0f;
        entity.radius = 28f;
        if (heroType != null) {
            entity.archetype = heroType.archetype();
        }
        entity.facingRad = legacyFacing(frame, blueSide);
        return entity;
    }

    private float validX(float x, Team team) {
        if (x > GameConfig.MIN_X && x < GameConfig.MAX_X) {
            return x;
        }
        return GameConfig.fountainX(team);
    }

    private float validY(float y, Team team) {
        if (y > GameConfig.MIN_Y && y < GameConfig.MAX_Y) {
            return y;
        }
        return GameConfig.fountainY(team);
    }

    private void addLegacyMinions(GameSnapshot snapshot, ReplayFrame frame) {
        float seconds = frame.timeMs / 1000f;
        for (int wave = 0; wave < LEGACY_MINION_WAVES; wave++) {
            float waveAge = seconds - wave * GameConfig.MINION_WAVE_INTERVAL;
            if (waveAge < 0f || waveAge > 32f) {
                continue;
            }
            addLegacyMinionWave(snapshot, Team.BLUE, waveAge, wave);
            addLegacyMinionWave(snapshot, Team.RED, waveAge, wave);
        }
    }

    private void addLegacyMinionWave(GameSnapshot snapshot, Team team, float waveAge, int waveIndex) {
        float dir = team == Team.BLUE ? 1f : -1f;
        float startX = team == Team.BLUE ? GameConfig.BLUE_BASE_X + 520f : GameConfig.RED_BASE_X - 520f;
        float x = startX + dir * waveAge * 86f;
        if (x < GameConfig.MIN_X || x > GameConfig.MAX_X) {
            return;
        }
        addLegacyMinion(snapshot, team, x, GameConfig.LANE_Y - 72f, MinionType.MELEE,
                waveIndex * 10L + (team == Team.BLUE ? 1L : 101L));
        addLegacyMinion(snapshot, team, x - dir * 92f, GameConfig.LANE_Y + 72f, MinionType.RANGED,
                waveIndex * 10L + (team == Team.BLUE ? 2L : 102L));
        if (waveIndex % 2 == 0) {
            addLegacyMinion(snapshot, team, x - dir * 164f, GameConfig.LANE_Y, MinionType.BRUTE,
                    waveIndex * 10L + (team == Team.BLUE ? 3L : 103L));
        }
    }

    private void addLegacyMinion(GameSnapshot snapshot, Team team, float x, float y, MinionType type, long id) {
        GameEntity minion = EntityFactory.minion(team, x, y, type, team == Team.BLUE ? "蓝方兵" : "红方兵");
        minion.assignReplayId(900000L + id);
        snapshot.entities.add(minion);
    }

    private void addLegacyMonsters(GameSnapshot snapshot, ReplayFrame frame) {
        int phase = Math.max(0, (int) (frame.timeMs / 1000L / GameConfig.MONSTER_RESPAWN_INTERVAL));
        for (int i = 0; i < MonsterCamp.ALL.length; i++) {
            MonsterCamp camp = MonsterCamp.ALL[i];
            if ((i + phase) % 3 == 1) {
                continue;
            }
            GameEntity monster = EntityFactory.monster(camp.shortLabel, camp.x, camp.y);
            monster.assignReplayId(950000L + i);
            snapshot.entities.add(monster);
        }
    }

    private float legacyFacing(ReplayFrame frame, boolean blueSide) {
        int index = frameIndex;
        if (index <= 0 || data.frames.isEmpty()) {
            return blueSide ? 0f : (float) Math.PI;
        }
        ReplayFrame prev = data.frames.get(index - 1);
        float dx = blueSide ? frame.blueX - prev.blueX : frame.redX - prev.redX;
        float dy = blueSide ? frame.blueY - prev.blueY : frame.redY - prev.redY;
        if (Math.abs(dx) < 0.01f && Math.abs(dy) < 0.01f) {
            return blueSide ? 0f : (float) Math.PI;
        }
        return (float) Math.atan2(dy, dx);
    }
}
