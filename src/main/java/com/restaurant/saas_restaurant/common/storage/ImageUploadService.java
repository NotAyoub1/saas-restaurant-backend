package com.restaurant.saas_restaurant.common.storage;

import org.springframework.web.multipart.MultipartFile;

public interface ImageUploadService {
    //Stores the given image and returns a publicly resolvable URL/path
    //that can be persisted on an entity (e.g. Product.imageUrl).
    String upload(MultipartFile file, String subfolder);
    //Deletes a previously uploaded image, given the URL/path returned by upload().Safe
    //to call even if the file no longer exists.
    void delete(String imageUrl);
}
