package com.aguilam.blockberg_terminal.region;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


import com.aguilam.blockberg_terminal.model.Regions.Region;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import net.minecraft.core.BlockPos;

public class RegionsManager {
    private static final File regionsFile = new File("regions.json");
    public static final List<Region> regions = new ArrayList<>();
    public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    public static BlockPos tempMinPos;
    public static BlockPos tempMaxPos;
    public static Region findRegionByName(String regionName) {
        for (Region region : regions) {
            if (region.getRegionName().equalsIgnoreCase(regionName)) {
                return region;
            }
        }
        return null;
    }

    public static void saveRegions() {
        try (FileWriter writer = new FileWriter(regionsFile)) {
            gson.toJson(regions, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void loadRegions() {
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
