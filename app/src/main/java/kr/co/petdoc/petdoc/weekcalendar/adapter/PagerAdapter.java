package kr.co.petdoc.petdoc.weekcalendar.adapter;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;

import kr.co.petdoc.petdoc.weekcalendar.eventbus.BusProvider;
import kr.co.petdoc.petdoc.weekcalendar.eventbus.Event;
import kr.co.petdoc.petdoc.weekcalendar.fragment.WeekFragment;

import static kr.co.petdoc.petdoc.weekcalendar.fragment.WeekFragment.DATE_KEY;
import static kr.co.petdoc.petdoc.weekcalendar.view.WeekPager.NUM_OF_PAGES;

/**
 * Created by nor on 12/4/2015.
 */
public class PagerAdapter extends FragmentStatePagerAdapter {
    private static final String TAG = "PagerAdapter";
    private int currentPage = NUM_OF_PAGES - 1;
    private DateTime date;

    public PagerAdapter(FragmentManager fm, DateTime date) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.date = date;
        WeekFragment.selectedDateTime = date;
    }

    @Override
    public Fragment getItem(int position) {
        WeekFragment fragment = new WeekFragment();
        Bundle bundle = new Bundle();

        if (position < currentPage)
            bundle.putSerializable(DATE_KEY, getPerviousDate());
        else if (position > currentPage)
            bundle.putSerializable(DATE_KEY, getNextDate());
        else
            bundle.putSerializable(DATE_KEY, getTodaysDate());

        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public int getCount() {
        return NUM_OF_PAGES;
    }

    private DateTime getTodaysDate() {
        return date;
    }

    private DateTime getPerviousDate() {
        return date.plusDays(-7);
    }

    private DateTime getNextDate() {
        return date.plusDays(7);
    }

    @Override
    public int getItemPosition(Object object) {
        //Force rerendering so the week is drawn again when you return to the view after
        // back button press.
        return POSITION_NONE;
    }

    public void swipeBack() {
        date = date.plusDays(-7);
        currentPage--;
        currentPage = currentPage <= 1 ? NUM_OF_PAGES / 2 : currentPage;
        BusProvider.getInstance().post(
                new Event.OnWeekChange(date.withDayOfWeek(DateTimeConstants.MONDAY), false));
    }


    public void swipeForward() {
        date = date.plusDays(7);
        currentPage++;
        //currentPage = currentPage >= NUM_OF_PAGES - 1 ? NUM_OF_PAGES / 2 : currentPage;
        BusProvider.getInstance().post(
                new Event.OnWeekChange(date.withDayOfWeek(DateTimeConstants.MONDAY), true));
    }


}
