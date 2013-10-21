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

package fr.eurecom.nerd.core.logging;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import fr.eurecom.nerd.api.Server;
import fr.eurecom.nerd.core.utils.PropFactory;

public enum LogFactory {
    
    /*
     *  Singleton mode
     */
    INSTANCE;
    
    /*
     *  Logging system
     */
    public static Logger logger = Logger.getLogger(Server.class.getName());     
    static {
        try {
          DateFormat dfm = new SimpleDateFormat("yyyy-MM-dd");
          Date date = new Date();
          String today = dfm.format(date);

          FileHandler fh = new FileHandler(
                  PropFactory.config.getProperty("fr.eurecom.nerd.log").concat(today),
                  true);
          //fh.setFormatter(new XMLFormatter());
          fh.setFormatter(new SimpleFormatter());
          logger.addHandler(fh);
        }
        catch (IOException e) {
          e.printStackTrace();
        }
    }
}

