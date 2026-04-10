package cvut.fel.facade;

import cvut.fel.facade.dto.BookDto;

public interface BookFacade {

    BookDto getByBookId(int id);
}
