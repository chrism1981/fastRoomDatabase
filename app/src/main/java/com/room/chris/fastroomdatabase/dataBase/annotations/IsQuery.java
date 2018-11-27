package com.room.chris.fastroomdatabase.dataBase.annotations;

import com.room.chris.fastroomdatabase.dataBase.dao.BaseDao;

/**
 * Created by cui.yan on 2018/7/10.
 * 该注解用于标记Dao中的查询方法，
 * 标记以后可以用
 * @see BaseDao 中的 query()方法便捷
 * 查询数据库，并且完成查询后回调你自己的函数，回传结果给你。
 */
public @interface IsQuery {
}
