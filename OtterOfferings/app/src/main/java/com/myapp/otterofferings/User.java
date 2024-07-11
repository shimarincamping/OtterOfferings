package com.myapp.otterofferings;

public class User {
    private String userID;

    // Fields
    private String fullName;
    private String email;
    private String hashedPassword;
    private String savedAddress;
    private String userLocation;
    private String phoneNumber;

    private String profileImageURL;
    private String loginToken;
    // private ArrayList<Item> cart;

    // Constructor

    public User() {}

    public User(String fullName, String email, String password) {
        this.userID = DebugScreen.sanitiseEmail(email);
        this.fullName = fullName;
        this.email = email;
        this.hashedPassword = DebugScreen.hashPassword(password);
        this.userLocation = "";
        this.savedAddress = "";
        this.phoneNumber = "";
        this.profileImageURL = "https://i.imgur.com/Gfuj3iL.jpeg";
        this.loginToken = "";
        // this.cart = new ArrayList<>();
    }

    // Getters and setters
    public String getUserID() {
        return userID;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }

    public String getSavedAddress() {
        return savedAddress;
    }

    public void setSavedAddress(String savedAddress) {
        this.savedAddress = savedAddress;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getLoginToken() {
        return loginToken;
    }

    public void setLoginToken(String loginToken) {
        this.loginToken = loginToken;
    }

    public String getUserLocation() {
        return userLocation;
    }

    public void setUserLocation(String userLocation) {
        this.userLocation = userLocation;
    }

    public String getProfileImageURL() {
        return profileImageURL;
    }

    public void setProfileImageURL(String profileImageURL) {
        this.profileImageURL = profileImageURL;
    }

    /*
    public ArrayList<Item> getCart() {
        return cart;
    }

    private void setCart(ArrayList<Item> cart) {
        this.cart = cart;
    }

    public void addToCart(Item i) {
        cart.add(i);
    }

    public void removeFromCart(int cartIndex) {
        cart.remove(cartIndex);
    }

    public void clearCart() {
        cart.clear();
    }
     */
}
