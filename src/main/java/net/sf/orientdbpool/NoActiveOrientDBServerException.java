package net.sf.orientdbpool;

/**
 * 没有可用的节点
 * @author Dody
 *
 */
public class NoActiveOrientDBServerException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public NoActiveOrientDBServerException(String msg) {
		super(msg);
	}

}
