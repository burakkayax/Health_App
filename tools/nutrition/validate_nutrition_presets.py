#!/usr/bin/env python3
"""Validate Health_App nutrition preset JSON.

This script is intentionally dependency-free so it can be run in CI without
installing jsonschema. It validates uniqueness, required fields, non-negative
nutrients, serving math, source metadata and search fields.

Usage:
  python tools/nutrition/validate_nutrition_presets.py app/src/main/assets/nutrition_presets/nutrition_presets_tr_v1.json
"""
from __future__ import annotations

import json
import math
import sys
from pathlib import Path
from typing import Any

REQUIRED_TOP = {"schema_version", "dataset_name", "locale", "unit_basis", "nutrient_units", "foods"}
REQUIRED_FOOD = {
    "id", "slug", "name_tr", "category_tr", "default_serving", "basis", "source",
    "nutrients_per_100g", "nutrients_per_default_serving", "data_quality",
}
REQUIRED_MACROS = {"energy_kcal", "protein_g", "fat_g", "carbs_g"}
VALID_QUALITY = {"high", "medium", "low"}
MAX_FILE_MB = 10
TOLERANCE = 0.02


def fail(errors: list[str], message: str) -> None:
    errors.append(message)


def is_number(value: Any) -> bool:
    return isinstance(value, (int, float)) and not isinstance(value, bool) and math.isfinite(float(value))


def validate(path: Path) -> list[str]:
    errors: list[str] = []
    if not path.exists():
        return [f"File not found: {path}"]
    size_mb = path.stat().st_size / (1024 * 1024)
    if size_mb > MAX_FILE_MB:
        fail(errors, f"File is too large: {size_mb:.2f} MB > {MAX_FILE_MB} MB")
    try:
        data = json.loads(path.read_text(encoding="utf-8"))
    except json.JSONDecodeError as exc:
        return [f"Invalid JSON: {exc}"]

    missing_top = REQUIRED_TOP - set(data)
    if missing_top:
        fail(errors, f"Missing top-level fields: {sorted(missing_top)}")
    foods = data.get("foods")
    if not isinstance(foods, list) or not foods:
        fail(errors, "foods must be a non-empty array")
        return errors

    seen_ids: set[str] = set()
    seen_slugs: set[str] = set()
    seen_names: set[str] = set()
    units = data.get("nutrient_units", {})
    unit_keys = set(units.keys())

    for index, food in enumerate(foods):
        prefix = f"foods[{index}]"
        if not isinstance(food, dict):
            fail(errors, f"{prefix}: must be an object")
            continue
        missing = REQUIRED_FOOD - set(food)
        if missing:
            fail(errors, f"{prefix}: missing fields {sorted(missing)}")
            continue
        food_id = food.get("id")
        slug = food.get("slug")
        name = food.get("name_tr")
        if not isinstance(food_id, str) or not food_id.startswith("food_"):
            fail(errors, f"{prefix}.id: invalid id {food_id!r}")
        if food_id in seen_ids:
            fail(errors, f"{prefix}.id: duplicate {food_id}")
        seen_ids.add(food_id)
        if not isinstance(slug, str) or not slug:
            fail(errors, f"{prefix}.slug: required")
        if slug in seen_slugs:
            fail(errors, f"{prefix}.slug: duplicate {slug}")
        seen_slugs.add(slug)
        if not isinstance(name, str) or len(name.strip()) < 2:
            fail(errors, f"{prefix}.name_tr: required")
        key_name = str(name).strip().lower()
        if key_name in seen_names:
            fail(errors, f"{prefix}.name_tr: duplicate visible name {name}")
        seen_names.add(key_name)

        if food.get("basis") not in {"per_100g_edible_portion", "per_100g_edible_portion_or_100ml_for_alcoholic_beverages"}:
            fail(errors, f"{prefix}.basis: unsupported basis {food.get('basis')!r}")

        serving = food.get("default_serving", {})
        grams = serving.get("grams")
        if not is_number(grams) or float(grams) <= 0:
            fail(errors, f"{prefix}.default_serving.grams: must be > 0")
            grams = None

        source = food.get("source", {})
        for field in ["dataset", "source_id"]:
            if not source.get(field):
                fail(errors, f"{prefix}.source.{field}: required")

        quality = food.get("data_quality", {})
        if quality.get("level") not in VALID_QUALITY:
            fail(errors, f"{prefix}.data_quality.level: must be one of {sorted(VALID_QUALITY)}")
        if not quality.get("notes_tr"):
            fail(errors, f"{prefix}.data_quality.notes_tr: required")

        nutrients = food.get("nutrients_per_100g", {})
        missing_macros = REQUIRED_MACROS - set(nutrients)
        if missing_macros:
            fail(errors, f"{prefix}.nutrients_per_100g: missing macros {sorted(missing_macros)}")
        for key, value in nutrients.items():
            if key not in unit_keys:
                fail(errors, f"{prefix}.nutrients_per_100g.{key}: unknown nutrient key")
            if value is None:
                continue
            if not is_number(value):
                fail(errors, f"{prefix}.nutrients_per_100g.{key}: must be number or null")
            elif float(value) < 0:
                fail(errors, f"{prefix}.nutrients_per_100g.{key}: negative value {value}")
        for key in REQUIRED_MACROS:
            value = nutrients.get(key)
            if not is_number(value):
                fail(errors, f"{prefix}.nutrients_per_100g.{key}: macro must be numeric")

        per_serving = food.get("nutrients_per_default_serving", {})
        if grams is not None:
            for key, value in nutrients.items():
                expected = None if value is None else round(float(value) * float(grams) / 100.0, 3)
                actual = per_serving.get(key)
                if expected is None:
                    if actual is not None:
                        fail(errors, f"{prefix}.nutrients_per_default_serving.{key}: expected null")
                else:
                    if not is_number(actual) or abs(float(actual) - expected) > TOLERANCE:
                        fail(errors, f"{prefix}.nutrients_per_default_serving.{key}: expected {expected}, got {actual}")

        common = food.get("common_servings", [])
        if common is not None:
            if not isinstance(common, list):
                fail(errors, f"{prefix}.common_servings: must be array")
            else:
                for s_idx, item in enumerate(common):
                    g = item.get("grams") if isinstance(item, dict) else None
                    if not is_number(g) or float(g) <= 0:
                        fail(errors, f"{prefix}.common_servings[{s_idx}].grams: must be > 0")
    return errors


def main() -> int:
    path = Path(sys.argv[1]) if len(sys.argv) > 1 else Path("nutrition_presets_tr_v1.cleaned.json")
    errors = validate(path)
    if errors:
        print(f"FAILED: {len(errors)} validation error(s)")
        for error in errors[:200]:
            print(f"- {error}")
        if len(errors) > 200:
            print(f"... {len(errors) - 200} more")
        return 1
    print(f"OK: {path} is valid")
    return 0

if __name__ == "__main__":
    raise SystemExit(main())
