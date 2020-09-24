package br.com.libraryapi.libraryapi.apiservice;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import br.com.libraryapi.libraryapi.domain.Book;
import br.com.libraryapi.libraryapi.repositories.BookRepository;
import br.com.libraryapi.libraryapi.resources.exception.BusinessException;
import br.com.libraryapi.libraryapi.services.BookService;
import br.com.libraryapi.libraryapi.services.impl.BookServiceImpl;

@ExtendWith(SpringExtension.class) //Criando contexto para rodar os testes
@ActiveProfiles("test") 
public class BookServiceTest {
	
	private BookService service;
	
	@MockBean
	private BookRepository repo;
	
	@BeforeEach
	public void setUp() {
		this.service = new BookServiceImpl(repo);
	}

	@Test
	@DisplayName("Deve salvar um livro")
	public void saveBookTest() {
		Book book = createNewBook();
		Mockito.when(service.save(book)).thenReturn(book);
		Mockito.when(repo.existsByIsbn(Mockito.anyString())).thenReturn(false);
		
		Book savedBook = service.save(book);
		assertThat(savedBook.getId()).isNotNull();
		assertThat(savedBook.getAuthor()).isEqualTo(book.getAuthor());
		assertThat(savedBook.getTitle()).isEqualTo(book.getTitle());
		assertThat(savedBook.getIsbn()).isEqualTo(book.getIsbn());
	}
	
	@Test
	@DisplayName("Deve dar erro ao tentar inserir livro com ISBN duplicado")
	public void createBookWithDuplicatedIsbn() {
		Book book = createNewBook();
		Mockito.when(repo.existsByIsbn(Mockito.anyString())).thenReturn(true);
		Throwable t = Assertions.catchThrowable(() -> service.save(book));
		assertThat(t).isInstanceOf(BusinessException.class).hasMessage("ISBN já existente!");
		Mockito.verify(repo, Mockito.never()).save(book);
	}

	private Book createNewBook() {
		return new Book((long)123,"Aventuras","Fulano","123456");
	}
	
}
