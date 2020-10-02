package br.com.libraryapi.libraryapi.apiservice;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

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
	
	@Test
	@DisplayName("Deve recuperar livro pelo ID")
	public void mustReturnBookById() {
		Long id = 1l;
		Book book = createNewBook();
		book.setId(id);
		Mockito.when(repo.findById(id)).thenReturn(Optional.of(book));
		
		Optional<Book> foundBook = service.getById(id);
		
		assertThat(foundBook.isPresent()).isTrue();
		assertThat(foundBook.get().getId()).isEqualTo(id);
		assertThat(foundBook.get().getAuthor()).isEqualTo(book.getAuthor());
		assertThat(foundBook.get().getTitle()).isEqualTo(book.getTitle());
		assertThat(foundBook.get().getIsbn()).isEqualTo(book.getIsbn());
	}
	
	@Test
	@DisplayName("Deve retornar vazio quando ID do livro não existir")
	public void bookNotFoundById() {
		Long id = 1l;
		
		Mockito.when(repo.findById(id)).thenReturn(Optional.empty());
		
		Optional<Book> foundBook = service.getById(id);
		
		assertThat(foundBook.isPresent()).isFalse();
	}
	
	@Test
	@DisplayName("Deve deletar um livro informando ID")
	public void deleteBookById() {
		Book book = createNewBook();
		
		org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> service.delete(book));
		
		Mockito.verify(repo, Mockito.times(1)).delete(book);
	}
	
	@Test
	@DisplayName("Deve dar exceção ao tentar deletar um livro nulo")
	public void deleteNullBook() {
		Book book = new Book();
		
		org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> service.delete(book));
		
		Mockito.verify(repo, Mockito.never()).delete(book);
	}
	
	@Test
	@DisplayName("Deve dar exceção ao tentar atualizar um livro nulo")
	public void updateNullBook() {
		Book book = new Book();
		
		org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> service.update(book));
		
		Mockito.verify(repo, Mockito.never()).save(book);
	}
	
	@Test
	@DisplayName("Deve atualizar um livro com sucesso")
	public void updateBookById() {
		Long id = 123l;
		
		Book updatingBook = new Book();
		updatingBook.setId(id);
		
		Book updatedBook = createNewBook();
		updatedBook.setId(id);
		
		Mockito.when(repo.save(updatingBook)).thenReturn(updatedBook);
		
		Book book = service.update(updatingBook);
		
		assertThat(book.getId()).isEqualTo(id);
		assertThat(book.getAuthor()).isEqualTo(updatedBook.getAuthor());
		assertThat(book.getTitle()).isEqualTo(updatedBook.getTitle());
		assertThat(book.getIsbn()).isEqualTo(updatedBook.getIsbn());
	}

	private Book createNewBook() {
		return new Book(123l,"Aventuras","Fulano","123456");
	}
	
}
