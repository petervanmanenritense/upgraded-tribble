package com.commutr.dashboard.model

import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "inwoners")
class Inwoner(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    var fullName: String = "",

    @Column(nullable = false)
    var birthdate: LocalDate = LocalDate.now(),

    @Column(unique = true, nullable = false)
    var administratienummer: String = ""
)
