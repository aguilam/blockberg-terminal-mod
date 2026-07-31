public class SendBarrelToServer {
    private void sendBarrelDataToServer(JsonArray barrelDataList) {
        try {    
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null) {
                client.player.sendMessage(Text.literal("Отправляю " + barrelDataList.size() + " записей..."), false);
            }
        
            //Add Network request
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
