package br.com.libraryapi.libraryapi.resources;

import javax.validation.Valid;

import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import br.com.libraryapi.libraryapi.domain.Book;
import br.com.libraryapi.libraryapi.dto.BookDTO;
import br.com.libraryapi.libraryapi.resources.exception.ApiErrors;
import br.com.libraryapi.libraryapi.resources.exception.BusinessException;
import br.com.libraryapi.libraryapi.services.impl.BookServiceImpl;

@RestController
@RequestMapping("/api/books")
public class BookResource {
	
	private BookServiceImpl service;
	
	private ModelMapper modelMapper;
	
	public BookResource(BookServiceImpl service, ModelMapper modelMapper) {
		this.service = service;
		this.modelMapper = modelMapper;
	}
	
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public BookDTO create(@Valid @RequestBody BookDTO bookDto) {
		//"fromDTO"
		Book book = modelMapper.map(bookDto, Book.class);
		Book savedBook = service.save(book);
		return modelMapper.map(savedBook,BookDTO.class);
	}
	
	@RequestMapping(method=RequestMethod.GET, value="/{id}")
	public BookDTO get(@PathVariable Long id) {
		return service
				.getById(id)
				.map(book -> modelMapper.map(book, BookDTO.class))
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
	}
	
	@RequestMapping(method=RequestMethod.DELETE,value="/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) {
		Book book = service.getById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		service.delete(book);
	}
	
	@RequestMapping(method=RequestMethod.PUT,value="/{id}")
	public BookDTO update(@PathVariable Long id, BookDTO dto) {
		return service.getById(id).map(book -> {
			book.setAuthor(dto.getAuthor());
			book.setTitle(dto.getTitle());
			book = service.update(book);
			return modelMapper.map(book, BookDTO.class);
		}).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
	}
	
	
	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ApiErrors handlerValidationException(MethodArgumentNotValidException e) {
		BindingResult res = e.getBindingResult();
		return new ApiErrors(res);
	}
	
	@ExceptionHandler(BusinessException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ApiErrors handlerBusinessException(BusinessException e) {
		return new ApiErrors(e);
	}
	
}
