package pvz.controller;

import pvz.model.entity.plant.PlantFactory;
import pvz.model.enums.PlantType;

class ShopController {

    private final GameApp app;

    ShopController(GameApp app) {this.app = app;}

    void handle(String line) {
        if (line.equals("help")) {
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

    }

    private void buy(String line) {
        Args args = Args.parse(line);
        Integer itemId = args.getInt("i");
        Integer count = args.getInt("n");
        if (itemId == null) {
            return;}

        PlantType chosen = null;
        if (args.get("t") != null) {
            chosen = PlantFactory.parseType(args.get("t"));
            if (chosen == null) {
                return;}
        }
        String result = app.shop.buy(app.currentUser, itemId, count == null ? 1 : count, chosen);
        app.saveAll();
    }
}
