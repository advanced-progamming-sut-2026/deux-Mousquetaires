package pvz.view;

import pvz.model.auth.User;
import pvz.model.enums.PlantType;
import pvz.model.enums.ZombieType;
import pvz.model.session.ActiveZombie;

import java.util.List;

public class ConsoleView {

    private final BannerView bannerView = new BannerView();
    private final MessageView messages = new MessageView();
    private final ProfileView profileView = new ProfileView();
    private final ZombiesView zombiesView = new ZombiesView();
    private final ChaptersView chaptersView = new ChaptersView();
    private final GreenhouseView greenhouseView = new GreenhouseView();
    private final CollectionView collectionView = new CollectionView();
    public void banner() {
        bannerView.print();
    }
    public void info(String message) {
        messages.info(message);
    }
    public void error(String message) {
        messages.error(message);
    }
    public void success(String message) {
        messages.success(message);
    }
    public void menuPath(String name) {
        messages.menuPath(name);
    }
    public void prompt(String menuName) {
        messages.prompt(menuName);
    }

    /// screen
    public void showProfile(User user) {
        profileView.render(user);
    }

    public void showLawnZombies(List<ActiveZombie> zombies) {
        zombiesView.render(zombies);
    }

    public void showChapters(User user) {
        chaptersView.render(user);
    }

    public void showGreenhouse(User user) {
        greenhouseView.render(user);
    }

    public void showPlantList(User user, boolean all) {
        collectionView.renderPlantList(user, all);
    }

    public void showZombieList(User user, boolean all) {
        collectionView.renderZombieList(user, all);
    }

    public void showPlantDetails(User user, PlantType type) {
        collectionView.renderPlantDetails(user, type);
    }

    public void showZombieDetails(ZombieType type) {
        collectionView.renderZombieDetails(type);
    }
}
