package edu.calpoly.jwmahone.firebaseverticalprototype;

import com.firebase.client.Firebase;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MountainPost implements Serializable {
    private String line;
    private String author;
    //private ArrayList<String> comments;
    //private List<String> comments;
    private int likes;

    public MountainPost() {

    }

    public MountainPost(String line, String author) {
        this.line = line;
        this.author = author;
        //this.comments = new ArrayList<>();
        this.likes = 0;
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

    public void setLine(String line) {
        this.line = line;
    }

    public void setAuthor(String auth) {
        this.author = auth;
    }

    public void like() {
        this.likes += 1;
    }

    public void dislike() {
        this.likes -= 1;
    }
/*
    public void addComment(String comment) {
        this.comments.add(comment);
    }

    public List<String> getComments() {
        return this.comments;
    }
*/
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

        return true;
    }
}
