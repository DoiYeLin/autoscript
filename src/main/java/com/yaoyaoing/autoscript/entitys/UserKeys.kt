package com.yaoyaoing.autoscript.entitys

import javax.persistence.Column
import javax.persistence.GeneratedValue
import javax.persistence.Table

@Table(name = "`user_keys`")
class UserKeys {

    @Column(name = "`id`")
    @GeneratedValue(generator = "JDBC")
    var id: String? = null

    @Column(name = "`key`")
    var key: String? = null

    @Column(name = "`desc`")
    var desc: String? = null

    @Column(name = "`device_num`")
    var deviceNum: Int? = null

    @Column(name = "`create_at`")
    var createAt: Long? = null

    companion object {
        const val user_key_cache: String = "user_key_cache"

        const val ID = "id"
        const val DB_ID = "id"
        const val KEY = "key"
        const val DB_KEY = "key"
        const val DESC = "desc"
        const val DB_DESC = "desc"
        const val DEVICE_NUM = "deviceNum"
        const val DB_DEVICE_NUM = "device_num"
        const val CREATE_AT = "createAt"
        const val DB_CREATE_AT = "create_at"
        fun defaultInstance(): UserKeys {
            val instance = UserKeys()
            instance.deviceNum = 100
            return instance
        }
    }
}