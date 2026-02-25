package uk.co.epicuri.serverapi.common.pojo.model.restaurant;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import uk.co.epicuri.serverapi.db.TableNames;
import uk.co.epicuri.serverapi.common.pojo.model.Deletable;

/**
 * Created by manish
 */
@Document(collection = TableNames.RESTAURANT_IMAGES)
public class RestaurantImage extends Deletable {
    @Indexed
    private String restaurantId;

    private byte[] image;
    private RestaurantImageType imageType;

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public RestaurantImageType getImageType() {
        return imageType;
    }

    public void setImageType(RestaurantImageType imageType) {
        this.imageType = imageType;
    }
}
