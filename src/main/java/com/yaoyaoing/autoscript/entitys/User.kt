package com.yaoyaoing.autoscript.entitys

import javax.persistence.Column
import javax.persistence.Id
import javax.persistence.Table

@Table(name = "`user`")
class User {
    @Id
    @Column(name = "`id`")
    var id: String? = null

    @Column(name = "`account`")
    var account: String? = null

    @Column(name = "`password`")
    var password: String? = null

    companion object {
        const val redisKey = "user"


        const val ID = "id"
        const val DB_ID = "id"
        const val ACCOUNT = "account"
        const val DB_ACCOUNT = "account"
        const val PASSWORD = "password"
        const val DB_PASSWORD = "password"
        fun defaultInstance(): User {
            val instance = User()
            instance.id = ""
            instance.account = ""
            instance.password = ""
            return instance
        }
    }
}