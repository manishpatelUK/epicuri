package uk.co.epicuri.serverapi.common.pojo.model.session;

/**
 * Created by manish
 */
public enum SessionIdStrategy {
    NONE, //uses the id of the saved session
    NUMERIC_ASCENDING,
    HASH
}
