package com.aguilam.blockberg_terminal.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import com.aguilam.blockberg_terminal.local.LocalServer;
public class ConfigManager {
    public static File file;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static boolean isSendBarrels = true;
    public static String apiUrl = "";
    public static String apiKey = "";
    public static boolean isLocalServer = false;
    public static String serverApiKey = "";
    public static String aiUrl = "";
    public static String aiKey = "";
    public static String aiModel = "";

    public static Integer minX = null;
    public static Integer maxX = null;
    public static Integer minY = null;
    public static Integer maxY = null;
    public static Integer minZ = null;
    public static Integer maxZ = null;

    public static void load() {
        if (file == null) return;

        if (!file.exists()) {
            save();
            return;
        }

        try (FileReader reader = new FileReader(file)) {
            ConfigData data = GSON.fromJson(reader, ConfigData.class);
            if (data != null) {
                isSendBarrels = data.isSendBarrels;
                apiUrl = data.apiUrl != null ? data.apiUrl : "";
                apiKey = data.apiKey != null ? data.apiKey : "";
                serverApiKey = data.serverApiKey != null ? data.serverApiKey : "";
                aiUrl = data.aiUrl != null ? data.aiUrl : "";
                aiKey = data.aiKey != null ? data.aiKey : "";
                aiModel = data.aiModel != null ? data.aiModel : "";

                minX = data.minX;
                maxX = data.maxX;
                minY = data.minY;
                maxY = data.maxY;
                minZ = data.minZ;
                maxZ = data.maxZ;
                if (isLocalServer) {
                    LocalServer.startLocalServer(data);
                }
            }
        } catch (Exception e) {
            System.err.println("[BlockbergTerminal] Cannot load config");
            e.printStackTrace();
        }

    }

    public static void save() {
        if (file == null) return;

        try (FileWriter writer = new FileWriter(file)) {
            ConfigData data = new ConfigData(
                isSendBarrels, apiUrl, serverApiKey, apiKey, isLocalServer,
                aiUrl, aiKey, aiModel,
                minX, maxX, minY, maxY, minZ, maxZ
            );
            GSON.toJson(data, writer);
            if (isLocalServer) {
                LocalServer.startLocalServer(data);
            } else {
                LocalServer.stopLocalServer();
            }
        } catch (Exception e) {
            System.err.println("[BlockbergTerminal] Cannot save config");
            e.printStackTrace();
        }
    }

    public static class ConfigData {
        public boolean isSendBarrels;
        public String apiUrl;
        public String apiKey;
        public boolean isLocalServer;
        public String serverApiKey;
        public String aiUrl;
        public String aiKey;
        public String aiModel;

        public Integer minX, maxX;
        public Integer minY, maxY;
        public Integer minZ, maxZ;

        ConfigData(boolean isSendBarrels, String apiUrl, String serverApiKey, String apiKey, boolean isLocalServer, String aiUrl, String aiKey, String aiModel,
            Integer minX, Integer maxX, Integer minY, Integer maxY, Integer minZ, Integer maxZ) {
            this.isSendBarrels = isSendBarrels;
            this.apiUrl = apiUrl;
            this.apiKey = apiKey;
            this.serverApiKey = serverApiKey;
            this.isLocalServer = isLocalServer;
            this.aiUrl = aiUrl;
            this.aiKey = aiKey;
            this.aiModel = aiModel;
            this.minX = minX;
            this.maxX = maxX;
            this.minY = minY;
            this.maxY = maxY;
            this.minZ = minZ;
            this.maxZ = maxZ;
        }
    }
}