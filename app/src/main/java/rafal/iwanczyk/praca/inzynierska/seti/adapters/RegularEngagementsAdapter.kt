package rafal.iwanczyk.praca.inzynierska.seti.adapters

import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_regular_engagement.view.*
import rafal.iwanczyk.praca.inzynierska.seti.R
import rafal.iwanczyk.praca.inzynierska.seti.models.RegularEngagement
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

open class RegularEngagementsAdapter (private val context: Context,
                                      private var list: ArrayList<RegularEngagement>)
    :RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    private var onClickListener: OnClickListener? = null
    private var onLongClickListener: OnLongClickListener? = null
    private val timeFormatter = if(Locale.getDefault().displayLanguage == "English") {
        SimpleDateFormat("hh:mm a")
    }else{
        SimpleDateFormat("HH:mm")
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return MyViewHolder(LayoutInflater.from(context)
            .inflate(R.layout.item_regular_engagement, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        if(holder is MyViewHolder){
            holder.itemView.tv_item_regular_engagement_start_time.text = timeFormatter.format(model.startTime)
            holder.itemView.tv_item_regular_engagement_end_time.text = timeFormatter.format(model.endTime)
            holder.itemView.tv_item_regular_engagement_title.text = model.name
            if(model.typeOfEngagement == context.getString(R.string.study)){
                holder.itemView.ll_lecture_room_and_building_number.visibility = View.VISIBLE
                holder.itemView.tv_item_regular_engagement_room.text = model.lectureRoom
                holder.itemView.tv_item_regular_engagement_building.text = model.buildingNumber
            }else{
                holder.itemView.ll_lecture_room_and_building_number.visibility = View.GONE
            }

            holder.itemView.setOnClickListener {
                if(onClickListener != null){
                    onClickListener!!.onClick(position,model)
                }
            }
            holder.itemView.setOnLongClickListener {
               if(onLongClickListener != null){
                   onLongClickListener!!.onLongClick(position, model)
                   holder.itemView.setBackgroundColor(Color.parseColor("#4cfc22"))
                   Handler().postDelayed({
                       holder.itemView.setBackgroundColor(context.resources.getColor(R.color.background_color))
                                         }, 3500)
                   return@setOnLongClickListener true
               }
                return@setOnLongClickListener false
            }
        }
    }

    interface OnClickListener{
        fun onClick(position: Int, model: RegularEngagement)
    }

    fun setOnClickListener(onClickListener: OnClickListener){
        this.onClickListener = onClickListener
    }
    interface OnLongClickListener{
        fun onLongClick(position: Int, model: RegularEngagement)
    }

    fun setOnLongClickListener(onLongClickListener: OnLongClickListener){
        this.onLongClickListener = onLongClickListener
    }

    override fun getItemCount(): Int {
       return list.size
    }

    private class MyViewHolder(view: View) : RecyclerView.ViewHolder(view){

    }

}