"""Router for bot avatar generation."""

from __future__ import annotations

import logging

from fastapi import APIRouter, HTTPException

from models.schemas import BotGenerationRequest, BotGenerationResponse
from services import imagen_service

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/api", tags=["Generation"])


@router.post(
    "/generate-bot",
    response_model=BotGenerationResponse,
    summary="Generate an Android bot avatar",
    description=(
        "Full pipeline: validates the selfie, captions the person's appearance, "
        "and generates a stylised 3D Android robot avatar matching their look and selected vibe."
    ),
)
async def generate_bot(request: BotGenerationRequest) -> BotGenerationResponse:
    vibe = request.vibe or "chill"
    logger.info(
        "POST /api/generate-bot - vibe=%s, custom_prompt=%s, payload size %d chars",
        vibe,
        bool(request.custom_prompt),
        len(request.image_base64),
    )

    if not request.image_base64:
        raise HTTPException(status_code=400, detail="image_base64 field is required and must not be empty.")

    # Validate the vibe value
    valid_vibes = {"sporty", "creative", "adventurous", "professional", "chill"}
    if vibe.lower().strip() not in valid_vibes:
        logger.warning("Unknown vibe '%s', defaulting to 'chill'.", vibe)
        vibe = "chill"

    try:
        generated_base64 = await imagen_service.generate_bot(
            image_base64=request.image_base64,
            vibe=vibe,
            custom_prompt=request.custom_prompt,
        )
        return BotGenerationResponse(
            generated_image_base64=generated_base64,
            style_used=vibe,
        )

    except RuntimeError as exc:
        logger.error("Bot generation pipeline error: %s", exc)
        raise HTTPException(status_code=502, detail=str(exc)) from exc
    except ValueError as exc:
        logger.warning("Bad input for bot generation: %s", exc)
        raise HTTPException(status_code=400, detail=str(exc)) from exc
    except Exception as exc:
        logger.exception("Unexpected error in bot generation")
        raise HTTPException(status_code=500, detail=f"Internal error: {exc}") from exc
