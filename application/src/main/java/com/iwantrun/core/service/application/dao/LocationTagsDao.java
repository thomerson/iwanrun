package com.iwantrun.core.service.application.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.iwantrun.core.service.application.domain.LocationTags;

public interface LocationTagsDao extends JpaRepository<LocationTags, Integer> {

}
