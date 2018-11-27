package com.room.chris.fastroomdatabase.dataBase.dao

import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Update
import com.room.chris.fastroomdatabase.dataBase.annotations.IsDeleteAll
import com.room.chris.fastroomdatabase.dataBase.annotations.IsDeleteWithKey
import com.room.chris.fastroomdatabase.dataBase.annotations.IsQuery
import com.room.chris.fastroomdatabase.dataBase.annotations.IsQueryWithKey
import com.room.chris.fastroomdatabase.dataBase.interfaces.QueryCallBack
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * Created by cui.yan on 2018/7/10.
 * 继承baseDao以后，只需要在子类定义 需要 @Query 的方法
 *
 * * ！!!!!!!!!！整个流程使用可参考!!!!!!!!!!!!
 * @see DBHelper
 */
abstract class BaseDao<in T> {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract fun insertRecord(record: T)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract fun insertRecord(record: List<T>)

    @Delete
    protected abstract fun deleteRecord(record: T)

    @Delete
    protected abstract fun deleteRecords(record: List<T>)

    @Update
    protected abstract fun updateRecord(record: T)

    fun update(record: T) {
        object : Thread() {
            override fun run() {
                this@BaseDao.updateRecord(record)
            }
        }.start()
    }

    fun delete(record: T) {
        object : Thread() {
            override fun run() {
                this@BaseDao.deleteRecord(record)
            }
        }.start()
    }

    fun delete(record: List<T>) {
        object : Thread() {
            override fun run() {
                this@BaseDao.deleteRecords(record)
            }
        }.start()
    }

    fun insert(record: T) {
        object : Thread() {
            override fun run() {
                this@BaseDao.insertRecord(record)
            }
        }.start()
    }


    fun insert(record: List<T>) {
        object : Thread() {
            override fun run() {
                this@BaseDao.insertRecord(record)
            }
        }.start()

    }

    fun <T> deleteWithKey(key: Any) {
        var methods = javaClass.superclass.getDeclaredMethods()

        methods.forEach {
            if (it.getAnnotation(IsDeleteWithKey::class.java) != null) {
                it.isAccessible = true
                object : Thread(){
                    override fun run() {
                        it.invoke(this@BaseDao, key)
                    }
                }.start()
            }
        }
    }

    /**
     * 包装子类当查询方法，包装rxjava流程
     * !!!只支持返回值为 Flowable 或者 Single
     * lambda 风格版本
     */
    fun <T> query(success: (t: List<T>) -> (Unit), fail: () -> (Unit)) {
        var methods = javaClass.superclass.getDeclaredMethods()

        methods.forEach {

            if (it.getAnnotation(IsQuery::class.java) != null) {
                it.isAccessible = true
                if (it.returnType == Flowable::class.java) {
                    var result: Flowable<List<T>> = it.invoke(this) as Flowable<List<T>>
                    result.subscribeOn(Schedulers.computation())
                            ?.observeOn(AndroidSchedulers.mainThread())?.subscribe({ t ->
                                success(t)
                            }, { fail() })
                } else if (it.returnType == Single::class.java) {
                    var result: Single<List<T>> = it.invoke(this) as Single<List<T>>
                    result.subscribeOn(Schedulers.computation())
                            ?.observeOn(AndroidSchedulers.mainThread())?.subscribe({ t ->
                                success(t)
                            }, { fail() })
                }
            }

        }
    }


    fun <T> queryWithKey(key: Any, success: (t: T) -> (Unit), fail: () -> (Unit)) {
        var methods = javaClass.superclass.getDeclaredMethods()

        methods.forEach {

            if (it.getAnnotation(IsQueryWithKey::class.java) != null) {
                it.isAccessible = true
                if (it.returnType == Flowable::class.java) {
                    var result: Flowable<T> = it.invoke(this,key) as Flowable<T>
                    result.subscribeOn(Schedulers.computation())
                            ?.observeOn(AndroidSchedulers.mainThread())?.subscribe({ t ->
                                success(t)
                            }, { fail() })
                } else if (it.returnType == Single::class.java) {
                    var result: Single<T> = it.invoke(this,key) as Single<T>
                    result.subscribeOn(Schedulers.computation())
                            ?.observeOn(AndroidSchedulers.mainThread())?.subscribe({ t ->
                                success(t)
                            }, { fail() })
                }
            }

        }
    }

    fun <T> queryListWithKey(key: Any, success: (t: List<T>) -> (Unit), fail: () -> (Unit)) {
        var methods = javaClass.superclass.getDeclaredMethods()

        methods.forEach {//通过检查method上标记等注解，来识别找出带参数等查询

            if (it.getAnnotation(IsQueryWithKey::class.java) != null) {
                it.isAccessible = true
                if (it.returnType == Flowable::class.java) {
                    var result: Flowable<List<T>> = it.invoke(this,key) as Flowable<List<T>>
                    result.subscribeOn(Schedulers.computation())
                            ?.observeOn(AndroidSchedulers.mainThread())?.subscribe({ t ->
                                success(t)
                            }, { fail() })
                } else if (it.returnType == Single::class.java) {
                    var result: Single<List<T>> = it.invoke(this,key) as Single<List<T>>
                    result.subscribeOn(Schedulers.computation())
                            ?.observeOn(AndroidSchedulers.mainThread())?.subscribe({ t ->
                                success(t)
                            }, { fail() })
                }
            }

        }
    }

    /**
     * 包装子类当查询方法，，包装rxjava流程
     * ！！只支持返回值为 Flowable 或者 Single
     * java风格版本
     */
    fun <T> query(callBack: QueryCallBack<T>) {
        var methods = javaClass.superclass.getDeclaredMethods()

        methods?.forEach {

            if (it.getAnnotation(IsQuery::class.java) != null) {
                it.isAccessible = true
                if (it.returnType == Flowable::class.java) {
                    var result: Flowable<List<T>> = it.invoke(this) as Flowable<List<T>>
                    result.subscribeOn(Schedulers.computation())?.observeOn(AndroidSchedulers.mainThread())
                            ?.subscribe({ t -> callBack.onSuccess(t) }, { callBack.onFail() })
                } else if (it.returnType == Single::class.java) {
                    var result: Single<List<T>> = it.invoke(this) as Single<List<T>>
                    result.subscribeOn(Schedulers.computation())
                            ?.observeOn(AndroidSchedulers.mainThread())?.subscribe({ t ->
                                callBack.onSuccess(t)
                            }, { callBack.onFail() })
                }
            }

        }
    }

    fun deleteAllRecord() {
        var methods = javaClass.superclass.getDeclaredMethods()
        methods?.forEach {
            if (it.getAnnotation(IsDeleteAll::class.java) != null) {
                it.isAccessible = true
                it.invoke(this)
            }
        }
    }


}



