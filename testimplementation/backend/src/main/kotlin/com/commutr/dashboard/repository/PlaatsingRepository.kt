package com.commutr.dashboard.repository

import com.commutr.dashboard.model.Plaatsing
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository

@Repository
interface PlaatsingRepository : JpaRepository<Plaatsing, Long>, JpaSpecificationExecutor<Plaatsing>
