package br.nom.pedrollo.emilio.mathpp.entities;

public class Answer extends Post {


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

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getQuestion() {
        return question;
    }

    public void setQuestion(int question) {
        this.question = question;
    }
}
