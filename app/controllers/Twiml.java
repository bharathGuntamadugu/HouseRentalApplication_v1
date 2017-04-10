package controllers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

public class Twiml extends Controller {

    public static Result call() throws IOException {
        response().setContentType("text/xml");
        return ok(buildTwiml());
    }

    public static Result recordCallback()
    {
        System.out.println(request().body().asXml());
        return ok();
    }

    public static Result action()
    {
        System.out.println("Action:\n" + request().body().asText());
        return ok();
    }

    public static String buildTwiml() throws IOException {
        StringBuilder sb = new StringBuilder();
        String sCurrentLine;
        try (BufferedReader br = new BufferedReader(new FileReader("response.xml"))){

            while (( sCurrentLine = br.readLine()) != null) {
                sb.append(sCurrentLine);
            }
        }
        return sb.toString();
    }
}
