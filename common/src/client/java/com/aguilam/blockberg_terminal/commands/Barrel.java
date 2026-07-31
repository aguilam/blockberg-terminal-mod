public class Barrel {
    public void showBarrelContent(int barrelId){
        getBarrelInfo(MinecraftClient.getInstance(),barrelId);
    }
    public void searchBarrels(string query, int page){
        highlightedBlocks.clear();
        searchBarrels(MinecraftClient.getInstance(), searchTerm, page);
    }
}
