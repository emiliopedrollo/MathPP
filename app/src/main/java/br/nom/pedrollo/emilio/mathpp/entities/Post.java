package br.nom.pedrollo.emilio.mathpp.entities;

public abstract class Post {
    protected int id;
    protected String title;
    protected String text;
    protected String author;
    protected String authorType;
    protected String authorIMEI;

    public Post(){}

    public Post(int id, String title, String text, String author, String authorType){
        this.id = id;
        this.title = title;
        this.text = text;
        this.author = author;
        this.authorType = authorType;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthorType() {
        return authorType;
    }

    public void setAuthorType(String authorType) {
        this.authorType = authorType;
    }

    public String getAuthorIMEI() {
        return authorIMEI;
    }

    public void setAuthorIMEI(String authorIMEI) {
        this.authorIMEI = authorIMEI;
    }

    public int getId() {
        return id;
    }
}
