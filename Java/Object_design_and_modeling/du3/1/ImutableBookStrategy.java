package cvut.fel.services.strategy;

import cvut.fel.dao.BookRepository;
import cvut.fel.model.Book;

public class ImutableBookStrategy implements BookStrategy {

    private BookRepository bookRepository;
    private static int currentId = 4; // začínáme od 4, protože 3 je již obsazené

    public ImutableBookStrategy() {
        this.bookRepository = new BookRepository();
    }

    public ImutableBookStrategy(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    public Book update(Book book) {
        Book newBook = new Book(currentId++, book.getISBN(), book.getName());
        bookRepository.save(newBook);
        return newBook;
    }
}