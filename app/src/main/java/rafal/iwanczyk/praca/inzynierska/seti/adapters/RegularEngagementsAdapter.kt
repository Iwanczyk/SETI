package rafal.iwanczyk.praca.inzynierska.seti.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_regular_engagement.view.*
import rafal.iwanczyk.praca.inzynierska.seti.R
import rafal.iwanczyk.praca.inzynierska.seti.models.RegularEngagement

open class RegularEngagementsAdapter (private val context: Context,
                                      private var list: ArrayList<RegularEngagement>)
    :RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    private var onClickListener: OnClickListener? = null



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return MyViewHolder(LayoutInflater.from(context)
            .inflate(R.layout.item_regular_engagement, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        if(holder is MyViewHolder){
            holder.itemView.tv_item_regular_engagement_start_time.text = model.startTime
            holder.itemView.tv_item_regular_engagement_end_time.text = model.endTime
            holder.itemView.tv_item_regular_engagement_title.text = model.name
            if(model.typeOfEngagement == context.getString(R.string.study)){
                holder.itemView.tv_item_regular_engagement_room.text = model.lectureRoom
                holder.itemView.tv_item_regular_engagement_building.text = model.buildingNumber
                holder.itemView.room_building_separator.visibility = View.VISIBLE
            }else{
                holder.itemView.tv_item_regular_engagement_room.visibility = View.GONE
                holder.itemView.tv_item_regular_engagement_building.visibility = View.GONE
                holder.itemView.room_building_separator.visibility = View.GONE
            }

            holder.itemView.setOnClickListener {
                if(onClickListener != null){
                    onClickListener!!.onClick(position,model)
                }
            }
        }
    }

    interface OnClickListener{
        fun onClick(position: Int, model: RegularEngagement)
    }

    fun setOnClickListener(onClickListener: OnClickListener){
        this.onClickListener = onClickListener
    }

    override fun getItemCount(): Int {
       return list.size
    }

    private class MyViewHolder(view: View) : RecyclerView.ViewHolder(view){

    }

}