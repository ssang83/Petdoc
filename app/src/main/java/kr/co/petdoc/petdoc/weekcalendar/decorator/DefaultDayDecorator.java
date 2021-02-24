package kr.co.petdoc.petdoc.weekcalendar.decorator;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.core.content.ContextCompat;

import org.joda.time.DateTime;

import kr.co.petdoc.petdoc.R;
import kr.co.petdoc.petdoc.weekcalendar.fragment.WeekFragment;

/**
 * Created by gokhan on 7/27/16.
 */
public class DefaultDayDecorator implements DayDecorator {

    private Context context;
    private final int selectedDateColor;
    private final int todayDateColor;
    private int todayDateTextColor;
    private int textColor;
    private float textSize;

    public DefaultDayDecorator(Context context,
                               @ColorInt int selectedDateColor,
                               @ColorInt int todayDateColor,
                               @ColorInt int todayDateTextColor,
                               @ColorInt int textColor,
                               float textSize) {
        this.context = context;
        this.selectedDateColor = selectedDateColor;
        this.todayDateColor = todayDateColor;
        this.todayDateTextColor = todayDateTextColor;
        this.textColor = textColor;
        this.textSize = textSize;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void decorate(View view, TextView dayTextView,
                         DateTime dateTime, DateTime firstDayOfTheWeek, DateTime selectedDateTime) {
        Drawable holoCircle = ContextCompat.getDrawable(context, R.drawable.holo_circle);

        if (firstDayOfTheWeek.getMonthOfYear() < dateTime.getMonthOfYear()
                || firstDayOfTheWeek.getYear() < dateTime.getYear())
            dayTextView.setTextColor(Color.GRAY);

        DateTime calendarStartDate = WeekFragment.CalendarStartDate;

        if (selectedDateTime != null) {
            if (selectedDateTime.toLocalDate().equals(dateTime.toLocalDate())) {
                dayTextView.setBackground(holoCircle);
                dayTextView.setTextColor(ContextCompat.getColor(context, R.color.orange));
            } else {
                dayTextView.setTextColor(ContextCompat.getColor(context, R.color.veryLightPinkSeven));
                dayTextView.setBackground(null);
            }
        } else {
            dayTextView.setTextColor(ContextCompat.getColor(context, R.color.veryLightPinkSeven));
        }

        DateTime today = new DateTime();
        if (today.toLocalDate().isBefore(dateTime.toLocalDate())) {
            dayTextView.setTextColor(ContextCompat.getColor(context, R.color.colorde4e50));
        }

        float size = textSize;
        if (size == -1)
            size = dayTextView.getTextSize();
        dayTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
    }
}
