package com.github.zhitron.jdbc_director;

/**
 * 事务操作异常类，用于封装与数据库事务处理相关的错误信息和行为。
 * 继承自 PersistenceException，适用于更具体的事务场景。
 *
 * @author zhitron
 */
public class TransactionException extends PersistenceException {
    private static final long serialVersionUID = 1L;

    /**
     * 构造一个新的 TransactionException 实例，不包含详细消息。
     */
    public TransactionException() {
        super();
    }

    /**
     * 构造一个新的 TransactionException 实例，并指定详细消息。
     *
     * @param message 错误描述信息
     */
    public TransactionException(String message) {
        super(message);
    }

    /**
     * 构造一个新的 TransactionException 实例，指定详细消息和根本原因。
     *
     * @param message 错误描述信息
     * @param cause   异常的根本原因
     */
    public TransactionException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 构造一个新的 TransactionException 实例，指定根本原因。
     *
     * @param cause 异常的根本原因
     */
    public TransactionException(Throwable cause) {
        super(cause);
    }

}
