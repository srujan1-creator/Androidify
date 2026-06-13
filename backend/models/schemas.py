"""Pydantic request / response schemas for all API endpoints."""

from typing import Optional

from pydantic import BaseModel, Field


# ---------------------------------------------------------------------------
# Health
# ---------------------------------------------------------------------------

class HealthResponse(BaseModel):
    """Response for the /api/health endpoint."""
    status: str = Field(..., examples=["ok"])
    version: str = Field(..., examples=["1.0.0"])


# ---------------------------------------------------------------------------
# Image Validation
# ---------------------------------------------------------------------------

class ImageValidationRequest(BaseModel):
    """Request body for validating a selfie image."""
    image_base64: str = Field(
        ...,
        description="Base64-encoded image data (JPEG or PNG). May optionally include the data-URI prefix.",
    )


class ImageValidationResponse(BaseModel):
    """Result of the face-validation check."""
    is_valid: bool = Field(..., description="Whether the image contains a suitable face.")
    message: str = Field(..., description="Human-readable explanation.")
    confidence: float = Field(
        ...,
        ge=0.0,
        le=1.0,
        description="Model confidence that a clear face is present (0.0 – 1.0).",
    )


# ---------------------------------------------------------------------------
# Image Captioning
# ---------------------------------------------------------------------------

class ImageCaptionRequest(BaseModel):
    """Request body for generating a caption that describes a person's appearance."""
    image_base64: str = Field(
        ...,
        description="Base64-encoded image data.",
    )


class ImageCaptionResponse(BaseModel):
    """Caption describing the person's visual appearance."""
    caption: str = Field(..., description="Detailed appearance description.")


# ---------------------------------------------------------------------------
# Bot Generation
# ---------------------------------------------------------------------------

class BotGenerationRequest(BaseModel):
    """Request body for generating an Android bot avatar from a selfie."""
    image_base64: str = Field(
        ...,
        description="Base64-encoded selfie image data.",
    )
    vibe: str = Field(
        default="chill",
        description="Style / vibe for the generated bot avatar.",
        examples=["sporty", "creative", "adventurous", "professional", "chill"],
    )
    custom_prompt: Optional[str] = Field(
        default=None,
        description="Optional free-text prompt to further customize the bot.",
    )


class BotGenerationResponse(BaseModel):
    """Generated Android bot avatar."""
    generated_image_base64: str = Field(
        ...,
        description="Base64-encoded PNG of the generated bot avatar.",
    )
    style_used: str = Field(
        ...,
        description="The vibe / style that was applied.",
    )


# ---------------------------------------------------------------------------
# Generic error wrapper (used by exception handlers)
# ---------------------------------------------------------------------------

class ErrorResponse(BaseModel):
    """Standard error payload returned by the API."""
    detail: str
