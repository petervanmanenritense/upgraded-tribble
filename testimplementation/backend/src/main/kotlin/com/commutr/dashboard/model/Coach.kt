package com.commutr.dashboard.model

import jakarta.persistence.*

@Entity
@Table(name = "coaches")
class Coach(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    var fullName: String = "",

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "team_id", nullable = false)
    var team: Team? = null
)
