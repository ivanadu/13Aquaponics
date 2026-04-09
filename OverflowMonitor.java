// In order to run code on your own device, take a look at the "IMPORTANT: VARIABLES TO CHANGE" section

// All libraries are standard Java libraries
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;



public class OverflowMonitor {
	
	// IMPORTANT: VARIABLES TO CHANGE
	private static String phone = "+16472840979"; // Phone number for notification receiving. Works with Apple iMessages
	private static int interval = 600; // 10 minutes. Used to determine interval for checking the website overflow statuses
	private static boolean useLocalFile = false; // false means reads from website. true reads from local file for testing
	private static boolean testOverflow = true; // determines which local file to read from 
	private static String filePath = "/Users/ivanadu/eclipse-workspace/ES1050/src/"; // stores the file path leading to the alarms.php and alarms2.php local files
	
	// final variables
	private static final String URL = "https://london.waterwai.com/alarms.php";
	private static final HttpClient client = HttpClient.newHttpClient();
	
	// operational variables
	private static boolean valveOpen = true;

	
	// main method
	public static void main(String[] args) {
		
		ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
		
		scheduler.scheduleAtFixedRate(() -> {
			try {
				checkAlarms();
				System.out.println("Last checked: " + time());
				
				// Response if valve is closed, but overflow is no longer active
				if (valveOpen == false) {
					valveOpen = true;
					openValve();
					sendIMessage(phone, "Overflow resolved");
				}
				
            } 
			
			// Exception thrown when overflow detected
			catch (OverflowDetectedException e) {
				
				String errorMessage = e.getMessage()+" detected at " + time();
				
				System.out.println(errorMessage);
				
				// Valve previously open
				if (valveOpen) {
					closeValve();
					valveOpen = false;
					sendIMessage(phone, errorMessage);
				}

				System.out.println(" ");
			}
			
			// Other errors
			catch (Exception e) {
				System.out.println("Error checking alarms:");
				e.printStackTrace();
			}
			
		// Time interval to check overflow status
		}, 0, interval, TimeUnit.SECONDS); 

	}

	// Private method to check alarms
	private static void checkAlarms() throws Exception {
		
		String responseBody;

	    if (useLocalFile) {
	        if (testOverflow)
	        	responseBody = Files.readString(Path.of(filePath + "alarms2.php"));
	        else 
	        	responseBody = Files.readString(Path.of(filePath + "alarms.php"));
	    } else {
	    	// Extracting alarms data from website
	        HttpRequest request = HttpRequest.newBuilder()
	            .uri(URI.create(URL))
	            .GET()
	            .build();

	        HttpResponse<String> response = client.send(request,
	            HttpResponse.BodyHandlers.ofString());
	        responseBody = response.body();
	    }
		
	        
		String[] raw = responseBody.split("\\*\\*\\*LSV\\*\\*\\*\\*\\*\\*LSV\\*\\*\\*");
		
		
		// Splitting data into sites
		String[] overflow_alarm_array = raw[1].replaceAll("\n", "").split("XXXZZZ");
		int num = overflow_alarm_array.length;
        
		
		// Initializing site names and overflow 
        String[] sites = new String[num];
        boolean[] overflow = new boolean[num];
    	
        
        // Splitting site entries into components
        
    	for (int i = 0; i<num; i++) {
    		
    		String[] values = overflow_alarm_array[i].split("X6X");
    		if (values.length<6) continue;
    		
    		sites[i] = values[1];
    		double level = Double.parseDouble(values[5]);
    		double overflowheight = Double.parseDouble(values[6]);
    		
    		overflow[i] = level>overflowheight;
    		
    	}
    	
    	
    	// Detect overflow  
    	// NEXT STEP: Can filter for relevant treatment plants that will affect the aquaponics
    	String message = "";
    	boolean overflowing_active = false;
    	
 		for (int i=0; i<num; i++) {
    		if (overflow[i]) {
    			if (overflowing_active) message = message +", ";
    			message = message + sites[i];
    			overflowing_active = true;
    		}
 	
    		// From testing
 			/*
    		System.out.print(sites[i]);
    		System.out.print(": ");
    		System.out.println(overflow[i]);
    		*/
    	}
 		
 		// initiates response to overflow
 		if (overflowing_active) {
 			throw new OverflowDetectedException(message);
 		}
    	
	}
	
	// private method to send a formatted message to specified number (variable defined at top)
	private static void sendIMessage(String phoneNumber, String messageText) {
	    try {
	        // AppleScript command to tell Messages to send a text
	        String script = String.format(
	            "tell application \"Messages\" to send \"%s\" to buddy \"%s\"",
	            messageText, phoneNumber
	        );

	        // Execute via the shell
	        ProcessBuilder pb = new ProcessBuilder("osascript", "-e", script);
	        pb.start();
	        
	        System.out.println("iMessage sent to " + phoneNumber);
	    } 
	    
	    catch (Exception e) {
	        System.err.println("Failed to send iMessage: " + e.getMessage());
	    }
	}
	
	// formats time into more legible format
	private static String time() {
		LocalDateTime now = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		String formattedDate = now.format(formatter);
		
		return formattedDate;
	}
	
	// Would hypothetically include commands to close a physical valve. 
	private static void closeValve() {
		// NEXT STEPS add a response 
		System.out.println("Valve closed at "+ time());
	}

	// Would hypothetically include commands to open a physical valve. 
	private static void openValve() {
		// NEXT STEPS add a response 
		System.out.println("Valve opened at " + time());
	}

}

// Custom Overflow error
class OverflowDetectedException extends Exception {

    public OverflowDetectedException(String message) {
        super("OVERFLOW at " + message);
    }

}


