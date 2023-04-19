package edu.northeasternn.studycircle.model;

import java.time.DayOfWeek;
import java.util.Date;
import java.util.List;

public class Group {
    private String title;
    private String subject;
    private String location;
    private String description;
    private List<DayOfWeek> days;
    private String startTime;
    private String endTime;
    private String creatorId;
    public String getCreatorId() {
        return creatorId;
    }
    public String getTitle() {
        return title;
    }

    public String getSubject() {
        return subject;
    }

    public String getLocation() {
        return location;
    }

    public String getDescription() {
        return description;
    }

    public List<DayOfWeek> getDays() {
        return days;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public Group () {

    }

    public Group(String title, String subject, String location,
                 String description, List<DayOfWeek> days, String startTime,
                 String endTime,String creatorId) {
        this.title = title;
        this.subject = subject;
        this.location = location;
        this.description = description;
        this.days = days;
        this.startTime = startTime;
        this.endTime = endTime;
        this.creatorId = creatorId;
    }
}