package br.nom.pedrollo.emilio.mathpp.entities;

public class Category {
    private String name;
    private String imgSrc;

    public Category(String name){
        this.name = name;
    }

    public Category(String name, String imgSrc){
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
}
