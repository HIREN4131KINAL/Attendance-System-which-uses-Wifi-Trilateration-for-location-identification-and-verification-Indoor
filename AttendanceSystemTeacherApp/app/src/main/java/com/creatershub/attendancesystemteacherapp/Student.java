package com.creatershub.attendancesystemteacherapp;

public class Student {
    private String name;
    private String roll;
    private String attendance;

    public Student(String roll, String name,  String attendance) {
        this.roll = roll;
        this.name = name;
        this.attendance = attendance;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAttendance() {
        return attendance;
    }

    public void setAttendance(String attendance) {
        this.attendance = attendance;
    }

    public String getRoll() {

        return roll;
    }

    public void setRoll(String roll) {
        this.roll = roll;
    }
}
