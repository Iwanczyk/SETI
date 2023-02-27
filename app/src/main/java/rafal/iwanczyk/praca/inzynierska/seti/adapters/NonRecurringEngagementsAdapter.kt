package rafal.iwanczyk.praca.inzynierska.seti.adapters

import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_non_recurring_engagement.view.*
import rafal.iwanczyk.praca.inzynierska.seti.R
import rafal.iwanczyk.praca.inzynierska.seti.models.NonRecurringEngagement
import rafal.iwanczyk.praca.inzynierska.seti.models.RegularEngagement
import java.text.SimpleDateFormat

open class NonRecurringEngagementsAdapter (private val context: Context,
                                           private var list: ArrayList<NonRecurringEngagement>)
    :RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    private var onClickListener: NonRecurringEngagementsAdapter.OnClickListener? = null
    private var onLongClickListener: NonRecurringEngagementsAdapter.OnLongClickListener? = null

    private val formatter = SimpleDateFormat("dd/MM")


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return NonRecurringEngagementsAdapter.MyViewHolder(
            LayoutInflater.from(context)
                .inflate(R.layout.item_non_recurring_engagement, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        if(holder is MyViewHolder){
            holder.itemView.tv_item_non_recurring_engagement_start_date.text = formatter.format(model.startDate)
            holder.itemView.tv_item_non_recurring_engagement_start_time.text = model.startTime
            holder.itemView.tv_item_non_recurring_engagement_title.text = model.name
            //TODO assignedTo
            holder.itemView.tv_item_non_recurring_engagement_end_date.text = formatter.format(model.endDate)
            holder.itemView.tv_item_non_recurring_engagement_end_time.text = model.endTime

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
                        holder.itemView.setBackgroundColor(context.resources.getColor(R.color.accent_color))
                    }, 3500)
                    return@setOnLongClickListener true
                }
                return@setOnLongClickListener false
            }
        }
    }

    interface OnClickListener{
        fun onClick(position: Int, model: NonRecurringEngagement)
    }

    fun setOnClickListener(onClickListener: OnClickListener){
        this.onClickListener = onClickListener
    }
    interface OnLongClickListener{
        fun onLongClick(position: Int, model: NonRecurringEngagement)
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