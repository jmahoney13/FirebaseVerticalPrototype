package edu.calpoly.jwmahone.firebaseverticalprototype;

public class Comment {

    private String comment;
    private String commentID;
    private String commentAuthor;

    public Comment() {

    }

    public Comment(String id, String comment, String author) {
        this.commentID = id;
        this.comment = comment;
        this.commentAuthor = author;
    }

    public String getComment() {
        return this.comment;
    }

    public String getCommentID() {
        return this.commentID;
    }

    public String getCommentAuthor() {
        return this.commentAuthor;
   }
}
