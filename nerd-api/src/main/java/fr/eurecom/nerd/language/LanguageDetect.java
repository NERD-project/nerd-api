//   NERD - The Named Entity Recognition and Disambiguation framework.
//          It processes textual resources for extracting named entities
//          linked to Web resources.
//
//   Copyright 2011 Politecnico di Torino
//             2011 EURECOM
//             2013 Universita' di Torino
//
//   Authors:
//      Giuseppe Rizzo <giuse.rizzo@gmail.com>
//
//   Licensed under both the CeCILL-B and the Apache License, Version 2.0 
//   (the "License"); you may not use this file except in compliance with 
//   the License. You may obtain a copy of the License at
//     http://www.cecill.info/licences/Licence_CeCILL-B_V1-en.html
//     http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.

package fr.eurecom.nerd.language;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;
import com.cybozu.labs.langdetect.Language;

import fr.eurecom.nerd.api.PropFactory;

public class LanguageDetect {
    
    private HashMap<String,String> languages;
    private HashMap<String, List<String>> extractorLanguages;
    
    public static LanguageDetect getDetector() 
    {
         return new LanguageDetect();
    }
    
    public void init(String languageListFile, String profileDirectory) 
    throws LangDetectException 
    {    
        // load the list of languages recognized by nerd
        languages = new HashMap<String, String>();
        try {
            BufferedReader in = 
                new BufferedReader( new FileReader(languageListFile) );
            String str;
            while ((str = in.readLine()) != null) {
                    String[] temp = str.split(";");
                    languages.put(temp[0], temp[1]);
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        } 
        
        // load the list of languages speak be each extractor
        extractorLanguages = new HashMap<String, List<String>>();
        String temp = PropFactory.config.getProperty("fr.eurecom.nerd.extractors");
        String[] extractors = temp.split(",");
        Set<String> set = new HashSet<String>();
        for (String extractor : extractors) {
            temp = PropFactory.config.getProperty( ("fr.eurecom.nerd.extractor.").concat(extractor).concat(".language") );
            String[] languages = temp.split(",");
            extractorLanguages.put(extractor, Arrays.asList(languages));
            set.addAll(Arrays.asList(languages));
        }
        // insert combined
        extractorLanguages.put("combined", new ArrayList<String>(set));
        
        // load profiles for language detector
        DetectorFactory.loadProfile(profileDirectory);
    }
    
    public String detect(String text) 
    throws LangDetectException {
        Detector detector = DetectorFactory.create();
        detector.append(text);
        return detector.detect();
    }
    public ArrayList<Language> detectLangs(String text) 
    throws LangDetectException {
        Detector detector = DetectorFactory.create();
        detector.append(text);
        return detector.getProbabilities();
    }
    
    public HashMap<String,String> getListLanguages() 
    {
        return languages;
    }
    
    public HashMap<String, List<String>> getExtractorLanguages() 
    {
        return extractorLanguages;
    }
}
