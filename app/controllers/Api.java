package controllers;

import com.twilio.twiml.TwiMLException;
import java.util.List;
import Models.ReceivedAd;
import Models.ReceivedCall;
import Models.TwilioHelper;
import Models.UserDetails;
import Models.Utils;
import play.mvc.Controller;
import play.mvc.Result;
import play.twirl.api.Html;
import scala.App;

public class Api extends Controller {

    public static Result sms(String number)
    {
        List<ReceivedAd> receivedAds = TwilioHelper.receiveMessages(number);
        return mainRender(views.html.receivedSms.render(receivedAds));
    }

    public static Result smsTrigger() throws TwiMLException
    {
        response().setContentType("text/xml");
        return ok();
    }

    public static Result voice(String number)
    {
        List<ReceivedCall> receivedRecordings = TwilioHelper.receiveRecordings(number);
        return mainRender(views.html.receivedCalls.render(receivedRecordings));
    }

    public static Result recordTrigger() throws TwiMLException
    {
        response().setContentType("text/xml");
        return ok(TwilioHelper.triggerRecording());
    }
    public static Result recordingEnd() throws TwiMLException
    {
        response().setContentType("text/xml");
        return ok(TwilioHelper.endRecording());
    }


    public static Result mainRender(Html content){
        String email = Utils.getDecryptedStringFromBase64String(session("connected"), Application.secretKey);
        UserDetails user = Utils.getUserDetails(email);
        String imageSrc = "../assets/images/"+user.name+".jpg";
        return ok(views.html.main.render(null,
                content,
                views.html.rentAProperty.render(),
                views.html.listAProperty.render(),
                views.html.logout.render(),
                views.html.userPane.render(user, imageSrc),
                null,
                false));
    }
}
