package com.aguilam.blockberg_terminal;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;

@Mod(BlockbergTerminal.MOD_ID)
public class BlockbergTerminal {
    public static final String MOD_ID = "blockberg_terminal";
    public BlockbergTerminal(IEventBus modEventBus) {
        if(FMLEnvironment.dist.isClient()){
            BlockbergTerminalClient.init();
        }
    }
}
