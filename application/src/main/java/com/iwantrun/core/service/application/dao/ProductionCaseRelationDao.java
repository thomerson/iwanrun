package com.iwantrun.core.service.application.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.iwantrun.core.service.application.domain.ProductionCaseRelation;

public interface ProductionCaseRelationDao extends JpaRepository<ProductionCaseRelation, Integer> {

}
