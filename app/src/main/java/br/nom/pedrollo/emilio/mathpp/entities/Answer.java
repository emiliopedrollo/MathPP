package br.nom.pedrollo.emilio.mathpp.entities;

public class Answer {
    private int id;
    private String title;
    private String text;
    private String author;
    private String authorType;
    private String authorIMEI;
    private int question;
    private int score;

    public Answer(){}

    public Answer(int id, String title, String text, String author, String authorType, int score){
        this.id = id;
        this.title = title;
        this.text = text;
        this.author = author;
        this.authorType = authorType;
        this.score = score;
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

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
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

    public int getQuestion() {
        return question;
    }

    public void setQuestion(int question) {
        this.question = question;
    }
}
