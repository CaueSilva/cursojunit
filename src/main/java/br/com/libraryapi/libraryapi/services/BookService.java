package br.com.libraryapi.libraryapi.services;

import java.util.Optional;

import br.com.libraryapi.libraryapi.domain.Book;

public interface BookService {
	
	public Book save(Book book);
	
	public Optional<Book> getById(Long id);
	
	public void delete(Book book);

	Book update(Book book);
	
}
