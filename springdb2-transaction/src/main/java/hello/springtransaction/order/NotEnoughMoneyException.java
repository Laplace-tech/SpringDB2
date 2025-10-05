package hello.springtransaction.order;

public class NotEnoughMoneyException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public NotEnoughMoneyException(String message) {
		super(message);
	}
	
}
