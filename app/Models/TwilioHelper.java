package Models;

import com.twilio.sdk.Twilio;
import com.twilio.sdk.creator.api.v2010.account.MessageCreator;
import com.twilio.sdk.reader.api.v2010.account.CallReader;
import com.twilio.sdk.reader.api.v2010.account.MessageReader;
import com.twilio.sdk.reader.api.v2010.account.call.RecordingReader;
import com.twilio.sdk.resource.ResourceSet;
import com.twilio.sdk.resource.api.v2010.account.Call;
import com.twilio.sdk.resource.api.v2010.account.Message;
import com.twilio.sdk.resource.api.v2010.account.call.Recording;
import com.twilio.sdk.type.PhoneNumber;
import com.twilio.twiml.Hangup;
import com.twilio.twiml.Record;
import com.twilio.twiml.Say;
import com.twilio.twiml.Trim;
import com.twilio.twiml.TwiMLException;
import com.twilio.twiml.VoiceResponse;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TwilioHelper {
    public static final String ACCOUNT_SID = "AC475..................................05a552af";
    public static final String AUTH_TOKEN =  "062958.................................84bae98c";

    static{
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
    }

    public static void sendMessage(String toNumber, String fromNumber, String messageBody) {

        Message message = new MessageCreator(
                ACCOUNT_SID,
                new PhoneNumber(toNumber), // To number
                new PhoneNumber(fromNumber), // From Twilio number
                messageBody
        ).execute();
    }

    public static List<ReceivedAd> receiveMessages(String toPhoneNumber) {

        ResourceSet<Message> messages = new MessageReader(
                ACCOUNT_SID
        ).byTo(new PhoneNumber(toPhoneNumber)).execute();

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm");
        List<ReceivedAd> receivedAds = new ArrayList<>();
        Iterator<Message> messagesIter = messages.iterator();

        while(messagesIter.hasNext()){
            Message message = messagesIter.next();
            ReceivedAd ad = new ReceivedAd();
            ad.date = sdf.format(message.getDateSent().toDate());
            ad.name = Utils.getUserDetailsFromNum(message.getFrom().getRawNumber().toString()).name;
            ad.number = message.getFrom().toString();
            ad.messageBody = message.getBody();

            receivedAds.add(ad);
        }
        return receivedAds;
    }

    public static List<ReceivedCall> receiveRecordings(String toPhoneNumber){
        ResourceSet<Call> calls = new CallReader(
                ACCOUNT_SID
        ).byTo(new PhoneNumber(toPhoneNumber)).execute();
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm");
        List<ReceivedCall> receivedCalls = new ArrayList<>();
        Iterator<Call> callsIter = calls.iterator();

        while(callsIter.hasNext()){
            Call call = callsIter.next();
            ReceivedCall receivedCall = new ReceivedCall();
            receivedCall.date = sdf.format(call.getStartTime().toDate());
            receivedCall.name = Utils.getUserDetailsFromNum(call.getFrom().toString()).name;
            receivedCall.number = call.getFrom().toString();
            ResourceSet<Recording> recordings= new RecordingReader(call.getAccountSid(), call.getSid()).execute();
            Iterator<Recording> recording = recordings.iterator();
            if(recording.hasNext())
                receivedCall.recording = "https://api.twilio.com" + recording.next().getUri().replaceAll(".json",".wav");
            receivedCalls.add(receivedCall);
        }
        return receivedCalls;
    }


    public static String triggerRecording() throws TwiMLException{
        Say say = new Say.Builder("Please leave your message. Press any button when you are done.")
                .voice(Say.Voice.WOMAN)
                .build();

        Record record = new Record.Builder()
                .playBeep(true)
                .trim(Trim.TRIM_SILENCE)
                .maxLength(60)
                .timeout(10)
                .build();

        VoiceResponse response = new VoiceResponse.Builder()
                .say(say)
                .record(record)
                .build();

        return response.toXml();

    }

    public static String endRecording() throws TwiMLException{
        Say say = new Say.Builder("Thanks for your message. I will get back to you soon")
                .voice(Say.Voice.WOMAN)
                .build();

        Hangup hangup = new Hangup();

        VoiceResponse response = new VoiceResponse.Builder()
                .say(say)
                .hangup(hangup)
                .build();

        return response.toXml();
    }
}
