"""Imagen service - Android bot avatar generation.

Pipeline:
1. Caption the selfie via Gemini Vision  (gemini_service.caption_image)
2. Build a prompt from the caption + selected vibe
3. Generate the bot image via Imagen 3
4. If Imagen is unavailable, fall back to Gemini's native image generation
5. Return the result as a base64-encoded PNG string
"""

from __future__ import annotations

import base64
import io
import logging

from google import genai
from google.genai import types
from PIL import Image, ImageDraw

from config import settings
from prompts.bot_prompts import build_bot_prompt
from services import gemini_service

logger = logging.getLogger(__name__)

# ---------------------------------------------------------------------------
# Lazy client (re-uses the same credential as the Gemini service)
# ---------------------------------------------------------------------------
_client: genai.Client | None = None


def _get_client() -> genai.Client:
    global _client
    if _client is None:
        if not settings.GEMINI_API_KEY:
            raise RuntimeError("GEMINI_API_KEY is not configured.")
        _client = genai.Client(api_key=settings.GEMINI_API_KEY)
        logger.info("Imagen GenAI client initialised.")
    return _client


# ---------------------------------------------------------------------------
# Internal generation strategies
# ---------------------------------------------------------------------------

async def _generate_via_imagen(prompt: str) -> str | None:
    """Try Imagen 3 ``generate_images`` and return base64 PNG, or *None* on failure."""
    try:
        client = _get_client()
        logger.info("Attempting image generation via Imagen 3...")

        response = client.models.generate_images(
            model=settings.IMAGEN_MODEL,
            prompt=prompt,
            config=types.GenerateImagesConfig(
                number_of_images=1,
                aspect_ratio="1:1",
                safety_filter_level="BLOCK_MEDIUM_AND_ABOVE",
            ),
        )

        if response.generated_images and len(response.generated_images) > 0:
            image = response.generated_images[0].image
            # The SDK returns an Image object; extract raw bytes.
            if hasattr(image, "image_bytes"):
                img_bytes = image.image_bytes
            elif hasattr(image, "data"):
                img_bytes = image.data
            else:
                # Fallback: try to access the bytes directly
                img_bytes = bytes(image)

            encoded = base64.b64encode(img_bytes).decode("utf-8")
            logger.info("Imagen 3 generation succeeded (%d bytes encoded).", len(encoded))
            return encoded

        logger.warning("Imagen returned no images.")
        return None

    except Exception as exc:
        logger.warning("Imagen generation failed, will try fallback: %s", exc)
        return None


async def _generate_via_gemini_fallback(prompt: str) -> str:
    """Fallback: use Gemini's native image generation capabilities."""
    try:
        client = _get_client()
        logger.info("Falling back to Gemini image generation...")

        response = client.models.generate_content(
            model="gemini-2.0-flash",
            contents=prompt,
            config=types.GenerateContentConfig(
                response_modalities=["IMAGE", "TEXT"],
            ),
        )

        # Walk parts looking for inline image data
        if response.candidates:
            for part in response.candidates[0].content.parts:
                if part.inline_data and part.inline_data.data:
                    encoded = base64.b64encode(part.inline_data.data).decode("utf-8")
                    logger.info(
                        "Gemini fallback image generation succeeded (%d bytes encoded).",
                        len(encoded),
                    )
                    return encoded

        raise RuntimeError("Gemini did not return any image data in the response.")

    except Exception as exc:
        logger.exception("Gemini fallback image generation also failed")
        raise RuntimeError(f"Image generation failed: {exc}") from exc


# ---------------------------------------------------------------------------
# Public API
# ---------------------------------------------------------------------------

def _generate_mock_bot(vibe: str) -> str:
    """Helper to load a premium, realistic 3D Android bot image from static files."""
    import os
    
    clean_vibe = vibe.lower().strip()
    # Path to static folder
    static_dir = os.path.join(os.path.dirname(os.path.dirname(__file__)), "static")
    image_path = os.path.join(static_dir, f"bot_{clean_vibe}.png")
    
    # Fallback if the file doesn't exist for some reason
    if not os.path.exists(image_path):
        image_path = os.path.join(static_dir, "bot_creative.png")
        
    try:
        with open(image_path, "rb") as f:
            return base64.b64encode(f.read()).decode("utf-8")
    except Exception as exc:
        logger.error("Failed to read mock 3D image: %s", exc)
        # Final emergency fallback: blank slate image
        img = Image.new("RGB", (512, 512), color=(15, 23, 42))
        buffer = io.BytesIO()
        img.save(buffer, format="PNG")
        return base64.b64encode(buffer.getvalue()).decode("utf-8")



async def generate_bot(
    image_base64: str,
    vibe: str,
    custom_prompt: str | None = None,
) -> str:
    """Full pipeline: selfie -> caption -> prompt -> bot image (base64 PNG).

    Parameters
    ----------
    image_base64:
        The user's selfie encoded as base64.
    vibe:
        One of 'sporty', 'creative', 'adventurous', 'professional', 'chill'.
    custom_prompt:
        Optional extra instructions from the user.

    Returns
    -------
    str
        Base64-encoded PNG of the generated Android bot avatar.
    """
    logger.info("Starting bot generation pipeline (vibe=%s)...", vibe)

    # Check for missing/placeholder API key to run in mock mode
    if not settings.GEMINI_API_KEY or settings.GEMINI_API_KEY == "your_api_key_here":
        logger.warning("Mock Mode: GEMINI_API_KEY not configured. Generating mock Android bot image.")
        return _generate_mock_bot(vibe)

    # Step 1 - Caption the selfie
    description = await gemini_service.caption_image(image_base64)
    logger.info("Person description: %s", description[:120])

    # Step 2 - Build the full prompt
    prompt = build_bot_prompt(description=description, vibe=vibe, custom_prompt=custom_prompt)
    logger.debug("Full generation prompt:\n%s", prompt)

    # Step 3 - Try Imagen first, then Gemini fallback
    result = await _generate_via_imagen(prompt)
    if result is not None:
        return result

    logger.info("Imagen unavailable, switching to Gemini fallback.")
    return await _generate_via_gemini_fallback(prompt)
