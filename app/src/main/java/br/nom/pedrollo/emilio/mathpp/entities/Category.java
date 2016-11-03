package br.nom.pedrollo.emilio.mathpp.entities;

public class Category {
    private int id;
    private String name;
    private String imgSrc;

    public Category(int id, String name){
        this.id = id;
        this.name = name;
    }

    public Category(int id, String name, String imgSrc){
        this.id = id;
        this.name = name;
        this.imgSrc = imgSrc;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setImgSrc(String imgSrc) {
        this.imgSrc = imgSrc;
    }

    public String getName() {
        return name;
    }

    public String getImgSrc() {
        return imgSrc;
    }

    public int getId() {
        return id;
    }
}
