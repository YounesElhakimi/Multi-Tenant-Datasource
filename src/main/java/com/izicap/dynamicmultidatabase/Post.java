package com.izicap.dynamicmultidatabase;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@ApiModel(description = "Post entity representing a blog post or article in the system")
public class Post {
    
    @Id
    @ApiModelProperty(value = "Unique identifier for the post", example = "1", required = true)
    private long id;
    
    @ApiModelProperty(value = "Name or title of the post", example = "My First Post", required = true)
    private String name;

    public Post() {
    }

    public Post(String name) {
        this.name = name;
    }

    public Post(long id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String toString() {
        return "Post{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}