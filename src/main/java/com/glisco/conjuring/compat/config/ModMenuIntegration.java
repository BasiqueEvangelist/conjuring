package com.glisco.conjuring.compat.config;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ModMenuIntegration /*implements ModMenuApi*/ {

    /*@Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> AutoConfig.getConfigScreen(ConjuringConfig.class, parent).get();
    }*/
}
