package com.camsys.s3editor.application.models;

import java.io.File;
import java.nio.file.Path;

import com.amazonaws.services.s3.model.S3ObjectSummary;

public class S3Item {
	
	String name;
	
	String prefix;
	
	public S3Item(S3ObjectSummary item) {
		Path p = new File(item.getKey()).toPath();		
		this.name = (p.getFileName() != null) ? p.getFileName().toString() : null;
		this.prefix = (p.getParent() != null) ? p.getParent().toString() : null;
	}

	public String getName() {
		return name;
	}

	public String getPrefix() {
		return prefix;
	}
}
