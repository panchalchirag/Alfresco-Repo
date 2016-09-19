import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONObject;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

/**
 * 
 */

/**
 * PCLRequest Class. It is used for check if the PCL file is indexed in SOLR and
 * Available in Alfresco
 * 
 * @author chirag.panchal
 * 
 */
public class PCLRequest {

	/**
	 * Main Class. Execution Start Point
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		try {
			// String ticket = getAlfticket();
			// String inputFile =
			// "F:\\Projects\\HP-ECM\\Issue\\20160724_Files\\input\\ORG_CORSPD_ID_ECM_Y.csv";
			// String outputFile =
			// "F:\\Projects\\HP-ECM\\Issue\\20160724_Files\\output\\ORG_CORSPD_ID_ECM_Y_Result.csv";
			// String countyId = "30";
			String countyId = args[0];
			String inputFile = args[1];
			String outputFile = args[2];
			// This is the update from Aasif
			// Call the processCSVFile function
			processCSVFile(inputFile, outputFile, countyId);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Process the CSV file and generate the report.
	 * 
	 * @param inputFile
	 *            Input File
	 * @param outputFile
	 *            Output File
	 * @param countyID
	 *            County ID
	 */
	private static void processCSVFile(String inputFile, String outputFile,
			String countyID) {
		try {

			// Input CSV File
			CsvReader correspondences = new CsvReader(inputFile);
			correspondences.readHeaders();

			// Output CSV File
			CsvWriter csvOutput = new CsvWriter(new FileWriter(outputFile,
					false), ',');

			csvOutput.write("COUNTY_ID");
			csvOutput.write("LANG_CD");
			csvOutput.write("TOTAL_RECORD_FOUND");
			csvOutput.endRecord();

			// Iterating over the records in input files
			while (correspondences.readRecord()) {
				String corrId = correspondences.get("COUNTY_ID");
				String langCD = correspondences.get("LANG_CD");

				// Check if the PCL file indexed and available in alfresco
				Integer totalRecordFound = processPCLRequest(countyID, corrId,
						langCD);

				System.out.println(corrId + "," + langCD + ","
						+ totalRecordFound);

				// Writing to OUTPUT CSV file
				csvOutput.write(corrId);
				csvOutput.write(langCD);
				if (null != totalRecordFound) {
					csvOutput.write(totalRecordFound.toString());
				} else {
					csvOutput.write("0");
				}
				csvOutput.endRecord();

			}
			// Closing reading mode
			correspondences.close();
			// Closing Writing mode
			csvOutput.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Process the PCL file request and return the no. of document found.
	 * 
	 * @param countyID
	 *            County ID
	 * @param corrId
	 *            Correspondence ID
	 * @param langCD
	 *            Language Code
	 * @return No. of document Found
	 */

	private static Integer processPCLRequest(String countyID, String corrId,
			String langCD) {
		Integer totalRecordFound = null;
		try {
			String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:47.0) Gecko/20100101 Firefox/47.0";
			String url = "http://ecm.production.calwin.org:3000/dpe/onlineexport?"
					+ "username=ZHBlc3lzdGVt&password=ZHBlc3lzdGVt"
					+ "&COUNTY_ID="
					+ countyID
					+ "&CORR_ID="
					+ corrId
					+ "&LANG_CODE=" + langCD + "&resp=pcl";

			HttpResponse response;
			HttpClient client = HttpClientBuilder.create().build();
			HttpGet request = new HttpGet(url);
			request.addHeader("User-Agent", USER_AGENT);
			// Hit the URL to DPE
			response = client.execute(request);

			// Get the content from response and parse the JSON content
			BufferedReader rd = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent()));

			StringBuffer result = new StringBuffer();
			String line = "";
			while ((line = rd.readLine()) != null) {
				result.append(line);
			}

			JSONObject jsonResult = new JSONObject(result.toString());
			totalRecordFound = (Integer) jsonResult.get("numberofdocuments");

		} catch (Exception e) {
			e.printStackTrace();
		}
		return totalRecordFound;
	}
}
