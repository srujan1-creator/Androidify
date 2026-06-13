"""Prompt templates for Gemini Vision and Imagen image generation."""

# ---------------------------------------------------------------------------
# Face / selfie validation prompt  (Gemini Vision)
# ---------------------------------------------------------------------------
VALIDATION_PROMPT = """Analyze this image and determine whether it contains a clear, 
well-visible human face that would be suitable for generating a stylized avatar.

Evaluate the following criteria:
1. **Face presence**: Is there at least one human face clearly visible?
2. **Face clarity**: Is the face in focus and not heavily obscured (e.g., by masks, extreme angles, or motion blur)?
3. **Lighting**: Is the face reasonably well-lit so that facial features are distinguishable?
4. **Obstruction**: Is the face free from heavy obstructions like sunglasses that cover most of the face, hands blocking features, etc.? Light accessories (glasses, hats, earrings) are acceptable.

Respond ONLY with a valid JSON object - no markdown fences, no extra text:
{
  "is_valid": true or false,
  "message": "Brief explanation of the result",
  "confidence": 0.0 to 1.0
}
"""

# ---------------------------------------------------------------------------
# Appearance caption prompt  (Gemini Vision)
# ---------------------------------------------------------------------------
CAPTION_PROMPT = """You are an expert at describing a person's visual appearance for the 
purpose of creating a cute, stylized robot avatar that reflects their look.

Examine the person in this photo and provide a detailed but concise description covering:
- **Hair**: color, length, style (curly, straight, braided, bun, etc.)
- **Skin tone**: general tone
- **Facial hair**: beard, mustache, clean-shaven
- **Glasses / eyewear**: type, frame color
- **Clothing**: visible outfit colors, patterns, style
- **Accessories**: hats, headbands, jewelry, headphones, scarves, etc.
- **Notable features**: anything distinctive that should carry over to the avatar

Keep the description to 3-5 sentences maximum. Focus only on visual attributes, 
do NOT comment on attractiveness, age estimation, ethnicity, or make subjective judgments.
Do NOT wrap your response in quotes or JSON - just output the plain description text.
"""

# ---------------------------------------------------------------------------
# Bot generation prompt template  (Imagen / Gemini image generation)
# ---------------------------------------------------------------------------
BOT_GENERATION_PROMPT_TEMPLATE = """Create a cute, friendly 3D Android robot mascot avatar 
inspired by the following person's appearance:

{description}

Style direction: {vibe_prompt}

{custom_addition}

Important rules for the generated image:
- The character MUST be a stylized 3D Android robot/bot, NOT a human
- Use the classic Android robot body shape: rounded rectangular head, capsule-shaped body, 
  cylindrical arms and legs, antenna on top
- Transfer the person's distinctive visual features (hair style/color, glasses, clothing 
  colors, accessories) onto the robot
- The robot should look cheerful, approachable, and high-quality
- Use a clean, simple gradient or solid color background
- Render in a modern 3D illustration style with soft lighting and slight shadows
- The image should be centered, showing the full robot character
- Output as a square image with no text or watermarks
"""

# ---------------------------------------------------------------------------
# Vibe-specific prompt additions
# ---------------------------------------------------------------------------
VIBE_PROMPTS: dict[str, str] = {
    "sporty": (
        "Give the robot an athletic, energetic vibe. Add sporty accessories like "
        "a sweatband, sneakers, or a jersey. Use bold, dynamic colors. "
        "The pose should feel active and confident - maybe mid-stride or with a fist pump."
    ),
    "creative": (
        "Give the robot an artsy, creative vibe. Add accessories like a beret, "
        "paint splashes, a pencil behind the ear, or colorful patterns on the body. "
        "Use vibrant, imaginative colors. The pose should feel expressive and inspired."
    ),
    "adventurous": (
        "Give the robot an explorer, adventurous vibe. Add a tiny backpack, "
        "a compass, hiking boots, or a safari hat. Use earthy tones mixed with bright accents. "
        "The pose should feel bold and curious - maybe looking toward the horizon."
    ),
    "professional": (
        "Give the robot a polished, professional vibe. Add a tiny tie, blazer, "
        "briefcase, or smart watch. Use clean, sophisticated colors like navy, charcoal, "
        "and white. The pose should feel confident and poised."
    ),
    "chill": (
        "Give the robot a relaxed, laid-back vibe. Add cozy accessories like headphones, "
        "a hoodie, sunglasses, or a coffee cup. Use warm, muted pastel colors. "
        "The pose should feel relaxed and comfortable - maybe a casual lean or peace sign."
    ),
}


def get_vibe_prompt(vibe: str) -> str:
    """Return the vibe-specific prompt addition, falling back to 'chill'."""
    return VIBE_PROMPTS.get(vibe.lower().strip(), VIBE_PROMPTS["chill"])


def build_bot_prompt(description: str, vibe: str, custom_prompt: str | None = None) -> str:
    """Assemble the complete Imagen / image-generation prompt."""
    vibe_prompt = get_vibe_prompt(vibe)
    custom_addition = f"Additional customization: {custom_prompt}" if custom_prompt else ""

    return BOT_GENERATION_PROMPT_TEMPLATE.format(
        description=description,
        vibe_prompt=vibe_prompt,
        custom_addition=custom_addition,
    ).strip()
