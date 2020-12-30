package com.camsys.s3editor.application.views.editor;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.Collections;

import org.apache.commons.io.IOUtils;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.ICsvMapReader;
import org.supercsv.prefs.CsvPreference;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.camsys.s3editor.application.models.S3Environment;
import com.camsys.s3editor.application.models.S3Item;
import com.camsys.s3editor.application.views.main.MainView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.hilerio.ace.AceEditor;
import com.hilerio.ace.AceMode;
import com.hilerio.ace.AceTheme;
import com.vaadin.flow.router.RouteAlias;

@Route(value = "files", layout = MainView.class)
@PageTitle("File List")
@CssImport("./styles/views/file-view.css")
@RouteAlias(value = "", layout = MainView.class)
public class FileListView extends HorizontalLayout {
	
	private AmazonS3 s3Client;
	
	private S3Environment activeEnvironment;
	
	private S3Item activeFile;
	
	private Text fileUseText;
			
    private Grid<S3Item> fileControl;
    
	private AceEditor editorControl;
	
    public FileListView() {
    	s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new ProfileCredentialsProvider())
                .withRegion(Regions.US_EAST_1)
                .build();
    	    	

    	VerticalLayout layout = new VerticalLayout();
        layout.setWidthFull();
        layout.setHeightFull();
        layout.setSpacing(false);

        HorizontalLayout layout3 = new HorizontalLayout();
        layout3.setWidthFull();
        layout3.setHeight("100px");
        layout3.setSpacing(true);
        layout3.setAlignItems(FlexComponent.Alignment.BASELINE);

        ComboBox<String> combobox = new ComboBox<>();
        combobox.setItems("dev", "demo");
        combobox.setLabel("Environment");
        combobox.setPreventInvalidInput(true);
        combobox.addValueChangeListener(e -> {
        	populateFileList(e.getValue());
        });
        layout3.add(combobox);
		
        Button button = new Button("Save");
        button.setAutofocus(true);
        button.getElement().getStyle().set("margin-left", "auto");
        button.addClickListener(e -> {
        	saveFile();
        });
        layout3.add(button);
        
        layout.add(layout3);
        
        HorizontalLayout layout2 = new HorizontalLayout();
        layout2.setWidthFull();
        layout2.setHeightFull();
        layout2.setSpacing(false);

        layout2.add(getGrid());
        layout2.add(getEditPanel());
        
        layout.add(layout2);
        
        setWidthFull();
        setHeightFull();
        setSpacing(false);
        setAlignItems(FlexComponent.Alignment.STRETCH);
        
        add(layout);
        add(getDetails());

		addAttachListener(attachEvent -> {
			combobox.setValue("dev");
		});
    }
    
    private Component getEditPanel() {
    	editorControl = new AceEditor();
    	editorControl.setTheme(AceTheme.github);
    	editorControl.setMode(AceMode.plain_text);
    	editorControl.setWidth("80%");
    	editorControl.setDisplayIndentGuides(true);
    	editorControl.setShowInvisibles(true);
    	editorControl.setSofttabs(false);
    	editorControl.setWrap(false);
    	editorControl.setHeightFull();
		
		return editorControl;
    }
    
    private Component getDetails() {
    	VerticalLayout layout = new VerticalLayout();
    	layout.setId("details");
    	
    	fileUseText = new Text("When a file is selected, information on it will be displayed here.");
    	layout.setWidth("400px");
    	
    	Details fileUsePanel = new Details("How Is This File Used?", fileUseText);
    	fileUsePanel.setOpened(true);

    	layout.add(fileUsePanel);
    	
    	return layout;
    }
        
    private Component getGrid() {
    	fileControl = new Grid<>(S3Item.class);
    	fileControl.setHeightFull();
    	fileControl.setMinWidth("15%");
    	fileControl.setWidth("20%");
    	
    	fileControl.removeAllColumns();
    	fileControl.addColumn(S3Item::getName).setHeader("Name");

    	fileControl.addItemClickListener(e -> {
			S3Item selectedFile = e.getItem();		
			if(selectedFile != null) {
				loadFile(selectedFile);
			}
    	});

    	return fileControl;        
    }
    
    private void populateFileList(String environmentKey) {     
        try {
        	fileControl.setItems(Collections.emptyList());        	
        	editorControl.setValue("");
        	
        	activeEnvironment = new S3Environment(environmentKey);
		    fileControl.setItems(activeEnvironment.getFiles(s3Client));

        	Notification notification = 
        			Notification.show("Environment " + environmentKey + " loaded");
        	add(notification);

        } catch(Exception e) {
        	Notification notification = 
        			Notification.show("An error occured while loading files for the environment: " + e.getMessage());
        	add(notification);
        }
    }
    
    private void saveFile() {
    	if(activeFile == null) {
        	Notification notification = Notification.show("No file is currently being edited");
        	add(notification);

        	return;
    	}
    	
        try {
	        if(activeFile.getName().endsWith(".json") || activeFile.getName().endsWith(".geojson")) {
	        	Gson gson = new GsonBuilder().setPrettyPrinting().create();
		        JsonParser jp = new JsonParser();
		        JsonElement je = jp.parse(editorControl.getValue());
		        
	        } else if(activeFile.getName().endsWith(".csv")) {
	            ICsvMapReader mapReader = new CsvMapReader(new StringReader(editorControl.getValue()),  
	            		CsvPreference.STANDARD_PREFERENCE);
	            final String[] header = mapReader.getHeader(true);
	            while ((mapReader.read(header)) != null) {}

	        } else if(activeFile.getName().endsWith(".txt")) {
	            ICsvMapReader mapReader = new CsvMapReader(new StringReader(editorControl.getValue()),  
	            		CsvPreference.TAB_PREFERENCE);
	            final String[] header = mapReader.getHeader(true);
	            while ((mapReader.read(header)) != null) {}
	        }	        	

	    	s3Client.putObject("camsys-mta-otp-graph", 
	    			activeFile.getPrefix() + "/" + activeFile.getName(), 
	    			editorControl.getValue());

	    	Notification notification = 
	    			Notification.show("File successfully saved");
	    	add(notification);
	    	
        } catch(Exception e) {
        	Notification notification = 
        			Notification.show("An error occured while saving the selected file: " + e.getMessage());
        	add(notification);
        }
	}
    
    private void loadFile(S3Item file) {
    	S3Object object = s3Client.getObject(new GetObjectRequest("camsys-mta-otp-graph", file.getPrefix() + "/" + file.getName()));
    	InputStream objectData = object.getObjectContent();    	
        BufferedReader reader = new BufferedReader(new InputStreamReader(objectData));

        try {
	        if(file.getName().endsWith(".json") || file.getName().endsWith(".geojson")) {
	        	Gson gson = new GsonBuilder().setPrettyPrinting().create();
		        JsonParser jp = new JsonParser();
		        JsonElement je = jp.parse(reader);
		        
	        	editorControl.setMode(AceMode.json);
		        editorControl.setValue(gson.toJson(je));

	        } else if(file.getName().endsWith(".html")) {
	        	editorControl.setMode(AceMode.html);
	        	String contents = IOUtils.toString(objectData, "utf8"); 
	        	editorControl.setValue(contents);	

	        } else {
	        	editorControl.setMode(AceMode.plain_text);
	        	String contents = IOUtils.toString(objectData, "utf8"); 
	        	editorControl.setValue(contents);	
	        }

            activeFile = file;            
	        fileUseText.setText(activeEnvironment.getDescriptionForFile(activeFile.getName()));
	        
	        objectData.close();
        } catch(Exception e) {
        	Notification notification = 
        			Notification.show("An error occured while loading the selected file: " + e.getMessage());
        	add(notification);
        }
    }
}
