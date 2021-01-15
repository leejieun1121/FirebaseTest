package com.example.firebaseauthtest;

import java.util.HashMap;
import java.util.Map;

public class ImageDTO {

    public String imageUrl;
    public String imageName;
    public String title;
    public String description;
    public String uid;
    public String userId;
    public int likeCount = 0;
    public Map<String, Boolean> imgLike = new HashMap<>();



}
