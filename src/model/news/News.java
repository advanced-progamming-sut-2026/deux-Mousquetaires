package pvz.model.news;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class News {

    private final String title;
    private final String content;
    private final LocalDateTime publishDate;
    private boolean isRead;

    public News(String title, String content) {
        this.title = title;
        this.content = content;
        this.publishDate = LocalDateTime.now();
        this.isRead = false;
    }

    public void markAsRead() { isRead = true; }

    public void show() {
        System.out.println("╔══ " + title + " ══ " + publishDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) + " ══╗");
        System.out.println(content);
        System.out.println("╚" + "═".repeat(60) + "╝");
        markAsRead();
    }
    /// get set
    public String getTitle() { return title; }
    public String  getContent() { return content; }
    public LocalDateTime getPublishDate() { return publishDate; }
    public boolean isRead() { return isRead; }
}
