package com.commutr.dashboard.model

import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "plaatsingen")
class Plaatsing(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "inwoner_id", nullable = false)
    var inwoner: Inwoner? = null,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "coach_id", nullable = false)
    var coach: Coach? = null,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "team_id", nullable = false)
    var team: Team? = null,

    @Column(nullable = false)
    var startDate: LocalDate = LocalDate.now(),

    @Column(nullable = false)
    var soort: String = "",

    @Column(nullable = true)
    var type: String? = null,

    @Column(nullable = false)
    var zaakstatus: String = "",

    @Column(nullable = true)
    var afsluitreden: String? = null
)
