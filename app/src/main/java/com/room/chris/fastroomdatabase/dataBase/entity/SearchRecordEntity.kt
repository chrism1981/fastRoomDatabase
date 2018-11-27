package com.room.chris.fastroomdatabase.dataBase.entity

import android.arch.persistence.room.Embedded
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey
import com.room.chris.fastroomdatabase.dataBase.entity.RecordDetail

/**
 * Created by cui.yan on 2018/7/9.
 * 搜索结果entity实例
 *
 * 1.不需要存库的字段，可以用
 * @see Ignore 注解标注
 * 2.对于自定义类型的成员变量，需要注解 Embedded，并且这个自定义对象也要标记@Entity，不需要写dao
 * @See SearchRecordEntity.detail 和
 * @see RecordDetail
 */
@Entity
class SearchRecordEntity {
    @PrimaryKey(autoGenerate = true)/** 主键不能为 nullable类型*/
    var id: Int = 0

    //自定义类型，需要处理
    @Embedded
    var detail: RecordDetail? = null

    var text = ""
    var isss :Int? = null


}