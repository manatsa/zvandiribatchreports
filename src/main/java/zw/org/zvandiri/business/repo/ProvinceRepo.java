/*
 * Copyright 2014 Judge Muzinda.
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

package zw.org.zvandiri.business.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zw.org.zvandiri.business.domain.Province;

/**
 *
 * @author Judge Muzinda
 */

@Repository
public interface ProvinceRepo extends JpaRepository<Province, String> {
    
   /* @Query("from Province p left join fetch p.createdBy left join fetch p.modifiedBy where p.active=:active Order By p.name ASC")
    public List<Province> getOptAll(@Param("active") Boolean active);
    
    @Query("from Province p where p.name=:name")
    public Province getProvinceByName(@Param("name") String name);   */

    public Province getProvinceByName(String name);
}