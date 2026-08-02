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

public class Regions {
    private static final File regionsFile = new File("regions.json");
    private static final List<Region> regions = new ArrayList<>();
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private static Region findRegionByName(String regionName) {
        for (Region region : regions) {
            if (region.getRegionName().equalsIgnoreCase(regionName)) {
                return region;
            }
        }
        return null;
    }

    private static void saveRegions() {
        try (FileWriter writer = new FileWriter(regionsFile)) {
            gson.toJson(regions, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private static void loadRegions() {
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
