package cvut.fel.facade;

import cvut.fel.facade.dto.BookDto;
import cvut.fel.model.Author;
import cvut.fel.model.Book;
import cvut.fel.model.Library;
import cvut.fel.dao.AuthorRepository;
import cvut.fel.dao.BookRepository;
import cvut.fel.dao.LibraryRepository;

public class BookFacadeImpl implements BookFacade {

    private BookRepository bookRepository;
    private AuthorRepository authorRepository;
    private LibraryRepository libraryRepository;

    public BookFacadeImpl() {
        bookRepository = new BookRepository();
        authorRepository = new AuthorRepository();
        libraryRepository = new LibraryRepository();
    }

    @Override
    public BookDto getByBookId(int id) {
        Book book = bookRepository.getById(id);
        Author author = authorRepository.getByBookId(id);
        Library library = libraryRepository.getByBookId(id);

        BookDto bookDto = new BookDto(book.getName(), author.getFirstname(), library.getName());

        return bookDto;
    }
}