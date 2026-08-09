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
    public static Boolean isLocalServer = false;
    public static void load(){
        if(!file.exists()){
            save();
            return;
        }
        try (FileReader reader = new FileReader(file)){
            ConfigData data = GSON.fromJson(reader, ConfigData.class);
            if (data != null) {
                isSendBarrels  = data.isSendBarrels;
                apiUrl = data.apiUrl;
                apiKey = data.apiKey;
                isLocalServer = data.isLocalServer;
            }
            reader.close();
        } catch (Exception e) {
            System.err.println("[BlockbergTerminal] Cannot load config");
            e.printStackTrace();
        }
        if (isLocalServer) {
            LocalServer.startLocalServer("");
        }
    }

    public static void save(){
        try (FileWriter writer = new FileWriter(file)){
            ConfigData data = new ConfigData(isSendBarrels,apiUrl,apiKey,isLocalServer);
            GSON.toJson(data,writer);
            writer.close();
        } catch (Exception e) {
            System.err.println("[BlockbergTerminal] Cannot save config");
            e.printStackTrace();
        }
        if (isLocalServer) {
            LocalServer.startLocalServer("");
        } else {
            LocalServer.stopLocalServer();
        }
    }
    
    private static class ConfigData {
        boolean isSendBarrels = true;
        String apiUrl = "";
        String apiKey = "";
        boolean isLocalServer = false;
        ConfigData(boolean isSendBarrels, String apiUrl, String apiKey, Boolean isLocalServer){
            this.isSendBarrels = isSendBarrels;
            this.apiUrl = apiUrl;
            this.apiKey = apiKey;
            this.isLocalServer = isLocalServer;
        }
    }
}