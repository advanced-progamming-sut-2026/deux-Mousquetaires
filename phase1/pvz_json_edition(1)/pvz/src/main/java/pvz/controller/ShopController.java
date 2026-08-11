package pvz.controller;

import pvz.model.entity.plant.PlantFactory;
import pvz.model.enums.PlantType;

class ShopController {

    private final GameApp app;

    ShopController(GameApp app) {this.app = app;}

    void handle(String line) {
        if (line.equals("help")) {
            app.view.info("Commands: shop list | shop daily" + " | shop buy -i <item_id> -n <count> [-t <plant_type>] | menu exit");
            return;}

        if (line.equals("shop list")) {
            app.shop.printCatalog(app.currentUser);
            return;}

        if (line.equals("shop daily")) {
            app.shop.printDailyOffer(app.currentUser);
            return;}

        if (line.startsWith("shop buy")) {
            buy(line);
            return;}

        app.view.error("Unknown command. Type 'help' for this menu's commands.");
    }

    private void buy(String line) {
        Args args = Args.parse(line);
        Integer itemId = args.getInt("i");
        Integer count = args.getInt("n");
        if (itemId == null) {
            app.view.error("Usage: shop buy -i <item_id> -n <count> [-t <plant_type>]");
            return;}

        PlantType chosen = null;
        if (args.get("t") != null) {
            chosen = PlantFactory.parseType(args.get("t"));
            if (chosen == null) {
                app.view.error("Unknown plant type: " + args.get("t"));
                return;}
        }
        String result = app.shop.buy(app.currentUser, itemId, count == null ? 1 : count, chosen);
        app.view.info(result);
        app.saveAll();
    }
}
