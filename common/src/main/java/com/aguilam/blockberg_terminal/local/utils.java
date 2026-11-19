package com.aguilam.blockberg_terminal.local;

import java.util.ArrayList;
import com.aguilam.blockberg_terminal.config.ConfigManager.ConfigData;

import java.util.List;

public class utils {
    public static List<String> buildArgs(ConfigData data) {
        List<String> args = new ArrayList<>();

        addArg(args, "api-key", data.serverApiKey);
        addArg(args, "ai-url", data.aiUrl);
        addArg(args, "ai-key", data.aiKey);
        addArg(args, "ai-model", data.aiModel);

        addArg(args, "x-min", data.minX);
        addArg(args, "x-max", data.maxX);
        addArg(args, "y-min", data.minY);
        addArg(args, "y-max", data.maxY);
        addArg(args, "z-min", data.minZ);
        addArg(args, "z-max", data.maxZ);

        return args;
    }

    private static void addArg(List<String> args, String flag, Object value) {
        if (value == null) return;

        String strVal = value.toString().trim();

        if (!strVal.isEmpty()) {
            args.add("--" + flag + "=" + strVal);
        }
    }
}
