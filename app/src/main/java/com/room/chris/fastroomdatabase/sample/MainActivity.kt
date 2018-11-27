package com.room.chris.fastroomdatabase.sample

import android.app.Activity
import android.os.Bundle
import com.room.chris.fastroomdatabase.R
import com.room.chris.fastroomdatabase.dataBase.DBHelper
import com.room.chris.fastroomdatabase.dataBase.dao.SearchRecordDao
import com.room.chris.fastroomdatabase.dataBase.entity.SearchRecordEntity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //数据库插入操作
        add.setOnClickListener {
            for ( i in 0..2){
                val data = SearchRecordEntity()
                data.id = System.currentTimeMillis().toInt()
                data.text = "id = ${data.id}"

                DBHelper.insertRecord(this@MainActivity, SearchRecordDao::class.java, data)
            }
        }

        show.setOnClickListener {

            DBHelper.query(this@MainActivity, SearchRecordDao::class.java, {
                list -> //查询成功后，会回调这个lambda表达式，将结果为List<SearchEntity> 的list返回供你操作
                var stringBuilder = StringBuilder()
                list.forEach {
                    stringBuilder.append("对象：")
                    stringBuilder.append(it.text)
                    stringBuilder.append("\n")
                }
                content.text = stringBuilder.toString()
            })
        }

        DBHelper.queryListWithKey(this@MainActivity,SearchRecordDao::class.java,"1270030",{
            t: List<SearchRecordEntity> ->
            t.forEach {  }// 你的操作
        },{})

    }
}
