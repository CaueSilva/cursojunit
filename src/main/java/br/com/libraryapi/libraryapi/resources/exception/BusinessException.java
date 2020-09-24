package br.com.libraryapi.libraryapi.resources.exception;

public class BusinessException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public BusinessException() {
		
	}
	
	public BusinessException(String message) {
		super(message);
	}
	
}
