package com.yaoyaoing.autoscript.common

import java.util.*

class Ri<T> {

    // code 码
    var code = 0
        private set

    var time: Long = 0
        private set

    // 返回信息
    var msg: String? = null

    // 返回数据
    var data: T? = null
        private set

    constructor()
    constructor(code: Int = 100, msg: String? = null) {
        this.code = code
        this.msg = msg
    }

    companion object {
        fun <T> create(code: Int, msg: String?, data: T?): Ri<T> {
            val resule = Ri<T>()
            resule.time = Calendar.getInstance().timeInMillis / 1000
            resule.code = code
            resule.msg = msg
            resule.data = data
            return resule
        }

        fun <T> create(codeEnum: CodeEnum): Ri<T> {
            return create(codeEnum.code, codeEnum.msg)
        }

        fun <T> create(data: T): Ri<T?> {
            return create(CodeEnum.SUCCESS, data)
        }

        fun <T> create(codeEnum: CodeEnum,
                       data: T): Ri<T?> {
            return create(codeEnum.code, codeEnum.msg, data)
        }

        fun <T> create(code: Int, msg: String?): Ri<T> {
            return create(code, msg, null)
        }

        fun <T> successObject(o: Any?): Ri<T> {
            return create(CodeEnum.SUCCESS)
        }

        fun <T> success(): Ri<T> {
            return create(CodeEnum.SUCCESS.code, CodeEnum.SUCCESS.msg)
        }

        fun <T> success(msg: String?): Ri<T> {
            return create(CodeEnum.SUCCESS.code, msg)
        }

        fun <T> successData(data: T?): Ri<T> {
            val resule = create<T>(CodeEnum.SUCCESS)
            resule.data = data
            return resule
        }

        fun <T> successMsgAndData(msg: String?, data: T): Ri<T> {
            val resule = create<T>(CodeEnum.SUCCESS)
            resule.msg = msg
            resule.data = data
            return resule
        }

        fun <T> error(): Ri<T> {
            return create(CodeEnum.ERROR.code,
                    CodeEnum.ERROR.msg)
        }

        fun <T> error(errorText: String?): Ri<T> {
            return create(CodeEnum.ERROR.code, errorText)
        }

        fun <T> error(errorText: String?, data: T): Ri<T> {
            val error = create<T>(CodeEnum.ERROR.code,
                    errorText)
            error.data = data
            return error
        }

        fun <T> errorData(data: T): Ri<T> {
            val error = create<T>(CodeEnum.ERROR)
            error.data = data
            return error
        }

        fun <T> warning(text: String?): Ri<T> {
            return create(CodeEnum.WARNING.code, text)
        }

        /**
         * 组装返回前端的数据
         *
         * @param data 源数据
         * @return
         */
        fun <T : List<*>?> createReturnSucces(data: T): Ri<T> {
            val resule = Ri<T>()
            resule.code = CodeEnum.SUCCESS.code
            resule.msg = CodeEnum.SUCCESS.msg
            resule.data = data
            return resule
        }

        fun <T> createReturnSuccessOne(data: T): Ri<T> {
            val resule = create<T>(CodeEnum.SUCCESS)
            resule.data = data
            return resule
        }
    }
}