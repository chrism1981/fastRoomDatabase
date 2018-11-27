package com.room.chris.fastroomdatabase.dataBase

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.migration.Migration
import com.room.chris.fastroomdatabase.dataBase.dao.BaseDao
import com.room.chris.fastroomdatabase.dataBase.dao.SearchRecordDao
import com.room.chris.fastroomdatabase.dataBase.entity.SearchRecordEntity

/**
 * Created by cui.yan on 2018/7/9.
 * 数据库类
 *
 * 采用单库多表，如有需求可以自己在开库。
 * 在 @Database 注解的 entities数组中添加一个entity类，即可创建一张表
 *
 * ！!!!!!!!!！整个流程使用可参考!!!!!!!!!!!!
 * @see DBHelper
 *
 * Dao写法看示例
 * @see SearchRecordDao
 * entity注解标示例
 * @see SearchRecordEntity
 *
 */

//数据库版本号
const val dataBaseVersion :Int = 1

@Database(entities = [SearchRecordEntity::class], version = dataBaseVersion)
abstract class BBDataBase : RoomDatabase() {

    abstract fun searchRecordDao(): SearchRecordDao

    //BaseDao<*>
    fun <T> getDao(clazz: Class<out BaseDao<T>>): BaseDao<T>? {
        var methods = javaClass.superclass.declaredMethods
//        var method = javaClass.superclass.getDeclaredMethod("getDao",Class::class.java)
//        var type = method.returnType
//        var gtype = method.genericReturnType
//        if(gtype is ParameterizedType){
//            var gtypes = gtype.actualTypeArguments
//            gtypes?.forEach {
//                it.toString()
//            }
//        }

        methods.forEach {
            if(it.returnType == clazz){
                it.isAccessible = true
                val dao: BaseDao<T> = it.invoke(this) as BaseDao<T>
                return dao
            }
        }
        return null
    }
}


/**
 * 这是一个数据库迁移处理的写法例子。任何基于sqlite的数据库框架包括 ormlite greendao room等，一旦
 * 发生了表结构的修改，如增加，删除列，都必须要改变数据库版本，并且进行alter table操作。
 * 对于开发过程中，只要删除应用重新安装就可以了。但是线上版本，如果要修改表结构，必须如下处理：
 * 1.new 一个migration对象，构造函数中写明从哪个版本，升级到哪个版本
 * 2.在该migration对象里，写sql语句，写明修改了哪些内容。
 * 3.支持增量更新，比如修改过2次表， 版本从1-3， 则new两个migration对象，第一个是 Migration(1,2)，第二个是
 * Migration(2,3)，然后塞进BBDataBase.addtMigration中.  该框架可以自动识别，从当前手机版本升到最新版本
 * 4.修改dataBaseVersion常量为最新版本号
 * 5.将新建的migration对象塞到 DabaBaseUpdateHelper.addtMigration方法中
 * @see DabaBaseUpdateHelper.addtMigration
 */
val demoMigration_1_2: Migration = object :Migration(1,2){
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE SearchRecordEntity "
                + " ADD COLUMN isss INTEGER")
    }

}
val demoMigration_2_3: Migration = object :Migration(2,3){
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE SearchRecordEntity "
                + " ADD COLUMN is2 INTEGER")
    }

}

object DabaBaseUpdateHelper{
    fun addtMigration( builder: RoomDatabase.Builder<BBDataBase>){
        //新建版本后，把对象塞到下面方法中,需要使用时，替换掉demo对象
        builder.addMigrations(demoMigration_1_2, demoMigration_2_3)
    }
}