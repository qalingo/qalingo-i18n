/**
 * Most of the code in the Qalingo project is copyrighted Hoteia and licensed
 * under the Apache License Version 2.0 (release version 0.7.0)
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *                   Copyright (c) Hoteia, 2012-2013
 * http://www.hoteia.com - http://twitter.com/hoteia - contact@hoteia.com
 *
 */
package fr.hoteia.qalingo.translation;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVReader;

/**
*
* <p>
* <a href="LoaderTranslationUtil.java.html"><i>View Source</i></a>
* </p>
*
* @author Denis Gosset (http://www.hoteia.com)
*/
public class LoaderTranslationUtil {

    private final static Logger LOG = LoggerFactory.getLogger(LoaderTranslationUtil.class);

	private static Format formatterFolder = new SimpleDateFormat(Constants.FORMATTER_DATE_TO_FOLDER);

	/**
	 *
	 */
	public LoaderTranslationUtil() {
	}

	public void finalize() {
	}

    public static final void buildMessagesProperties(String currentPath, String project, String filePath, List<String> activedLanguages, String defaultLanguage, String inputEncoding, String outputEncoding) {
        try {
            String newPath = currentPath + project + Constants.PROPERTIES_PATH;
            File folderProject = new File(newPath);
            if (!folderProject.exists()) {
                folderProject.mkdirs();
            }

            InputStreamReader reader = new InputStreamReader(new FileInputStream(new File(filePath)), inputEncoding);
            LOG.info("File CSV encoding: " + inputEncoding);
            LOG.info("File properties encoding: " + outputEncoding);
            CSVReader readerCSV = new CSVReader(reader);

            String prefixFileName = "";
            String[] firstLine = readerCSV.readNext();
            String[] prefixFileNameTemp = firstLine[0].split("## Filename :");
            if(prefixFileNameTemp.length > 1){
                prefixFileName = prefixFileNameTemp[1].trim();
            }

            String prefixKey = "";
            firstLine = readerCSV.readNext();
            String[] prefixKeyTemp = firstLine[0].split("## PrefixKey :");
            if(prefixKeyTemp.length > 1){
                prefixKey = prefixKeyTemp[1].trim();
            }
            
            String [] nextLine;
            Map<String, Integer> availableLanguages = new HashMap<String, Integer>();
            while ((nextLine = readerCSV.readNext()) != null) {
                if(nextLine[0].contains("Prefix")){
                    for (int i = 2; i < nextLine.length; i++) {
                        availableLanguages.put(nextLine[i], new Integer(i));
                    }
                    break;
                } 
            }
            
            // BUILD DEFAULT FILE
            readerCSV = new CSVReader(new FileReader(filePath));
            String fileFullPath = newPath + "/" + prefixFileName + ".properties";
            Integer defaultLanguagePosition = availableLanguages.get(defaultLanguage);
            buildMessagesProperties(readerCSV, fileFullPath, prefixKey, defaultLanguage, defaultLanguagePosition, outputEncoding, true);
            
            for (Iterator<String> iterator = availableLanguages.keySet().iterator(); iterator.hasNext();) {
                // RESET READER
                readerCSV = new CSVReader(new FileReader(filePath));
                String languageCode = (String) iterator.next();
                if(activedLanguages.contains(languageCode)){
                    Integer languagePosition = availableLanguages.get(languageCode);
                    String languegFileFullPath = newPath + "/" + prefixFileName + "_" + languageCode + ".properties";
                    buildMessagesProperties(readerCSV, languegFileFullPath, prefixKey, languageCode, languagePosition.intValue(), outputEncoding, false);
                }
            }
            LOG.info(newPath + "/" + prefixFileName + ".properties");
            
        } catch (Exception e) {
            LOG.info("Exception", e);
        }
    }

    public static final void buildMessagesProperties(CSVReader reader, String fileFullPath, String prefixKey, String languageCode, int languagePosition, String outputEncoding, boolean isDefault) {
        try {
            File file = new File(fileFullPath);
            if (!file.exists()) {
                file.createNewFile();
            }

            DataOutputStream writer = new DataOutputStream(new FileOutputStream(file));
            
            String [] nextLine;
            int linePosition = 1;
            while ((nextLine = reader.readNext()) != null) {
                if(nextLine[0].contains("Prefix") || nextLine[0].contains("Filename")){
                    // THIS IS THE LINE TITLE - DO NOTHING
                } else if(nextLine[0].contains("##")){
                    // THIS IS A COMMENT
                    String value = nextLine[0];
                    if(value.contains("XXLANGUAGE_CODEXX")){
                        if(isDefault){
                            value = value.replace("XXLANGUAGE_CODEXX", "Default Locale: " + languageCode);
                        } else {
                            value = value.replace("XXLANGUAGE_CODEXX", "Locale: " + languageCode);
                        }
                    }
                    processLineWithComment(writer, languageCode, value, outputEncoding);
                    linePosition++;
                } else {
                    processLineWithValue(writer, prefixKey, nextLine, languagePosition, linePosition, outputEncoding);
                    linePosition++;
                }
            }
            writer.flush();
            writer.close();

        } catch (Exception e) {
            LOG.info("Exception", e);
        }
    }
    
    private static void processLineWithComment(DataOutputStream writer, String languageCode, String line, String outputEncoding) throws UnsupportedEncodingException, IOException {
        writer.write(((String) line).getBytes(outputEncoding));
        writer.write(buildCarriageReturn(outputEncoding));
    }
    
    private static void processLineWithValue(DataOutputStream writer, String prefixKey, String[] line, int languagePosition, int linePosition, String outputEncoding) throws UnsupportedEncodingException, IOException {
        String key = prefixKey + ".";
        if(StringUtils.isNotEmpty(line[0])){
            key = key + line[0].replaceAll("\\.", "_").trim() + ".";
        }
        if(line.length > 1){
            key = key + line[1].replaceAll("\\.", "_").trim();
            
            if(StringUtils.isNotEmpty(line[1])){
                String value = line[languagePosition];
                if(value.contains("\"\"")){
                    LOG.warn("Some properties values contain double quote twice: " + value);
                    value = value.replace("\"\"", "\"");
                }
                writer.write(((String) key + "=" + value).getBytes(outputEncoding));
            }
        }
        if(linePosition != 1){
            writer.write(buildCarriageReturn(outputEncoding));
        }
    }

    private static byte[] buildCarriageReturn(String outputEncoding) throws UnsupportedEncodingException {
        return ((String) "\n").getBytes(outputEncoding);
    }

	public static final void copyPropertiesFiles(String folderSource, String folderTarget, String project) {
		LOG.info("forlder source: " + folderSource);
		LOG.info("folder target: " + folderTarget);

        File folder = new File(folderSource);
        for (final File fileEntry : folder.listFiles()) {
            if(fileEntry.isFile()){
                LoaderTranslationUtil.copyFile(folderSource, folderTarget, fileEntry.getName());
            }
        }
	}

	public static final String getFolderPath(String path) {
		String date = formatterFolder.format(new Date());
		String currentPath = "";
		if (path != null && !path.equalsIgnoreCase("")) {
			currentPath = path;
		} else {
			currentPath = "output/" + date + "/";
		}
		File folderBuild = new File(currentPath);
		if (!folderBuild.exists()) {
			folderBuild.mkdirs();
		}

		return currentPath;
	}

	private static final void copyFile(String srcDir, String destDir, String name) {
		try {
			String newPath = destDir;
			File folderProject = new File(newPath);
			if (!folderProject.exists())
				folderProject.mkdirs();
			
			File f1 = new File(srcDir + name + "");
			File f2 = new File(destDir + name + "");
			InputStream in = new FileInputStream(f1);

			// For Overwrite the file.
			OutputStream out = new FileOutputStream(f2);

			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
		} catch (FileNotFoundException e) {
			LOG.error("FileNotFoundException", e);
		} catch (IOException e) {
			LOG.error("IOException", e);
		}
	}
	
}