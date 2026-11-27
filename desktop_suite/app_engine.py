"""Bob's Bunny: Carrot City — Desktop Backend & Persistent Save Engine."""
from __future__ import annotations

import json
from pathlib import Path
from flask import Flask, jsonify, request, send_from_directory
import platform_exclusive

DEFAULT_SAVE = {
    "carrots": 0,
    "golden_shards": 0,
    "high_score": 0,
    "unlocked_stage": 1,
    "stage_stars": {},
    "owned_swag": ["aviator_goggles", "carrot_hoodie", "emerald_kicks"],
    "equipped": {
        "headgear": "aviator_goggles",
        "hoodie": "carrot_hoodie",
        "kicks": "emerald_kicks",
        "trail": "amber_spark",
    },
    "fox_hunt_wins": 0,
    "badges": ["Carrot City Rookie"],
}

SWAG_CATALOG = [
    {"id": "aviator_goggles", "slot": "headgear", "name": "Carrot City Aviator Goggles", "cost": 0, "color": "#FF9A2E"},
    {"id": "swag_shades", "slot": "headgear", "name": "24K Swag Shades", "cost": 80, "color": "#FACC15"},
    {"id": "crown_ears", "slot": "headgear", "name": "Golden World Crown", "cost": 250, "color": "#F59E0B"},
    {"id": "carrot_hoodie", "slot": "hoodie", "name": "Twilight Street Vest", "cost": 0, "color": "#2EE58D"},
    {"id": "cyber_jacket", "slot": "hoodie", "name": "Toky-Oat Neon Jacket", "cost": 110, "color": "#38BDF8"},
    {"id": "gold_tux", "slot": "hoodie", "name": "24K Golden Drip", "cost": 300, "color": "#FBBF24"},
    {"id": "emerald_kicks", "slot": "kicks", "name": "Emerald Bounce Kicks", "cost": 0, "color": "#2EE58D"},
    {"id": "spring_kicks", "slot": "kicks", "name": "Spring-Loaded High-Tops", "cost": 95, "color": "#FF6B35"},
    {"id": "amber_spark", "slot": "trail", "name": "Carrot Amber Trail", "cost": 0, "color": "#FF9A2E"},
    {"id": "rainbow_rift", "slot": "trail", "name": "Golden Rift Rainbow Trail", "cost": 160, "color": "#A855F7"},
]


def _save_path() -> Path:
    return platform_exclusive.local_data_dir("bobs-bunny-carrot-city") / "save_profile.json"


def load_save() -> dict:
    p = _save_path()
    if p.exists():
        try:
            data = json.loads(p.read_text(encoding="utf-8"))
            merged = dict(DEFAULT_SAVE)
            merged.update(data)
            return merged
        except Exception:
            pass
    return dict(DEFAULT_SAVE)


def write_save(data: dict) -> dict:
    p = _save_path()
    p.write_text(json.dumps(data, indent=2), encoding="utf-8")
    return data


def start_server(port: int, bundle_root: Path, meta: dict):
    app = Flask(__name__)
    ui_dir = bundle_root / "ui"
    assets_dir = bundle_root / "assets"

    @app.route("/")
    def index():
        return send_from_directory(ui_dir, "index.html")

    @app.route("/assets/<path:filename>")
    def serve_assets(filename):
        return send_from_directory(assets_dir, filename)

    @app.route("/api/health")
    def health():
        return jsonify({"ok": True, "app": meta["title"], "version": meta["version"]})

    @app.route("/api/meta")
    def get_meta():
        return jsonify(meta)

    @app.route("/api/platform")
    def get_platform():
        return jsonify(platform_exclusive.get_system_overview())

    @app.route("/api/save", methods=["GET", "POST"])
    def save_route():
        if request.method == "GET":
            return jsonify({"save": load_save(), "catalog": SWAG_CATALOG})
        body = request.get_json(force=True, silent=True) or {}
        cur = load_save()
        cur.update(body)
        write_save(cur)
        return jsonify({"ok": True, "save": cur})

    @app.route("/api/swag/buy", methods=["POST"])
    def buy_swag():
        body = request.get_json(force=True, silent=True) or {}
        item_id = body.get("id")
        item = next((x for x in SWAG_CATALOG if x["id"] == item_id), None)
        if not item:
            return jsonify({"ok": False, "error": "Unknown item"}), 404
        cur = load_save()
        if item_id not in cur["owned_swag"]:
            if cur["carrots"] < item["cost"]:
                return jsonify({"ok": False, "error": "Not enough carrots"}), 400
            cur["carrots"] -= item["cost"]
            cur["owned_swag"].append(item_id)
        cur["equipped"][item["slot"]] = item_id
        write_save(cur)
        return jsonify({"ok": True, "save": cur})

    app.run(host="127.0.0.1", port=port, debug=False, use_reloader=False)
