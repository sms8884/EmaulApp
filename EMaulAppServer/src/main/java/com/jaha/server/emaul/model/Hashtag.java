package com.jaha.server.emaul.model;

import javax.persistence.*;

import org.springframework.core.style.ToStringCreator;

@Entity
@Table(name = "board_post_hashtag")
public class Hashtag {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;

    public Hashtag() {

    }

    @Override
    public String toString() {
        return new ToStringCreator(this)
                .append("id", id)
                .append("name", name)
                .toString();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
