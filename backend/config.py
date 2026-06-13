"""Application configuration loaded from environment variables."""

import os
from pathlib import Path

from dotenv import load_dotenv

# Load .env file from the backend directory
_env_path = Path(__file__).resolve().parent / ".env"
load_dotenv(dotenv_path=_env_path)


class Settings:
    """Central configuration sourced from environment variables."""

    GEMINI_API_KEY: str = os.environ.get("GEMINI_API_KEY", "")
    LOG_LEVEL: str = os.environ.get("LOG_LEVEL", "INFO")
    HOST: str = os.environ.get("HOST", "0.0.0.0")
    PORT: int = int(os.environ.get("PORT", "8000"))

    # Model identifiers
    GEMINI_MODEL: str = "gemini-2.0-flash"
    IMAGEN_MODEL: str = "imagen-3.0-generate-002"

    # App metadata
    APP_NAME: str = "Androidify Backend"
    APP_VERSION: str = "1.0.0"

    # Image constraints
    MAX_IMAGE_SIZE_BYTES: int = 10 * 1024 * 1024  # 10 MB


settings = Settings()
