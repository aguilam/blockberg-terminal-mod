package com.pepeprice.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import net.minecraft.text.Text;
public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parentScreen -> YetAnotherConfigLib.createBuilder()
            .title(Text.literal("Blockberg Terminal"))
            .category(ConfigCategory.createBuilder()
                .name(Text.literal("Settings"))
                .option(
                    Option.<String>createBuilder()
                    .name(Text.literal("API URL"))
                    .binding(
                        "", 
                        () -> ConfigManager.apiUrl,
                        val -> ConfigManager.apiUrl = val
                    )
                    .controller(StringControllerBuilder::create)
                    .build()
                )
                .option(
                    Option.<String>createBuilder()
                    .name(Text.literal("API Key"))
                    .binding(
                        "", 
                        () -> ConfigManager.apiKey,
                        val -> ConfigManager.apiKey = val
                    )
                    .controller(StringControllerBuilder::create)
                    .build()
                )
                .option(
                    Option.<Boolean>createBuilder()
                    .name(Text.literal("Send Barrel Items"))
                    .binding(
                        true, 
                        () -> ConfigManager.isSendBarrels,
                        val -> ConfigManager.isSendBarrels = val
                    )
                    .controller(BooleanControllerBuilder::create)
                    .build()
                )
                .build()
            )
            .save(() -> ConfigManager.save())
            .build()
            .generateScreen(parentScreen);
    }
}