exports.createLoginRequest = (restaurantId, userName, password) => {
    return {
        "Username": userName,
        "Password": password,
        "RestaurantId": restaurantId
    }
};