package com.yaoyaoing.autoscript.dao

import com.yaoyaoing.autoscript.entitys.UserKeys
import org.apache.ibatis.annotations.Select
import org.springframework.stereotype.Repository
import tk.mybatis.mapper.common.Mapper

@Repository
interface UserKeysDao : Mapper<UserKeys?>