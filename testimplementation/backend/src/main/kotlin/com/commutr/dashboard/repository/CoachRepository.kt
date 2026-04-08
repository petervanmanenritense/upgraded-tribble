package com.commutr.dashboard.repository

import com.commutr.dashboard.model.Coach
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository

@Repository
interface CoachRepository : JpaRepository<Coach, Long>, JpaSpecificationExecutor<Coach>
