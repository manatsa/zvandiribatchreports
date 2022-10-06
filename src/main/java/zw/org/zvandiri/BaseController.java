/*
 * Copyright 2015 Judge Muzinda.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package zw.org.zvandiri;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.ResponseBody;
import zw.org.zvandiri.business.domain.Patient;
import zw.org.zvandiri.business.domain.Settings;
import zw.org.zvandiri.business.domain.User;
import zw.org.zvandiri.business.domain.util.TestType;
import zw.org.zvandiri.business.domain.util.UserLevel;
import zw.org.zvandiri.business.service.*;
import zw.org.zvandiri.business.util.dto.SearchDTO;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 *
 * @author Judge Muzinda
 */
abstract public class BaseController implements IAppTitle {

    @Resource
    private UserService userService;
    @Resource
    private DatePropertyService datePropertyService;
    @Resource
    private SettingsService settingsService;
    public Settings settings = null;
    @Resource
    private ContactService contactService;
    @Resource
    private InvestigationTestService investigationTestService;

    @ModelAttribute("currentuser")
    public User getUserName() {
        return userService.getCurrentUser();
    }

    @ModelAttribute("yearRanges")
    public Map<String, String> getDateYearRanges() {
        return datePropertyService.getAllYearRanges();
    }
    
    @ModelAttribute("userLevel")
    public UserLevel getUserLevel (){
        User user = userService.getCurrentUser();
        if  (user == null){
            return null;
        } else if (user.getUserLevel() == null) {
            return UserLevel.NATIONAL;
        }
        return userService.getCurrentUser().getUserLevel();
    }
    
    public SearchDTO getUserLevelObjectState(SearchDTO dto){
        User user = getUserName();
        if (user.getUserLevel() == null){
            return dto.getInstance(dto);
        }
        else if (user.getUserLevel().equals(UserLevel.PROVINCE)){
            dto.setProvince(user.getProvince());
        } else if (user.getUserLevel().equals(UserLevel.DISTRICT)){
            dto.setDistrict(user.getDistrict());
        }
       // System.err.println(dto.toString());
        return dto;
    }

    


    public String getTitle(SearchDTO dto, String desc) {
        if (dto.getPrimaryClinic()!= null) {
            return dto.getPrimaryClinic().getName() + " Facility " + desc;
        } else if (dto.getDistrict() != null) {
            return dto.getDistrict().getName() + " District " + desc;
        } else if (dto.getProvince() != null) {
            return dto.getProvince().getName() + " Province " + desc;
        }
        return "National " + desc;
    }

    @ResponseBody
    public void forceDownLoadXLSX(XSSFWorkbook workbook, String name, HttpServletResponse response) {
        try(ServletOutputStream myOut = response.getOutputStream();) {
            //Write the workbook in file system
            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "filename=" + name + ".xlsx");
            workbook.write(myOut);
            myOut.flush();
            myOut.close();
        } catch (IOException e) {
            System.err.println("ForceDOWNLOAD Method: ");            
            e.printStackTrace();
        }
    }


    @ResponseBody
    public void forceDownLoadDatabase(XSSFWorkbook workbook, String name, HttpServletResponse response) {

            //Write the workbook in file system
            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "filename=" + name + ".xlsx");

        try {
            workbook.write(response.getOutputStream());
            response.getOutputStream().flush();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
    
    public void setViralLoad(ModelMap model, Patient item) {
        model.addAttribute("latestViralLoad", investigationTestService.getLatestTestByTestType(item, TestType.VIRAL_LOAD));
    }
    
    @PostConstruct
    public void getSystemSettings(){
        settings = settings == null ? settingsService.getItem() : settings;
    }
}
