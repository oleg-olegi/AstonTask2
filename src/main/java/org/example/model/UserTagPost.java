package org.example.model;

public class UserTagPost {
    private Long userId;
    private Long postId;
    private Long tagId;

    public UserTagPost() {
    }

    public UserTagPost(Long userId, Long postId, Long tagId) {
        this.userId = userId;
        this.postId = postId;
        this.tagId = tagId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public Long getTagId() {
        return tagId;
    }

    public void setTagId(Long tagId) {
        this.tagId = tagId;
    }
}
