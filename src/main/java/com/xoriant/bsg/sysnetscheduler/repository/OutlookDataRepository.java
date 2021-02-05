package com.xoriant.bsg.sysnetscheduler.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.xoriant.bsg.sysnetscheduler.model.OutlookData;

@Repository
public interface OutlookDataRepository extends CrudRepository<OutlookData, Long>{

}
