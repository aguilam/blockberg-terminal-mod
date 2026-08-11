package com.aguilam.blockberg_terminal.local;

import java.util.ArrayList;
import com.aguilam.blockberg_terminal.config.ConfigManager.ConfigData;

import java.util.List;

public class utils {
    public static List<String> buildArgs(ConfigData data) {
        List<String> args = new ArrayList<>();

        addArg(args, "serverApiKey", data.serverApiKey);
        addArg(args, "isLocalServer", data.isLocalServer);
        addArg(args, "aiUrl", data.aiUrl);
        addArg(args, "aiKey", data.aiKey);
        addArg(args, "aiModel", data.aiModel);

        addArg(args, "minX", data.minX);
        addArg(args, "maxX", data.maxX);
        addArg(args, "minY", data.minY);
        addArg(args, "maxY", data.maxY);
        addArg(args, "minZ", data.minZ);
        addArg(args, "maxZ", data.maxZ);

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
