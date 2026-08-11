package pvz.model.news;

import java.util.ArrayList;
import java.util.List;

public class NewsManager {

    private final List<News> newsList = new ArrayList<>();
    public void publish(String title, String content) {
        newsList.add(new News(title, content));
    }
    public void publishUnlock(String content) {
        newsList.add(new News("New unlock!", content));
    }
    public void addNews(News news) {
        newsList.add(news);
    }
    public boolean hasUnread() {
        return newsList.stream().anyMatch(n -> !n.isRead());
    }
    public int unreadCount() {
        return (int) newsList.stream().filter(n -> !n.isRead()).count();
    }
    public void showUnread() {
        List<News> unread = new ArrayList<>();
        for (News news : newsList) {if (!news.isRead()) unread.add(news);}
        if (unread.isEmpty()) {
            System.out.println("No unread news.");
            return;
        }
        for (News news : unread) {news.show();}
    }

    public void showAll() {
        if (newsList.isEmpty()) {
            System.out.println("No news yet. Go make some history!");
            return;
        }
        for (News news : newsList) {news.show();}
    }

    public List<News> getAll() {return newsList;}
}
