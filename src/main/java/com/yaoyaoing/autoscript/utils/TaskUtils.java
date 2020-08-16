package com.yaoyaoing.autoscript.utils;

public class TaskUtils {
    public static String getTaskNO(Integer taskId) {
        String taskNo = String.valueOf(taskId);
        int len = 6 - taskNo.length();
        for (int i = 0; i < len; i++) {
            taskNo = "0" + taskNo;
        }
        return taskNo;
    }
}
