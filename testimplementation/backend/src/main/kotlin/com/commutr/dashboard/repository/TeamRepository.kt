package com.commutr.dashboard.repository

import com.commutr.dashboard.model.Team
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository

@Repository
interface TeamRepository : JpaRepository<Team, Long>, JpaSpecificationExecutor<Team>
