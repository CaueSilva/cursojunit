package br.com.libraryapi.libraryapi.apiresource;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

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
public class BookResourceTest {
	
	private static final String BOOK_API = "/api/books";
	
	@Autowired
	MockMvc mvc;
	
	@MockBean
	BookServiceImpl service;
	
	@Test
	@DisplayName("Deve criar um livro com sucesso")
	public void createBookTest() throws Exception {
		
		BookDTO dto = createBookDTO();
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
		BookDTO dto = createBookDTO();
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
	
	@Test
	@DisplayName("Deve recuperar os detalhes de um livro específico")
	public void mustRecoverTheDetailsOfABook() throws Exception {
		Long id = 1l;
		Book book = new Book(id,"Meu livro","Autor","123456");
		BDDMockito.given(service.getById(id)).willReturn(Optional.of(book));
		
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
				.get(BOOK_API.concat("/"+id))
				.accept(MediaType.APPLICATION_JSON);
		
		mvc.perform(request)
			.andExpect(status().isOk())
			.andExpect(jsonPath("id").value(id))
			.andExpect(jsonPath("title").value(book.getTitle()))
			.andExpect(jsonPath("author").value(book.getAuthor()))
			.andExpect(jsonPath("isbn").value(book.getIsbn()));
	}
	
	@Test
	@DisplayName("Deve retornar NOT FOUND quando o livro procurado não existir")
	public void bookNotFoundTest() throws Exception {
		
		BDDMockito.given(service.getById(Mockito.anyLong())).willReturn(Optional.empty());
		
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
				.get(BOOK_API.concat("/"+1))
				.accept(MediaType.APPLICATION_JSON);
		
		mvc.perform(request)
		.andExpect(status().isNotFound());
	}
	
	@Test
	@DisplayName("Deve deletar um livro")
	public void mustDeleteABook() throws Exception {
		Book book = new Book(1l,"Meu livro","Autor","123456");
		BDDMockito.given(service.getById(Mockito.anyLong())).willReturn(Optional.of(book));

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
				.delete(BOOK_API.concat("/"+1))
				.accept(MediaType.APPLICATION_JSON);

		mvc.perform(request)
		.andExpect(status().isNoContent());
	}
	
	@Test
	@DisplayName("Deve retornar NOT FOUND ao deletar um livro")
	public void mustReturnNotFoundWhenDeleteABook() throws Exception {
		BDDMockito.given(service.getById(Mockito.anyLong())).willReturn(Optional.empty());

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
				.delete(BOOK_API.concat("/"+1))
				.accept(MediaType.APPLICATION_JSON);

		mvc.perform(request)
		.andExpect(status().isNotFound());
	}
	
	@Test
	@DisplayName("Deve atualizar livro")
	public void mustUpdateBook() throws Exception {
		Long id = 1l;
		String json = new ObjectMapper().writeValueAsString(createBookDTO());
		
		Book updatingBook = new Book(id,"Meu livro","Autor","123456");
		Book updatedBook = new Book(id, "Algum título", "Algum autor", "123456");
		
		BDDMockito.given(service.getById(id)).willReturn(Optional.of(updatingBook));
		BDDMockito.given(service.update(updatingBook)).willReturn(updatedBook);

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
				.put(BOOK_API.concat("/"+1))
				.content(json)
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON);

		mvc.perform(request)
		.andExpect(status().isOk())
		.andExpect(jsonPath("id").value(updatedBook.getId()))
		.andExpect(jsonPath("title").value("Algum título"))
		.andExpect(jsonPath("author").value("Algum autor"))
		.andExpect(jsonPath("isbn").value(updatedBook.getIsbn()));
	}
	
	@Test
	@DisplayName("Deve dar NOT FOUND ao atualizar livro inexistente")
	public void mustUpdateInexistentBook() throws Exception {
		String json = new ObjectMapper().writeValueAsString(createBookDTO());
		BDDMockito.given(service.getById(Mockito.anyLong())).willReturn(Optional.empty());

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
				.put(BOOK_API.concat("/"+1))
				.content(json)
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON);

		mvc.perform(request)
		.andExpect(status().isNotFound());
	}

	private BookDTO createBookDTO() {
		return new BookDTO(1l,"Meu livro","Autor","123456");
	}
	
}
