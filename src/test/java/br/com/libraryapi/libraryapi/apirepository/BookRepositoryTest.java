package br.com.libraryapi.libraryapi.apirepository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import br.com.libraryapi.libraryapi.domain.Book;
import br.com.libraryapi.libraryapi.repositories.BookRepository;

@ExtendWith(SpringExtension.class) 
@ActiveProfiles("test") 
@DataJpaTest
public class BookRepositoryTest {
	
	@Autowired
	TestEntityManager entityManager;
	
	@Autowired
	BookRepository repo;
	
	@Test
	@DisplayName("Deve retornar verdadeiro quando existir um livro na base com o ISBN informado")
	public void mustReturnTrueWhenExistsBookWithIsbnInformed() {
		String isbn = "123";
		Book book = new Book(null,"Aventuras","Fulano","123");
		entityManager.persist(book);
		boolean exists = repo.existsByIsbn(isbn);
		assertThat(exists).isTrue();
	}
	
	@Test
	@DisplayName("Deve retornar falso quando não existir livro na base com ISBN informado")
	public void mustReturnFalseWhenBookNoutFoundByIsbn() {
		String isbn = "123";
		boolean exists = repo.existsByIsbn(isbn);
		assertThat(exists).isFalse();
	}
	
	@Test
	@DisplayName("Deve retornar um livro por ID")
	public void findById(){
		Long id = 1l;
		Book book = new Book(null,"Aventuras","Fulano","123");
		entityManager.persist(book);
		Optional<Book> foundBook = repo.findById(id);
		
		assertThat(foundBook.isPresent()).isTrue();
	}
	
	
}
