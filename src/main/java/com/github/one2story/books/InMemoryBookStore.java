package com.github.one2story.books;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.Map;

public class InMemoryBookStore {
  private Map<Long, Book> books = new HashMap<>();
  InMemoryBookStore() {
    books.put(1L, new Book(1L, "Vertx in action"));
    books.put(2L, new Book(2L, "Building microservices"));
  }

  public JsonArray getAll(){
    JsonArray all = new JsonArray();
    books.values().forEach(book -> {
      all.add(JsonObject.mapFrom(book));
    });

    return all;
  }

  public void add(Book entry) {
    books.put(entry.getIsbn(), entry);
  }

  public Book update(String isbn, Book entry) {
    Long key = Long.parseLong(isbn);
    if(key != entry.getIsbn()) {
      throw new IllegalArgumentException("ISBN does not match!");
    }
    books.put(key, entry);
    return entry;
  }

  public Book delete(String isbn) {
    Long key = Long.parseLong(isbn);
    return books.remove(key);
  }

  public Book get(String isbn)
  {
    Long key = Long.parseLong(isbn);
    return books.get(key);
  }
}
