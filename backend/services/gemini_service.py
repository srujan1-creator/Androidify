"""Gemini Vision service - face validation and appearance captioning."""

from __future__ import annotations

import base64
import json
import logging
import re
from typing import Any

from google import genai
from google.genai import types

from config import settings
from models.schemas import ImageValidationResponse
from prompts.bot_prompts import CAPTION_PROMPT, VALIDATION_PROMPT

logger = logging.getLogger(__name__)

# ---------------------------------------------------------------------------
# Lazy-initialised client (created on first call so startup isn't blocked
# when the API key hasn't been set yet).
# ---------------------------------------------------------------------------
_client: genai.Client | None = None


def _get_client() -> genai.Client:
    global _client
    if _client is None:
        if not settings.GEMINI_API_KEY:
            raise RuntimeError(
                "GEMINI_API_KEY is not configured. "
                "Set it in the .env file or as an environment variable."
            )
        _client = genai.Client(api_key=settings.GEMINI_API_KEY)
        logger.info("Google GenAI client initialised.")
    return _client


# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

def _strip_base64_prefix(image_base64: str) -> str:
    """Remove an optional ``data:image/...;base64,`` prefix."""
    if "," in image_base64[:80]:
        return image_base64.split(",", 1)[1]
    return image_base64


def _decode_image_bytes(image_base64: str) -> bytes:
    """Decode base64 string to raw bytes, stripping any data-URI prefix."""
    clean = _strip_base64_prefix(image_base64)
    try:
        return base64.b64decode(clean)
    except Exception as exc:
        raise ValueError(f"Invalid base64 image data: {exc}") from exc


def _detect_mime_type(image_bytes: bytes) -> str:
    """Sniff the MIME type from magic bytes."""
    if image_bytes[:8].startswith(b"\x89PNG"):
        return "image/png"
    if image_bytes[:3] == b"\xff\xd8\xff":
        return "image/jpeg"
    if image_bytes[:4] == b"RIFF" and image_bytes[8:12] == b"WEBP":
        return "image/webp"
    # Default to JPEG - Gemini handles most formats gracefully.
    return "image/jpeg"


def _parse_json_response(text: str) -> dict[str, Any]:
    """Best-effort extraction of a JSON object from model output."""
    # Strip possible markdown fences
    cleaned = re.sub(r"```(?:json)?", "", text).strip().strip("`")
    try:
        return json.loads(cleaned)
    except json.JSONDecodeError:
        # Try to find the first { ... } block
        match = re.search(r"\{.*\}", cleaned, re.DOTALL)
        if match:
            return json.loads(match.group())
        raise ValueError(f"Could not parse JSON from model response: {text[:200]}")


# ---------------------------------------------------------------------------
# Public API
# ---------------------------------------------------------------------------

async def validate_image(image_base64: str) -> ImageValidationResponse:
    """Use Gemini 2.0 Flash to decide if the image has a clear human face.

    Returns an ``ImageValidationResponse`` regardless of whether validation
    passes or fails - errors are surfaced as ``is_valid=False``.
    """
    logger.info("Starting image validation via Gemini Vision...")
    
    # Check for missing/placeholder API key to run in mock mode
    if not settings.GEMINI_API_KEY or settings.GEMINI_API_KEY == "your_api_key_here":
        logger.warning("Mock Mode: GEMINI_API_KEY not configured. Mocking face validation success.")
        return ImageValidationResponse(
            is_valid=True,
            message="Mock: Face validation passed (API key not configured).",
            confidence=0.95,
        )

    try:
        image_bytes = _decode_image_bytes(image_base64)
        mime = _detect_mime_type(image_bytes)

        client = _get_client()
        response = client.models.generate_content(
            model=settings.GEMINI_MODEL,
            contents=[
                types.Content(
                    parts=[
                        types.Part.from_text(text=VALIDATION_PROMPT),
                        types.Part.from_bytes(data=image_bytes, mime_type=mime),
                    ]
                )
            ],
        )

        raw_text = response.text.strip()
        logger.debug("Validation raw response: %s", raw_text)

        data = _parse_json_response(raw_text)

        return ImageValidationResponse(
            is_valid=bool(data.get("is_valid", False)),
            message=str(data.get("message", "No details provided.")),
            confidence=float(data.get("confidence", 0.0)),
        )

    except ValueError as exc:
        logger.warning("Image validation value error: %s", exc)
        return ImageValidationResponse(
            is_valid=False,
            message=f"Validation failed: {exc}",
            confidence=0.0,
        )
    except Exception as exc:
        logger.exception("Unexpected error during image validation")
        return ImageValidationResponse(
            is_valid=False,
            message=f"An error occurred during validation: {exc}",
            confidence=0.0,
        )


async def caption_image(image_base64: str) -> str:
    """Use Gemini 2.0 Flash to describe the person's appearance.

    Returns a plain-text description string.  Raises on error.
    """
    logger.info("Generating appearance caption via Gemini Vision...")
    
    # Check for missing/placeholder API key to run in mock mode
    if not settings.GEMINI_API_KEY or settings.GEMINI_API_KEY == "your_api_key_here":
        logger.warning("Mock Mode: GEMINI_API_KEY not configured. Mocking appearance caption.")
        return "A person with neat hair, wearing a stylish outfit with a cheerful look."

    try:
        image_bytes = _decode_image_bytes(image_base64)
        mime = _detect_mime_type(image_bytes)

        client = _get_client()
        response = client.models.generate_content(
            model=settings.GEMINI_MODEL,
            contents=[
                types.Content(
                    parts=[
                        types.Part.from_text(text=CAPTION_PROMPT),
                        types.Part.from_bytes(data=image_bytes, mime_type=mime),
                    ]
                )
            ],
        )

        caption = response.text.strip()
        logger.info("Caption generated (%d chars).", len(caption))
        return caption

    except Exception as exc:
        logger.exception("Error generating caption")
        raise RuntimeError(f"Caption generation failed: {exc}") from exc
