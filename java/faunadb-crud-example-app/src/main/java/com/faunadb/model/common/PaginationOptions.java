package com.faunadb.model.common;

import java.util.Optional;

/**
 * It defines parameters for looking up a result using pagination.
 */
public class PaginationOptions {
    private Optional<Integer> size;
    private Optional<String> before;
    private Optional<String> after;

    /**
     * It creates a new PaginationOptions object with the given parameters.
     *
     * @param size the max number of elements to return in the requested Page
     * @param before the before cursor – if any, it indicates to return the previous Page of results before this Id (exclusive)
     * @param after the after cursor –  if any, it indicates to return the next Page of results after this Id (inclusive)
     */
    public PaginationOptions(Optional<Integer> size, Optional<String> before, Optional<String> after) {
        this.size = size;
        this.before = before;
        this.after = after;
    }

    public Optional<Integer> getSize() {
        return size;
    }

    public void setSize(Optional<Integer> size) {
        this.size = size;
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
