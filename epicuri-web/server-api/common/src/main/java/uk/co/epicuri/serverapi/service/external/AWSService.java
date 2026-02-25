package uk.co.epicuri.serverapi.service.external;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Service
public class AWSService {

    private String staticGenericProfileImagesDir = "guest-app.restaurant-profile/";
    private String bucketName = "epicuri.static.images";

    private final AmazonS3 s3;

    public AWSService() {
        //don't want to put following in config file
        System.setProperty("aws.accessKeyId", "AKIAJPVDLWX7S6J7MMMA");
        System.setProperty("aws.secretKey", "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");

        s3 = AmazonS3ClientBuilder.standard().withRegion(Regions.EU_WEST_2).build();
    }

    public List<String> getGenericRestaurantImages() throws Exception {
        return getGenericRestaurantImages(staticGenericProfileImagesDir);
    }

    public List<String> getGenericRestaurantImages(String subfolder) throws Exception {
        ListObjectsV2Request req = new ListObjectsV2Request().withBucketName(bucketName).withPrefix(subfolder);
        ListObjectsV2Result listing = s3.listObjectsV2(req);
        List<String> urls = new ArrayList<>();
        for(S3ObjectSummary s3ObjectSummary : listing.getObjectSummaries()) {
            if(s3ObjectSummary.getKey() != null && s3ObjectSummary.getKey().endsWith(".jpg")) {
                URL url = s3.getUrl(bucketName, s3ObjectSummary.getKey());
                if (url != null) urls.add(url.toString());
            }
        }
        return urls;
    }
}
