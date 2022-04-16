package com.gomezrondon.exchangevef;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import static com.gomezrondon.exchangevef.service.ImageService.downloadImage;
import static com.gomezrondon.exchangevef.service.ImageService.getImageFromXPath;


@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

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
			downloadImage("https://dolartoday.com/custom/rate2.jpg");
//			scaleImage("./images/image.jpg", "./images/image.jpg");
//			toGrayScale("./images/image.jpg", "./images/image.jpg");

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.MULTIPART_FORM_DATA);

			MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
			try {
				body.add("file", getTestFile());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

			HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

			RestTemplate restTemplate = new RestTemplate();
			ResponseEntity<String> response = restTemplate.postForEntity(ocrServiceUrl, requestEntity, String.class);

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

			return tasaBsF;
		};
	}


	public static Resource getTestFile() throws IOException {
		File file = new File("./images/image.jpg");
		System.out.println("Creating and Uploading Test File: " + file.getPath());
		return new FileSystemResource(file);
	}



}
