package com.commutr.dashboard.repository

import com.commutr.dashboard.model.Contactmoment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository

@Repository
interface ContactmomentRepository : JpaRepository<Contactmoment, Long>, JpaSpecificationExecutor<Contactmoment>
