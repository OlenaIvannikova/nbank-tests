package common.extensions;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import common.annotations.FraudCheckMock;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

// Это только для последовательного выполнения тестов с отдельным экземпляром extension на тест
public class FraudCheckWireMockExtension implements BeforeEachCallback, AfterEachCallback {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private WireMockServer wireMockServer;

  @Override
  public void beforeEach(ExtensionContext context) throws Exception {
    FraudCheckMock config = findMockConfig(context);

    if (config == null) {
      return;
    }

    startWireMock(config);
    configureStub(config);
  }

  @Override
  public void afterEach(ExtensionContext context) throws Exception {
    if (wireMockServer != null) {
      wireMockServer.stop();
      wireMockServer = null;
    }
  }

  // Возвращает адрес WireMock, который затем можно передать приложению, обращающемуся к Fraud Check
  // service
  public String getBaseUrl() {
    if (wireMockServer == null || !wireMockServer.isRunning()) {
      throw new IllegalStateException("WireMock server is not running");
    }

    // host.docker.internal нужен, если backend запущен в Docker, а WireMock — непосредственно на
    // Ubuntu-хосте
    return "http://host.docker.internal:"
        + wireMockServer.port(); // port() возвращает порт, на котором работает WireMock
  }

  private FraudCheckMock findMockConfig(ExtensionContext context) {
    FraudCheckMock methodAnnotation =
        context
            .getTestMethod()
            .map(method -> method.getAnnotation(FraudCheckMock.class))
            .orElse(null);

    if (methodAnnotation != null) {
      return methodAnnotation;
    }

    return context
        .getTestClass()
        .map(clazz -> clazz.getAnnotation(FraudCheckMock.class))
        .orElse(null);
  }

  private void startWireMock(FraudCheckMock config) {
    /* В config передаются параметры из аннотации @FraudCheckMock
        new WireMockServer() — создаёт экземпляр сервера
        WireMockConfiguration.wireMockConfig() — создаёт конфигурацию WireMock
        .port(config.port()) — задаёт порт из аннотации
    */
    wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().port(config.port()));

    wireMockServer
        .start(); // Запускается HTTP-сервер WireMock на машине, где выполняются Java-тесты
  }

  private void configureStub(FraudCheckMock config) {
    Map<String, Object> response = new LinkedHashMap<>();

    response.put("status", config.status());
    response.put("decision", config.decision());
    response.put("riskScore", config.riskScore());
    response.put("reason", config.reason());
    response.put("requiresManualReview", config.requiresManualReview());
    response.put("additionalVerificationRequired", config.additionalVerificationRequired());

    String responseBody = toJson(response);
    /*
     Если придет запрос, соответствующий post(urlPathMatching(config.endpoint())) (POST /fraud/check)
       stubFor() — регистрирует правило в WireMock:
          * post() — правило будет срабатывать только на HTTP POST.
          * urlPathMatching() — проверяет URL по регулярному выражению.
          * config.endpoint() — путь берётся из аннотации.,
     то запрос не пересылается настоящему серверу, а сразу возвращается подготовленный ответ responseBody
    */
    wireMockServer.stubFor(
        post(urlPathMatching(config.endpoint()))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(responseBody)));
  }

  private String toJson(Map<String, Object> response) {
    try {
      // ObjectMapper из библиотеки Jackson преобразует Java-объект в JSON
      return OBJECT_MAPPER.writeValueAsString(response);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Failed to serialize FraudCheck mock response", e);
    }
  }
}
