"""Androidify Backend - FastAPI application entry point.

Start the server:
    uvicorn main:app --host 0.0.0.0 --port 8000 --reload
"""

from __future__ import annotations

import logging
import os
import sys
from contextlib import asynccontextmanager
from typing import AsyncIterator

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import HTMLResponse

from config import settings
from models.schemas import HealthResponse
from routers import generation, image

# ---------------------------------------------------------------------------
# Logging
# ---------------------------------------------------------------------------

logging.basicConfig(
    level=getattr(logging, settings.LOG_LEVEL.upper(), logging.INFO),
    format="%(asctime)s | %(levelname)-8s | %(name)s | %(message)s",
    datefmt="%Y-%m-%d %H:%M:%S",
    stream=sys.stdout,
)
logger = logging.getLogger(__name__)


# ---------------------------------------------------------------------------
# Lifespan (startup / shutdown hooks)
# ---------------------------------------------------------------------------

@asynccontextmanager
async def lifespan(_app: FastAPI) -> AsyncIterator[None]:
    """Run one-time checks at startup and teardown logic at shutdown."""
    # --- Startup ---
    logger.info("=" * 60)
    logger.info("  %s v%s starting up", settings.APP_NAME, settings.APP_VERSION)
    logger.info("=" * 60)

    if not settings.GEMINI_API_KEY:
        logger.warning(
            "[WARN] GEMINI_API_KEY is not set! "
            "All AI endpoints will fail until a valid key is provided. "
            "Set it in backend/.env or as an environment variable."
        )
    else:
        key_preview = settings.GEMINI_API_KEY[:8] + "..."
        logger.info("[OK] GEMINI_API_KEY loaded (%s)", key_preview)

    logger.info("[OK] Gemini model : %s", settings.GEMINI_MODEL)
    logger.info("[OK] Imagen model : %s", settings.IMAGEN_MODEL)
    logger.info("[OK] Listening on : %s:%s", settings.HOST, settings.PORT)

    yield  # Application is running

    # --- Shutdown ---
    logger.info("%s shutting down.", settings.APP_NAME)


# ---------------------------------------------------------------------------
# App
# ---------------------------------------------------------------------------

app = FastAPI(
    title=settings.APP_NAME,
    version=settings.APP_VERSION,
    description=(
        "Backend API for the Androidify app. Transforms user selfies into "
        "cute 3D Android robot avatars using Google Gemini and Imagen AI."
    ),
    lifespan=lifespan,
)

# ---------------------------------------------------------------------------
# Middleware
# ---------------------------------------------------------------------------

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # Permissive for development - lock down in production
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# ---------------------------------------------------------------------------
# Routers
# ---------------------------------------------------------------------------

app.include_router(image.router)
app.include_router(generation.router)


# ---------------------------------------------------------------------------
# Root-level endpoints
# ---------------------------------------------------------------------------

@app.get("/", response_class=HTMLResponse, include_in_schema=False)
async def read_index() -> HTMLResponse:
    """Serves the frontend static index.html file."""
    index_path = os.path.join(os.path.dirname(__file__), "static", "index.html")
    if os.path.exists(index_path):
        with open(index_path, "r", encoding="utf-8") as f:
            return HTMLResponse(content=f.read())
    return HTMLResponse(content="<h1>Index file not found</h1>", status_code=404)


@app.get(
    "/api/health",
    response_model=HealthResponse,
    tags=["Health"],
    summary="Health check",
)
async def health() -> HealthResponse:
    """Returns service health and version info."""
    return HealthResponse(status="ok", version=settings.APP_VERSION)


# ---------------------------------------------------------------------------
# Dev server entry point
# ---------------------------------------------------------------------------

if __name__ == "__main__":
    import uvicorn

    uvicorn.run(
        "main:app",
        host=settings.HOST,
        port=settings.PORT,
        reload=True,
        log_level=settings.LOG_LEVEL.lower(),
    )
