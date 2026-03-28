package com.example.eduview.data.model;

import com.example.eduview.R;

/**
 * Enum representing the different profile pictures users can have.
 */
public enum ProfilePicture {

    // Red Dinosaurs
    DIPLO_RED(R.drawable.pfp_diplo_red),
    SPINO_RED(R.drawable.pfp_spino_red),
    STEGO_RED(R.drawable.pfp_stego_red),
    INGUANO_RED(R.drawable.pfp_inguano_red),

    // Pink Dinosaurs
    DIPLO_PINK(R.drawable.pfp_diplo_pink),
    STEGO_PINK(R.drawable.pfp_stego_pink),
    DIPLO_PINK2(R.drawable.pfp_diplo_pink2),

    // Yellow Dinosaurs
    TREX_YELLOW(R.drawable.pfp_trex_yellow),
    SPINO_YELLOW(R.drawable.pfp_spino_yellow),

    // Green Dinosaurs
    DIPLO_GREEN(R.drawable.pfp_diplo_green),
    TRICERA_GREEN_YELLOW(R.drawable.pfp_tricera_green_yellow),
    BRACHIO_GREEN(R.drawable.pfp_brachio_green),
    TRICERA_GREEN(R.drawable.pfp_tricera_green),
    TRICERA_GREEN_PINK(R.drawable.pfp_tricera_green_pink),
    PLATEO_GREEN(R.drawable.pfp_plateo_green),

    DEFAULT(R.drawable.ic_person);

    private final int drawableId;

    /**
     * Creates a profile picture with a drawable contained in resources.
     * @param drawableId a profile picture
     */
    ProfilePicture(int drawableId) {
        this.drawableId = drawableId;
    }

    /**
     * Returns the ID of the drawable for the profile picture.
     * @return ID of the drawable
     */
    public int getDrawableId() {
        return drawableId;
    }
}
