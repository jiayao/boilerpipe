package com.kohlschutter.boilerpipe;

import com.kohlschutter.boilerpipe.sax.HTMLDocument;
import com.kohlschutter.boilerpipe.sax.HTMLFetcher;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.integration.ClientAndServer;

import java.io.IOException;
import java.net.URL;

import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@RunWith(MockitoJUnitRunner.class)
public class HTMLFetcherTest {
    private ClientAndServer mockServer;

    @Before
    public void setUp() {
        mockServer = startClientAndServer(1080);
        new MockServerClient("localhost", 1080)
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/redirect1")
                )
                .respond(
                        response()
                                .withStatusCode(302)
                                .withHeader(
                                        "Location", "http://localhost:1080/redirect2"
                                )
                );
        new MockServerClient("localhost", 1080)
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/redirect2")
                )
                .respond(
                        response()
                                .withStatusCode(302)
                                .withHeader(
                                        "Location", "http://localhost:1080/dst"
                                )
                );
        new MockServerClient("localhost", 1080)
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/dst")
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withHeader(
                                        "Content-Type", "text/html"
                                )
                                .withBody("hello world")
                );
    }

    @After
    public void stopProxy() {
        mockServer.stop();
    }

    @Test
    public void fetchSmoke() throws IOException {
        HTMLDocument doc = HTMLFetcher.fetch(new URL(
                "http://localhost:1080/dst"));
        String content = new String(doc.getData());
        Assert.assertEquals("hello world", content);
    }

    @Test
    public void fetchRedirectTest() throws IOException {
        HTMLDocument doc = HTMLFetcher.fetch(new URL(
                "http://localhost:1080/redirect2"));
        String content = new String(doc.getData());
        Assert.assertEquals("hello world", content);
    }

    @Test
    public void fetchRedirectX2Test() throws IOException {
        HTMLDocument doc = HTMLFetcher.fetch(new URL(
                "http://localhost:1080/redirect1"));
        String content = new String(doc.getData());
        Assert.assertEquals("hello world", content);
    }
}
