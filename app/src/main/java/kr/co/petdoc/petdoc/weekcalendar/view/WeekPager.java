package kr.co.petdoc.petdoc.weekcalendar.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.squareup.otto.Subscribe;

import org.joda.time.DateTime;

import kr.co.petdoc.petdoc.R;
import kr.co.petdoc.petdoc.weekcalendar.adapter.PagerAdapter;
import kr.co.petdoc.petdoc.weekcalendar.eventbus.BusProvider;
import kr.co.petdoc.petdoc.weekcalendar.eventbus.Event;
import kr.co.petdoc.petdoc.weekcalendar.fragment.WeekFragment;


/**
 * Created by gokhan on 7/28/16.
 */
public class WeekPager extends ViewPager {
    private PagerAdapter adapter;
    private int pos;
    private boolean check;
    public static int NUM_OF_PAGES;
    private TypedArray typedArray;

    public WeekPager(Context context) {
        super(context);
        initialize(null);
    }

    public WeekPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(attrs);
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(state);
        post(new Runnable() {
            @Override
            public void run() {
                //Force rerendering so the week is drawn again when you return to the view after
                // back button press.
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }

    private void initialize(AttributeSet attrs) {
        if (attrs != null) {
            typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.WeekCalendar);
            NUM_OF_PAGES = typedArray.getInt(R.styleable.WeekCalendar_numOfPages, 10);
        }
        setId(View.generateViewId());
        if (!isInEditMode()) {
            initPager(new DateTime());
            BusProvider.getInstance().register(this);
        }
    }

    private void initPager(DateTime dateTime) {
        pos = NUM_OF_PAGES - 1;
        adapter = new PagerAdapter(((AppCompatActivity) getContext())
                .getSupportFragmentManager(), dateTime);
        setAdapter(adapter);
        addOnPageChangeListener(new SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (!check)
                    if (position < pos)
                        adapter.swipeBack();
                    else if (position > pos)
                        adapter.swipeForward();
                pos = position;
                check = false;

            }

        });
        setOverScrollMode(OVER_SCROLL_NEVER);
        setCurrentItem(pos);
        if (typedArray != null)
            setBackgroundColor(typedArray.getColor(R.styleable.WeekCalendar_daysBackgroundColor,
                    ContextCompat.getColor(getContext(), R.color.orange)));
        if (WeekFragment.selectedDateTime == null)
            WeekFragment.selectedDateTime = new DateTime();
    }

    @Subscribe
    public void setCurrentPage(Event.SetCurrentPageEvent event) {
        check = true;
        if (event.getDirection() == 1)
            adapter.swipeForward();
        else
            adapter.swipeBack();
        setCurrentItem(getCurrentItem() + event.getDirection());

    }

    @Subscribe
    public void reset(Event.ResetEvent event) {
        WeekFragment.selectedDateTime = new DateTime(WeekFragment.CalendarStartDate);
        //WeekFragment.CalendarStartDate = new DateTime();
        initPager(WeekFragment.CalendarStartDate);
    }

    @Subscribe
    public void setSelectedDate(Event.SetSelectedDateEvent event) {
        WeekFragment.selectedDateTime = event.getSelectedDate();
        initPager(event.getSelectedDate());
    }

    @Subscribe
    public void setStartDate(Event.SetStartDateEvent event) {
        WeekFragment.CalendarStartDate = event.getStartDate();
        WeekFragment.selectedDateTime = event.getStartDate();
        initPager(event.getStartDate());
    }

    private int idCheck() {
        int id = 0;
        while (true) {
            if (findViewById(++id) == null) break;
        }
        return id;
    }
}
