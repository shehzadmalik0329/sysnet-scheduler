package com.xoriant.bsg.sysnetscheduler.scheduler;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import com.xoriant.bsg.sysnetscheduler.model.OutlookData;
import com.xoriant.bsg.sysnetscheduler.repository.OutlookDataRepository;
import com.xoriant.bsg.sysnetscheduler.service.EmailService;
import com.xoriant.bsg.sysnetscheduler.util.CSVHelper;
import com.xoriant.bsg.sysnetscheduler.util.FileHelper;

@Component
public class TestScheduler {
	
	private static final String OUTLOOKDATE_CREATED_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

	private static final int LATEST_FILE = 0;

	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Value("${csv.file.path}")
	private String csvFilePath;
	
	@Value("${cron.expression}")
	private String cronExpression;
	
	@Value("${csv.file.transfer.path}")
	private String fileTransferPath;
	
	@Value("${csv.file.transfer.name}")
	private String fileTransferName;
	
	@Autowired
	private FileHelper fileHelper;
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private OutlookDataRepository outlookDataRepository;
	
	@Autowired
	private CSVHelper csvHelper;
	
	@Scheduled(cron = "${cron.expression}")
	public void processOutlookData() {
		logger.info("Start processOutlookData.");
		readOutlookdataFromDatabase();
	}
	
	private void readOutlookdataFromDatabase() {
		
		File[] files = fileHelper.getAllFiles(csvFilePath);
		
		if(ObjectUtils.isEmpty(files) || files.length == 0) {
			logger.info("No file to process");
			emailService.sendNoFileToProcessNotification();
		} else {
			logger.info("File(s) found.");
			File latestFile = files[LATEST_FILE];
			
			String filePath = latestFile.getAbsolutePath();
			
			Map<String, OutlookData> csvDataMap = csvHelper.convertCsvToMap(filePath);
			
			Map<String, OutlookData> outlookDataMap = getOutlookDataMap();
			
			//New data in CSV
			Map<String,OutlookData> newDataMap = new HashMap<>();
			Map<String,OutlookData> updatedDataMap = new HashMap<>();
			Map<String,OutlookData> deletedEmpDataMap = new HashMap<>();

			prepareNewAndUpdatedDataMap(csvDataMap, outlookDataMap, newDataMap, updatedDataMap);

			prepareDeletedDataMap(csvDataMap, outlookDataMap, deletedEmpDataMap);
			
			String fileDate = getFileDate(latestFile);
			
			String createdDate = getDateTime(OUTLOOKDATE_CREATED_DATE_FORMAT);
			logger.info("Starting Database process.");
			insertUpdateOutlookData(newDataMap, fileDate, createdDate);
			
			insertUpdateOutlookData(updatedDataMap, fileDate, createdDate);
			
			List<OutlookData> newOutlookDataList = newDataMap.values().stream().collect(Collectors.toList());
			
			List<OutlookData> updatedOutlookDataList = updatedDataMap.values().stream().collect(Collectors.toList());
			
			newOutlookDataList.addAll(updatedOutlookDataList);
			logger.info("Prepare csv file for transfer");
			csvHelper.convertListToCsvNew(newOutlookDataList);
			
			String filePathToMail = fileTransferPath+fileTransferName;
			File[] attachments = new File[] {new File(filePathToMail)};
			
			emailService.sendNotificationWithAttachment(attachments);
			
			fileHelper.moveFile(files);
			logger.info("Process Completed Successfully");
		}
		
	}

	private void insertUpdateOutlookData(Map<String, OutlookData> dataMap, String fileDate, String createdDate) {
		Collection<OutlookData> newDataList= dataMap.values();
		newDataList.forEach(data -> {
			data.setCreatedDatetime(createdDate);
			data.setFileDate(fileDate);
		});
		outlookDataRepository.saveAll(newDataList);
	}

	private String getFileDate(File latestFile) {
		String fileName = latestFile.getName();
		String[] fileNameString = fileName.split("_|\\.");
		return fileNameString.length > 1 ? fileNameString[1] : "";
	}

	private void prepareDeletedDataMap(Map<String, OutlookData> csvDataMap, Map<String, OutlookData> outlookDataMap, Map<String,OutlookData> deletedEmpDataMap) {
		for(Map.Entry<String,OutlookData> entry: outlookDataMap.entrySet()) {
			String deletedEmpId = entry.getKey();
			OutlookData deletedEmpData = entry.getValue();
			if(csvDataMap.get(deletedEmpId) == null) {
				deletedEmpDataMap.put(deletedEmpId, deletedEmpData);
			}
		}
	}

	private void prepareNewAndUpdatedDataMap(Map<String, OutlookData> csvDataMap,
			Map<String, OutlookData> outlookDataMap, Map<String, OutlookData> newDataMap,
			Map<String, OutlookData> updatedDataMap) {
		for(Map.Entry<String,OutlookData> entry: csvDataMap.entrySet()) {
			String newEmployeeId = entry.getKey();
			OutlookData newEmployeeData = entry.getValue();
			if(outlookDataMap.get(newEmployeeId) == null) {
				newDataMap.put(newEmployeeId, newEmployeeData);
			} else {
				//Updated data in CSV
				OutlookData oldData = outlookDataMap.get(newEmployeeId);
				if(!oldData.equals(newEmployeeData)) {
					mapNewEmployeeData(oldData,newEmployeeData);
					updatedDataMap.put(newEmployeeId, oldData);
				}
			}
		}
	}

	private Map<String, OutlookData> getOutlookDataMap() {
		Iterable<OutlookData> outlookData = outlookDataRepository.findAll();
		
		List<OutlookData> outlookDataList = StreamSupport 
		        .stream(outlookData.spliterator(), false) 
		        .collect(Collectors.toList()); 
		
		return outlookDataList.stream().collect(Collectors.toMap(OutlookData::getEmployeeId, data ->data));

	}
	
	private void mapNewEmployeeData(OutlookData oldData, OutlookData newData) {
		oldData.setDisplayName(newData.getDisplayName());
		oldData.setTitle(newData.getTitle());
		oldData.setOffice(newData.getOffice());
		oldData.setEmailAddress(newData.getEmailAddress());
		oldData.setMobilePhone(newData.getMobilePhone());
		oldData.setManager(newData.getManager());
		oldData.setDepartment(newData.getDepartment());
		oldData.setJoinDate(newData.getJoinDate());
		oldData.setStreetAddress(newData.getStreetAddress());
		oldData.setCity(newData.getCity());
		oldData.setState(newData.getState());
		oldData.setPostalCode(newData.getPostalCode());
	}
	
	private static String getDateTime(String pattern) {
	    LocalDateTime myDateObj = LocalDateTime.now();
	    DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern(pattern);
	    return  myDateObj.format(myFormatObj);
	}

}
