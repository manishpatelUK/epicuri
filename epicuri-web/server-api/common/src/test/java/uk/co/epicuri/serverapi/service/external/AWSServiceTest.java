package uk.co.epicuri.serverapi.service.external;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.co.epicuri.serverapi.repository.BaseIT;

import java.util.List;

import static org.junit.Assert.*;

public class AWSServiceTest extends BaseIT {

    @Autowired
    private AWSService awsService;

    @Test
    public void testGetGenericRestaurantImages() throws Exception {
        List<String> strings = awsService.getGenericRestaurantImages();
        assertTrue(strings.size() > 0);
    }
}