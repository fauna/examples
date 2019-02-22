package com.faunadb.model.common;

import java.util.List;
import java.util.Optional;

public class Page<T> {
    private List<T> data;
    private Optional<String> before;
    private Optional<String> after;

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
