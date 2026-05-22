package com.rhythia.game.screens;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net.HttpMethods;
import com.badlogic.gdx.Net.HttpRequest;
import com.badlogic.gdx.Net.HttpResponse;
import com.badlogic.gdx.Net.HttpResponseListener;
import com.badlogic.gdx.net.HttpRequestBuilder;


public class NetworkManager {
   private static final String SERVER_URL = "http://127.0.0.1:5000";


   public static void submitScore(String songTitle, String playerName, int score) {
       HttpRequestBuilder requestBuilder = new HttpRequestBuilder();
       HttpRequest httpRequest = requestBuilder.newRequest()
               .method(HttpMethods.POST)
               .url(SERVER_URL + "/submit_score")
               .header("Content-Type", "application/json")
               .build();


       String payload = "{ \"song\": \"" + songTitle + "\", \"player\": \"" + playerName + "\", \"score\": " + score + " }";
       httpRequest.setContent(payload);


       Gdx.net.sendHttpRequest(httpRequest, new HttpResponseListener() {
           @Override
           public void handleHttpResponse(HttpResponse httpResponse) {
               Gdx.app.log("Network", "Score submitted successfully!");
           }
           @Override
           public void failed(Throwable t) {
               Gdx.app.error("Network", "Failed to submit score", t);
           }
           @Override
           public void cancelled() {}
       });
   }


   public static void fetchLeaderboard(String songTitle) {
       HttpRequestBuilder requestBuilder = new HttpRequestBuilder();
       HttpRequest httpRequest = requestBuilder.newRequest()
               .method(HttpMethods.GET)
               .url(SERVER_URL + "/leaderboard/" + songTitle)
               .build();


       Gdx.net.sendHttpRequest(httpRequest, new HttpResponseListener() {
           @Override
           public void handleHttpResponse(HttpResponse httpResponse) {
               String result = httpResponse.getResultAsString();
               Gdx.app.log("Leaderboard Data", result);
           }
           @Override
           public void failed(Throwable t) {
               Gdx.app.error("Network", "Failed to fetch leaderboard", t);
           }
           @Override
           public void cancelled() {}
       });
   }
}

