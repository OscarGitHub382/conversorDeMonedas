import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import modelos.DivisaExchangerate;

import java.io.FilterWriter;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;

public class ConvertidorDeDivisas {
    private static final String apiURL = "https://v6.exchangerate-api.com/v6/7ed9aa3789dcf87762de8eb1/latest/USD";
    private static final HttpClient cliente = HttpClient.newHttpClient();

    public static void main(String[] args) {
        ConvertidorDeDivisas convertidor = new ConvertidorDeDivisas();
        try {
            String respuesta = convertidor.obtenerDatosDeMoneda();
            System.out.println("Datos de tasas de cambio recibidos:\n" + respuesta);
        } catch (HttpTimeoutException e) {
            System.err.println("La solicitud a la API ha excedido el tiempo de espera.");
        } catch (IOException e) {
            System.err.println("Error de E/S al conectarse a la API: " + e.getMessage());
        } catch (InterruptedException e) {
            System.err.println("La solicitud fue interrumpida: " + e.getMessage());
            Thread.currentThread().interrupt(); //Restaurar el estado interrumpido
        } catch (ExcepcionDeRespuestaIncompleta e) {
            System.err.println("Respuesta incompleta de la API: " + e.getMessage());
        }

    }

    public String obtenerDatosDeMoneda() throws IOException, InterruptedException, ExcepcionDeRespuestaIncompleta{
        HttpRequest solicitud = HttpRequest.newBuilder()
                .uri(URI.create(apiURL))
                .timeout(Duration.ofSeconds(10)) //tiempo de espera
                .GET()
                .build();

        HttpResponse<String> respuesta = cliente.send(solicitud, HttpResponse.BodyHandlers.ofString());

        if (respuesta.statusCode() != 200) {
            throw new IOException("Error en la respuesta de la API: código de estado " + respuesta.statusCode());
        }

        String respuestaJson = respuesta.body();
        if (respuestaJson == null || respuestaJson.isEmpty()) {
            throw new ExcepcionDeRespuestaIncompleta("La API devolvió una respuesta vacía.");
        }

        // Parsear y validar la estructura JSON
        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
                .setPrettyPrinting()
                .create();
        DivisaExchangerate divisa = gson.fromJson(respuestaJson, DivisaExchangerate.class);

        //docDivisas.add(divisa);

        //FilterWriter escritura =




        // Validación adicional para verificar la estructura JSON o campos específicos, si fuera necesario.
        if (!respuestaJson.contains("rates")) {
            throw new ExcepcionDeRespuestaIncompleta("La respuesta de la API no contiene datos de tasas de cambio esperados.");
        }
        return respuestaJson;
    }
}
