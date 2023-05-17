import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.aiplatform.util.ValueConverter;
import com.google.cloud.aiplatform.v1.EndpointName;
import com.google.cloud.aiplatform.v1.PredictResponse;
import com.google.cloud.aiplatform.v1.PredictionServiceClient;
import com.google.cloud.aiplatform.v1.PredictionServiceSettings;
import com.google.cloud.aiplatform.v1.schema.predict.instance.ImageObjectDetectionPredictionInstance;
import com.google.protobuf.Message;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class Main {

    public static void main(String[] args) {

        PredictionServiceSettings predictionServiceSettings = null;

        try {
            String apiEndpoint = "us-central1-aiplatform.googleapis.com:443";

            // Load the service account credentials from the JSON file
            System.setProperty("GOOGLE_APPLICATION_CREDENTIALS", "C:\\Users\\TECRA\\Desktop\\Uni\\3ano\\ESTAGIO\\" +
                    "Google_Cloud\\teak-node-386212-87b132b61be4.json");
            GoogleCredentials credentials = GoogleCredentials.fromStream(
                    new FileInputStream(System.getProperty("GOOGLE_APPLICATION_CREDENTIALS"))
            ).createScoped(Arrays.asList("https://www.googleapis.com/auth/cloud-platform"));

            // Set up the prediction service client with the loaded credentials
            predictionServiceSettings = PredictionServiceSettings.newBuilder()
                    .setCredentialsProvider(FixedCredentialsProvider.create(credentials)).setEndpoint(apiEndpoint).build();

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        try (PredictionServiceClient predictionServiceClient = PredictionServiceClient.create(predictionServiceSettings)) {

            EndpointName endpoint =
                    EndpointName.of("teak-node-386212", "us-central1", "7036852427533844480");

            // Lê-se a imagem.
            byte[] fileContent = FileUtils.readFileToByteArray(new File("eee.jpg"));

            // Codifica-se a imagem em base64.
            String encodedString = Base64.getEncoder().encodeToString(fileContent);

            // Adiciona-se a imagem codificada a instances.
            List<Value> instances = new ArrayList<>();
            ImageObjectDetectionPredictionInstance.Builder instance = ImageObjectDetectionPredictionInstance.
                    newBuilder().setContent(encodedString);
            Message message = instance.build();
            instances.add(ValueConverter.toValue(message));

            // Definem-se os parâmetros.
            Value parameters = Value.newBuilder().setStructValue(Struct.newBuilder().
                    putFields("confidence_threshold", Value.newBuilder().setNumberValue(0.5).build()).
                    putFields("max_predictions", Value.newBuilder().setNumberValue(5).build()))
                    .build();

            // Envia-se a imagem até ao endpoint e recebe-se a resposta do modelo.
            PredictResponse response = predictionServiceClient.predict(endpoint, instances, parameters);
            System.out.println("EOF");

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
