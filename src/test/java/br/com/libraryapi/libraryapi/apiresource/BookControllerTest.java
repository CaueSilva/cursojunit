package br.com.libraryapi.libraryapi.apiresource;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.libraryapi.libraryapi.domain.Book;
import br.com.libraryapi.libraryapi.dto.BookDTO;
import br.com.libraryapi.libraryapi.resources.exception.BusinessException;
import br.com.libraryapi.libraryapi.services.impl.BookServiceImpl;

@ExtendWith(SpringExtension.class) //Criando contexto para rodar os testes
@ActiveProfiles("test") //Profile ativo (application.properties)
@WebMvcTest //Configura um objeto para receber as requisições
@AutoConfigureMockMvc 
public class BookControllerTest {
	
	private static final String BOOK_API = "/api/books";
	
	@Autowired
	MockMvc mvc;
	
	@MockBean
	BookServiceImpl service;
	
	@Test
	@DisplayName("Deve criar um livro com sucesso")
	public void createBookTest() throws Exception {
		
		BookDTO dto = createBook();
		Book savedBook = new Book((long)1,"Meu livro","Autor","123456");
		
		BDDMockito.given(service.save(Mockito.any(Book.class))).willReturn(savedBook);
		String json = new ObjectMapper().writeValueAsString(dto);
		
		//Criando um mock no endpoint BOOK_API que irá receber um JSON
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
			.post(BOOK_API)
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.APPLICATION_JSON)
			.content(json);
		
		//Criando a requisição e esperando status 201 - CREATED, ID do objeto criando
		mvc.perform(request)
		.andExpect(status().isCreated())
		.andExpect(jsonPath("id").isNotEmpty())
		.andExpect(jsonPath("title").value(dto.getTitle()))
		.andExpect(jsonPath("author").value(dto.getAuthor()))
		.andExpect(jsonPath("isbn").value(dto.getIsbn()));
		
	}
	
	@Test
	@DisplayName("Deve lançar erro de validação quando não há dados suficientes para criação")
	public void createInvalidBookTest() throws Exception {
		String json = new ObjectMapper().writeValueAsString(new Book());
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
				.post(BOOK_API)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(json);
		mvc.perform(request).andExpect(status().isBadRequest()).andExpect(jsonPath("errors",Matchers.hasSize(3)));
	}
	
	@Test
	@DisplayName("Deve dar erro ao tentar inserir livro com ISBN duplicado")
	public void createBookWithDuplicatedIsbn() throws Exception {
		BookDTO dto = createBook();
		String json = new ObjectMapper().writeValueAsString(dto),
				mensagem = "ISBN já existente!";
		
		BDDMockito.given(service.save(Mockito.any(Book.class))).willThrow(new BusinessException(mensagem));
		
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
				.post(BOOK_API)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(json);
		
		mvc.perform(request)
			.andExpect(status()
			.isBadRequest())
			.andExpect(jsonPath("errors",Matchers.hasSize(1)))
			.andExpect(jsonPath("errors[0]").value(mensagem));
	}

	private BookDTO createBook() {
		return new BookDTO((long)1,"Meu livro","Autor","123456");
	}
	
}
