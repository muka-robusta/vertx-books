package com.github.one2story.books;

import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainVerticle extends AbstractVerticle {

  private static final Logger logger = LoggerFactory.getLogger(MainVerticle.class.getName());
  private InMemoryBookStore store = new InMemoryBookStore();

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    logger.debug("Starting app... ");

    Router booksRouter = Router.router(vertx);
    booksRouter.route().handler(BodyHandler.create());

    // GET books
    getAll(booksRouter);

    // POST books
    createBook(booksRouter);

    // PUT /books/:isbn
    changeBook(booksRouter);

    // GET /books/:isbn
    getBook(booksRouter);

    // DELETE /books/:isbn
    deleteBook(booksRouter);

    // Error handler
    handleError(booksRouter);

    vertx.createHttpServer().requestHandler(booksRouter).listen(8888, http -> {
      if (http.succeeded()) {
        startPromise.complete();
        logger.info("HTTP server started on port 8888");
      } else {
        startPromise.fail(http.cause());
      }
    });
  }

  private void deleteBook(Router booksRouter) {
    booksRouter.delete("/books/:isbn").handler(request -> {
      final String isbn = request.pathParam("isbn");
      final Book deletedBook = store.delete(isbn);
      if(deletedBook == null)
      {
        request.response()
          .putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
          .setStatusCode(HttpResponseStatus.NOT_FOUND.code())
          .end(new JsonObject().put("deleting", "fail").encode());
      } else {
        request.response()
          .putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
          .end(new JsonObject().put("deleting", "ok").encode());

      }
    });
  }

  private void getBook(Router booksRouter) {
    booksRouter.get("/books/:isbn").handler(request -> {
      final String isbn = request.pathParam("isbn");
      final Book foundBook = store.get(isbn);
      request.response()
        .putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
        .end(JsonObject.mapFrom(foundBook).encode());
    });
  }

  private void handleError(Router booksRouter) {
    booksRouter.errorHandler(500, event -> {
      logger.error("Failed: " + event.failure());
      event.response()
        .putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
        .end(new JsonObject().put("error", event.failure().getMessage()).encode());
    });
  }

  private void changeBook(Router booksRouter) {
    booksRouter.put("/books/:isbn").handler(req -> {
      final String isbn = req.pathParam("isbn");
      final JsonObject requestBody = req.getBodyAsJson();
      final Book updatedBook = store.update(isbn, requestBody.mapTo(Book.class));
      req.response()
        .putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
        .end(JsonObject.mapFrom(updatedBook).encode());
    });
  }

  private void createBook(Router booksRouter) {
    booksRouter.post("/books").handler(req -> {
      // Read body
      final JsonObject requestBody = req.getBodyAsJson();
      logger.info("Request body: " + requestBody);
      // store
      store.add(requestBody.mapTo(Book.class));
      // return a response
      req.response()
        .putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
        .setStatusCode(HttpResponseStatus.CREATED.code())
        .end(requestBody.encode());
    });
  }

  private void getAll(Router booksRouter) {
    booksRouter.get("/books").handler(req -> {
      req.response()
        .putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
        .end(store.getAll().encode());
    });
  }

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(new MainVerticle());


  }
}
