/**
 * Most of the code in the Qalingo project is copyrighted Hoteia and licensed
 * under the Apache License Version 2.0 (release version 0.8.0)
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *                   Copyright (c) Hoteia, 2012-2014
 * http://www.hoteia.com - http://twitter.com/hoteia - contact@hoteia.com
 *
 */
package org.hoteia.qalingo.translation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Denis Gosset (denis.gosset@hoteia.com)
 */
public class LoaderTranslation {

    public static final String UTF8 = "UTF8";
    public static final String ANSI = "ISO-8859-1";
    
    public static final String PROPERTIES_PATH = "/properties/";

    private final static Logger LOG = LoggerFactory.getLogger(LoaderTranslation.class);

    /**
	 *
	 */
    public LoaderTranslation(){
    }
    
    public void finalize(){
    }
    
	/**
	 *
	 */
	public static void main(String[] args) throws IOException {
		
		LOG.info("LoaderTranslation.main() : Start...");
		// root directory for this project
		String currentPath = args[0];
		// target directory for the files
		String folderWWW = args[1];
		// folder for the files
        String project = args[2];
		// selected languages
        String languages = args[3];
		// default language
        String defaultLanguage = args[4];
        // source enconding
        String inputEncoding = args[5];
        if(StringUtils.isEmpty(inputEncoding)){
            inputEncoding = ANSI;
        }
        // output enconding
        String outputEncoding = args[6];
        if(StringUtils.isEmpty(outputEncoding)){
            outputEncoding = UTF8;
        }

        List<String> activedLanguages = new ArrayList<String>();
        if(StringUtils.isNotEmpty(languages)){
            activedLanguages = Arrays.asList(languages.split(","));
        } else {
            activedLanguages.add("en");
        }
        
        String folderOutput = currentPath + "/target/output/";
		
		LoaderTranslation loaderTranslation = null;
		if( loaderTranslation == null ){
			loaderTranslation = new LoaderTranslation();
		}
		
		String folderOutputPath = LoaderTranslationUtil.getFolderPath(folderOutput);
		String folderWWWPath = LoaderTranslationUtil.getFolderPath(folderWWW);
		
		String folderInput = currentPath + "/src/main/resources/input/";
		File folder = new File(folderInput);
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                // NOTHING - No sub folder right now
            } else {
                String filefullPath = folderInput + fileEntry.getName();
                LoaderTranslationUtil.buildMessagesProperties(folderOutputPath, project, filefullPath, activedLanguages, defaultLanguage, inputEncoding, outputEncoding);
            }
        }

		LoaderTranslationUtil.copyPropertiesFiles(folderOutputPath + project  + PROPERTIES_PATH, folderWWWPath, project);

	}
	
}