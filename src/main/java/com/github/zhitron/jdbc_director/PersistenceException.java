package com.github.zhitron.jdbc_director;

/**
 * 持久化操作异常类，用于封装与数据持久化相关的错误信息和行为。
 *
 * @author zhitron
 */
public class PersistenceException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 构造一个新的 PersistenceException 实例，不包含详细消息。
     */
    public PersistenceException() {
        super();
    }

    /**
     * 构造一个新的 PersistenceException 实例，并指定详细消息。
     *
     * @param message 错误描述信息
     */
    public PersistenceException(String message) {
        super(message);
    }

    /**
     * 构造一个新的 PersistenceException 实例，指定详细消息和根本原因。
     *
     * @param message 错误描述信息
     * @param cause   异常的根本原因
     */
    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 构造一个新的 PersistenceException 实例，指定根本原因。
     *
     * @param cause 异常的根本原因
     */
    public PersistenceException(Throwable cause) {
        super(cause);
    }
}
