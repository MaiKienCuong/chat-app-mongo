package iuh.dhktpm14.cnm.chatappmongo.rest;

import io.swagger.annotations.ApiOperation;
import iuh.dhktpm14.cnm.chatappmongo.entity.User;
import iuh.dhktpm14.cnm.chatappmongo.payload.MessageResponse;
import iuh.dhktpm14.cnm.chatappmongo.service.AmazonS3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/*
upload 1 hoặc nhiều file trả về list url
 */
@RestController
@RequestMapping("api/file")
@CrossOrigin("${spring.security.cross_origin}")
public class UploadFileRest {

    @Autowired
    private AmazonS3Service s3Service;

    @Autowired
    private MessageSource messageSource;

    @PostMapping(value = "/", consumes = "application/x-www-form-urlencoded")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("gui file")
    public ResponseEntity<?> uploadFiles(@ApiIgnore @AuthenticationPrincipal User user,
                                         @RequestParam List<MultipartFile> files,
                                         Locale locale) {
        return uploadFileMobile(user, files, locale);
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("gui file")
    public ResponseEntity<?> uploadFileMobile(@ApiIgnore @AuthenticationPrincipal User user,
                                              @RequestParam List<MultipartFile> files,
                                              Locale locale) {
        List<String> urls = new ArrayList<>();
        String message;
        if (files == null) {
            message = messageSource.getMessage("file_is_null", null, locale);
            return ResponseEntity.badRequest().body(new MessageResponse(message));
        }
        if (files.isEmpty()) {
            message = messageSource.getMessage("file_is_empty", null, locale);
            return ResponseEntity.badRequest().body(new MessageResponse(message));
        }
        for (MultipartFile file : files) {
            String newImageUrl = s3Service.uploadFile(file);
            urls.add(newImageUrl);
        }
        return ResponseEntity.ok(urls);
    }

}
