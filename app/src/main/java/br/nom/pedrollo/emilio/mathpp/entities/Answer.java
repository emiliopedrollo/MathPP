package br.nom.pedrollo.emilio.mathpp.entities;

public class Answer {
    private String title;
    private String text;
    private String author;
    private int upvotes;
    private int downvotes;

    public Answer(String author, String title, String text){
        this.author = author;
        this.title = title;
        this.text = text;
        upvotes = 0;
        downvotes = 0;
    }

    public int getDownvotes() {
        return downvotes;
    }

    public void setDownvotes(int downvotes) {
        this.downvotes = downvotes;
    }

    public int getUpvotes() {
        return upvotes;
    }

    public void setUpvotes(int upvotes) {
        this.upvotes = upvotes;
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
}
