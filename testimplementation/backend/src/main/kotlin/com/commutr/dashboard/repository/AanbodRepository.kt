package com.commutr.dashboard.repository

import com.commutr.dashboard.model.Aanbod
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository

@Repository
interface AanbodRepository : JpaRepository<Aanbod, Long>, JpaSpecificationExecutor<Aanbod>
