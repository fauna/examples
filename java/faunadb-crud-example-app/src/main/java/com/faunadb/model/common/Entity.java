package com.faunadb.model.common;

/**
 * <p>Base class for implementing Domain Entities.</p>
 *
 * <p>An Entity is defined by its unique Identity.</p>
 */
public abstract class Entity {

    protected String id;

    public String getId() {
        return id;
    }

    protected void setId(String id) {
        this.id = id;
    }

}
