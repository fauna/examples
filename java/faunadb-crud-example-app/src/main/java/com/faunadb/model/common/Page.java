package com.faunadb.model.common;

import java.util.List;
import java.util.Optional;

/**
 * <p>It represents a sequence of elements along with
 * cursors indicators to move backwards and forwards.</p>
 *
 * <p>Note that this class does not mean to model Fauna's Page
 * object, but to be a database agnostic page abstraction.</p>
 *
 * <p>One of the main differences with Fauna's Page is that the
 * latter offers a broader flexibility when it comes to the definition
 * of which elements can be contained within the data and cursors fields.
 * Those elements, which are defined during the creation of the Index
 * from which the Page is being derived, has been constrained in this
 * example in order to keep a simple design. Nonetheless, be aware that
 * Fauna's Indexes and Pages can be used to build more complex results</p>
 *
 * @see <a https://docs.fauna.com/fauna/current/reference/queryapi/write/createindex">Index</a>
 * @see <a href="https://docs.fauna.com/fauna/current/reference/queryapi/types.html#page">Page</a>
 *
 * @param <T> the type of data within the sequence
 */
public class Page<T> {

    private List<T> data;
    private Optional<String> before;
    private Optional<String> after;

    /**
     * It creates a new Page with the given parameters.
     *
     * @param data the data within the current sequence
     * @param before the before cursor – if any, it contains the Id of the previous record before the current sequence of data
     * @param after the after cursor – if any, it contains the Id of the next record after the current sequence of data
     */
    public Page(List<T> data, Optional<String> before, Optional<String> after) {
        this.data = data;
        this.before = before;
        this.after = after;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public Optional<String> getBefore() {
        return before;
    }

    public void setBefore(Optional<String> before) {
        this.before = before;
    }

    public Optional<String> getAfter() {
        return after;
    }

    public void setAfter(Optional<String> after) {
        this.after = after;
    }
}
