package com.aguilam.blockberg_terminal.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import net.minecraft.network.chat.Component;
public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parentScreen -> YetAnotherConfigLib.createBuilder()
            .title(Component.translatable("blockberg_terminal.settings"))
            .category(ConfigCategory.createBuilder()
                .name(Component.translatable("blockberg_terminal.client_settings"))
                .option(
                    Option.<String>createBuilder()
                    .name(Component.literal("API URL"))
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
                    .name(Component.literal("API Key"))
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
                    .name(Component.translatable("blockberg_terminal.send_barrel_items"))
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
            .category(ConfigCategory.createBuilder()
                .name(Component.translatable("blockberg_terminal.server_settings"))
                .option(
                    Option.<Boolean>createBuilder()
                    .name(Component.translatable("blockberg_terminal.start_local_server"))
                    .binding(false,
                        () -> ConfigManager.isLocalServer,
                        val -> ConfigManager.isLocalServer = val
                    )
                    .controller(BooleanControllerBuilder::create)
                    .build()
                )
                .option(
                    Option.<String>createBuilder()
                    .name(Component.translatable("blockberg_terminal.server_api_key"))
                    .binding("",
                        () -> ConfigManager.serverApiKey,
                        val -> ConfigManager.serverApiKey = val
                    )
                    .controller(StringControllerBuilder::create)
                    .build()
                )
                .option(
                    Option.<String>createBuilder()
                    .name(Component.translatable("blockberg_terminal.ai_url"))
                    .binding("",
                        () -> ConfigManager.aiUrl,
                        val -> ConfigManager.aiUrl = val
                    )
                    .controller(StringControllerBuilder::create)
                    .build()
                )
                .option(
                    Option.<String>createBuilder()
                    .name(Component.translatable("blockberg_terminal.ai_key"))
                    .binding("",
                        () -> ConfigManager.aiKey,
                        val -> ConfigManager.aiKey = val
                    )
                    .controller(StringControllerBuilder::create)
                    .build()
                )
                .option(
                    Option.<String>createBuilder()
                    .name(Component.translatable("blockberg_terminal.ai_model"))
                    .binding("",
                        () -> ConfigManager.aiModel,
                        val -> ConfigManager.aiModel = val
                    )
                    .controller(StringControllerBuilder::create)
                    .build()
                )
                .option(
                    Option.<Integer>createBuilder()
                    .name(Component.translatable("blockberg_terminal.min_x"))
                    .binding(0,
                        () -> ConfigManager.minX != null ? ConfigManager.minX : 0,
                        val -> ConfigManager.minX = (val == 0) ? null : val
                    )
                    .controller(IntegerFieldControllerBuilder::create)
                    .build()
                )
                .option(
                    Option.<Integer>createBuilder()
                    .name(Component.translatable("blockberg_terminal.max_x"))
                    .binding(0,
                        () -> ConfigManager.maxX != null ? ConfigManager.maxX : 0,
                        val -> ConfigManager.maxX = (val == 0) ? null : val
                    )
                    .controller(IntegerFieldControllerBuilder::create)
                    .build()
                )
                .option(
                    Option.<Integer>createBuilder()
                    .name(Component.translatable("blockberg_terminal.min_y"))
                    .binding(0,
                        () -> ConfigManager.minY != null ? ConfigManager.minY : 0,
                        val -> ConfigManager.minY = (val == 0) ? null : val
                    )
                    .controller(IntegerFieldControllerBuilder::create)
                    .build()
                )
                .option(
                    Option.<Integer>createBuilder()
                    .name(Component.translatable("blockberg_terminal.max_y"))
                    .binding(0,
                        () -> ConfigManager.maxY != null ? ConfigManager.maxY : 0,
                        val -> ConfigManager.maxY = (val == 0) ? null : val
                    )
                    .controller(IntegerFieldControllerBuilder::create)
                    .build()
                )
                .option(
                    Option.<Integer>createBuilder()
                    .name(Component.translatable("blockberg_terminal.min_z"))
                    .binding(0,
                        () -> ConfigManager.minZ != null ? ConfigManager.minZ : 0,
                        val -> ConfigManager.minZ = (val == 0) ? null : val
                    )
                    .controller(IntegerFieldControllerBuilder::create)
                    .build()
                )
                .option(
                    Option.<Integer>createBuilder()
                    .name(Component.translatable("blockberg_terminal.max_z"))
                    .binding(0,
                        () -> ConfigManager.maxZ != null ? ConfigManager.maxZ : 0,
                        val -> ConfigManager.maxZ = (val == 0) ? null : val
                    )
                    .controller(IntegerFieldControllerBuilder::create)
                    .build()
                )
                .build()
            )
            .save(() -> ConfigManager.save())
            .build()
            .generateScreen(parentScreen);
    }
}