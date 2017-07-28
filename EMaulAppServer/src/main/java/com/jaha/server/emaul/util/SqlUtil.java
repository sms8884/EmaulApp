package com.jaha.server.emaul.util;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Iterator;


public class SqlUtil {


    public static String getJpaColMapping(String src){

        if(src==null || src.isEmpty()) return "";

        StringBuilder sb = new StringBuilder(src.length()*2);
        for(int i=0;i<src.length();i++){
            char ch =src.charAt(i);
            if(Character.isUpperCase(ch)){
                sb.append("_").append(Character.toLowerCase(ch));
            }else{
                sb.append(ch);
            }
        }
        return sb.toString();
    }


    public static String getPageSql(Pageable pageable,String columnAlias){
        StringBuilder sb = new StringBuilder();

        if(columnAlias==null){
            columnAlias=new String();
        }else if(!Util.isEmpty(columnAlias)){
            columnAlias+=".";
        }

        try {
            Iterator<Sort.Order> order = pageable.getSort().iterator();
            for (int i = 0; order != null && order.hasNext(); i++) {
                Sort.Order tmp = order.next();
                if (i == 0) {
                    sb.append(" \n order by");
                } else {
                    sb.append(",");
                }
                sb.append(String.format(" %s%s %s ",columnAlias, getJpaColMapping(tmp.getProperty()),
                        tmp.getDirection()));
            }
            sb.append("\n");
        }catch(Exception e){
            e.printStackTrace();
            Debug.err(e);
            sb.setLength(0);
        }

        //Debug.log("Sort:"+sb.toString());
        return  String.format(" %s limit %d,%d ",
                sb.toString(),pageable.getPageNumber()*pageable.getPageSize(),pageable.getPageSize());
    }

    public static String getPageSql(Pageable pageable){
        return getPageSql(pageable,"");
    }
}
