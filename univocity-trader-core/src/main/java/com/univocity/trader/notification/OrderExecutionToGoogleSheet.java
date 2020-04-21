package com.univocity.trader.notification;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.univocity.trader.account.Order;
import com.univocity.trader.account.Trade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Logs Order executions to a google sheet.  */
public class OrderExecutionToGoogleSheet implements OrderListener {

	private static final Logger log = LoggerFactory.getLogger(OrderExecutionToGoogleSheet.class);

	private static final String APPLICATION_NAME = "univocity";
	public static final String SERVICE_CREDS_FILE = "/google-sheets-credentials.json";

	private Sheets service;
	private final String spreadsheetId;
	private final String range;
	private List<Object> columnData = new ArrayList<>();


	/**
	 * Creates a new instance whereby output is saved to a google sheet.
	 *
	 * For this OrderListener to work, you need to provide a service credentials file which grants you access rights to
	 * the sheet. To obtain this credentials file:
	 *
	 * 1) Create a project in the Google Developers Console (https://console.developers.google.com/)
	 *
	 * 2) Enable the Google Sheets API for the project (See https://developers.google.com/sheets/api/quickstart/java)
	 *
	 * 3) Under https://console.cloud.google.com/apis/credentials?project=YOURPROJECTNAME create a service account and
	 * TAKE NOTE OF THE SERVICE ACCOUNT ID EMAIL ADDRESS
	 *
	 * 4) Download the key in JSON format and save it to google-sheets-credentials.json in the resources folder
	 *
	 * Then go and create a new Google sheets spreadsheet and make sure to share it (with edit permissions) with the
	 * service email account that you created in step 3. For example this looks like: univocity@univocity.iam.gserviceaccount.com
	 *
	 * *
	 *
	 * @param spreadsheetId The ID coming from the URL of the sheet you created (https://docs.google.com/spreadsheets/d/SPREADSHEET-ID/edit#gid=0)
	 *                      For example to write to https://docs.google.com/spreadsheets/d/2I5o_jCREk01_5-JvmTckjViTA6YwCzpjXLTEOmANwhY/edit#gid=0
	 *                      you should pass in: 2I5o_jCREk01_5-JvmTckjViTA6YwCzpjXLTEOmANwhY
	 *
	 * @param range where to append data to, for example "Sheet1!A1:C" where A1:C refers to the 1st column of "Sheet1",
	 *                 B1:B 2nd column and so on.
	 *              For more details refer to https://developers.google.com/sheets/api/guides/concepts
	 */
	public OrderExecutionToGoogleSheet(String spreadsheetId, String range) {
		this.spreadsheetId = spreadsheetId;
		this.range = range;

		try {
			GoogleCredentials credential = getCredentials();
			this.service = getSheetsService(credential);

			addColumnNames();

		} catch (Exception e) {
			log.error("Unable to access Google Sheets service", e);
		}

	}

	private void addColumnNames() throws IOException {
		ValueRange values = this.service.spreadsheets().values()
				.get(spreadsheetId, this.range)
				.execute();

		if (values.getValues() == null || values.getValues().isEmpty()) {
			ReflectionUtils.doWithFields(OrderExecutionLine.class, field -> addToRow(columnData, field.getName()));
		}
	}

	public OrderExecutionToGoogleSheet(String spreadsheetId) {
		this(spreadsheetId, "Sheet1!A1:C"); // this means starting from the top of the sheet named "Sheet1"

	}
	private Sheets getSheetsService(GoogleCredentials credential) throws Exception {

		return new Sheets.Builder(GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance(), new HttpCredentialsAdapter(credential))
				.setApplicationName(APPLICATION_NAME)
				.build();
	}

	private GoogleCredentials getCredentials() throws IOException {

		InputStream is = OrderExecutionToGoogleSheet.class
				.getResourceAsStream(SERVICE_CREDS_FILE);
		return ServiceAccountCredentials.fromStream(is)
				.createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS));
	}


	@Override
	public void orderSubmitted(Order order, Trade trade, Client client) {
		logDetails(order, trade, client);
	}

	@Override
	public void orderFinalized(Order order, Trade trade, Client client) {
		logDetails(order, trade, client);
	}

	void addToRow(List<Object> rowData, Object o){
		Object tmp = o == null ? "" : o;
		if (o != null){
			if (o instanceof Timestamp){
				tmp = new DateTime(((Timestamp) o).getTime());
			} else if (o instanceof Enum){
				tmp = o.toString();
			}
		}
		rowData.add(tmp);
	}
	protected void logDetails(Order order, Trade trade, Client client) {

		List<Object> rowData = new ArrayList<>();

		List<List<Object>> values = new ArrayList<>();

		if (!columnData.isEmpty()) {
			values.add(columnData);
		}
		values.add(rowData);

		ValueRange valueRange = new ValueRange();
		valueRange.setMajorDimension("ROWS");
		valueRange.setValues(values);



		OrderExecutionLine o = new OrderExecutionLine(order, trade, trade.trader(), client);
		ReflectionUtils.doWithFields(OrderExecutionLine.class, field -> addToRow(rowData, field.get(o)), field -> field.canAccess(o) && field.getAnnotation(NotForExport.class) == null);


		try {
			if (service != null) {
				service.spreadsheets().values()
						.append(spreadsheetId, this.range, valueRange)
						.setValueInputOption("RAW")
						.execute();

				columnData.clear();

			}
		} catch (IOException e) {
			log.error("Unable to log to Google Sheets", e);
		}
	}
}
