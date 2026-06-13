"""Router for image utility endpoints (validation & captioning)."""

from __future__ import annotations

import logging

from fastapi import APIRouter, HTTPException

from models.schemas import (
    ImageCaptionRequest,
    ImageCaptionResponse,
    ImageValidationRequest,
    ImageValidationResponse,
)
from services import gemini_service

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/api", tags=["Image"])


@router.post(
    "/validate-image",
    response_model=ImageValidationResponse,
    summary="Validate a selfie for avatar generation",
    description="Analyses the uploaded image to verify it contains a clear human face suitable for bot avatar creation.",
)
async def validate_image(request: ImageValidationRequest) -> ImageValidationResponse:
    logger.info("POST /api/validate-image - payload size %d chars", len(request.image_base64))

    if not request.image_base64:
        raise HTTPException(status_code=400, detail="image_base64 field is required and must not be empty.")

    result = await gemini_service.validate_image(request.image_base64)
    return result


@router.post(
    "/caption-image",
    response_model=ImageCaptionResponse,
    summary="Generate an appearance caption",
    description="Generates a textual description of the person's appearance from the provided image.",
)
async def caption_image(request: ImageCaptionRequest) -> ImageCaptionResponse:
    logger.info("POST /api/caption-image - payload size %d chars", len(request.image_base64))

    if not request.image_base64:
        raise HTTPException(status_code=400, detail="image_base64 field is required and must not be empty.")

    try:
        caption = await gemini_service.caption_image(request.image_base64)
        return ImageCaptionResponse(caption=caption)
    except RuntimeError as exc:
        logger.error("Caption generation failed: %s", exc)
        raise HTTPException(status_code=502, detail=str(exc)) from exc
    except ValueError as exc:
        logger.warning("Bad input for captioning: %s", exc)
        raise HTTPException(status_code=400, detail=str(exc)) from exc
