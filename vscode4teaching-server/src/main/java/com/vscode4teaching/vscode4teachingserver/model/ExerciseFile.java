package com.vscode4teaching.vscode4teachingserver.model;

import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonView;
import com.vscode4teaching.vscode4teachingserver.model.views.FileViews;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
public class ExerciseFile {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonView(FileViews.GeneralView.class)
    private Long id;

    @JsonView(FileViews.GeneralView.class)
    private String path;

    // If null the file is a template
    @ManyToOne
    @JsonView(FileViews.GeneralView.class)
    private User owner;

    @CreationTimestamp
    @JsonView(FileViews.GeneralView.class)
    private LocalDateTime createDateTime;

    @UpdateTimestamp
    @JsonView(FileViews.GeneralView.class)
    private LocalDateTime updateDateTime;

    public ExerciseFile(String path) {
        this.path = path;
    }

    public ExerciseFile(String path, User owner) {
        this.path = path;
        this.owner = owner;
    }

    public ExerciseFile() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public LocalDateTime getCreateDateTime() {
        return createDateTime;
    }

    public LocalDateTime getUpdateDateTime() {
        return updateDateTime;
    }

}