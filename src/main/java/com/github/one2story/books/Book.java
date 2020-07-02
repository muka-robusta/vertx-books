package com.github.one2story.books;

public class Book {
  private long isbn;
  private String title;

  public Book(){
    // default constructor

  }

  public Book(long isbn, String title) {
    this.isbn = isbn;
    this.title = title;
  }

  public long getIsbn() {
    return isbn;
  }

  public void setIsbn(long isbn) {
    this.isbn = isbn;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }
}
