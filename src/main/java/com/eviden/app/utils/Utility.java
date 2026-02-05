package com.eviden.app.utils;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface Utility {


    public  static <E> ResponseEntity<E> responseEntityBuilder(E obj) {
        return new ResponseEntity<E>(obj, HttpStatus.OK);
    }

    public static Long  dateToEpoc(String myDate){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        try {
            date = dateFormat.parse(myDate);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
       return date.getTime();
    }

    public static String epocToDate(Integer epoc){
        Date date = new Date(epoc);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(date);
    }

    public static <T> List<T> removeDuplicate(List<T>  listElements) {
        return new ArrayList<T>(new LinkedHashSet<>(listElements));
    }
    public static <T> List<T> findDuplicates(List<T> list, Function<T, ?> uniqueKey) {
        if (list == null) {
            return null;
        }
        Function<T, ?> notNullUniqueKey = el -> uniqueKey.apply(el) == null ? "" : uniqueKey.apply(el);
        return list.stream()
                .collect(Collectors.groupingBy(notNullUniqueKey))
                .values()
                .stream()
                .filter(matches -> matches.size() > 1)
                .map(matches -> matches.get(0))
                .collect(Collectors.toList());
    }


}
