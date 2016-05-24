package edu.calpoly.jwmahone.firebaseverticalprototype;


import java.io.Serializable;

public class MountainPost implements Serializable {
    private String line;
    private String author;
    private int likes;
    private String postID;

    public MountainPost() {

    }

    public MountainPost(String line, String author, String id) {
        this.line = line;
        this.author = author;
        this.likes = 0;
        this.postID = id;
    }

    public void setID(String id) {
        this.postID = id;
    }

    public String getID() {
        return this.postID;
    }

    public String getLine() {
        return this.line;
    }

    public String getAuthor() {
        return this.author;
    }

    public int getLikes() {
        return this.likes;
    }

    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (this.getClass() != o.getClass()) {
            return false;
        }
        if (!(this.line.equals(((MountainPost)o).line))) {
            return false;
        }
        if (!(this.author.equals(((MountainPost)o).author))) {
            return false;
        }
        if (!(this.likes == (((MountainPost)o).likes))) {
            return false;
        }
        if (!(this.postID.equals(((MountainPost)o).postID))) {
            return false;
        }

        return true;
    }
}