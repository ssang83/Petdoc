package kr.co.petdoc.petdoc.widget

import android.content.Context
import android.database.DataSetObserver
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListAdapter
import android.widget.SpinnerAdapter

/**
 * Petdoc
 * Class: NothingSelectedSpinnerAdapter
 * Created by kimjoonsung on 2020/04/27.
 *
 * Description :
 */
class NothingSelectedSpinnerAdapter(
    spinnerAdapter: SpinnerAdapter,
    nothingSelectedLayout: Int,
    nothingSelectedDropdownLayout: Int,
    context: Context?
) :
    SpinnerAdapter, ListAdapter {
    protected var adapter: SpinnerAdapter
    protected var context: Context?
    protected var nothingSelectedLayout: Int
    protected var nothingSelectedDropdownLayout: Int
    protected var layoutInflater: LayoutInflater

    /**
     * Use this constructor to have NO 'Select One...' item, instead use
     * the standard prompt or nothing at all.
     *
     * @param spinnerAdapter        wrapped Adapter.
     * @param nothingSelectedLayout layout for nothing selected, perhaps
     * you want text grayed out like a prompt...
     * @param context
     */
    constructor(
        spinnerAdapter: SpinnerAdapter,
        nothingSelectedLayout: Int, context: Context?
    ) : this(spinnerAdapter, nothingSelectedLayout, -1, context) {
    }

    override fun getView(
        position: Int,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        // This provides the View for the Selected Item in the Spinner, not
        // the dropdown (unless dropdownView is not set).
        return if (position == 0) {
            getNothingSelectedView(parent)
        } else adapter.getView(
            position - EXTRA,
            null,
            parent
        )
        // Could re-use
        // the convertView if possible.
    }

    /**
     * View to show in Spinner with Nothing Selected
     * Override this to do something dynamic... e.g. "37 Options Found"
     *
     * @param parent
     * @return
     */
    protected fun getNothingSelectedView(parent: ViewGroup?): View {
        return layoutInflater.inflate(nothingSelectedLayout, parent, false)
    }

    override fun getDropDownView(
        position: Int,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        // Android BUG! http://code.google.com/p/android/issues/detail?id=17128 -
        // Spinner does not support multiple view types
        return if (position == 0) {
            if (nothingSelectedDropdownLayout == -1) View(context) else getNothingSelectedDropdownView(
                parent
            )
        } else adapter.getDropDownView(
            position - EXTRA,
            null,
            parent
        )

        // Could re-use the convertView if possible, use setTag...
    }

    /**
     * Override this to do something dynamic... For example, "Pick your favorite
     * of these 37".
     *
     * @param parent
     * @return
     */
    protected fun getNothingSelectedDropdownView(parent: ViewGroup?): View {
        return layoutInflater.inflate(nothingSelectedDropdownLayout, parent, false)
    }

    override fun getCount(): Int {
        val count = adapter.count
        return if (count == 0) 0 else count + EXTRA
    }

    override fun getItem(position: Int): Any? {
        return if (position == 0) null else adapter.getItem(position - EXTRA)
    }

    override fun getItemViewType(position: Int): Int {
        return 0
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    override fun getItemId(position: Int): Long {
       return if(position >= EXTRA) {
           adapter.getItemId(position - EXTRA)
       } else {
           (position - EXTRA).toLong()
       }
    }

    override fun hasStableIds(): Boolean {
        return adapter.hasStableIds()
    }

    override fun isEmpty(): Boolean {
        return adapter.isEmpty
    }

    override fun registerDataSetObserver(observer: DataSetObserver?) {
        adapter.registerDataSetObserver(observer)
    }

    override fun unregisterDataSetObserver(observer: DataSetObserver?) {
        adapter.unregisterDataSetObserver(observer)
    }

    override fun areAllItemsEnabled(): Boolean {
        return false
    }

    override fun isEnabled(position: Int): Boolean {
        return position != 0 // Don'amount allow the 'nothing selected'
        // item to be picked.
    }

    companion object {
        protected const val EXTRA = 1
    }

    /**
     * Use this constructor to Define your 'Select One...' layout as the first
     * row in the returned choices.
     * If you do this, you probably don'amount want a prompt on your spinner or it'll
     * have two 'Select' rows.
     *
     * @param spinnerAdapter                wrapped Adapter. Should probably return false for isEnabled(0)
     * @param nothingSelectedLayout         layout for nothing selected, perhaps you want
     * text grayed out like a prompt...
     * @param nothingSelectedDropdownLayout layout for your 'Select an Item...' in
     * the dropdown.
     * @param context
     */
    init {
        adapter = spinnerAdapter
        this.context = context
        this.nothingSelectedLayout = nothingSelectedLayout
        this.nothingSelectedDropdownLayout = nothingSelectedDropdownLayout
        layoutInflater = LayoutInflater.from(context)
    }
}
