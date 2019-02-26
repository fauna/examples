package com.faunadb.model.common;

import java.util.List;
import java.util.Optional;

/**
 * It represents a sequence of elements along with
 * cursors indicators to move backwards and forwards.
 *
 * @param <T> the type of data within the sequence
 */
public class Page<T> {
    private List<T> data;
    private Optional<String> before;
    private Optional<String> after;

    /**
     * It creates a new Page with the given parameters
     *
     * @param data the data within the current sequence
     * @param before the before cursor – if any, it contains the Id of the previous record before the current sequence
     * @param after the after cursor – if any, it contains the Id of the next record after the current sequence
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
