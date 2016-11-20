package br.nom.pedrollo.emilio.mathpp.entities;

public class Question extends Post{
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

    public int getAnswers() {
        return answers;
    }

    public void setAnswers(int answers) {
        this.answers = answers;
    }
}
