package edu.calpoly.jwmahone.firebaseverticalprototype;

public class Comment {

    private String comment;
    private String commentID;

    public Comment() {

    }

    public Comment(String id, String comment) {
        this.commentID = id;
        this.comment = comment;
    }

    public String getComment() {
        return this.comment;
    }

    public String getCommentID() {
        return this.commentID;
    }
}
