package specs;

import configs.Config;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import models.LoginUserRequest;
import requests.skeleton.Endpoint;
import requests.skeleton.HttpHeaders;
import requests.skeleton.requesters.CrudRequester;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Готовые шаблоны для настройки запросов (RequestSpecification)
public class RequestSpecs {

    // Для кеширования токенов
    private static final Map<String, String> authHeaders = new HashMap<>();

    private RequestSpecs() {
    }

    private static RequestSpecBuilder defaultRequestBuilder() {
        return new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addFilters(List.of(
                        new RequestLoggingFilter(),
                        new ResponseLoggingFilter()
                ))
                .setBaseUri(Config.getProperty("server") + Config.getProperty("apiVersion"));
    }

    public static RequestSpecification unauthSpec() {
        return defaultRequestBuilder().build();
    }

    public static RequestSpecification adminSpec() {
        return authAsUser(
                Config.getProperty("admin.username"),
                Config.getProperty("admin.password"));
    }

    public static RequestSpecification authAsUser(String username, String password) {
        return defaultRequestBuilder()
                .addHeader(
                        HttpHeaders.AUTHORIZATION,
                        getAuthHeader(username, password))
                .build();
    }


    public static String getAuthHeader(String username, String password) {
        return authHeaders.computeIfAbsent(username, key -> {
                    LoginUserRequest request = LoginUserRequest.builder()
                            .username(username)
                            .password(password)
                            .build();

                    String authHeader = new CrudRequester(
                            RequestSpecs.unauthSpec(),
                            Endpoint.LOGIN,
                            ResponseSpecs.requestReturnsOK())
                            .post(request)
                            .extract()
                            .header(HttpHeaders.AUTHORIZATION);

                    if (authHeader == null || authHeader.isBlank()) {
                        throw new IllegalStateException(
                                "Authorization header is missing from login response");
                    }

                    return authHeader;
                }
        );

    }

}
