package com.blockberg_terminal.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class ConfigManager {
    private static final File FILE = FabricLoader.getInstance().getConfigDir().resolve("blockberg-terminal.json").toFile();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static boolean isSendBarrels = true;
    public static String apiUrl = "";
    public static String apiKey = "";

    public static void load(){
        if(!FILE.exists()){
            save();
            return;
        }
        try (FileReader reader = new FileReader(FILE)){
            ConfigData data = GSON.fromJson(reader, ConfigData.class);
            if (data != null) {
                isSendBarrels  = data.isSendBarrels;
                apiUrl = data.apiUrl;
                apiKey = data.apiKey;
            }
            reader.close();
        } catch (Exception e) {
            System.err.println("[BlockbergTerminal] Cannot load config");
            e.printStackTrace();
        }
    }

    public static void save(){
        try (FileWriter writer = new FileWriter(FILE)){
            ConfigData data = new ConfigData(isSendBarrels,apiUrl,apiKey);
            GSON.toJson(data,writer);
            writer.close();
        } catch (Exception e) {
            System.err.println("[BlockbergTerminal] Cannot save config");
            e.printStackTrace();
        }
    }
    
    private static class ConfigData {
        boolean isSendBarrels = true;
        String apiUrl = "";
        String apiKey = "";

        ConfigData(boolean isSendBarrels, String apiUrl, String apiKey){
            this.isSendBarrels = isSendBarrels;
            this.apiUrl = apiUrl;
            this.apiKey = apiKey;
        }
    }
}