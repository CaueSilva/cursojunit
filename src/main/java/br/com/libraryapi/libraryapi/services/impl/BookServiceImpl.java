package br.com.libraryapi.libraryapi.services.impl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.libraryapi.libraryapi.domain.Book;
import br.com.libraryapi.libraryapi.repositories.BookRepository;
import br.com.libraryapi.libraryapi.resources.exception.BusinessException;
import br.com.libraryapi.libraryapi.services.BookService;

@Service
public class BookServiceImpl implements BookService {
	
	@Autowired
	private BookRepository repo;
	
	public BookServiceImpl(BookRepository repo) {
		this.repo = repo;
	}
	
	@Override
	public Book save(Book book) {
		if(repo.existsByIsbn(book.getIsbn())) {
			throw new BusinessException("ISBN já existente!");
		}
		return repo.save(book);
	}

	@Override
	public Optional<Book> getById(Long id) {
		return Optional.empty();
	}
	
	@Override
	public void delete(Book book) {
		repo.delete(book);
	}
	
	@Override
	public Book update(Book book) {
		return null;
	}


}
