package com.yaoyaoing.autoscript.dao

import com.yaoyaoing.autoscript.entitys.OutAccount
import org.apache.ibatis.annotations.Param
import org.apache.ibatis.annotations.Update
import tk.mybatis.mapper.common.Mapper

interface OutAccountDao : Mapper<OutAccount> {

    @Update("update out_account set money = money - #{money} where id = #{accId}")
    fun moneySub(@Param("accId") accId: String, @Param("money") money: Double): Int

    @Update("update out_account set money = money + #{money} where id = #{accId}")
    fun moneyAdd(@Param("accId") accId: String, @Param("money") money: Double): Int

    @Update("update out_account set sim_money = sim_money - #{money} where id = #{accId}")
    fun simMoneySub(@Param("accId") accId: String, @Param("money") money: Double): Int

    @Update("update out_account set sim_money = sim_money + #{money} where id = #{accId}")
    fun simMoneyAdd(@Param("accId") accId: String, @Param("money") money: Double): Int


}