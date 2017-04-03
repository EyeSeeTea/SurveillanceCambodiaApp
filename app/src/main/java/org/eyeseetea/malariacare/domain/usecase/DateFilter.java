package org.eyeseetea.malariacare.domain.usecase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateFilter {

    boolean all = true;
    boolean thisWeek;
    boolean thisMonth;
    boolean lastWeek;
    boolean lastMonth;
    boolean last6Days;
    boolean last6Weeks;
    boolean last6Month;

    public boolean isAll() {
        return all;
    }

    public boolean isThisWeek() {
        return thisWeek;
    }

    public void setThisWeek(boolean thisWeek) {
        all = false;
        this.thisWeek = thisWeek;
    }

    public boolean isThisMonth() {
        return thisMonth;
    }

    public void setThisMonth(boolean thisMonth) {
        all = false;
        this.thisMonth = thisMonth;
    }

    public boolean isLastWeek() {
        return lastWeek;
    }

    public void setLastWeek(boolean last_week) {
        all = false;
        this.lastWeek = last_week;
    }

    public boolean isLastMonth() {
        return lastMonth;
    }

    public void setLastMonth(boolean lastMonth) {
        all = false;
        this.lastMonth = lastMonth;
    }

    //This filter include the current day
    public boolean isLast6Days() {
        return last6Days;
    }

    public void setLast6Days(boolean last6Days) {
        all = false;
        this.last6Days = last6Days;
    }

    //This filter include the current week
    public boolean isLast6Weeks() {
        return last6Weeks;
    }

    public void setLast6Weeks(boolean last6Weeks) {
        all = false;
        this.last6Weeks = last6Weeks;
    }
    //This filter ignore the current month
    public boolean isLast6Month() {
        return last6Month;
    }

    public void setLast6Month(boolean last6Month) {
        all = false;
        this.last6Month = last6Month;
    }

    public Date getStartFilterDate(Calendar calendar) {
        if (isLast6Days()) {
            calendar.add(Calendar.DAY_OF_YEAR, -5);
        } else if (isLast6Weeks()) {
            calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
            calendar.add(Calendar.WEEK_OF_YEAR, -5);
        } else if (isLast6Month()) {
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.add(Calendar.MONTH, -7);
        } else if (isLastWeek()) {
            calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
            calendar.add(Calendar.WEEK_OF_YEAR, -1);
        } else if (isThisWeek()) {
            calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
        } else if (isThisMonth()) {
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.add(Calendar.MONTH, -1);
        } else if (isLastMonth()) {
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.add(Calendar.MONTH, -2);
        }
        return calendar.getTime();
    }

    public Date getEndFilterDate(Calendar calendar) {
        if (isLastWeek()) {
            calendar.add(Calendar.WEEK_OF_YEAR, -1);
            calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
            calendar.add(Calendar.DAY_OF_YEAR, 6);
        } else if (isLastMonth() || isLast6Month()) {
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.add(Calendar.DAY_OF_YEAR, -1);
        }else if (isThisWeek()){
            calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
            calendar.add(Calendar.DAY_OF_YEAR, 6);
        }else if (isThisMonth()){
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.add(Calendar.MONTH, 1);
            calendar.set(Calendar.DAY_OF_MONTH, -1);
        }else if (isLast6Month()){
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.DAY_OF_MONTH, -1);
        } else if (isLast6Weeks()) {
            calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
            calendar.add(Calendar.DAY_OF_YEAR, 6);
        }
        return calendar.getTime();
    }

    public boolean isDateBetweenDates(Date dateInTheMiddle, Date startDate, Date endDate) {
        System.out.println(
                "start: " + getHumanReadableDate(startDate) + " middle: " + getHumanReadableDate(
                        dateInTheMiddle) + " end " + getHumanReadableDate(endDate));
        return dateInTheMiddle.getTime() >= startDate.getTime()
                && dateInTheMiddle.getTime() <= endDate.getTime();
    }

    private String getHumanReadableDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        String dateAsString = sdf.format(date);
        return dateAsString;

    }
}
