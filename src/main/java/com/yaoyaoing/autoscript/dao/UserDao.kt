package com.yaoyaoing.autoscript.dao

import com.yaoyaoing.autoscript.entitys.User
import org.springframework.stereotype.Repository
import tk.mybatis.mapper.common.Mapper

@Repository
interface UserDao : Mapper<User>