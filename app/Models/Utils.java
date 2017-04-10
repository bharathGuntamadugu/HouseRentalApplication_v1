package Models;

import java.security.Key;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import play.db.DB;

public class Utils{

    private static Connection connection = DB.getConnection();
    private static Cipher cipher = null;

    public static boolean authenticate(String email, String password, String secretKey) {
        UserDetailsWithPassword userDetails = getUserDetails(email);
        if(userDetails == null) {
            return false;
        }
        else {
            return (userDetails.email.equalsIgnoreCase(email) && decryptString(userDetails.password, secretKey).equals(password)) ? true : false;
        }
    }

    public static void NotifyValidUsers(ListAd ad)
    {
        List<UserDetails> rentAdUserDetails = getRentAdUserDetails(ad);
        String messageBody = "DreamHouse! New listing posted.\n" +
                String.format("Location:%s\nRent:%s\nBedrooms:%s\n",
                        ad.location, ad.rent, ad.numBedrooms)+
                "Reply or call this number to know more about this ad";

        String fromPhoneNum = getUserDetails(ad.email).phone;
        for(UserDetails user : rentAdUserDetails) {
            TwilioHelper.sendMessage(user.phone, fromPhoneNum, messageBody);
        }
    }

    public static byte[] encryptString(String text, String secretKey) {
        if (text == null) {
            return null;
        }
        // Create key and cipher
        Key aesKey = new SecretKeySpec(secretKey.getBytes(), "AES");
        try {
            if (cipher == null) {
                cipher = Cipher.getInstance("AES");
            }            // encrypt the text
            cipher.init(Cipher.ENCRYPT_MODE, aesKey);
            return cipher.doFinal(text.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String decryptString(byte[] encryptedBytes, String secretKey) {
        if (encryptedBytes == null) {
            return null;
        }
        // Create key and cipher
        Key aesKey = new SecretKeySpec(secretKey.getBytes(), "AES");
        try {
            if (cipher == null) {
                 cipher = Cipher.getInstance("AES");
            }
            // decrypt the text
            cipher.init(Cipher.DECRYPT_MODE, aesKey);
            return new String(cipher.doFinal(encryptedBytes));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getEncodedString(byte[] encryptedBytes) {
        if (encryptedBytes == null) {
            return null;
        }
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    public static byte[] getDecodedBytes(String base64String) {
        if (base64String == null || base64String.isEmpty()) {
            return null;
        }
        return Base64.getDecoder().decode(base64String);
    }

    public static String getDecryptedStringFromBase64String(String base64String, String secretKey) {
        byte[] encryptedBytes = getDecodedBytes(base64String);
        return decryptString(encryptedBytes, secretKey);
    }

    public static void setUserDetails(UserDetailsWithPassword userDetails) {
        try {
            String createQuery = String.format("insert into UserDetails (email, name, password, phone, dateCreated, dateUpdated) " +
                    "values ('%s', '%s', ?, '%s', now(), now())", userDetails.email, userDetails.name, userDetails.phone);
            PreparedStatement preparedStatement = connection.prepareStatement(createQuery);
            preparedStatement.setBytes(1, userDetails.password);
            preparedStatement.execute();
        } catch (SQLException e) {
             e.printStackTrace();
        }
    }

    public static UserDetailsWithPassword getUserDetails(String email) {
        try {
            String getQuery = String.format("select * from UserDetails where email = '%s' limit 1", email);
            PreparedStatement preparedStatement = connection.prepareStatement(getQuery);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                String emailId = resultSet.getString("email");
                String name = resultSet.getString("name");
                String phone = resultSet.getString("phone");
                byte[] password = resultSet.getBytes("password");

                return new UserDetailsWithPassword(emailId, password, name, phone);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static UserDetails getUserDetailsFromNum(String phoneNum){
        try {
            String getQuery = String.format("select * from UserDetails where phone = '%s' limit 1", phoneNum);
            PreparedStatement preparedStatement = connection.prepareStatement(getQuery);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                String emailId = resultSet.getString("email");
                String name = resultSet.getString("name");
                String phone = resultSet.getString("phone");

                return new UserDetails(emailId, name, phone);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void saveListAd(ListAd ad) {
        try {
            String query = String.format("insert into ListedAds (email, location, rent, numBedrooms, dateCreated, dateUpdated) values " +
                            "('%s', '%s', ? ,?, now(), now())", ad.email, ad.location);
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setFloat(1, ad.rent);
            preparedStatement.setFloat(2, ad.numBedrooms);
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<ListAd> getListedAds(String email) {
        List<ListAd> listedAds = new ArrayList<>();
        try {
            String getQuery = String.format("select * from ListedAds where email = '%s'", email);
            PreparedStatement preparedStatement = connection.prepareStatement(getQuery);
            ResultSet resultSet = preparedStatement.executeQuery();
            while(resultSet.next()) {
                ListAd listAd = new ListAd();
                listAd.email = resultSet.getString("email");
                listAd.location = resultSet.getString("location");
                listAd.rent = Float.parseFloat(resultSet.getString("rent"));
                listAd.numBedrooms = Integer.parseInt(resultSet.getString("numBedrooms"));
                listAd.dateCreated = resultSet.getString("dateCreated");
                listedAds.add(listAd);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return listedAds;
    }

    public static List<RentAd> getRentalAds(String email) {
        List<RentAd> rentAds = new ArrayList<>();
        try {
            String getQuery = String.format("select * from RentAds where email = '%s'", email);
            PreparedStatement preparedStatement = connection.prepareStatement(getQuery);
            ResultSet resultSet = preparedStatement.executeQuery();
            while(resultSet.next()) {
                RentAd rentAd = new RentAd();
                rentAd.email = resultSet.getString("email");
                rentAd.location = resultSet.getString("location");
                rentAd.minRent = Float.parseFloat(resultSet.getString("minRent"));
                rentAd.maxRent = Float.parseFloat(resultSet.getString("maxRent"));
                rentAd.numBedrooms = Integer.parseInt(resultSet.getString("numBedrooms"));
                rentAd.dateCreated = resultSet.getString("dateCreated");
                rentAds.add(rentAd);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rentAds;
    }

    public static void saveRentAd(RentAd ad) {
        try {
            String getQuery = String.format("insert into RentAds (email, location, minRent, maxRent, numBedrooms, dateCreated, dateUpdated) values " +
                    "('%s', '%s', ? , ?, ?, now(), now())", ad.email, ad.location);
            PreparedStatement preparedStatement = connection.prepareStatement(getQuery);
            preparedStatement.setFloat(1, ad.minRent);
            preparedStatement.setFloat(2, ad.maxRent);
            preparedStatement.setFloat(3, ad.numBedrooms);
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<UserDetails> getRentAdUserDetails(ListAd ad) {
        List<UserDetails> userList = new ArrayList<>();
        try {
            String getQuery = String.format("select distinct a.email, a.name, a.phone from UserDetails as a " +
                    "inner join RentAds as b on a.email = b.email where b.location = '%s' and b.minRent <= %s and b.maxRent >= %s and " +
                    "numBedrooms = %s", ad.location, ad.rent, ad.rent, ad.numBedrooms);
            PreparedStatement preparedStatement = connection.prepareStatement(getQuery);
            ResultSet resultSet = preparedStatement.executeQuery();
            while(resultSet.next()) {
                String emailId = resultSet.getString("email");
                String name = resultSet.getString("name");
                String phone = resultSet.getString("phone");
                userList.add(new UserDetails(emailId, name, phone));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userList;
    }
}

