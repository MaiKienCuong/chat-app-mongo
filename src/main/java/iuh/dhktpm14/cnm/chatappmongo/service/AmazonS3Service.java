package iuh.dhktpm14.cnm.chatappmongo.service;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

@Service
public class AmazonS3Service {
    private AmazonS3 s3client;

    @Value("${amazonProperties.endpointUrl}")
    private String endpointUrl;

    @Value("${amazonProperties.bucketName}")
    private String bucketName;

    @Value("${amazonProperties.accessKey}")
    private String accessKey;

    @Value("${amazonProperties.secretKey}")
    private String secretKey;

    @PostConstruct
    private void initializeAmazon() {
        AWSCredentials credentials = new BasicAWSCredentials(this.accessKey, this.secretKey);
        this.s3client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials)).build();
    }

    /**
     * chuyển từ multipart file sang file
     */
    private File convertMultiPartToFile(MultipartFile file) throws IOException {
        Objects.requireNonNull(file.getOriginalFilename());
        var convFile = new File(file.getOriginalFilename());
        try (var fos = new FileOutputStream(convFile)) {
            fos.write(file.getBytes());
        }
        return convFile;
    }

    /**
     * đặt tên file ngẫu nhiêu
     */
    private String generateFileName(MultipartFile multiPart) {
        if (multiPart.getOriginalFilename() != null)
            return new Date().getTime() + "_" + multiPart.getOriginalFilename().replace(" ", "_");
        return new Date().getTime() + "_" + UUID.randomUUID().toString().replace("-", "_");
    }

    /**
     * upload file lên S3
     */
    private void uploadFileTos3bucket(String fileName, File file) {
        s3client.putObject(new PutObjectRequest(bucketName, fileName, file)
                .withCannedAcl(CannedAccessControlList.PublicRead));
    }

    /**
     * gọi hàm upload và trả về link của file
     */
    public String uploadFile(MultipartFile multipartFile) {
        var fileUrl = "";
        try {
            var file = convertMultiPartToFile(multipartFile);
            String fileName = generateFileName(multipartFile);
            uploadFileTos3bucket(fileName, file);
            fileUrl = s3client.getUrl(bucketName, fileName).toString();
            file.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileUrl;
    }
}
