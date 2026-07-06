#!/usr/bin/env python3
"""Generate battle voice WAV files under app/src/main/res/raw."""
import re
import subprocess
import sys
import wave
import audioop
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
RAW_DIR = ROOT / "app/src/main/res/raw"
HERO_TYPE_FILE = ROOT / "app/src/main/java/com/example/duizhan/game/HeroType.java"
PICK_LINE_FILE = ROOT / "app/src/main/java/com/example/duizhan/ui/audio/HeroVoiceLineProvider.java"
VOICE = "Tingting"

PHRASES = {
    "voice_defeat": ("击败", "150"),
    "voice_slain": ("你已被击杀", "145"),
    "voice_kill_single": ("单杀！", "165"),
    "voice_kill_double": ("双杀！", "170"),
    "voice_kill_triple": ("三杀！", "175"),
    "voice_kill_quadra": ("四杀！", "180"),
    "voice_kill_penta": ("五杀！", "185"),
    "voice_kill_godlike": ("接近神了！", "165"),
    "voice_kill_legendary": ("超神了！", "170"),
    "voice_kill_monster": ("击败野怪", "150"),
    "battle_welcome": ("欢迎进入封神峡谷，战斗开始", "150"),
}

HERO_RATE = "155"
PICK_RATE = "148"
HERO_PATTERN = re.compile(
    r"^\s*([A-Z_]+)\(\"([^\"]+)\", \"[^\"]+\", \"[^\"]+\", \"([^\"]+)\""
)
PICK_CASE_PATTERN = re.compile(
    r"case\s+([A-Z_]+):\s*\n(?:\s*case[^\n]*\n)*\s*return\s+\"([^\"]+)\";",
    re.MULTILINE,
)


def synth(name: str, text: str, rate: str = "150") -> None:
    RAW_DIR.mkdir(parents=True, exist_ok=True)
    wav = RAW_DIR / f"{name}.wav"
    if wav.exists() and wav_has_audio(wav):
        return
    wav.unlink(missing_ok=True)
    aiff = RAW_DIR / f"{name}.aiff"
    subprocess.run(
        ["say", "-v", VOICE, "-r", rate, "-o", str(aiff), text],
        check=True,
    )
    subprocess.run(
        ["afconvert", str(aiff), str(wav), "-f", "WAVE", "-d", "LEI16@22050"],
        check=True,
    )
    normalize_wav(wav)
    aiff.unlink(missing_ok=True)
    print(f"generated {wav.name}")


def wav_has_audio(path: Path) -> bool:
    try:
        with wave.open(str(path), "rb") as wav:
            return wav.getnframes() > 0
    except wave.Error:
        return False


def normalize_wav(path: Path, target_peak: int = 26000) -> None:
    try:
        with wave.open(str(path), "rb") as wav:
            params = wav.getparams()
            data = wav.readframes(wav.getnframes())
        if not data:
            return
        peak = audioop.max(data, params.sampwidth)
        if peak <= 0:
            return
        factor = min(4.0, target_peak / peak)
        if factor <= 1.05:
            return
        boosted = audioop.mul(data, params.sampwidth, factor)
        with wave.open(str(path), "wb") as wav:
            wav.setparams(params)
            wav.writeframes(boosted)
    except wave.Error:
        return


def load_pick_lines() -> dict[str, str]:
    source = PICK_LINE_FILE.read_text(encoding="utf-8")
    start = source.find("static String pickLine")
    end = source.find("static String moveLine", start)
    block = source[start:end]
    custom = {}
    for hero_key, line in PICK_CASE_PATTERN.findall(block):
        custom[hero_key] = line
    return custom


def load_heroes() -> list[tuple[str, str, str]]:
    heroes = []
    for line in HERO_TYPE_FILE.read_text(encoding="utf-8").splitlines():
        match = HERO_PATTERN.match(line)
        if match:
            heroes.append(match.groups())
    return heroes


def pick_line(hero_key: str, label: str, skill: str, custom: dict[str, str]) -> str:
    if hero_key in custom:
        return custom[hero_key]
    skill_name = skill.split("：", 1)[0]
    return f"{label}参战，{skill_name}。"


def main() -> int:
    custom_pick_lines = load_pick_lines()
    heroes = load_heroes()
    for name, value in PHRASES.items():
        text, rate = value
        synth(name, text, rate)
    for hero_key, label, skill in heroes:
        key_lower = hero_key.lower()
        synth(f"voice_hero_{key_lower}", label, HERO_RATE)
        synth(
            f"voice_pick_{key_lower}",
            pick_line(hero_key, label, skill, custom_pick_lines),
            PICK_RATE,
        )
    print(f"heroes: {len(heroes)}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
