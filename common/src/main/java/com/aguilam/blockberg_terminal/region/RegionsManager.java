package com.aguilam.blockberg_terminal.region;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.aguilam.blockberg_terminal.config.ConfigManager;
import com.aguilam.blockberg_terminal.model.Regions.Region;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.phys.AABB;

public class RegionsManager {
    public static final List<Region> regions = new ArrayList<>();
    public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    public static BlockPos tempMinPos;
    public static BlockPos tempMaxPos;
    public static Boolean isDrawEnabled = false;
    public static List<Region> regionsForRender = new ArrayList<>();
    private static File getRegionsFile() {
        return ConfigManager.gameDir.resolve("regions.json").toFile();
    }

    public static Region findRegionByName(String regionName) {
        for (Region region : regions) {
            if (region.getRegionName().equalsIgnoreCase(regionName)) {
                return region;
            }
        }
        return null;
    }

    public static void saveRegions() {
        try (FileWriter writer = new FileWriter(getRegionsFile())) {
            gson.toJson(regions, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void deleteRegion(String regionName) {
        File regionsFile = getRegionsFile();
    
        Type listType = new TypeToken<List<Region>>() {}.getType();
        List<Region> loadedRegions;
    
        try (FileReader reader = new FileReader(regionsFile)) {
            loadedRegions = gson.fromJson(reader, listType);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    
        if (loadedRegions == null) return;
    
        boolean removed = loadedRegions.removeIf(
            region -> regionName.equals(region.getRegionName())
        );
    
        if (!removed) return;
    
        try (FileWriter writer = new FileWriter(regionsFile)) {
            gson.toJson(loadedRegions, writer);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    
        regions.clear();
        regions.addAll(loadedRegions);
    }

    public static void renderRegion(SubmitNodeCollector collector,CameraRenderState camera) {
        if (!RegionsManager.isDrawEnabled || RegionsManager.regions.isEmpty()) return;
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null) return;
        regionsForRender.clear();
        regionsForRender.addAll(regions);
        try (Gizmos.TemporaryCollection _ = client.levelRenderer.collectPerFrameRenderThreadGizmos()) {
            for (Region region : RegionsManager.regionsForRender) {
                //int a = (int) (block.alpha * 255) & 0xFF;
                //int r = (int) (block.red * 255) & 0xFF;
                //int g = (int) (block.green * 255) & 0xFF;
                //int b = (int) (block.blue * 255) & 0xFF;
                //int argb = (a << 24) | (r << 16) | (g << 8) | b;

                Gizmos.cuboid(AABB.encapsulatingFullBlocks(region.getMinPos(), region.getMaxPos()),GizmoStyle.stroke(0xFFFFCC00, 2.0F)).setAlwaysOnTop();
            }
        }
    }
    public static void loadRegions() {
        File regionsFile = getRegionsFile();
        if (regionsFile.exists()) {
            try (FileReader reader = new FileReader(regionsFile)) {
                Type listType = new TypeToken<List<Region>>() {}.getType();
                List<Region> loadedRegions = gson.fromJson(reader, listType);
                if (loadedRegions != null) {
                    regions.clear();
                    regions.addAll(loadedRegions);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
