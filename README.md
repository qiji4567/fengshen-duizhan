# 封神峡谷 · duizhan

单机 Android 神话 MOBA 对战 Demo。Java + MVP + ViewBinding + 自定义 Canvas 战场 + SQLite 本地战绩/录像。

## 功能概览

| 模块 | 说明 |
|------|------|
| 英雄选择 | 100+ 中国神话/封神/西游英雄，按职业筛选 |
| 战前独白 | 动态生成背景故事，TTS 逐句朗读 |
| 横屏战斗 | 虚拟摇杆、技能扇形面板、商店、天赋 |
| 兵线/野怪 | 自动刷兵、野怪争夺、防御塔推进 |
| 语音 | 欢迎语、击杀播报、英雄台词（WAV + TTS 兜底） |
| 战绩 | 本地 SQLite 永久保存，不设条数上限 |
| 录像回放 | 自动录制，gzip 压缩；超大录像存文件 |
| 战绩分析 | 胜率、KDA、英雄统计 |

## 环境要求

- Android Studio Ladybug 或更高
- JDK 17
- Android SDK 36（`compileSdk = 36`，`minSdk = 23`）

## 构建与安装

```bash
./gradlew :app:assembleDebug
```

APK 输出路径：

```text
app/build/outputs/apk/debug/app-debug.apk
```

安装到已连接设备：

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## 项目结构

```text
app/src/main/java/com/example/duizhan/
├── ui/              Activity、ViewBinding 页面
├── mvp/             MVP Contract / Presenter
├── model/           业务模型，封装引擎与仓库
├── game/            战斗引擎、实体、AI、技能
├── view/            BattleView 等自定义绘制
├── data/            SQLite、战绩仓库、录像存储
└── ui/audio/        语音 WAV、TTS、音效
```

## 数据存储

- **战绩表** `battle_record`（`duizhan.db`）：胜负、英雄、KDA、出装、时长等
- **录像**：
  - 小于约 900KB（gzip 后）→ 内联存数据库（`gz:…`）
  - 更大 → 存 `files/replays/{id}.replay.gz`，数据库记 `file:{id}`

## 语音说明

- 预置 WAV：`app/src/main/res/raw/`（欢迎、击杀、英雄台词）
- 独白/动态台词：依赖系统 **中文 TTS 语音包**
- 路径：系统设置 → 语言和输入法 → 文字转语音 → 安装中文语音

重新生成 WAV（仅 macOS，需 `say` + `afconvert`）：

```bash
python3 scripts/generate_battle_voices.py
```

## 常见问题

**历史记录没保存？**  
打完一局看结算弹窗是否有「查看战绩」。若提示保存失败，请重装最新 APK；旧版本数据库迁移可能异常。

**录像无法回放？**  
需打完一整局（摧毁高地塔）才会自动保存录像；也可战斗中点「保存录像」手动存。

**只有「欢迎」两个字？**  
请调高 **媒体音量**；独白需安装中文 TTS。

## 技术栈

- Java 17、Android ViewBinding
- MVP 分层
- SQLite + WAL
- `MediaPlayer` + `TextToSpeech` 双通道语音
- 自研 2D 战场渲染（Canvas）

## 许可证

仅供学习与交流使用。
