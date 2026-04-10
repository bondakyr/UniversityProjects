package cvut.fel.services;

import cvut.fel.dao.BookRepository;
import cvut.fel.model.Book;
import cvut.fel.services.strategy.BookStrategy;

public class BookServiceImpl {

    private BookRepository bookRepository;
    private BookStrategy bookStrategy;

    public BookServiceImpl() {
        bookRepository = new BookRepository();
    }

    public void setBookStrategy(BookStrategy bookStrategy) {
        this.bookStrategy = bookStrategy;
    }

    public Book getByBookId(int bookId) {
        return bookRepository.getById(bookId);
    }

    public Book getByBookName(String name) {
        return bookRepository.getByName(name);
    }

    public Book updateBook(Book book) {
        return bookStrategy.update(book);
    }
}
