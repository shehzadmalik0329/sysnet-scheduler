package com.xoriant.bsg.sysnetscheduler.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.QuoteMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.xoriant.bsg.sysnetscheduler.model.OutlookData;

@Component
public class CSVHelper {
	
	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private static final String MOBILE_PHONE_START_COMMA = "'";
	
	@Value("${csv.file.transfer.path}")
	private String fileTransferPath;
	
	@Value("${csv.file.transfer.name}")
	private String fileTransferName;
	
	@Value("${csv.file.headers}")
	private String headers;

	public static String TYPE = "text/csv";
	static String[] HEADERs = { "Id", "Title", "Description", "Published" };

	public Map<String, OutlookData> convertCsvToMap(String filePath) {
		logger.info("Starting csvToList");
		
		try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF-8"));
				CSVParser csvParser = new CSVParser(fileReader,
						CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim());) {

			Iterable<CSVRecord> csvRecords = csvParser.getRecords();
			
			Map<String, OutlookData> outlookDataMap = new HashMap<>();

			for (CSVRecord csvRecord : csvRecords) {
				String mobilePhone = csvRecord.get("MobilePhone");
				String formattedMobilePhone = mobilePhone.replace(MOBILE_PHONE_START_COMMA,"");
				
				OutlookData outlookData = new OutlookData(
						csvRecord.get("EmployeeId"),
						csvRecord.get("DisplayName"),
						csvRecord.get("Title"),
						csvRecord.get("Office"),
						csvRecord.get("EmailAddress"),
						formattedMobilePhone,
						csvRecord.get("Manager"),
						csvRecord.get("Department"),
						csvRecord.get("date of joining"),
						csvRecord.get("StreetAddress"),
						csvRecord.get("City"),
						csvRecord.get("State"),
						csvRecord.get("PostalCode")
						);

				outlookDataMap.put(csvRecord.get("EmployeeId"), outlookData);
			}
			return outlookDataMap;
		} catch (IOException e) {
			logger.info("fail to parse CSV file: [{}]", e.getMessage());
		}
		return Collections.emptyMap();
	}
	
	public void convertListToCsvNew(List<OutlookData> outlookDataList) {
		logger.info("Starting convertListToCsvNew");
		String filePath = fileTransferPath+fileTransferName;
		
		try(Writer writer = Files.newBufferedWriter(Paths.get(filePath));
				CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(headers.split(",")));) {
			
			for (OutlookData outlookData : outlookDataList) {
				List<String> data = Arrays.asList(
						outlookData.getEmployeeId(),
						outlookData.getDisplayName(),
						outlookData.getTitle(),
						outlookData.getOffice(),
						outlookData.getEmailAddress(),
						outlookData.getMobilePhone(),
						outlookData.getManager(),
						outlookData.getDepartment(),
						outlookData.getJoinDate(),
						outlookData.getStreetAddress(),
						outlookData.getCity(),
						outlookData.getState(),
						outlookData.getPostalCode());

				csvPrinter.printRecord(data);
			}
			csvPrinter.flush();
		} catch(IOException e) {
			logger.info("fail to parse CSV file: [{}]", e.getMessage());
		}
		
	}


}
