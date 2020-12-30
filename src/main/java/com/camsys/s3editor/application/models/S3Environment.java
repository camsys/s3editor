package com.camsys.s3editor.application.models;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;

public class S3Environment {
	
	private String name;
		
	private Map<String, String> fileIndex;
	
	public S3Environment(String name) {
		this.name = name;
	}
	
	public void fetchIndex(AmazonS3 s3Client) throws IOException {
		try {
			S3Object index = s3Client.getObject("camsys-mta-otp-graph", this.name + "/control_data/INDEX");
			String contents = IOUtils.toString(index.getObjectContent(), "utf8");
					
			fileIndex = new HashMap<String, String>();
			for(String fileEntry : contents.split("\r|\n")) {
	
				// only split on the first colon
				String[] fileEntryParts = fileEntry.split(":");
				if(fileEntryParts.length != 2)
					continue;
				
				String filename = fileEntryParts[0].trim();
				String description = fileEntryParts[1].trim();
	
				fileIndex.put(filename,  description);
			}
		} catch (com.amazonaws.AmazonServiceException e) {
			// failure okay -- just means there's no index file
			fileIndex = new HashMap<String, String>();
		}
	}
	
	public List<S3Item> getFiles(AmazonS3 s3Client) throws IOException {
		List<S3Item> fileList = new ArrayList<>();

		ListObjectsV2Request req = new ListObjectsV2Request()
				.withBucketName("camsys-mta-otp-graph")
				.withDelimiter("/")
				.withPrefix(this.name + "/control_data/");

		ListObjectsV2Result result = s3Client.listObjectsV2(req);

		for (S3ObjectSummary item : result.getObjectSummaries()) {
			if(item.getSize() == 0 || item.getKey().endsWith("/INDEX")) // is a directory
				continue;	
			
			fileList.add(new S3Item(item));
		}            		

		fetchIndex(s3Client);
		
		return fileList;
	}

	public String getDescriptionForFile(String file) {
		String description = fileIndex.get(file);
		
		if(description == null) 
			return "No information is available for this file.";
		else
			return description;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}	
}