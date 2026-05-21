package com.example.control2.dtos;

public interface TasksForUserPerSectorProjection {
    Long getTaskCount();
    String getUsername();
    String getSectorName();
}