package controllers;

import java.util.ArrayList;
import java.util.List;
import Models.Ad;
import Models.ListAd;
import Models.RentAd;
import Models.UserDetails;
import Models.UserDetailsWithPassword;
import Models.Utils;
import play.data.DynamicForm;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;

public class Application extends Controller {
    public static String secretKey = "qkjll5@2md3gs5Q@";

    public static Result app() {
        String email = Utils.getDecryptedStringFromBase64String(session("connected"), secretKey);
        if(email!=null) {
            UserDetails user = Utils.getUserDetails(email);
            String imageSrc = "assets/images/"+user.name+".jpg";
            String toastMessage = null;
            if(flash("Success")!=null)
                toastMessage = flash("Success");

            List<Ad> ads = new ArrayList<>();
            List<ListAd> listedAds = Utils.getListedAds(email);
            for (ListAd listAd:listedAds) {
                Ad ad = new Ad();
                ad.rentAProperty = false;
                ad.location = listAd.location;
                ad.rent = (int)listAd.rent;
                ad.bedrooms = listAd.numBedrooms;
                ad.date = listAd.dateCreated;
                ad.userDetails = Utils.getUserDetails(email);
                ads.add(ad);
            }
            List<RentAd> rentAds = Utils.getRentalAds(email);
            for (RentAd rentAd:rentAds) {
                Ad ad = new Ad();
                ad.rentAProperty = true;
                ad.location = rentAd.location;
                ad.minRent = (int)rentAd.minRent;
                ad.maxRent = (int)rentAd.maxRent;
                ad.bedrooms = rentAd.numBedrooms;
                ad.date = rentAd.dateCreated;
                ad.userDetails = Utils.getUserDetails(email);
                ads.add(ad);
            }

            return ok(views.html.main.render(null,
                    views.html.myAds.render(ads, "Success"),
                    views.html.rentAProperty.render(),
                    views.html.listAProperty.render(),
                    views.html.logout.render(),
                    views.html.userPane.render(user, imageSrc),
                    toastMessage,
                    false));
        } else {
            return redirect("/login");
        }
    }

    public static Result login() {
        return ok(views.html.main.render(views.html.login.render(), null, null, null, null, null, null, true));
    }

    public static Result loginSubmit(){
        DynamicForm form = Form.form().bindFromRequest();
        String username = form.get("username");
        String password = form.get("password");
        if(Utils.authenticate(username, password, secretKey))
        {
            session("connected", Utils.getEncodedString(Utils.encryptString(username, secretKey)));
            return redirect("/app");
        }
        return redirect("/login");
    }

    public static Result logout(){
        session().remove("connected");
        return redirect("/login");
    }

    public static Result signup() {
        return ok(views.html.main.render(views.html.signup.render(), null, null, null, null, null, null, true));
    }

    public static Result signupSubmit(){
        DynamicForm form = Form.form().bindFromRequest();
        String email = form.get("email");
        String name = form.get("name");
        String password = form.get("password");
        String confirmPassword = form.get("confirmPassword");
        String phone = form.get("phone");
        String toast = null;
        // ToDo: Do all form field validations.
        // I did only one validation.
        if (!password.equals(confirmPassword)) {
            toast = "Passwords don't match";
            return ok(views.html.main.render(views.html.signup.render(), null, null, null, null, null, toast, true));
        }
        try {
            byte[] encryptedPassword = Utils.encryptString(password, secretKey);
            UserDetailsWithPassword userDetails= new UserDetailsWithPassword(email, encryptedPassword, name, phone);
            Utils.setUserDetails(userDetails);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return redirect("/login");
//        return ok(views.html.main.render(views.html.login.render(), null, null, null, null, null, null, true));
    }

    public static Result rentAPropertySubmit(){
        DynamicForm form = Form.form().bindFromRequest();
        String location = form.get("locationDropdown");
        int minRent =  Integer.parseInt(form.get("minRent"));
        int maxRent = Integer.parseInt(form.get("maxRent"));
        int bedrooms = Integer.parseInt(form.get("noOfBedroomsDropDown"));

        RentAd ad = new RentAd();

        ad.email = Utils.getDecryptedStringFromBase64String(session("connected"), secretKey);
        ad.location = location;
        ad.minRent = minRent;
        ad.maxRent = maxRent;
        ad.numBedrooms = bedrooms;

        Utils.saveRentAd(ad);
        flash("success", "Success! We will notify you if any listing matches your requirement");
        return redirect("/app");
    }

    public static Result listAPropertySubmit(){
        DynamicForm form = Form.form().bindFromRequest();
        String location = form.get("locationDropdown");
        int rent =  Integer.parseInt(form.get("rent"));
        int numBedrooms = Integer.parseInt(form.get("noOfBedroomsDropDown"));

        ListAd ad = new ListAd();

        ad.location = location;
        ad.rent = rent;
        ad.numBedrooms = numBedrooms;
        ad.email = Utils.getDecryptedStringFromBase64String(session("connected"), secretKey);

        Utils.saveListAd(ad);
        Utils.NotifyValidUsers(ad);

        flash("success", "Success! Your listing has been posted");
        return redirect("/app");
    }

    public static Result myAds(){
        DynamicForm form = Form.form().bindFromRequest();
        String location = form.get("id");
        return redirect("/app");
    }
}
