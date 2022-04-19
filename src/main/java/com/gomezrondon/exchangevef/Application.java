package com.gomezrondon.exchangevef;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import static com.gomezrondon.exchangevef.service.ImageService.downloadImage;
import static com.gomezrondon.exchangevef.service.ImageService.getImageFromXPath;
import static com.gomezrondon.exchangevef.service.ImageService.readFile;
import static com.gomezrondon.exchangevef.service.ImageService.writeToFile;


@SpringBootApplication
public class Application {

	@Autowired
	private Storage storage;

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Value("${gcs-resource-test-bucket}")
	private String bucketName;
	@Value("${gcs-file-object-name}")
	private String objectName;

	@Value("${gcs-orc-service-url}")
	private String ocrServiceUrl;

	@Bean
	public Supplier<String> getTime() {
		return () -> {
			UUID uuid = UUID.randomUUID();
			String string = LocalDateTime.now() + " " + uuid;
			System.out.println(string);
			return string;
		};
	}


	public void upload(String filePath, String bucketName, String objectName) throws IOException {
		BlobId blobId = BlobId.of(bucketName, objectName);
		BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
		storage.create(blobInfo, Files.readAllBytes(Paths.get(filePath)));
		System.out.println("File " + filePath + " uploaded to bucket " + bucketName + " as " + objectName);
	}

/*	public void download(String destFilePath, String bucketName, String objectName) {
		Blob blob = storage.get(BlobId.of(bucketName, objectName));
		blob.downloadTo(Paths.get(destFilePath));
	}*/

	@Bean
	public Supplier<String> getBsF() {
		return () -> {
			String url = "https://monitordolarvenezuela.com/";
			String xpath = "/html/body/div[3]/div/div/div[3]/div[4]/div[1]/h6";
			String xPath = getImageFromXPath(url, xpath);
			return xPath.replace(",", ".").split(" ")[1];
		};
	}


	@Bean
	public Supplier<String> getBsFDolarToday() {
		return () -> {

			String tempBsF = readFile("./images/data.txt");

			if (!tempBsF.isEmpty()) {
				System.out.println("Retrieving from Cache: "+tempBsF);
				return tempBsF;
			}

			//1) download image
			downloadImage("https://dolartoday.com/custom/rate2.jpg");
			try {
				//2) push image to bucket
				upload("./images/image.jpg", bucketName, objectName);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

			RestTemplate restTemplate = new RestTemplate();
			//3) call the service to get the text from image
			ResponseEntity<String> response = restTemplate.getForEntity(ocrServiceUrl, String.class);

			String regex = "\\*DOLARTODAY.*?[\\d]{0,}[,|.][\\d]{2}";
			final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);



			String tasaBsF = Arrays.stream(response.getBody().split("\n"))
					.map(String::trim)
					.filter(x -> !x.isEmpty())
					.map(line -> {
						final Matcher matcher = pattern.matcher(line);
						if (matcher.find()) {
							return matcher.group(0);
						}
						return "";
					})
					.filter(x -> !x.isEmpty())
					.map(x -> x.split(" ")[2])
					.map(x -> x.replace(",", "."))
					.collect(Collectors.joining());

			writeToFile("./images/data.txt", tasaBsF);

			return tasaBsF;
		};
	}


}
