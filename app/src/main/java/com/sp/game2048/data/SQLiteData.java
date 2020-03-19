package com.sp.game2048.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.sp.game2048.MainActivity;
import com.sp.game2048.util.ClassUtil;

public class SQLiteData {
    private SQLiteDatabase database;
    public SQLiteData(){
        createDatabase();
    }
    public SQLiteDatabase createDatabase(String name){
        //创建表SQL语句
        String sql=new StringBuffer().append("create table if not exists ").append(name)
                .append("(config_key varchar primary key,config_value varchar not null)").toString();
        database.execSQL(sql);
        return database;
    }

    /**
     * 创建表
     * @return
     */
    public SQLiteDatabase createDatabase(){
        Context context = ClassUtil.get(MainActivity.class);
        database=SQLiteDatabase.openOrCreateDatabase(context.getDatabasePath("game2048"),null);
        //创建配置表SQL语句
        createDatabase("config");
        updateConfig("domain","http://118.190.102.137/game/api");
        return database;
    }


    /**
     * 获取最后一期期号
     * @return
     */
    public boolean updateConfig(String key,String value){
        if(getConfig(key)==null) {
            database.execSQL(String.format("insert into config(config_key,config_value) values('%s','%s')", key, value));
        }else{
            database.execSQL(String.format("update config set config_value='%s' where config_key='%s'", value, key));
        }
       return true;
    }

    /**
     * 获取配置
     * @param key
     * @return
     */
    public String getConfig(String key){
        Cursor cursor = database.rawQuery(String.format("select config_value from config where config_key='%s'",key),null);
        if(cursor.getCount() <1){
            return null;
        }
        cursor.moveToFirst();
        return cursor.getString(cursor.getColumnIndex("config_value"));
    }
}
