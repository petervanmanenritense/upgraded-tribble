package com.commutr.dashboard.repository

import com.commutr.dashboard.model.Inwoner
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository

@Repository
interface InwonerRepository : JpaRepository<Inwoner, Long>, JpaSpecificationExecutor<Inwoner>
