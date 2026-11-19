package com.kiet.library_management.controller;

import com.kiet.library_management.model.Book;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/books")
public class BookController {
    private final ConcurrentHashMap<Integer, Book> books = new ConcurrentHashMap<>();
    private final AtomicInteger nextId = new AtomicInteger(1);

    @PostMapping
    public ResponseEntity<Book> addBook(@RequestBody Book book) {
        Integer requestedId = book.getId();
        int id = requestedId == null ? nextId.getAndIncrement() : requestedId;
        Book created = new Book(id, book.getTitle(), book.getAuthor(), book.getCategory(), book.getAvailableCopies());

        if (books.putIfAbsent(id, created) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        nextId.updateAndGet(current -> Math.max(current, id + 1));
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public List<Book> getAllBooks() {
        return books.values().stream().sorted(Comparator.comparing(Book::getId)).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable Integer id) {
        Book book = books.get(id);
        return book == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(book);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Book> updateBook(@PathVariable Integer id, @RequestBody Book details) {
        Book existing = books.get(id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }

        Book updated = new Book(id, details.getTitle(), details.getAuthor(), details.getCategory(), details.getAvailableCopies());
        books.put(id, updated);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Integer id) {
        return books.remove(id) == null
                ? ResponseEntity.notFound().build()
                : ResponseEntity.noContent().build();
    }
}
