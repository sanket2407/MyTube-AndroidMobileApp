package com.example.sanket.connectionutil;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by sanket on 10/17/15.
 */
public class ConnectionUtil {

    /**
     *
     * @param url - String
     * @param reqParam refer POST method
     * @param accessToken - String
     * @return ArrayList<String> 0: responseBody
     * 								 1: responseCode
     */
    public static ArrayList<String> getResponse(String url, String reqParam, String accessToken)
    {
        StringBuilder response = null;
        StringBuilder urlBuilder =  null;
        BufferedReader in = null;
        HttpsURLConnection con = null;
        ArrayList<String> responseList =  new ArrayList<String>();
        int responseCode = -1;
        try
        {
            response = new StringBuilder();

            urlBuilder =  new StringBuilder();
            urlBuilder.append(url);
            urlBuilder.append("?").append(reqParam);
            urlBuilder.append("&access_token=").append(accessToken);

            URL urlObj = new URL(urlBuilder.toString());

            con = (HttpsURLConnection)urlObj.openConnection();
            con.setRequestMethod("GET");
            con.setDoInput(true);
            con.setDoOutput(false);

            con.setRequestProperty("Authorization: Bearer",accessToken);
            in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            responseCode = con.getResponseCode();
            String inputLine = "";
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

        } catch (Exception e)
        {
            in = new BufferedReader(
                    new InputStreamReader(con.getErrorStream()));
            String inputLine = "";

            try
            {
                while ((inputLine = in.readLine()) != null)
                {
                    response.append(inputLine);
                }
            }
            catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            if(responseCode == -1)
            {

            }
            else
            {


                System.out.println(response.toString());
            }
        }

        finally
        {
            try {
                in.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        responseList.add(response.toString());
        responseList.add(String.valueOf(responseCode));
        return responseList;

    }

    /**
     *
     * @param url - String
     * @param reqParam - String e.g. part=snippet
     * @param reqBody JSON
     * @param accessToken - String
     * @return responseCode - String
     */
    public static String postRequest(String url, String reqParam, String reqBody, String accessToken, Boolean isPost)
    {
        StringBuilder response = null;
        StringBuilder urlBuilder =  null;
        BufferedReader in = null;
        HttpsURLConnection con = null;
        int responseCode = -1;
        DataOutputStream wr = null;
        try
        {
            response = new StringBuilder();

            urlBuilder =  new StringBuilder();
            urlBuilder.append(url);
            urlBuilder.append("?").append(reqParam);
            urlBuilder.append("&access_token=").append(accessToken);

            URL urlObj = new URL(urlBuilder.toString());

            con = (HttpsURLConnection)urlObj.openConnection();
            if (isPost) {

                con.setRequestMethod("POST");
            } else {

                con.setRequestMethod("DELETE");
            }
            con.setDoInput(true);
            con.setDoOutput(false);

            con.setRequestProperty("Authorization: Bearer",accessToken);
            con.setRequestProperty("Content-Type", "application/json");

            // Send post request
            wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(reqBody);

            responseCode = con.getResponseCode();

        } catch (Exception e)
        {
            if(responseCode == -1)
            {
                System.out.println("");
            }
            else
            {
                in = new BufferedReader(
                        new InputStreamReader(con.getErrorStream()));
                String inputLine = "";

                try
                {
                    while ((inputLine = in.readLine()) != null)
                    {
                        response.append(inputLine);
                    }
                }
                catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                finally
                {
                    try {
                        in.close();
                    } catch (IOException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                }

                System.out.println(response.toString());
            }

        }

        finally
        {
            try {
                wr.flush();
                wr.close();

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return String.valueOf(responseCode);

    }
}