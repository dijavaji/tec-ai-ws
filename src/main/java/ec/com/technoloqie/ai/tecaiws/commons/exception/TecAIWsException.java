package ec.com.technoloqie.ai.tecaiws.commons.exception;

public class TecAIWsException extends RuntimeException{
	
	public TecAIWsException() {
        super();
    }
    
	public TecAIWsException (String msg, Throwable nested) {
        super(msg, nested);
    }
    
	public TecAIWsException (String message) {
        super(message);
    }
    
	public TecAIWsException(Throwable nested) {
        super(nested);
	}
	
	private static final long serialVersionUID = 1L;
	
}