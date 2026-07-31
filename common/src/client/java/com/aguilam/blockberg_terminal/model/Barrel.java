
public static class Barrel {
    public static class BarrelSearchBody {
        int total;
        int page;
        int limit;
        BarrelSearchItem[] barrels;
    }
    
    public static class BarrelSearchItem {
        int id;
        String name;
        String seller;
        int price;
        int quantity;
        int x;
        int y;
        int z;
        double benefitRatio;
        int snapshotsCount;
        String recordDate;
    }
    
    public static class BarrelItem {
        String name;
        int quantity;
    }
    
    public static class BarrelSnapshot {
        BarrelItem[] items;
        String recordDate;
    }
    
    public static class BarrelInfo {
        int id;
        String name;
        String seller;
        int price;
        int quantity;
        int x;
        int y;
        int z;
        double benefitRatio;
        BarrelSnapshot barrelItems;
        String recordDate;
    }
}
