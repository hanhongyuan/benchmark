package com.example.benchmark.wsTest;

import org.apache.commons.io.FileUtils;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.concurrent.ExecutionException;

@Component
public class WSManager {

    private static final int BUFFER_SIZE = 8192;
    private StompSession stompSession;
    private MySessionHandler sessionHandler;
    private TestProperties props;

    /**
     * Constructor that create the session, establishes the connection and subscribes it
     */
    public WSManager() {

//        String url = props.getWs();
        String url = "ws://127.0.0.1:8020/events/websocket";
        WebSocketClient webSocketClient = new StandardWebSocketClient();
        WebSocketStompClient stompClient = new WebSocketStompClient(webSocketClient);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
        stompClient.setTaskScheduler(new ConcurrentTaskScheduler());
        this.sessionHandler = new MySessionHandler();
        this.stompSession = null;
        try {
            stompSession = stompClient.connect(url, sessionHandler).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        stompSession.subscribe("/topic/upload", sessionHandler);
    }

    public void movieListUpload(MovieList movieList) {


        for (int i = 0; i < movieList.getMovieResources().size(); i++) {
            MovieInfo movieInfo = movieList.getMovieResources().get(i);
            uploadRequest(movieInfo, "/home/davide/Desktop/Bambino_Freddo.mp4");
        }
    }

    public void echoRequest() {

        stompSession.subscribe("/topic/echo", sessionHandler);
        stompSession.send("/app/echo", "{\"name\":\"Helloooo\"}".getBytes());
        stompSession.disconnect();
    }

    public void uploadRequest(MovieInfo movieInfo, String uploadPath) {

        String id = movieInfo.getMovieId();
        File f = new File(uploadPath);
        sessionHandler.startTimer(id);

        String path = f.getAbsolutePath();
        byte[] buffer = new byte[BUFFER_SIZE];

        try (InputStream input = FileUtils.openInputStream(f)) {
            System.out.println(f.length());
            final long totalBytesCount = input.available();
            int partCount = Math.round(totalBytesCount / BUFFER_SIZE);
            System.out.println("partcount= " + partCount + " totalBytes= " + totalBytesCount);
            UploadRequest uploadRequest = null;

            int partId = 0;
            while (partId <= partCount) {
                final int readBytesCount = input.read(buffer, 0, BUFFER_SIZE);
                String s = Base64.getEncoder().encodeToString(buffer);
                uploadRequest = new UploadRequest(id, s, partId, partCount);
                stompSession.send("/app/upload", uploadRequest);
                partId++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

