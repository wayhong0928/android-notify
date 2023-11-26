package com.example.cycu_notification_system

class JsonModel {
    data class ResponseModel(
        val done_YN: String,
        val content: List<DataModel>
    )
    data class DataModel(
        val RR: Int,
        val DATE_BEG: Long,
        val TITLE: String,
        val SN: String,
        val DATE_END: Long,
        val SN_TYPE: String,
        val NAME: String
    )
}