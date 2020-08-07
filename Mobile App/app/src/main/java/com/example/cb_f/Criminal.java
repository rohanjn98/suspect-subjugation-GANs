package com.example.cb_f;

public class Criminal {
    private String name, description;
    private int age;
    private double height;
    private String photo,Id;

    public Criminal() {
    }

    public Criminal(String name, int age, String photo, String description, double height, String Id) {
        this.name = name;
        this.age = age;
        this.photo = photo;
        this.Id = Id;
        this.description = description;
        this.height = height;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }
}
