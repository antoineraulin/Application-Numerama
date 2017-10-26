package dev.antoineraulin.numerama;

public class MyObject {
    private String text;
    private String imageUrl;
    private String link;
    private String date;
    private String unite;
    private String description;
    private String cat;

    public MyObject(String text, String imageUrl, String link, String date, String unite, String description, String cat) {
        this.text = text;
        this.imageUrl = imageUrl;
        this.link = link;
        this.date = date;
        this.unite = unite;
        this.description = description;
        this.cat = cat;
    }

    public String getText() {
        return text;
    }
    public String getDescription() {
        return description;
    }
    public String getImageUrl() {
        return imageUrl;
    }
    public String getLink() {
        return link;
    }
    public String getDate() {
        return date;
    }
    public String getUnite() {
        return unite;
    }
    public String getCat() {
        return cat;
    }

    //getters & setters
}