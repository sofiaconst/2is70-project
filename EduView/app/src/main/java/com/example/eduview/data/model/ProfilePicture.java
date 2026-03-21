package com.example.eduview.data.model;

import com.example.eduview.R;

public enum ProfilePicture {
    DIPLO_RED(R.drawable.pfp_diplo_red),
    SPINO_RED(R.drawable.pfp_spino_red),
    STEGO_RED(R.drawable.pfp_stego_red),
    DIPLO_PINK(R.drawable.pfp_diplo_pink),
    STEGO_PINK(R.drawable.pfp_stego_pink),
    DIPLO_GREEN(R.drawable.pfp_diplo_green),
    DIPLO_PINK2(R.drawable.pfp_diplo_pink2),
    INGUANO_RED(R.drawable.pfp_inguano_red),
    TREX_YELLOW(R.drawable.pfp_trex_yellow),
    PLATEO_GREEN(R.drawable.pfp_plateo_green),
    SPINO_YELLOW(R.drawable.pfp_spino_yellow),
    BRACHIO_GREEN(R.drawable.pfp_brachio_green),
    TRICERA_GREEN(R.drawable.pfp_tricera_green),
    TRICERA_GREEN_PINK(R.drawable.pfp_tricera_green_pink),
    TRICERA_GREEN_YELLOW(R.drawable.pfp_tricera_green_yellow),
    DEFAULT(R.drawable.ic_person);

    private final int drawableId;

    ProfilePicture(int drawableId) {
        this.drawableId = drawableId;
    }

    public int getDrawableId() {
        return drawableId;
    }
}
