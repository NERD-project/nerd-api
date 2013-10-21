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

import java.util.Date;

public class TUser {
   
    private Integer id;
    private String firstName;
    private String lastName;
    private String nickName;
    private String openidEmail;
    private String email;
    private String website;
    private String projectName;
    private String projectUri;
    private String organization;
    private String country;
    private Date registrationDate;
    private Boolean validity;    
    private Integer dailyUsage;
    private Integer dailyQuota;
          
    public Date getRegistrationDate() {
        return registrationDate;
    }
    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
    }
    public Boolean getValidity() {
        return validity;
    }
    public void setValidity(Boolean validity) {
        this.validity = validity;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public String getWebsite() {
        return website;
    }
    public void setWebsite(String website) {
        this.website = website;
    }
    public String getProjectName() {
        return projectName;
    }
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
    public String getProjectUri() {
        return projectUri;
    }
    public void setProjectUri(String projectUri) {
        this.projectUri = projectUri;
    }
    public String getOrganization() {
        return organization;
    }
    public void setOrganization(String organization) {
        this.organization = organization;
    }
    public String getCountry() {
        return country;
    }
    public void setCountry(String country) {
        this.country = country;
    }
    public String getNickName() {
        return nickName;
    }
    public void setNickName(String nickName) {
        this.nickName = nickName;
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    
    public TUser(   Integer id, String firstName, String lastName,
                    String nickName, String email, String website,
                    String projectName, String projectUri, String organization,
                    String country, Date registrationDate, Boolean validity) 
    {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.nickName = nickName;
        this.email = email;
        this.website = website;
        this.projectName = projectName;
        this.projectUri = projectUri;
        this.organization = organization;
        this.country = country;
        this.registrationDate = registrationDate;
        this.validity = validity;
    }
    
    public TUser () {}
    public TUser(   Integer id, String firstName, String lastName,
                    String nickName, String email, String website,
                    String projectName, String projectUri, String organization,
                    String country )
    {
        this( id, firstName, lastName, nickName, email, website, projectName, 
              projectUri, organization, country, null, null);
    }
    public String getOpenidEmail() {
        return openidEmail;
    }
    public void setOpenidEmail(String openidEmail) {
        this.openidEmail = openidEmail;
    }
    public Integer getDailyUsage() {
        return dailyUsage;
    }
    public void setDailyUsage(Integer dailyUsage) {
        this.dailyUsage = dailyUsage;
    }
    public Integer getDailyQuota() {
        return dailyQuota;
    }
    public void setDailyQuota(Integer dailyQuota) {
        this.dailyQuota = dailyQuota;
    }

}
