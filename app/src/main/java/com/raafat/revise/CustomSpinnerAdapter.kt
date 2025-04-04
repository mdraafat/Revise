package com.raafat.revise

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.raafat.revise.R

class CustomSpinnerAdapter(
    context: Context,
    private val items: Array<String>
) : ArrayAdapter<String>(context, R.layout.custom_spinner_item, items) {

    init {
        setDropDownViewResource(R.layout.custom_spinner_item)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent) as TextView
        view.text = (context.getString(R.string.sura_string_ar)).plus(items[position].substringAfter(" "))
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getDropDownView(position, convertView, parent) as TextView
        view.text = items[position].substringBefore(" ").plus(context.getString(R.string.sura_string_ar)).plus(items[position].substringAfter(" "))
        return view
    }
}
