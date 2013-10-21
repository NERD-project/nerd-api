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

package fr.eurecom.nerd.core.db.table;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;
import com.cybozu.labs.langdetect.Language;
import com.google.gson.annotations.Expose;
import com.googlecode.mp4parser.authoring.tracks.TextTrackImpl;
import com.googlecode.mp4parser.authoring.tracks.TextTrackImpl.Line;
import com.googlecode.mp4parser.srt.SrtParser;

import fr.eurecom.nerd.core.exceptions.LanguageException;
import fr.eurecom.nerd.core.language.LangFactory;
import fr.eurecom.nerd.core.utils.HTMLScraper;
import fr.eurecom.nerd.core.utils.StringUtils;

public class TDocument {
    @Expose private Integer idDocument;
    @Expose private String text;
    @Expose private String timedtext;
    @Expose private String uri;
    @Expose private String language;
    private String type;
    private List<Line> subs;

    public TDocument(int idDocument, String text)
    throws LanguageException 
    {
        this(text, null, null);
        this.setIdDocument(idDocument);
    }

    public TDocument(String text, String timedtext, String uri) 
    throws LanguageException 
    {
        if(uri!=null && text == null) {
            HTMLScraper htmlscraper = new HTMLScraper();
            this.text = StringUtils.cleanText( htmlscraper.run(uri) );
            this.setType(DocumentType.WEBTEXTTYPE);
        }
        else if(timedtext != null) {
            InputStream is;
            try {
                is = new ByteArrayInputStream(timedtext.getBytes("UTF-8"));
                TextTrackImpl textTrack = SrtParser.parse(is);
                subs = textTrack.getSubs();
                this.text = new String();
                for(Line line : subs)
                    this.text += line.getText();
                this.timedtext = timedtext;
                this.setType(DocumentType.TIMEDTEXTTYPE);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            this.text = text;
            this.setType(DocumentType.PLAINTEXTTYPE);
        }
        
        try {
            this.setLanguage(LangFactory.detector.detect(this.text));
        } catch (LangDetectException e) {
            this.setLanguage("NN");
            throw new LanguageException(e.getMessage());
        }
    }
    
    public TDocument(int idDocument, String text, String timedtext, String uri, String type, String language) 
    throws LanguageException 
    {
        this(text,timedtext,uri);
        this.idDocument = idDocument;
        this.setType(type);
        this.setLanguage(language);
    }

    public Integer getIdDocument() {
        return idDocument;
    }

    public void setIdDocument(Integer idDocument) {
        this.idDocument = idDocument;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getURI() {
        return uri;
    }

    public void setURI(String URI) {
        this.uri = URI;
    }

    public List<Line> getSubs() {
        return subs;
    }

    public void setSubs(List<Line> subs) {
        this.subs = subs;
    }

    public String getTimedtext() {
        return timedtext;
    }

    public void setTimedtext(String timedtext) {
        this.timedtext = timedtext;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    
    /* language detection */
    public void init(String profileDirectory) throws LangDetectException {
        DetectorFactory.loadProfile(profileDirectory);
    }
    public String detect(String text) throws LangDetectException {
        Detector detector = DetectorFactory.create();
        detector.append(text);
        return detector.detect();
    }
    public ArrayList<Language> detectLangs(String text) throws LangDetectException {
        Detector detector = DetectorFactory.create();
        detector.append(text);
        return detector.getProbabilities();
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
