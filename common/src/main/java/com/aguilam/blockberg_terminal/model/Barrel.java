package com.aguilam.blockberg_terminal.model;

public class Barrel {
    public static class BarrelSearchBody {
        public int total;
        public int page;
        public int limit;
        public BarrelSearchItem[] barrels;
    }
    
    public static class BarrelSearchItem {
        public int id;
        public String name;
        public String seller;
        public int price;
        public int quantity;
        public int x;
        public int y;
        public int z;
        public double benefitRatio;
        public int snapshotsCount;
        public String recordDate;
    }
    
    public static class BarrelItem {
        public String name;
        public int quantity;
    }
    
    public static class BarrelSnapshot {
        public BarrelItem[] items;
        public String recordDate;
    }
    
    public static class BarrelInfo {
        public int id;
        public String name;
        public String seller;
        public int price;
        public int quantity;
        public int x;
        public int y;
        public int z;
        public double benefitRatio;
        public BarrelSnapshot barrelItems;
        public String recordDate;
    }
}
