#!/usr/bin/env python3
"""
SpotLocal Python AI - Model Configuration & Metadata Specification
High-Precision 4-Stem Deep Audio Separation Model Specification (ONNX v2)
"""

MODEL_NAME = "Mobile-UNet-4Stem-HD-v2"
DEFAULT_MODEL_PATH = "models/mobile_unet_4stems_hd.onnx"
SAMPLE_RATE = 44100
STEREO_CHANNELS = 2
QUANTIZATION_TYPE = "INT8-FP16-Mixed"

MODEL_SPEC = {
    "architecture": "Mobile-UNet-4Stem-DeepHD",
    "quantization": QUANTIZATION_TYPE,
    "input_shape": [1, STEREO_CHANNELS, SAMPLE_RATE],
    "output_stems": ["vocals", "drums", "bass", "other"],
    "target_runtime": "ONNXRuntime-Mobile-Android-NPU",
    "file_size_bytes": 19400000,
    "num_stems": 4,
    "frequency_resolution_hz": 10.75
}
