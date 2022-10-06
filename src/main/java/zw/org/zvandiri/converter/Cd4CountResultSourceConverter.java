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
package zw.org.zvandiri.converter;

import org.springframework.core.convert.converter.Converter;
import zw.org.zvandiri.business.domain.util.Cd4CountResultSource;

/**
 *
 * @author Judge Muzinda
 */
public class Cd4CountResultSourceConverter implements Converter<String, Cd4CountResultSource> {

    @Override
    public Cd4CountResultSource convert(String s) {
        if(s.equals("")) return null;
        return Cd4CountResultSource.get(Integer.valueOf(s));
    }
    
}