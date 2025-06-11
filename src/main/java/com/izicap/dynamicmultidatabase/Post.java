package com.izicap.dynamicmultidatabase;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Column;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

@Entity
@ApiModel(description = "Post entity representing a blog post or article in the system")
public class Post {
    
    @Id
    @ApiModelProperty(value = "Unique identifier for the post", example = "1", required = true)
    private long id;
    
    @Column(nullable = false)
    @ApiModelProperty(value = "Name or title of the post", example = "My First Post", required = true)
    private String name;
    
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    @ApiModelProperty(value = "Creation timestamp", example = "2024-01-15T10:30:00Z")
    private Date createdAt;
    
    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    @ApiModelProperty(value = "Last update timestamp", example = "2024-01-15T10:30:00Z")
    private Date updatedAt;

    public Post() {
    }

    public Post(String name) {
        this.name = name;
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    public Post(long id, String name) {
        this.id = id;
        this.name = name;
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    @Override
    public String toString() {
        return "Post{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

    // Getters and Setters
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

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}