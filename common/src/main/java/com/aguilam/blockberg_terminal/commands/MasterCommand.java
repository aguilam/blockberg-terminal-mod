package com.aguilam.blockberg_terminal.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

public class MasterCommand {
    public static <S> LiteralArgumentBuilder<S> createMasterCommand() {
        return LiteralArgumentBuilder.<S>literal("bbt")
            .then(Barrel.searchBarrels())
            .then(Barrel.searchSnapshot())
            .then(Barrel.showBarrelContent())
            .then(Regions.addRegion())
            .then(Regions.allRegions())
            .then(Regions.setMin())
            .then(Regions.setMax())
            .then(Regions.scan())
            .then(Regions.clearHighlighted());
    }
}
