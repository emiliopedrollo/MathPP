package br.nom.pedrollo.emilio.mathpp.entities;

public class Question {
    private int id;
    private String title;
    private String text;
    private String author;
    private String authorType;
    private String authorIMEI;
    private int answers;

    public Question(){
    }

    public Question(int id, String title, String text, String author, String authorType, int answers){
        this.id = id;
        this.title = title;
        this.text = text;
        this.author = author;
        this.authorType = authorType;
        this.answers = answers;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getAnswers() {
        return answers;
    }

    public void setAnswers(int answers) {
        this.answers = answers;
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
}
