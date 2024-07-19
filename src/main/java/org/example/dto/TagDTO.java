package org.example.dto;

public class TagDTO {
    private Long id;
    private String name;

    public TagDTO(long l, String tag) {
        this.id = l;
        this.name = tag;
    }

    public TagDTO() {

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
