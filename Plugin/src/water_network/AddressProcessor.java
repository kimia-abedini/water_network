package water_network;

import java.net.URLEncoder;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AddressProcessor {

    private static final String HERE_API_URL = "https://revgeocode.search.hereapi.com/v1/revgeocode";
    private static final String SAWAZ_API_URL = "http://www.sawaz.it/Univ/GIS/Progetti/anagrafe.php";
    private static final String HERE_API_KEY = "RJsyNwWRs81JHn6vP9O64bJ1DA4OQLX0XfhBPkLddHA";

    public static void main(String[] args) {
        double latitude = 45.3960043;
        double longitude = 11.9014451;

        try {
            String address = getAddressFromCoordinates(latitude, longitude);
            String sawazResponse = getSawazApiResponse(address);
            System.out.println("Sawaz API Response:");
            System.out.println(sawazResponse); // Coppola,Costanza;Simoni,Carlo;Lorenzi,Sara
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getAddressFromCoordinates(double latitude, double longitude) throws Exception {
        String url = HERE_API_URL + "?at=" + latitude + "," + longitude + "&apiKey=" + HERE_API_KEY;

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new RuntimeException("Failed to retrieve address: " + response);

            ObjectMapper objectMapper = new ObjectMapper();
            HereApiResponse hereApiResponse = objectMapper.readValue(response.body().string(), HereApiResponse.class);
            if (hereApiResponse.getItems().isEmpty()) throw new RuntimeException("No address found.");

            HereApiItem item = hereApiResponse.getItems().get(0);
            return item.getAddress().getLabel();
        }
    }

    public static String getSawazApiResponse(String cleanedAddress) throws Exception {
        String encodedAddress = URLEncoder.encode(cleanedAddress, "UTF-8");
        String url = SAWAZ_API_URL + "?indirizzo=" + encodedAddress + "&request=residenti";

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new RuntimeException("Failed to retrieve Sawaz API response: " + response);

            return response.body().string();
        }
    }

    // Helper classes for JSON parsing
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class HereApiResponse {
        @JsonProperty("items")
        private List<HereApiItem> items;

        public List<HereApiItem> getItems() {
            return items;
        }

        public void setItems(List<HereApiItem> items) {
            this.items = items;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class HereApiItem {
        @JsonProperty("address")
        private Address address;

        public Address getAddress() {
            return address;
        }

        public void setAddress(Address address) {
            this.address = address;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Address {
        @JsonProperty("label")
        private String label;

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }
    }
}
