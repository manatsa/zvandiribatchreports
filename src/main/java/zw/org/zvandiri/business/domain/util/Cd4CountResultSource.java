/*
 * Copyright 2016 Judge Muzinda.
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
package zw.org.zvandiri.business.domain.util;

import zw.org.zvandiri.business.util.StringUtils;

/**
 *
 * @author Judge Muzinda
 */
public enum Cd4CountResultSource {
    
    SELF_REPORT(1), CARD_REVIEW(2), CLINIC(3);
    
    private final Integer code;
    
    private Cd4CountResultSource(Integer code){
        this.code = code;
    }
    
    public Integer getCode(){
        return code;
    }
    
    public static Cd4CountResultSource get(Integer code){
        if(code ==null){
            return null;
        }
        switch(code){
            case 1:
                return SELF_REPORT;
            case 2:
                return CARD_REVIEW;
            case 3:
                return CLINIC;
            default:
                throw new IllegalArgumentException("Illegal parameter passed to method :"+code);
        }
    }
    
    public String getName(){
        return StringUtils.toCamelCase3(super.name());
    }
}