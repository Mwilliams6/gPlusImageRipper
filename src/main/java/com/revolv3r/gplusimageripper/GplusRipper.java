package com.revolv3r.gplusimageripper;

import static java.util.concurrent.CompletableFuture.supplyAsync;
import static java.util.stream.Collectors.toList;

import com.revolv3r.gplusimageripper.service.GplusService;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;

@RestController
@SpringBootApplication
public class GplusRipper
{
  @Resource
  private GplusService mGplusService;

	private List<Future<?>> futureResults;

	private Logger mLogger = LogManager.getLogger(GplusRipper.class);

	@RequestMapping("/")
	public ModelAndView get() {
		return new ModelAndView("index");
	}

	/**
	 * Initiates the grab process
	 * @param input the google profile ID
	 */
	// Exercise using curl http://localhost:8080/async?input=<google_profile_id>
	@RequestMapping(path = "initReq", method = RequestMethod.POST)
	public void get(@RequestParam String input) {

		//get album urls from profile
		final List<String> albumUrls = mGplusService.retrieveAlbumsFromProfile(input);
		mLogger.info(String.format("fatching %s albums.. Please Wait", albumUrls.size()));

    // Create the collection of futures.
    futureResults =
            albumUrls.stream()
                    .map(albumPath -> supplyAsync(() -> mGplusService.retrieveImages(albumPath)))
                    .collect(toList());
	}

  /**
   * Poller to update the GUI with the future result outcomes, as/when
   * @return list of img paths successfully completed
   */
	@RequestMapping(path = "poller", method = RequestMethod.GET)
	public Future<String> poll(){

		// Restructure as varargs because that's what CompletableFuture.allOf requires.
		CompletableFuture<?>[] futuresAsVarArgs = futureResults
						.toArray(new CompletableFuture<?>[futureResults.size()]);

		// Create a new future that completes once once all of the previous futures complete.
		CompletableFuture<?> jobsDone = CompletableFuture.anyOf(futuresAsVarArgs);

		CompletableFuture<String> output = new CompletableFuture<>();

		// Once all of the futures have completed, build out the result string from results.
		jobsDone.thenAccept(ignored -> {
			StringBuilder stringBuilder = new StringBuilder();
			futureResults.forEach(f -> {
				try {
					stringBuilder.append(f.get());
				} catch (Exception e) {
					output.completeExceptionally(e);
				}
			});
			output.complete(stringBuilder.toString());
		});

		return output;
	}

  /**
   * SpringApplication
   * @param args args
   */
	public static void main(String[] args) {
		SpringApplication.run(GplusRipper.class, args);
	}

}