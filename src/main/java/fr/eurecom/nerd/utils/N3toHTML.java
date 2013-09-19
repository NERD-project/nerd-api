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

package fr.eurecom.nerd.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

public class N3toHTML {

	public static void main(String[] args) {
		N3toHTML parser = new N3toHTML();
		String result = parser.getContents(new File("ontology/nerd-last.n3"));
	
		try {
			BufferedWriter br = new BufferedWriter(new FileWriter("ontology/nerd.html"));
			br.write(result);
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getContents(File aFile) 
	{
	    boolean empty = true;
		StringBuilder buffer = new StringBuilder();
		StringBuilder result = new StringBuilder();
		//String subClass = new String();
		//String equivalent = new String();
		
		List<String> subclasses= new LinkedList<String>();
		List<String> equivalentclasses = new LinkedList<String>();
		
		Hashtable<String, String> ontologyuri = new Hashtable<String, String>();
		ontologyuri.put("alchemy", "http://www.alchemyapi.com/api/entity/types.html#");
		ontologyuri.put("dbpedia-owl", "http://dbpedia.org/ontology/");
		ontologyuri.put("extractiv", "http://wiki.extractiv.com/w/page/29179775/Entity-Extraction#"); 
		ontologyuri.put("opencalais", "http://www.opencalais.com/documentation/calais-web-service-api/api-metadata/entity-index-and-definitions#"); 
		ontologyuri.put("zemanta", "http://developer.zemanta.com/docs/entity_type#");
		ontologyuri.put("nerd", "http://nerd.eurecom.fr/ontology#");
		
		try {
			//use buffering, reading one line at a time
			//FileReader always assumes default encoding is OK!
			BufferedReader input =  new BufferedReader(new FileReader(aFile));
			try {
				String line = null; //not declared within while loop
				while (( line = input.readLine()) != null)
				{		
					if(line.trim().equals("") && !empty) 
					{
						for(String s : buffer.toString().split("\n")) 
						{
							if(s.startsWith("nerd:")) {
								String axiom = s.split("[: ]")[1];
								result.append("<h4 id=\"" + axiom + "\">Class: " + axiom + "</h4>\n");
								result.append("\t<div class='description'>\n");
								result.append("\t<p class='definition'></p>\n");
								result.append("\t<p class='comment'></p>\n");
								result.append("\t<table class='properties'>\n");
								result.append("\t\t<tbody>\n");
								
								result.append("\t\t\t<tr>\n");
								result.append("\t\t\t\t<th>URI:</th>\n");
								result.append("\t\t\t\t<td>http://nerd.eurecom.fr/ontology#" + axiom + "</td>\n");
								result.append("\t\t\t</tr>\n");
								
								result.append("\t\t\t<tr>\n");
								result.append("\t\t\t\t<th>Label:</th>\n");
								result.append("\t\t\t\t<td>" + axiom + "</td>\n");
								result.append("\t\t\t</tr>\n");
							}
							
							if(s.startsWith("  rdfs:subClassOf")) {
								String tmp = s.split(" ")[3];
								subclasses.add(tmp);
							}
							
							if(s.startsWith("  owl:equivalentClass")) {
								String tmp = s.split(" ")[3];
								equivalentclasses.add(tmp);
							}
						}
						
						// subclassof
						if(subclasses.size()>0){
							result.append("\t\t\t<tr>\n");
							result.append("\t\t\t\t<th>Subclass of:</th>\n");
							result.append("\t\t\t\t<td>\n");
							for(String s : subclasses) {
								String oname = s.split(":")[0];	
								String axiom = s.split(":")[1];
								String ouri = ontologyuri.get(oname);
								result.append("\t\t\t\t\t<a href=\"" + ouri + axiom + "\">" + s + "</a> \n");
							}
							result.append("\t\t\t\t</td>\n");
							result.append("\t\t\t</tr>\n");
						}
						
						// equivalentclass
						if(equivalentclasses.size()>0){
							result.append("\t\t\t<tr>\n");
							result.append("\t\t\t\t<th>Equivalent to:</th>\n");
							result.append("\t\t\t\t<td>\n");
							for(String s : equivalentclasses) {
								String oname = s.split(":")[0];	
								String axiom = s.split(":")[1];
								String ouri = ontologyuri.get(oname);
								result.append("\t\t\t\t\t<a href=\"" + ouri + axiom + "\">" + s + "</a> \n");
							}
							result.append("\t\t\t\t</td>\n");
							result.append("\t\t\t</tr>\n");
						}
						result.append("\t\t</tbody>\n");
						result.append("\t</table>\n");
						result.append("\t</div>\n");
						
						empty = true;
						buffer = new StringBuilder();
						subclasses.clear();
						equivalentclasses.clear();
					}
					else {	
						if(line.startsWith("nerd:")) 
							empty = false;
						
						if(!line.startsWith("@prefix") && !line.equals("") && !line.startsWith("#"))
							buffer.append(line + System.getProperty("line.separator"));			
					}

				}
			}
			finally {
				input.close();
			}
	    }
	    catch (IOException ex){
	      ex.printStackTrace();
	    }
	    
	    return result.toString();
	}
		
//	static public String getContents(File aFile) 
//	{
//	    //...checks on aFile are elided
//		StringBuilder contents = new StringBuilder();
//	
//		try {
//			//use buffering, reading one line at a time
//			//FileReader always assumes default encoding is OK!
//			BufferedReader input =  new BufferedReader(new FileReader(aFile));
//			try {
//				String line = null; //not declared within while loop
//				while (( line = input.readLine()) != null)
//				{
//					contents.append(line);
//					contents.append(System.getProperty("line.separator"));
//				}
//			}
//			finally {
//				input.close();
//			}
//	    }
//	    catch (IOException ex){
//	      ex.printStackTrace();
//	    }
//	    
//	    return contents.toString();
//	}
}
