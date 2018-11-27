package com.room.chris.fastroomdatabase.dataBase.dao

import android.arch.persistence.room.*
import com.room.chris.fastroomdatabase.dataBase.DBHelper
import com.room.chris.fastroomdatabase.dataBase.entity.SearchRecordEntity
import com.room.chris.fastroomdatabase.dataBase.annotations.IsQuery
import com.room.chris.fastroomdatabase.dataBase.annotations.IsQueryWithKey
import io.reactivex.Flowable
import io.reactivex.Single

/**
 * Created by cui.yan on 2018/7/9.
 *
 * * ！!!!!!!!!！整个流程使用可参考!!!!!!!!!!!!
 * @see DBHelper
 *
 *
 * 1.Dao继承BaseDao ，其中增删改已经包含，只需要自己写查询（受限于注解中属性只能为常量，无法在baseDao中动态指定表）
 * @see BaseDao
 *
 * 2.配合rxjava使用，主要注意2种observable类型，可跟据需求选
 * @see Flowable  只要数据库里的记录改变了，就会通知他的observer，一直生效，可用于需要监听改变ui等场景。
 * @see Single    查询到记录，通知一次observer，只生效一次
 *
 * 3.dao，需要注解 @Dao  并且为抽象类。
 * 表名可以直接敲对应entity类名, where查询可以直接用参数名
 */

@Dao
abstract class SearchRecordDao : BaseDao<SearchRecordEntity>() {

    @IsQuery
    @Query("SELECT * FROM SearchRecordEntity ORDER BY id DESC")
    abstract  fun getAllRecords(): Flowable<List<SearchRecordEntity>>

    /**
     * 如需条件查询，方法里的实参就是select语句条件值
     */
    @IsQueryWithKey
    @Query("SELECT * FROM SearchRecordEntity WHERE id = :id")
    abstract fun getSearchRecordById(id:Int): Single<SearchRecordEntity>

    @Query("DELETE FROM SearchRecordEntity")
    protected abstract fun deleteAll()

    fun deleteAllRecords(){
        object : Thread(){
            override fun run() {
                deleteAll()
            }
        }.start()
    }
    /**
     * 当查询是无参函数，可以用该注解
     * 可选：
     * @see IsQuery 来标记，在外部调用时候可以用 baseDao当中 query(QueryCallBack) 方法便捷调用。不用
     * 自己再写subscribe 使用方式见
     * @see DBHelper 中的例子方法
     * 注意 ！如果用IsQuery，方法返回值只支持 Flowable<List>或者Single<List>
     */

}