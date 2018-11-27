package com.room.chris.fastroomdatabase.dataBase

import android.arch.persistence.room.Room
import android.content.Context
import android.util.Log
import com.room.chris.fastroomdatabase.dataBase.dao.BaseDao
import com.room.chris.fastroomdatabase.dataBase.interfaces.QueryCallBack
import com.room.chris.fastroomdatabase.dataBase.dao.SearchRecordDao
import com.room.chris.fastroomdatabase.dataBase.entity.SearchRecordEntity

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers


/**
 * Created by cui.yan on 2018/7/10.
 * 基于谷歌ROOM框架，特点是结合RxJava，支持LiveDate，数据变化自动通知监听类。查询采用子线程运行，异步回调方式
 * 复杂查询不需要在调用时候使用各种where之api，一次性的用注解写在Dao中，任何人调用对应方法即可，无api学习成本。
 * 数据库迁移和表结构更新处理非常简单，只需要addMigration
 *
 * 包装表操作便捷方法。 根据范型，操作不同的表.
 * 由于没有地方需要持续快速查询表实时更新ui，所有方法采取一次性操作后即关闭数据库
 *
 * 整个库使用步骤：
 *
 * 1. 创建Entity类，标记注解，每个entity类就是一个表
 * 2. 创建对应entity 的dao类，继承BaseDao，只需要声明query方法。
 * 3. 在BBDataBase 中，注解属性中添加新entity，每个entity相当于增加一张表
 * @see BBDataBase
 *
 * 4.通过 该类query等方法，进行增删改查
 *
 * 5.受限于安卓系统的SQLite数据库，每当库中的表结构修改，数据库版本号必须要增加，对于开发阶段，当表发生结构改变时，没必要
 * 处理数据库升级，只需要删除应用重新安装即可，相当于删库重建。当上线以后，新版本要修改原先表的字段时，为了让老版本用户的数据
 * 得已保存，要处理数据库版本升级，alter table 详见BBDataBase 类中的例子。
 *
 * T entity
 */
object DBHelper {

    /**
     * 例子1 例子1 ！！回调方式
     *
     * @see SearchRecordDao 中如何标记
     * 通过@IsQuery注解标记dao子类中的query方法后，便捷操作数据库的例子，非常简便，推荐用这种
     * 外部调用用法示例代码
     */
    private fun sampleFunc1(context: Context){

        //lambda版本
        //需要操作哪个表， 传哪个class
        query(context, SearchRecordDao::class.java, { list ->
            //success 回调
            if(list?.size > 0){
                //list 就是查询后的结果集合
            }
        },{
            //fail回调
        })




        // 匿名对象版本
        //需要操作哪个表，传哪个dao的class
        query(context, SearchRecordDao::class.java, object : QueryCallBack<SearchRecordEntity> {
            override fun onFail() {
                //fail回调
            }

            override fun onSuccess(t: List<SearchRecordEntity>) {
                //success 回调
            }
        })

        //插入方法
        insertRecord(context, SearchRecordDao::class.java, arrayListOf(SearchRecordEntity()))

    }

    /**
     * 例子2 例子2 ！！！
     * !!! 也可以直接操作 dao对象的 方式操作数据库
     * 对于 增 删 改，直接操作dao方式简单
     */
    fun sampleNoAnnotation(context: Context){

        //例如获取SearchDao进行操作对应的表
        var dao = getDataBase(context)?.searchRecordDao()
        //query方法
        dao?.getAllRecords()?.subscribeOn(Schedulers.computation())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe({ list ->
                    if(list.isNotEmpty()){
                        // list就是查询结果
                    }
                }, { Log.d("reflect","faild") })

        //insert方法
        dao?.insert(SearchRecordEntity())

    }

    /* 例子结束 -----------*/

    fun  getDataBase(context: Context): BBDataBase? {
//        var database = Room.databaseBuilder(context,BBDataBase::class.java,"BBDataBase.db").build()
//        var currentVersion = database?.openHelper?.writableDatabase?.version ?: 1
//        if(currentVersion < dataBaseVersion){//本机当前数据库版本旧（数据库发生过表结构修改，必须要有migration过程）
//            synchronized(this){
//                database.close()
//                var updatedDataBase = Room.databaseBuilder(context,BBDataBase::class.java,"BBDataBase.db")
//                BBDataBase.addtMigration(updatedDataBase)
//                return updatedDataBase.build()
//            }
//        }
//        return database
        var updatedDataBase = Room.databaseBuilder(context, BBDataBase::class.java,"BBDataBase.db")
//        DabaBaseUpdateHelper.addtMigration(updatedDataBase)
        updatedDataBase.fallbackToDestructiveMigration()
        return updatedDataBase.build()
    }

    /**
     * 查询搜索历史记录
     * lambda
     *
     * @param daoClazz 要操作哪个表，传对应的dao class类
     * @param success 成功的回调lambda
     */
    fun <T> query(context: Context, daoClazz: Class<out BaseDao<T>>, success:(t:List<T>)->(Unit), fail: ()->(Unit) = {}){

        var database = getDataBase(context)

        database?.getDao(daoClazz)?.query<T>( {t ->
            success(t)
            database.close()
        },{
            fail()
            database.close()
        } )
    }

    /**
     * 查询搜索历史记录
     * lambda
     *
     * @param daoClazz 要操作哪个表，传对应的dao class类
     * @param success 成功的回调lambda
     * @param key 条件查询时后，查询某条记录的key
     */
    fun <T> queryWithKey(context: Context, daoClazz: Class<out BaseDao<T>>, key:Any, success:(t:T)->(Unit), fail: ()->(Unit)){

        var database = getDataBase(context)

        database?.getDao(daoClazz)?.queryWithKey<T>(key,{ t ->
            success(t)
            database.close()
        },{
            fail()
            database.close()
        } )
    }
    fun <T> queryListWithKey(context: Context, daoClazz: Class<out BaseDao<T>>, key:Any, success:(t:List<T>)->(Unit), fail: ()->(Unit)){

        var database = getDataBase(context)

        database?.getDao(daoClazz)?.queryListWithKey<T>(key,{ t ->
            success(t)
            database.close()
        },{
            fail()
            database.close()
        } )
    }

    /**
     * 查询搜索历史记录
     * 回调接口方式
     * @param daoClazz 要操作哪个表，传对应的dao class类
     * @param success 成功的回调lambda
     */
    fun <T> query(context: Context, daoClazz: Class<out BaseDao<T>>, callBack: QueryCallBack<T>){
        var database = getDataBase(context)

        database?.getDao(daoClazz)?.query(object : QueryCallBack<T> {
            override fun onSuccess(t: List<T>) {
                callBack.onSuccess(t)
                database.close()
            }

            override fun onFail() {
                callBack.onFail()
                database.close()//To change body of created functions use File | Settings | File Templates.
            }
        })
    }

    fun <T> deleteAll(context: Context, daoClazz: Class<out BaseDao<T>>){
        var database = getDataBase(context)

        database?.getDao(daoClazz)?.deleteAllRecord()
    }

    fun <T> deleteWithKey(context: Context, daoClazz: Class<out BaseDao<T>>, key: Any){
        var database = getDataBase(context)

        database?.getDao(daoClazz)?.deleteWithKey<T>(key)
    }

    /**
     * 插入一组记录
     * @param daoClazz 要操作哪个表，传对应的dao class类
     */
    fun <T> insertRecord(context: Context, daoClazz: Class<out BaseDao<T>>, list: List<T>){
        getDataBase(context)?.getDao(daoClazz)?.insert(list)
    }

    fun <T> insertRecord(context: Context, daoClazz: Class<out BaseDao<T>>, record: T){
        getDataBase(context)?.getDao(daoClazz)?.insert(record)
    }

    /**
     * 删除记录
     * @param daoClazz 要操作哪个表，传对应的dao class类
     */
    fun <T> deleteRecord(context: Context, daoClazz: Class<out BaseDao<T>>, record: T){
        getDataBase(context)?.getDao(daoClazz)?.delete(record)
    }

    fun <T> deleteRecord(context: Context, daoClazz: Class<out BaseDao<T>>, record: List<T>){
        getDataBase(context)?.getDao(daoClazz)?.delete(record)
    }

    /**
     * 更新记录
     * @param daoClazz 要操作哪个表，传对应的dao class类
     */
    fun <T> updateRecord(context: Context, daoClazz: Class<out BaseDao<T>>, record: T){
        getDataBase(context)?.getDao(daoClazz)?.update(record)
    }












}