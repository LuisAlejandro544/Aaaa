#!/usr/bin/env python3
"""
SpotLocal Python AI - Audio Processing & 4-Stem Separation Math Utilities
"""

import os
from model_config import SAMPLE_RATE, STEREO_CHANNELS

class AudioProcessor:
    def __init__(self, sample_rate=SAMPLE_RATE, channels=STEREO_CHANNELS):
        self.sample_rate = sample_rate
        self.channels = channels

    def prepare_output_paths(self, input_audio_path, output_dir):
        os.makedirs(output_dir, exist_ok=True)
        base_name = os.path.splitext(os.path.basename(input_audio_path))[0]
        return {
            "vocals": os.path.join(output_dir, f"{base_name}_vocals.wav"),
            "drums": os.path.join(output_dir, f"{base_name}_drums.wav"),
            "bass": os.path.join(output_dir, f"{base_name}_bass.wav"),
            "other": os.path.join(output_dir, f"{base_name}_other.wav")
        }

    def compute_stem_gains(self, stem_mode):
        # Gains format: (vocals_db, drums_db, bass_db, other_db)
        gains = {
            "ORIGINAL": (0.0, 0.0, 0.0, 0.0),
            "VOCALS": (0.0, -60.0, -60.0, -60.0),
            "DRUMS": (-60.0, 0.0, -60.0, -60.0),
            "BASS": (-60.0, -60.0, 0.0, -60.0),
            "OTHER": (-60.0, -60.0, -60.0, 0.0),
            "KARAOKE": (-14.0, 1.5, 1.5, 1.5)
        }
        return gains.get(stem_mode, (0.0, 0.0, 0.0, 0.0))
