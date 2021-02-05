package com.xoriant.bsg.sysnetscheduler.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

@Component
public class FileHelper {
	
	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Value("${csv.file.destination.path}")
	private String destinationFilePath;
	
	public File[] getAllFiles(String sourceFolder) {
		if(!StringUtils.isEmpty(sourceFolder)) {
			File directory = new File(sourceFolder);
			return readFileNames(directory);
		}
		return new File[] {};
	}
	
	public void moveFile(File[] files) {
		
		for(File file: files) {
			String source = file.getPath();
			String destination = destinationFilePath + file.getName();
			Path temp;
			try {
				temp = Files.move(Paths.get(source),Paths.get(destination),StandardCopyOption.REPLACE_EXISTING);
				
				if(temp != null) 
				{ 
					logger.info("File renamed and moved successfully");
				} 
				else
				{ 
					logger.info("Failed to move the file:[{}]",source);
				} 
			} catch (IOException e) {
				logger.info("Failed to move the file:[{}] with error [{}]",source, e.getMessage());
				e.printStackTrace();
			}
		}

	}

	private static File[] readFileNames(File directory) {
		File[] files = directory.listFiles();
		if(!ObjectUtils.isEmpty(files) && files.length > 0) {
			Arrays.sort(files, LastModifiedFileComparator.LASTMODIFIED_REVERSE);
			return files;
		}
		return files;
	}

}
