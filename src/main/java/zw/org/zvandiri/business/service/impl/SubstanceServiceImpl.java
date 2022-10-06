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
package zw.org.zvandiri.business.service.impl;

import java.util.Date;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import zw.org.zvandiri.business.domain.Substance;
import zw.org.zvandiri.business.repo.SubstanceRepo;
import zw.org.zvandiri.business.service.SubstanceService;
import zw.org.zvandiri.business.service.UserService;
import zw.org.zvandiri.business.util.UUIDGen;

/**
 *
 * @author Judge Muzinda
 */
@Repository
@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
public class SubstanceServiceImpl implements SubstanceService {

    @Resource
    private SubstanceRepo substanceRepo;
    @Resource
    private UserService userService;

    @Override
    public List<Substance> getAll() {
        return substanceRepo.getOptAll(Boolean.TRUE);
    }

    @Override
    public Substance get(String id) {
        if (id == null) {
            throw new IllegalStateException("Item to be does not exist :" + id);
        }
        return substanceRepo.findById(id).get();
    }

    @Override
    public void delete(Substance t) {
        if (t.getId() == null) {
            throw new IllegalStateException("Item to be deleted is in an inconsistent state");
        }
        substanceRepo.delete(t);
    }

    @Override
    public List<Substance> getPageable() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Substance save(Substance t) {
        if (t.getId() == null) {
            t.setId(UUIDGen.generateUUID());
            t.setCreatedBy(userService.getCurrentUser());
            t.setDateCreated(new Date());
            return substanceRepo.save(t);
        }
        t.setModifiedBy(userService.getCurrentUser());
        t.setDateModified(new Date());
        return substanceRepo.save(t);
    }

    @Override
    public Substance getByName(String name) {
        return substanceRepo.findByName(name);
    }

    @Override
    public Boolean checkDuplicate(Substance current, Substance old) {
        if (current.getId() != null) {
            /**
             * @param current is in existence
             */
            if (!current.getName().equalsIgnoreCase(old.getName())) {
                if (getByName(current.getName()) != null) {
                    return true;
                }
            }

        } else if (current.getId() == null) {
            /**
             * @param current is new
             */
            if (getByName(current.getName()) != null) {
                return true;
            }
        }
        return false;
    }
}
