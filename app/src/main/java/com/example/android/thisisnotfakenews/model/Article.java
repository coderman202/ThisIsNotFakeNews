package com.example.android.thisisnotfakenews.model;

/**
 * Created by Reggie on 14/07/2017.
 * A custom object to represent news articles.
 */
public class Article {

    private String title;

    private String subtitle;

    private String sectionName;

    private String webUrl;

    private String thumbnailUrl;

    private String date;

    private String author;

    /**
     * Instantiates a new Article.
     *
     * @param title        the title
     * @param subtitle     the subtitle
     * @param sectionName  the section name
     * @param webUrl       the web url
     * @param thumbnailUrl the thumbnail url
     * @param date         the date
     * @param author       the author
     */
    public Article(String title, String subtitle, String sectionName, String webUrl, String thumbnailUrl, String date, String author) {
        this.title = title;
        this.subtitle = subtitle;
        this.sectionName = sectionName;
        this.webUrl = webUrl;
        this.thumbnailUrl = thumbnailUrl;
        this.date = date;
        this.author = author;
    }

    /**
     * Gets subtitle.
     *
     * @return the subtitle
     */
    public String getSubtitle() {
        return subtitle;
    }

    /**
     * Gets thumbnail url.
     *
     * @return the thumbnail url
     */
    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    /**
     * Gets title.
     *
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Gets section name.
     *
     * @return the section name
     */
    public String getSectionName() {
        return sectionName;
    }

    /**
     * Gets web url.
     *
     * @return the web url
     */
    public String getWebUrl() {
        return webUrl;
    }

    /**
     * Gets date.
     *
     * @return the date
     */
    public String getDate() {
        return date;
    }

    /**
     * Gets author.
     *
     * @return the author
     */
    public String getAuthor() {
        return author;
    }

    @Override
    public String toString() {
        return "Article{" +
                "title='" + title + '\'' +
                ", sectionName='" + sectionName + '\'' +
                ", webUrl='" + webUrl + '\'' +
                ", date='" + date + '\'' +
                ", author='" + author + '\'' +
                '}';
    }
}
