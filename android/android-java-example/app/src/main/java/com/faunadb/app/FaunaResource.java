package com.faunadb.app;

import java.util.List;

public class FaunaResource {

    private final String name;
    private final List<String> content;

    public FaunaResource(String name, List<String> content) {
        this.name = name;
        this.content = content;
    }

    public String getName() {
        return name;
    }

    public List<String> getContent() {
        return content;
    }
}
