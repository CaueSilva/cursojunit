package br.com.libraryapi.libraryapi.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.libraryapi.libraryapi.domain.Book;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
	
	boolean existsByIsbn(String isbn);

}
