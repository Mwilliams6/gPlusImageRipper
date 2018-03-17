package com.revolv3r.gplusimageripper;

import static java.util.concurrent.CompletableFuture.supplyAsync;
import static java.util.stream.Collectors.toList;

import com.revolv3r.gplusimageripper.service.GplusService;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;

@RestController
@SpringBootApplication
public class GplusRipper
{
	private double overallProgress=0,total = 0, currentPos=0;
  private final static String PROGRESS_BAR = "<div class='progress'><div class='progress-bar' role='progressbar' style='width:%s%%'></div></div>";

  @Resource
  private GplusService mGplusService;

	private List<Future<?>> futureResults;
	private Logger mLogger = LogManager.getLogger(GplusRipper.class);

	//Default mapping
	@RequestMapping("/")
	public ModelAndView get() {
		return new ModelAndView("index");
	}

	/**
	 * Initiates the grab process
	 * @param input the google profile ID
	 */
	@RequestMapping(path = "initReq", method = RequestMethod.POST)
	public ResponseEntity<?> get(@RequestParam String input) {
		resetCounters();
		//get album urls from profile
		final Set<String> albumUrls = mGplusService.retrieveAlbumsFromProfile(input);
		mLogger.debug(String.format("fatching %s albums.. Please Wait", albumUrls.size()));

    // Create the collection of futures.
    futureResults =
            albumUrls.stream()
                    .map(albumPath -> supplyAsync(() -> mGplusService.retrieveImages(albumPath)))
                    .collect(toList());
		total = futureResults.size();
    return ResponseEntity.ok(total);
	}

	private void resetCounters() {
		overallProgress=0;
		total = 0;
		currentPos=0;
		mGplusService.setCancelled(false);
	}

	/**
	 * Statusbar poll method
	 * @return bootstrap statusbar with updated progress values
	 */
	@RequestMapping(path = "getProgress", method = RequestMethod.GET)
	public String updateStatus(){
		currentPos = (overallProgress / total)*100;
		return String.format(PROGRESS_BAR, currentPos);
	}

	/**
	 * Cancel pending jobs
	 */
	@RequestMapping(path = "cancel", method = RequestMethod.GET)
	public void cancel(){
		mGplusService.setCancelled(true);
		//return String.format("  <div class='progress'><div class='progress-bar' role='progressbar' style='width:%s%%'></div></div>", currentPos);
	}

  /**
   * Poller to update the GUI with the future result outcomes, as/when
   * @return list of img paths successfully completed
   */
	@RequestMapping(path = "poller", method = RequestMethod.GET)
	public Future<String> poll(){
		CompletableFuture<String> output = new CompletableFuture<>();
    List<Future<?>> removeArray = new ArrayList<>();
		StringBuilder stringBuilder = new StringBuilder();
		try
    {
    	if (futureResults!=null)
			{
				for (Future<?> item : futureResults)
				{
					if (item.isDone())
					{
						stringBuilder.append(item.get(10, TimeUnit.MILLISECONDS));
						removeArray.add(item);
						overallProgress++;
					}
				}
				//all done?
				if (futureResults.size()==0)
				{
					stringBuilder.append("<div style='display:none'>XX-FINISHED-XX</div>");
				}
				else
				{
					//remove completed
					futureResults.removeAll(removeArray);
				}

				//return result
				output.complete(stringBuilder.toString());
			}
			else
			{
				stringBuilder.append("<div style='display:none'>XX-FINISHED-XX</div>");
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

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