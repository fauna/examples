package com.faunadb.model.common;

import java.util.Optional;

public class PaginationOptions {
    private Optional<Integer> size;
    private Optional<String> before;
    private Optional<String> after;

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
