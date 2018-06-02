package com.iwantrun.core.service.application.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.iwantrun.core.service.application.domain.Cases;

public interface CasesDao  extends JpaRepository<Cases,Integer>,JpaSpecificationExecutor<Cases> {
}
